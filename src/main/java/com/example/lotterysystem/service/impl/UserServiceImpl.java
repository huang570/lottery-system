package com.example.lotterysystem.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.example.lotterysystem.common.constant.Constants;
import com.example.lotterysystem.common.errorcode.ServiceErrorCodeConstants;
import com.example.lotterysystem.common.exception.ServiceException;
import com.example.lotterysystem.common.utils.JWTUtil;
import com.example.lotterysystem.common.utils.JacksonUtil;
import com.example.lotterysystem.common.utils.RegexUtil;
import com.example.lotterysystem.controller.param.ShortMessageLoginParam;
import com.example.lotterysystem.controller.param.UserLoginParam;
import com.example.lotterysystem.controller.param.UserPasswordLoginParam;
import com.example.lotterysystem.controller.param.UserRegisterParam;
import com.example.lotterysystem.dao.dataobject.Encrypt;
import com.example.lotterysystem.dao.dataobject.UserDO;
import com.example.lotterysystem.dao.mapper.UserMapper;
import com.example.lotterysystem.service.UserService;
import com.example.lotterysystem.service.VerificationCodeService;
import com.example.lotterysystem.service.dto.UserDTO;
import com.example.lotterysystem.service.dto.UserLoginDTO;
import com.example.lotterysystem.service.dto.UserRegisterDTO;
import com.example.lotterysystem.service.enums.UserIdentityEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private VerificationCodeService verificationCodeService;

    /**
     * 注册
     *
     * @param param
     * @return
     */
    @Override
    public UserRegisterDTO register(UserRegisterParam param) {
        // 校验用户信息
        checkRegisterInfo(param);
        //加密数据
        UserDO userDO = new UserDO();
        userDO.setUserName(param.getName());
        userDO.setEmail(param.getMail());
        userDO.setPhoneNumber(new Encrypt(param.getPhoneNumber()));
        userDO.setIdentity(param.getIdentity());
        if(StringUtils.hasLength(param.getPassword())){
            userDO.setPassword(DigestUtil.sha256Hex(param.getPassword()));
        }
        // 保存用户数据
        userMapper.insert(userDO);
        // 构造返回


        UserRegisterDTO userRegisterDTO = new UserRegisterDTO();
        userRegisterDTO.setUserId(userDO.getId());
        return userRegisterDTO;
    }

    /**
     * 登录
     *
     * @param param
     * @return
     */
    @Override
    public UserLoginDTO login(UserLoginParam param) {
        UserLoginDTO userLoginDTO;

        // 类型校验和类型转换.
        if(param instanceof UserPasswordLoginParam loginParam){
            // 密码登录流程
            userLoginDTO = loginByUserPassword(loginParam);
        }else if(param instanceof ShortMessageLoginParam loginParam){
            // 短信验证登录流程
            userLoginDTO = loginByShortMessage(loginParam);
        }else {
            throw new ServiceException(ServiceErrorCodeConstants.LOGIN_INFO_NOT_EXIST);
        }


        return userLoginDTO;
    }

    @Override
    public List<UserDTO> findUserInfo(UserIdentityEnum identity) {
        String identityString = null == identity ? null : identity.name();

        List<UserDO> userDOList = userMapper.selectUserListByIdentity(identityString);
        List<UserDTO> userDTOList = userDOList.stream()
                .map(userDO -> {
                    UserDTO userDTO = new UserDTO();
                    userDTO.setUserId(userDO.getId());
                    userDTO.setUserName(userDO.getUserName());
                    userDTO.setEmail(userDO.getEmail());
                    userDTO.setPhoneNumber(userDO.getPhoneNumber().getValue());
                    userDTO.setIdentity(UserIdentityEnum.forName(userDO.getIdentity()));
                    return userDTO;
                }).collect(Collectors.toList());
        return userDTOList;
    }

    /**
     * 验证码登录
     *
     * @param loginParam
     * @return
     */
    private UserLoginDTO loginByShortMessage(ShortMessageLoginParam loginParam) {
        if(!RegexUtil.checkMobile(loginParam.getLoginName())){
            throw new ServiceException(ServiceErrorCodeConstants.PHONE_NUMBER_ERROR);
        }

        UserDO userDO = userMapper.selectByPhoneNumber(
                new Encrypt(loginParam.getLoginName()));
        log.info("userDO: {}", JacksonUtil.writeValueAsString(userDO));
        log.info("loginParam.getMandatoryIdentity(): {}", JacksonUtil.writeValueAsString(loginParam));
        if(null == userDO){
            throw new ServiceException(ServiceErrorCodeConstants.USER_INFO_IS_EMPTY);
        } else if (StringUtils.hasText(loginParam.getMandatoryIdentity())) {
            throw new ServiceException(ServiceErrorCodeConstants.IDENTITY_ERROR);
        }
        log.info("接收userDO：{}",userDO.toString());
        // 校验验证码
        String code = verificationCodeService.getVerificationCode(
                loginParam.getLoginName());

        if(!loginParam.getVerificationCode().equals(code)){
            throw new ServiceException(ServiceErrorCodeConstants.VERIFICATION_CODE_ERROR);
        }

        // 塞入返回值
        Map<String,Object> claim = new HashMap<>();
        // 随机存放可解密
        claim.put("id",userDO.getId());
        claim.put("identity",userDO.getIdentity());

        String token = JWTUtil.genJwt(claim);

        UserLoginDTO userLoginDTO = new UserLoginDTO();
        userLoginDTO.setToken(token);
        userLoginDTO.setIdentity(UserIdentityEnum.forName(userDO.getIdentity()));

        return userLoginDTO;
    }

    /**
     * 密码登录
     *
     * @param loginParam
     * @return
     */
    private UserLoginDTO loginByUserPassword(UserPasswordLoginParam loginParam) {
        UserDO userDO = null;

        // 判断手机还是邮箱登录
        if(RegexUtil.checkMail(loginParam.getLoginName())){
            // 邮箱登录
            userDO = userMapper.selectByMail(loginParam.getLoginName());
        }else if(RegexUtil.checkMobile(loginParam.getLoginName())){
            // 手机号登录
            userDO = userMapper.selectByPhoneNumber(new Encrypt(loginParam.getLoginName()));
        }else {
            throw new ServiceException(ServiceErrorCodeConstants.LOGIN_NOT_EXIST);
        }

        // 校验登录信息
        if(null == userDO){
            throw new ServiceException(ServiceErrorCodeConstants.USER_INFO_IS_EMPTY);
        }else if(StringUtils.hasText(loginParam.getMandatoryIdentity())
                    && !loginParam.getMandatoryIdentity().equalsIgnoreCase(userDO.getIdentity())){
            // 强制身份登录校验不通过
            throw new ServiceException(ServiceErrorCodeConstants.IDENTITY_ERROR);
        }else if(!DigestUtil.sha256Hex(loginParam.getPassword())
                .equals(userDO.getPassword())){
            // 校验密码
            throw new ServiceException(ServiceErrorCodeConstants.PASSWORD_ERROR);
        }
        // 塞入返回值
        Map<String,Object> claim = new HashMap<>();
        // 随机存放可解密
        claim.put("id",userDO.getId());
        claim.put("identity",userDO.getIdentity());

        String token = JWTUtil.genJwt(claim);

        UserLoginDTO userLoginDTO = new UserLoginDTO();
        userLoginDTO.setToken(token);
        userLoginDTO.setIdentity(UserIdentityEnum.forName(userDO.getIdentity()));

        return userLoginDTO;
    }

    private void checkRegisterInfo(UserRegisterParam param) {
        if(null == param){
            throw new ServiceException(ServiceErrorCodeConstants.REGISTER_INFO_IS_EMPTY);
        }
        // 校验邮箱格式 xxx@xxx.xxx
        if(!RegexUtil.checkMail(param.getMail())){
            throw new ServiceException(ServiceErrorCodeConstants.MAIL_ERROR);
        }
        // 校验手机号格式
        if(!RegexUtil.checkMobile(param.getPhoneNumber())){
            throw new ServiceException(ServiceErrorCodeConstants.PHONE_NUMBER_ERROR);
        }
        // 校验身份信息
        if(null == UserIdentityEnum.forName(param.getIdentity())){
            throw new ServiceException(ServiceErrorCodeConstants.IDENTITY_ERROR);
        }
        // 校验密码（管理员必填）
        if (param.getIdentity().equalsIgnoreCase(UserIdentityEnum.ADMIN.name())
                && !StringUtils.hasLength(param.getPassword())) {
            throw new ServiceException(ServiceErrorCodeConstants.PASSWORD_IS_EMPTY);
        }
        // 校验密码格式 至少6位
        if(StringUtils.hasLength(param.getPassword())
                && !RegexUtil.checkPassword(param.getPassword())){
            throw new ServiceException(ServiceErrorCodeConstants.PASSWORD_ERROR);
        }
        // 校验邮箱唯一
        if(checkMailUsed(param.getMail())){
            throw new ServiceException(ServiceErrorCodeConstants.MAIL_USED);
        }
        // 校验手机号唯一
        if(checkPhoneNumberUsed(param.getPhoneNumber())){
            throw new ServiceException(ServiceErrorCodeConstants.PHONE_NUMBER_USED);
        }
    }

    /**
     * 检验手机号是否被使用
     * @param phoneNumber
     * @return
     */
    private boolean checkPhoneNumberUsed(String phoneNumber) {
        int count = userMapper.countByPhone(new Encrypt(phoneNumber));
        return count > 0;
    }

    /**
     * 校验邮箱是否被使用
     * @param mail
     * @return
     */
    private boolean checkMailUsed(String mail) {
        int count = userMapper.countByMail(mail);
        return count > 0;
    }
}
