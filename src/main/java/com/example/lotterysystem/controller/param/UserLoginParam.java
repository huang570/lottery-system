package com.example.lotterysystem.controller.param;

import lombok.Data;

import java.io.Serializable;

/**
 * 请求公共参数
 */
@Data
public class UserLoginParam implements Serializable {
    /**
     * 强制某身份登录，不填不限制身份
     */
    private String mandatoryIdentity;
}
