package com.example.lotterysystem.service.dto;

import com.example.lotterysystem.service.enums.ActivityPrizeStatusEnum;
import com.example.lotterysystem.service.enums.ActivityPrizeTiersEnum;
import com.example.lotterysystem.service.enums.ActivityStatusEnum;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PrizeDetailDTO {

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
    private ActivityPrizeTiersEnum tiers;

    /**
     * 奖品数量
     */
    private Long prizeAmount;

    /**
     * 奖品状态
     */
    private ActivityPrizeStatusEnum status;

    public Boolean valid(){
        return status.equals(ActivityPrizeStatusEnum.INIT);
    }
}
