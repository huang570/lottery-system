package com.example.lotterysystem.service.impl;

import com.example.lotterysystem.common.constant.Constants;
import com.example.lotterysystem.common.errorcode.ServiceErrorCodeConstants;
import com.example.lotterysystem.common.exception.ServiceException;
import com.example.lotterysystem.common.utils.CaptchaUtil;
import com.example.lotterysystem.common.utils.RedisUtil;
import com.example.lotterysystem.common.utils.RegexUtil;
import com.example.lotterysystem.common.utils.SMSUtil;
import com.example.lotterysystem.service.VerificationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 阿里云短信验证码服务类
 */
@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {

    @Autowired
    private SMSUtil smsUtil;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 发送并缓存验证码
     *
     * @param phoneNumber
     */
    @Override
    public void sendVerificationCode(String phoneNumber) {
        // 校验手机号
        if(!RegexUtil.checkMobile(phoneNumber)){
            throw new ServiceException(ServiceErrorCodeConstants.PHONE_NUMBER_ERROR);
        }

        // 生成随机验证码 ，长度 4
        String code = CaptchaUtil.getCaptcha(Constants.CODE_LENGTH);

        // 发送验证码
        smsUtil.sendVerifyCode(phoneNumber,code);

        // 缓存验证码
        // 一个手机号对应一个验证码，再次接收会覆盖
        redisUtil.set(Constants.VERIFICATION_CODE_PREFIX + phoneNumber
                ,code
                ,Constants.VERIFICATION_CODE_TIMEOUT);

    }

    /**
     * 获取验证码
     *
     * @param phoneNumber
     * @return
     */
    @Override
    public String getVerificationCode(String phoneNumber) {
        if(!RegexUtil.checkMobile(phoneNumber)){
            throw new ServiceException(ServiceErrorCodeConstants.PHONE_NUMBER_ERROR);
        }

        return redisUtil.get(Constants.VERIFICATION_CODE_PREFIX + phoneNumber);
    }

}
