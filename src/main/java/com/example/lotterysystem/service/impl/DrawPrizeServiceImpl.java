package com.example.lotterysystem.service.impl;

import com.example.lotterysystem.common.errorcode.ServiceErrorCodeConstants;
import com.example.lotterysystem.common.exception.ServiceException;
import com.example.lotterysystem.common.utils.JacksonUtil;
import com.example.lotterysystem.common.utils.RedisUtil;
import com.example.lotterysystem.controller.param.DrawPrizeParam;
import com.example.lotterysystem.controller.param.ShowWinningRecordsParam;
import com.example.lotterysystem.dao.dataobject.*;
import com.example.lotterysystem.dao.mapper.*;
import com.example.lotterysystem.service.DrawPrizeService;
import com.example.lotterysystem.service.dto.WinningRecordDTO;
import com.example.lotterysystem.service.enums.ActivityPrizeStatusEnum;
import com.example.lotterysystem.service.enums.ActivityPrizeTiersEnum;
import com.example.lotterysystem.service.enums.ActivityStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.example.lotterysystem.common.config.DirectRabbitConfig.EXCHANGE_NAME;
import static com.example.lotterysystem.common.config.DirectRabbitConfig.ROUTING;

@Slf4j
@Service
public class DrawPrizeServiceImpl implements DrawPrizeService {

    private static final Long WINNING_RECORDS_TIMEOUT = 60 * 60 * 24 * 7L;
    private static final String WINNING_RECORDS_PREFIX = "WINNING_RECORDS_";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private ActivityPrizeMapper activityPrizeMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PrizeMapper prizeMapper;

    @Autowired
    private WinningRecordMapper winningRecordMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public void drawPrize(DrawPrizeParam param) {

        Map<String, String> map = new HashMap<>();
        map.put("messageId",String.valueOf(UUID.randomUUID()));
        map.put("messageData", JacksonUtil.writeValueAsString(param));
        // 发消息: 交换机, 绑定的key, 哪个队列, 消息体
        rabbitTemplate.convertAndSend(EXCHANGE_NAME,ROUTING,map);

        log.info("mq消息发送成功, map: {}",JacksonUtil.writeValueAsString(map));
    }

    @Override
    public Boolean checkDrawPrizeParam(DrawPrizeParam param) {

        ActivityDO activityDO = activityMapper.selectById(param.getActivityId());

        ActivityPrizeDO activityPrizeDO = activityPrizeMapper.selectByAPId(
                param.getActivityId(), param.getPrizeId()
        );

        // 活动或奖品是否存在
        if(null == activityDO || null == activityPrizeDO){
            log.info("校验抽奖请求失败！失败原因: {}"
                    , ServiceErrorCodeConstants.ACTIVITY_OR_PRIZE_IS_EMPTY.getMsg());
            // throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_OR_PRIZE_IS_EMPTY);
            return false;
        }

        // 活动是否有效
        if(activityDO.getStatus().equals(ActivityStatusEnum.COMPLETED.name())){
            // throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_COMPLETED);
            log.info("校验抽奖请求失败！失败原因: {}"
                    , ServiceErrorCodeConstants.ACTIVITY_COMPLETED.getMsg());

            return false;
        }

        // 奖品是否有效
        if(activityPrizeDO.getStatus().equals(ActivityPrizeStatusEnum.COMPLETED.name())){
            // throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_PRIZE_COMPLETED);
            log.info("校验抽奖请求失败！失败原因: {}"
                    , ServiceErrorCodeConstants.ACTIVITY_PRIZE_COMPLETED.getMsg());

            return false;
        }

        // 中奖者列表和奖品数量
        if(activityPrizeDO.getPrizeAmount() != param.getWinnerList().size()){
            // throw new ServiceException(ServiceErrorCodeConstants.WINNER_PRIZE_AMOUNT_ERROR);
            log.info("校验抽奖请求失败！失败原因: {}"
                    , ServiceErrorCodeConstants.WINNER_PRIZE_AMOUNT_ERROR.getMsg());

            return false;
        }
        return true;
    }

    @Override
    public List<WinningRecordDO> saveWinnerRecords(DrawPrizeParam param) {

        // 查询相关信息，活动，人员，奖品，活动关联奖品......
        ActivityDO activityDO = activityMapper.selectById(param.getActivityId());
        List<UserDO> userDOList = userMapper.batchSelectByIds(
                param.getWinnerList()
                        .stream()
                        .map(DrawPrizeParam.Winner::getUserId)
                        .collect(Collectors.toList())
        );
        PrizeDO prizeDO = prizeMapper.selectById(param.getPrizeId());
        ActivityPrizeDO activityPrizeDO = activityPrizeMapper.selectByAPId(param.getActivityId(), param.getPrizeId());

        // 构造中奖者记录

        List<WinningRecordDO> winningRecordDOList = userDOList
                .stream()
                .map(userDO -> {
                    WinningRecordDO winningRecordDO = new WinningRecordDO();
                    winningRecordDO.setActivityId(activityDO.getId());
                    winningRecordDO.setActivityName(activityDO.getActivityName());
                    winningRecordDO.setPrizeId(prizeDO.getId());
                    winningRecordDO.setPrizeName(prizeDO.getName());
                    winningRecordDO.setPrizeTier(activityPrizeDO.getPrizeTiers());
                    winningRecordDO.setWinnerId(userDO.getId());
                    winningRecordDO.setWinnerName(userDO.getUserName());
                    winningRecordDO.setWinnerEmail(userDO.getEmail());
                    winningRecordDO.setWinnerPhoneNumber(userDO.getPhoneNumber());
                    winningRecordDO.setWinningTime(param.getWinningTime());

                    return winningRecordDO;
                }).collect(Collectors.toList());

        // 保存中奖记录
        winningRecordMapper.batchInsert(winningRecordDOList);

        // 缓存中奖者记录
        // 缓存奖品的中奖信息（key:前缀 + activityId + prizeId, winningRecordDOList（奖品维度））
        cacheWinningRecords( param.getActivityId() + "_" + param.getPrizeId()
                , winningRecordDOList
                , WINNING_RECORDS_TIMEOUT);

        // 缓存活动维度的中奖记录（key:前缀 +  activityId, winningRecordDOList（活动维度中奖名单））
        // 当活动已完成去存放活动维度中奖记录
        if(activityDO.getStatus()
                .equalsIgnoreCase(ActivityStatusEnum.COMPLETED.name())){

            List<WinningRecordDO> allList = winningRecordMapper.selectByActivityId(param.getActivityId());
            cacheWinningRecords( String.valueOf(param.getActivityId())
                    , allList
                    , WINNING_RECORDS_TIMEOUT);
        }

        return winningRecordDOList;
    }

    /**
     * 删除中奖记录
     * 
     * @param activityId
     * @param prizeId
     */
    @Override
    public void deleteRecords(Long activityId, Long prizeId) {
        
        if(null == activityId){
            log.warn("要删除的中奖记录相关的活动ID为空！");
            return;
        }
        
        // 删除数据表
        winningRecordMapper.deleteRecords(activityId,prizeId);
        
        // 删除缓存，奖品维度缓存，活动维度缓存
        if(null != prizeId){
            // 删除奖品维度
            deleteWinningRecords(activityId + "_" + prizeId);
        }

        // 删除活动维度
        deleteWinningRecords(String.valueOf(activityId));
    }

    @Override
    public List<WinningRecordDTO> getRecords(ShowWinningRecordsParam param) {
        // 查询redis: 奖品，活动
        String key = null == param.getPrizeId()
                ? String.valueOf(param.getActivityId())
                : param.getActivityId() + "_" + param.getPrizeId();
        List<WinningRecordDO> winningRecordDOList = getWinningRecords(key);

        if(CollectionUtils.isEmpty(winningRecordDOList)){
            return convetToWinningRecordDTOList(winningRecordDOList);
        }
        // 如果redis不存在， 查库

        winningRecordDOList = winningRecordMapper.selectByActivityIdOrPrizeId(param.getActivityId(),param.getPrizeId());

        // 整合存放记录到redis中
        if(CollectionUtils.isEmpty(winningRecordDOList)){
            log.info("查询的中奖记录为空, param: {}",JacksonUtil.writeValueAsString(param));
            return Arrays.asList();
        }

        cacheWinningRecords(key , winningRecordDOList , WINNING_RECORDS_TIMEOUT);

        // 构造返回
        return convetToWinningRecordDTOList(getWinningRecords(key));
    }

    private List<WinningRecordDTO> convetToWinningRecordDTOList(List<WinningRecordDO> winningRecordDOList) {

        if(CollectionUtils.isEmpty(winningRecordDOList)){
            return Arrays.asList();
        }

        return winningRecordDOList
                .stream()
                .map(winningRecordDO -> {
                    WinningRecordDTO winningRecordDTO = new WinningRecordDTO();
                    winningRecordDTO.setWinnerId(winningRecordDO.getWinnerId());
                    winningRecordDTO.setWinnerName(winningRecordDO.getWinnerName());
                    winningRecordDTO.setPrizeName(winningRecordDO.getPrizeName());
                    winningRecordDTO.setPrizeTier(
                            ActivityPrizeTiersEnum.forName(winningRecordDO.getPrizeTier()));
                    winningRecordDTO.setWinningTime(winningRecordDO.getWinningTime());

                    return winningRecordDTO;
                }).collect(Collectors.toList());
    }

    /**
     * 从缓存中删除中奖记录
     *
     * @param key
     */
    private void deleteWinningRecords(String key) {
        try {
            if(redisUtil.hasKey(WINNING_RECORDS_PREFIX + key)){
                // 存在再去删除
                redisUtil.del(WINNING_RECORDS_PREFIX + key);
            }
        }catch (Exception e){
            log.error("删除缓存中奖记录异常,key: {}",key);
        }
    }

    /**
     * 缓存中奖记录
     *
     * @param key
     * @param winningRecordDOList
     * @param time
     */
    private void cacheWinningRecords(String key
            , List<WinningRecordDO> winningRecordDOList
            , Long time) {

        String str = "";

        try {
            if(!StringUtils.hasText(key)
                    || CollectionUtils.isEmpty(winningRecordDOList)){
                log.warn("要缓存的内容为空！key: {},value: {}",key,JacksonUtil.writeValueAsString(winningRecordDOList));
                return;
            }
            str = JacksonUtil.writeValueAsString(winningRecordDOList);
            redisUtil.set(WINNING_RECORDS_PREFIX + key
                    , str, time);

        }catch (Exception e){
            log.info("缓存中奖记录异常！key: {},value: {}",WINNING_RECORDS_PREFIX + key, str);
        }
    }

    /**
     * 从缓存中获取中奖记录
     *
     * @param key
     * @return
     */
    private List<WinningRecordDO> getWinningRecords(String key){

        try {
            if(!StringUtils.hasText(key)){
                log.warn("要从缓存中查询的中奖记录key为空! key: {}",key);
                return Arrays.asList();
            }
            String str = redisUtil.get(WINNING_RECORDS_PREFIX + key);
            if(!StringUtils.hasText(str)){
                return Arrays.asList();
            }

            return JacksonUtil.readListValue(str, WinningRecordDO.class);
        }catch (Exception e){
            log.error("从缓存中查询中奖记录异常! key: {}",WINNING_RECORDS_PREFIX + key);
            return Arrays.asList();
        }
    }
}
