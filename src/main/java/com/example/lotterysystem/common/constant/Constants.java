package com.example.lotterysystem.common.constant;

public class Constants {
    /**
     * 阿里云短信服务签名
     */
    public static final String SIGNNAME = "速通互联验证码";
    /**
     * 登录随机验证码长度
     */
    public static final int CODE_LENGTH = 4;

    // 对于 redis 里面的key 需要标准化，为了区分业务，应该给key定义前缀。
    /**
     * 登录存放验证码中 key 的前缀
     */
    public static final String VERIFICATION_CODE_PREFIX = "VERIFICATION_CODE_";

    /**
     * 验证码过期时间 60 * 5s
     */
    public static final Long VERIFICATION_CODE_TIMEOUT = 60*5L;

    /**
     * 存放创建活动完成信息中 key 的前缀
     */
    public static final String ACTIVITY_PREFIS = "ACTIVITY_";

    /**
     * 存放创建活动完成信息的 过期时间
     */
    public static final Long ACTIVITY_TIMEOUT = 60 * 60 * 24 * 3L;
}
