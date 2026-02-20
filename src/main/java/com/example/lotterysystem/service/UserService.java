package com.example.lotterysystem.service;

import com.example.lotterysystem.controller.param.UserLoginParam;
import com.example.lotterysystem.controller.param.UserPasswordLoginParam;
import com.example.lotterysystem.controller.param.UserRegisterParam;
import com.example.lotterysystem.service.dto.UserDTO;
import com.example.lotterysystem.service.dto.UserLoginDTO;
import com.example.lotterysystem.service.dto.UserRegisterDTO;
import com.example.lotterysystem.service.enums.UserIdentityEnum;

import java.util.List;

public interface UserService {
    /**
     * 注册
     */
    UserRegisterDTO register(UserRegisterParam param);

    /**
     * 用户登录
     *
     * @param param
     * @return
     */
    UserLoginDTO login(UserLoginParam param);

    /**
     * 根据身份查询人员列表
     *
     * @param identity 如果为空，查询所有身份人员
     * @return
     */
    List<UserDTO> findUserInfo(UserIdentityEnum identity);
}
