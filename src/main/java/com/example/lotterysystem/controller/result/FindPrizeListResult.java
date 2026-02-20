package com.example.lotterysystem.controller.result;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class FindPrizeListResult implements Serializable {

    /**
     * 奖品总量
     */
    private Integer total;

    /**
     * 当前页奖品列表
     */
    private List<PrizeInfo> records;

    @Data
    public static class PrizeInfo implements Serializable{

        /**
         * 奖品ID
         */
        private Long prizeId;

        /**
         * 奖品名称
         */
        private String prizeName;

        /**
         * 描述
         */
        private String description;

        /**
         * 奖品价格
         */
        private BigDecimal price;

        /**
         * 奖品图
         */
        private String imageUrl;
    }


}
