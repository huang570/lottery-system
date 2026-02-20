package com.example.lotterysystem.common.utils;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.example.lotterysystem.common.constant.Constants;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 短信服务
 */
@Component
public class SMSUtil {

    private static final Logger logger = LoggerFactory.getLogger(SMSUtil.class);

    private static final Gson GSON = new Gson();

    @Value("${sms.access-key-id}")
    private String accessKeyId;

    @Value("${sms.access-key-secret}")
    private String accessKeySecret;

//    @Value("${sms.sign-name}")
    private String signName = Constants.SIGNNAME;

    /**
     * 发送短信验证码
     */
    public boolean sendVerifyCode(String phoneNumber, String code) {

        try {

            Client client = createClient();

            /* ===== 用 Map 构造参数，避免转义问题 ===== */
            Map<String, String> param = new HashMap<>();
            param.put("code", code);
            param.put("min", "5"); // 和控制台保持一致

            String templateParam = GSON.toJson(param);

            logger.info("发送短信参数: {}", templateParam);

            SendSmsVerifyCodeRequest request =
                    new SendSmsVerifyCodeRequest()
                            .setSignName(signName)
                            .setTemplateCode("100001")
                            .setPhoneNumber(phoneNumber)
                            .setTemplateParam(templateParam);

            RuntimeOptions runtime = new RuntimeOptions();

            SendSmsVerifyCodeResponse response =
                    client.sendSmsVerifyCodeWithOptions(request, runtime);

            if (response.getBody() != null
                    && "OK".equals(response.getBody().getMessage())
                    && null != response.getBody().getMessage()) {

                logger.info("验证码发送成功：{}", phoneNumber);
                return true;
            }

            logger.error("发送失败：{}",
                    GSON.toJson(response.getBody()));

            return false;

        } catch (TeaException e) {

            logger.error("短信发送异常", e);
            return false;

        } catch (Exception e) {

            logger.error("系统异常", e);
            return false;
        }
    }


    /**
     * 创建 Client
     */
    private Client createClient() throws Exception {

        Config config = new Config()
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(accessKeySecret);
//                .setRegionId("ap-southeast-1");

        config.endpoint = "dypnsapi.aliyuncs.com";

        return new Client(config);
    }
}
