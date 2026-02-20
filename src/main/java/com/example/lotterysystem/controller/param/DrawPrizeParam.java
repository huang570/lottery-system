package com.example.lotterysystem.controller.param;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class DrawPrizeParam {

    /**
     * 活动ID
     */
    @NotNull(message = "活动Id不能为空")
    private Long activityId;

    /**
     * 奖品ID
     */
    @NotNull(message = "奖品Id不能为空")
    private Long prizeId;

    /**
     * 中奖时间
     */
    @NotNull(message = "中奖时间不能为空")
    private Date winningTime;

    /**
     * 中奖者列表
     */
    @NotEmpty
    @Valid
    private List<Winner> winnerList;

    @Data
    public static class Winner{

        /**
         * 中奖者ID
         */
        @NotNull(message = "中奖者Id 不能为空")
        private Long userId;

        /**
         * 中奖者姓名
         */
        @NotBlank(message = "中奖者姓名不能为空")
        private String userName;
    }
}
