package com.example.lotterysystem.dao.dataobject;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class PrizeDO extends BaseDO{

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
