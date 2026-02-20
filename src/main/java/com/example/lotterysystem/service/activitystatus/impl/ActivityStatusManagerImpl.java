package com.example.lotterysystem.service.activitystatus.impl;

import com.example.lotterysystem.common.errorcode.ServiceErrorCodeConstants;
import com.example.lotterysystem.common.exception.ServiceException;
import com.example.lotterysystem.service.ActivityService;
import com.example.lotterysystem.service.activitystatus.ActivityStatusManager;
import com.example.lotterysystem.service.activitystatus.operater.AbstractActivityOperator;
import com.example.lotterysystem.service.dto.ConvertActivityStatusDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Component
public class ActivityStatusManagerImpl implements ActivityStatusManager {

    private static final Logger logger = LoggerFactory.getLogger(ActivityStatusManagerImpl.class);

    private static final int PROCESS_TYPE_USER_PRIZE = 1; // 人员、奖品
    private static final int PROCESS_TYPE_ACTIVITY = 2;     // 活动

    @Autowired
    private final Map<String, AbstractActivityOperator> operatorMap = new HashMap<>();

    @Autowired
    private ActivityService activityService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handlerEvent(ConvertActivityStatusDTO convertActivityStatusDTO) {

        // map<String, AbstractActivityOperator>
        if(CollectionUtils.isEmpty(operatorMap)){
            logger.warn("operatorMap 为空");
            return;
        }

        Map<String, AbstractActivityOperator> currMap = new HashMap<>(operatorMap);
        Boolean update = false;

        // 先处理: 人员, 奖品
        update = processConvertStatus(convertActivityStatusDTO, currMap, PROCESS_TYPE_USER_PRIZE);

        // 后处理: 活动
        update = processConvertStatus(convertActivityStatusDTO, currMap, PROCESS_TYPE_ACTIVITY) || update;

        if(update){
            // 更新缓存
            activityService.cacheActivity(convertActivityStatusDTO.getActivityId());
        }


    }

    @Override
    public void rollbackHandlerEvent(ConvertActivityStatusDTO convertActivityStatusDTO) {
        for (AbstractActivityOperator operator : operatorMap.values()){
            operator.convert(convertActivityStatusDTO);
        }

        // 缓存更新
        activityService.cacheActivity(convertActivityStatusDTO.getActivityId());
    }

    /**
     * 扭转状态
     *
     * @param convertActivityStatusDTO
     * @param currMap
     * @param sequence
     * @return
     */
    private Boolean processConvertStatus(ConvertActivityStatusDTO convertActivityStatusDTO
            , Map<String, AbstractActivityOperator> currMap
            , int sequence) {

        Boolean update = false;

        // 遍历 currMap
        Iterator<Map.Entry<String, AbstractActivityOperator>> iterator = currMap.entrySet().iterator();
        while (iterator.hasNext()){
            AbstractActivityOperator operator = iterator.next().getValue();
            // Operatior 是否需要转换
            if(operator.sequence() != sequence
                    || !operator.needConvert(convertActivityStatusDTO)){
                continue;
            }

            // 需要转换
            if(!operator.convert(convertActivityStatusDTO)){
                logger.error("{} 状态转换失败! ",operator.getClass().getName());
                throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_STATUS_CONVERT_ERROR);
            }

            // currMap 删除当前Operatior
            iterator.remove();
            update = true;
        }
        // 返回
        return update;
    }
}
