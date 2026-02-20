package com.example.lotterysystem.controller.result;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class FIndActivityListResult implements Serializable {

    /**
     * 活动总量
     */
    private Integer total;

    /**
     * 当前页奖品列表
     */
    private List<ActivityInfo> records;

    @Data
    public static class ActivityInfo implements Serializable {
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
        private Boolean valid;
    }
}
