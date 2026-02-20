package com.example.lotterysystem.controller.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录接口返回
 */
@Data
public class UserLoginResult implements Serializable {
    /**
     * JWT 令牌
     */
    private String token;
    /**
     * 登录人员身份
     */
    private String identity;
}
