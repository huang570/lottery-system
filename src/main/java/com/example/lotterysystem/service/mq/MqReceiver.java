package com.example.lotterysystem.service.mq;

import cn.hutool.core.date.DateUtil;
import com.example.lotterysystem.common.config.ExecutorConfig;
import com.example.lotterysystem.common.exception.ServiceException;
import com.example.lotterysystem.common.utils.JacksonUtil;
import com.example.lotterysystem.common.utils.MailUtil;
import com.example.lotterysystem.controller.param.DrawPrizeParam;
import com.example.lotterysystem.dao.dataobject.ActivityPrizeDO;
import com.example.lotterysystem.dao.dataobject.WinningRecordDO;
import com.example.lotterysystem.dao.mapper.ActivityPrizeMapper;
import com.example.lotterysystem.dao.mapper.WinningRecordMapper;
import com.example.lotterysystem.service.DrawPrizeService;
import com.example.lotterysystem.service.activitystatus.ActivityStatusManager;
import com.example.lotterysystem.service.dto.ConvertActivityStatusDTO;
import com.example.lotterysystem.service.enums.ActivityPrizeStatusEnum;
import com.example.lotterysystem.service.enums.ActivityPrizeTiersEnum;
import com.example.lotterysystem.service.enums.ActivityStatusEnum;
import com.example.lotterysystem.service.enums.ActivityUserStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.lotterysystem.common.config.DirectRabbitConfig.QUEUE_NAME;

@Component
@RabbitListener(queues = QUEUE_NAME)
public class MqReceiver {

    private static final Logger logger = LoggerFactory.getLogger(MqReceiver.class);

    @Autowired
    private DrawPrizeService drawPrizeService;

    @Autowired
    private ActivityStatusManager activityStatusManager;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    private MailUtil mailUtil;

    @Autowired
    private ActivityPrizeMapper activityPrizeMapper;

    @Autowired
    private WinningRecordMapper winningRecordMapper;

    @RabbitHandler
    public void process(Map<String, String> message){

        // 成功接收到队列消息
        logger.info("Mq成功接收到消息, message: {}", JacksonUtil.writeValueAsString(message));

        String paramString = message.get("messageData");
        DrawPrizeParam param = JacksonUtil.readValue(paramString,DrawPrizeParam.class);

        // 处理抽奖流程

        try {

            // 校验抽奖请求是否有效
            // 如果有两个一样的抽奖请求，
            if(!drawPrizeService.checkDrawPrizeParam(param)){
                return;
            }

            // 状态扭转处理
            statusConvert(param);
            // 保存中奖者名单
            List<WinningRecordDO> winningRecordDOList =
                    drawPrizeService.saveWinnerRecords(param);

            // 通知中奖者
            syncExecute(winningRecordDOList);

        }catch (ServiceException e){
            logger.error("处理 MQ 消息异常: {} : {}",e.getCode(),e.getMessage(),e);
            // 如果异常, 需要保证事务一致性, 需要回滚; 并且抛出异常
            rollback(param);
            throw e;

        }catch (Exception e){
            logger.error("处理 MQ 消息异常: ",e);
            rollback(param);
            throw e;
        }
    }

    /**
     * 处理抽奖异常回滚行为：恢复处理请求之前的库存状态
     *
     * @param param
     */
    private void rollback(DrawPrizeParam param) {
        // 回滚状态：活动表、奖品表、人员表、活动奖品关联表

        // 1. 状态是否需要回滚
        // 不需要，直接return
        if(!statusNeedRollback(param)){
            return;
        }
        // 需要，进行回滚
        rollbackStatus(param);
        // 2. 回滚中奖者名单

        // 是否需要回滚中奖者名单

        // 不需要，直接return
        if(!winnerNeedRollBack(param)){
            return;
        }
        // 需要，进行回滚
        rollbackWinner(param);
    }

    /**
     * 回滚中奖记录: 删除奖品下的中奖者
     *
     * @param param
     */
    private void rollbackWinner(DrawPrizeParam param) {
        drawPrizeService.deleteRecords(param.getActivityId(), param.getPrizeId());
    }

    private boolean winnerNeedRollBack(DrawPrizeParam param) {
        // 判断活动中的奖品是否存在中奖者
        int count = winningRecordMapper.countByAPId(param.getActivityId(),param.getPrizeId());
        return count > 0 ;
    }

    /**
     * 恢复状态
     *
     * @param param
     */
    private void rollbackStatus(DrawPrizeParam param) {
        // 涉及状态的恢复工作
        ConvertActivityStatusDTO convertActivityStatusDTO = new ConvertActivityStatusDTO();
        convertActivityStatusDTO.setActivityId(param.getActivityId());
        convertActivityStatusDTO.setTargetActivityStatus(ActivityStatusEnum.RUNNING);
        convertActivityStatusDTO.setPrizeId(param.getPrizeId());
        convertActivityStatusDTO.setTargetPrizeStatus(ActivityPrizeStatusEnum.INIT);
        convertActivityStatusDTO.setUserIds(
                param.getWinnerList()
                        .stream()
                        .map(DrawPrizeParam.Winner::getUserId)
                        .collect(Collectors.toList())
        );
        convertActivityStatusDTO.setTargetUserStatus(ActivityUserStatusEnum.INIT);

        activityStatusManager.rollbackHandlerEvent(convertActivityStatusDTO);
    }

    private boolean statusNeedRollback(DrawPrizeParam param) {
        // 判断活动，人员，活动关联奖品状态是否已经扭转

        // 扭转状态时，保证了事务一致性，因此只需判断一张表是否扭转（不包含活动表）
        ActivityPrizeDO activityPrizeDO = activityPrizeMapper.selectByAPId(param.getActivityId(), param.getPrizeId());

        return activityPrizeDO.getStatus()
                .equalsIgnoreCase(ActivityPrizeStatusEnum.COMPLETED.name());
    }

    /**
     * 并发处理
     *
     * @param winningRecordDOList
     */
    private void syncExecute(List<WinningRecordDO> winningRecordDOList) {
        // 通过线程池处理异步任务 threadPoolTaskExecutor

        // 短信通知 TODO
        // threadPoolTaskExecutor.execute(() -> sendMessage(winningRecordDOList));

        // 邮件通知
        threadPoolTaskExecutor.execute(() -> sendMail(winningRecordDOList));

    }

    /**
     * 发送邮件
     * @param winningRecordDOList
     */
    private void sendMail(List<WinningRecordDO> winningRecordDOList) {

        if(CollectionUtils.isEmpty(winningRecordDOList)){
            logger.warn("中将列表为空, 不需要发送邮件! ");
            return;
        }

        for (WinningRecordDO winningRecordDO : winningRecordDOList){

            String context = "Hi，" + winningRecordDO.getWinnerName() + "。恭喜你在"
                    + winningRecordDO.getActivityName() + "活动中获得"
                    + ActivityPrizeTiersEnum.forName(winningRecordDO.getPrizeTier()).getMessage()
                    + "，奖品为：" + winningRecordDO.getPrizeName() + "。获奖时间为"
                    + DateUtil.formatTime(winningRecordDO.getWinningTime()) + "，请尽快领取您的奖励！";

            mailUtil.sendSampleMail(winningRecordDO.getWinnerEmail(), "中奖通知", context);
        }
    }

    /**
     * 状态扭转
     *
     * @param param
     */
    private void statusConvert(DrawPrizeParam param) {

        ConvertActivityStatusDTO convertActivityStatusDTO = new ConvertActivityStatusDTO();
        convertActivityStatusDTO.setActivityId(param.getActivityId());
        convertActivityStatusDTO.setTargetActivityStatus(ActivityStatusEnum.COMPLETED);
        convertActivityStatusDTO.setPrizeId(param.getPrizeId());
        convertActivityStatusDTO.setTargetPrizeStatus(ActivityPrizeStatusEnum.COMPLETED);
        convertActivityStatusDTO.setUserIds(
                param.getWinnerList()
                        .stream()
                        .map(DrawPrizeParam.Winner::getUserId)
                        .collect(Collectors.toList())
        );
        convertActivityStatusDTO.setTargetUserStatus(ActivityUserStatusEnum.COMPLETED);


        activityStatusManager.handlerEvent(convertActivityStatusDTO);

    }
//    private void statusConvert(DrawPrizeParam param) {
//
//        // 问题:
//        // 1. 活动状态扭转具有依赖性, 代码维护性差
//        // 2. 状态扭转条件也许扩展, 扩展性差
//        // 3. 代码的灵活性, 扩展性, 维护性差
//        // 解决方案: 使用设计模式( 责任链模式, 策略模式, 单例模式 )
//
//
//        // 活动状态: RUNNING --> COMPLETED
//        // 奖品全部抽完之后才修改状态
//
//        // 活动关联奖品状态: init --> COMPLETED
//
//        // 人员状态: init --> COMPLETED
//
//        // 1. 扭转奖品状态
//        // 查询活动关联奖品信息,
//        // 判断当前状态是否为 COMPLETED, 如果是, 就不要进行扭转了, 如果不是就扭转
//
//
//        // 2. 扭转人员状态
//        // 查询活动关联人员信息,
//        // 判断当前状态是否为 COMPLETED, 如果是, 就不要进行扭转了, 如果不是就扭转
//
//        // 3. 扭转活动状态
//        // 查询活动关联人员信息,
//        // 判断当前状态是否为 COMPLETED, 如果是, 就不要进行扭转了, 如果不是且全部奖品抽完之后, 才扭转
//
//        // 更新缓存
//    }
}
