package com.example.lotterysystem.service.dto;

import com.example.lotterysystem.dao.dataobject.Encrypt;
import com.example.lotterysystem.service.enums.UserIdentityEnum;
import lombok.Data;

/**
 * 人员全属性
 */
@Data
public class UserDTO {

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phoneNumber;

    /**
     * 身份信息
     */
    private UserIdentityEnum identity;

}
