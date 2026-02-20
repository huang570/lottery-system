package com.example.lotterysystem.controller.result;

import lombok.Data;

import java.io.Serializable;

@Data
public class BaseUserInfoResult implements Serializable {
    /**
     * 人员ID
     */
    private Long userId;

    /**
     * 姓名
     */
    private String userName;

    /**
     * 身份信息
     */
    private String identity;
}
