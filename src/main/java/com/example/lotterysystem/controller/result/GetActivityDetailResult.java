package com.example.lotterysystem.controller.result;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class GetActivityDetailResult implements Serializable {
    // 活动信息
    private Long activityId;

    private String activityName;

    private String description;

    /**
     * 活动是否有效
     */
    private Boolean valid;

    // 奖品信息（列表）
    private List<Prize> prizes;

    // 人员信息（列表）
    private List<User> users;

    @Data
    public static class Prize{
        /**
         * 奖品ID
         */
        private Long prizeId;

        /**
         * 奖品名称
         */
        private String name;

        /**
         * 奖品图
         */
        private String imageUrl;

        /**
         * 奖品价格
         */
        private BigDecimal price;

        /**
         * 描述
         */
        private String description;

        /**
         * 奖品等级
         */
        private String prizeTierName;

        /**
         * 奖品数量
         */
        private Long prizeAmount;

        /**
         * 奖品是否有效
         */
        private Boolean valid;

    }

    @Data
    public static class User{
        /**
         * 用户 id
         */
        private Long userId;

        /**
         * 用户名
         */
        private String userName;

        /**
         * 用户是否被抽取
         */
        private Boolean valid;
    }
}
