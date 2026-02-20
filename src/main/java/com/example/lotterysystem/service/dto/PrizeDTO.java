package com.example.lotterysystem.service.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PrizeDTO {
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
}
