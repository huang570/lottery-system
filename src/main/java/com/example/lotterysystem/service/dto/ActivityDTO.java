package com.example.lotterysystem.service.dto;

import com.example.lotterysystem.service.enums.ActivityStatusEnum;
import lombok.Data;

@Data
public class ActivityDTO {

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 活动名称
     */
    private String activityName;

    /**
     * 活动描述
     */
    private String description;

    /**
     * 活动是否有效
     */
    private ActivityStatusEnum status;

    public Boolean valid(){
        return status.equals(ActivityStatusEnum.RUNNING);
    }
}
