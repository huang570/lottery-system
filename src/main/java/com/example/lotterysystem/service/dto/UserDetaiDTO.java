package com.example.lotterysystem.service.dto;

import com.example.lotterysystem.service.enums.ActivityPrizeStatusEnum;
import com.example.lotterysystem.service.enums.ActivityUserStatusEnum;
import com.example.lotterysystem.service.enums.UserIdentityEnum;
import lombok.Data;

@Data
public class UserDetaiDTO {

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户状态
     */
    private ActivityUserStatusEnum status;

    public Boolean valid(){
        return status.equals(ActivityUserStatusEnum.INIT);
    }
}
