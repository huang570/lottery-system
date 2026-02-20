package com.example.lotterysystem.service.dto;

import com.example.lotterysystem.service.enums.ActivityStatusEnum;
import com.example.lotterysystem.service.enums.ActivityUserStatusEnum;
import lombok.Data;

import java.util.List;

@Data
public class ActivityDetailDTO {
    // 活动信息
    private Long activityId;

    private String activityName;

    private String desc;

    private ActivityStatusEnum status;

    public Boolean valid(){
        return status.equals(ActivityStatusEnum.RUNNING);
    }

    // 奖品信息（列表）
    private List<PrizeDetailDTO> prizeDetailDTOList;

    // 人员信息（列表）
    private List<UserDetaiDTO> userDetaiDTOList;
}
