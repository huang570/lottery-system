package com.example.lotterysystem;

import cn.hutool.crypto.digest.DigestUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

import java.util.UUID;

@SpringBootTest
public class EncryptTest {

    @Test
    void Test(){
        // 获取盐值
        String salt = UUID.randomUUID().toString().replace("-","");

        String encrypt = DigestUtil.sha256Hex("123456");
        System.out.println(encrypt);
        String encrypt1 = encrypt+salt;
        System.out.println(encrypt1);

    }
}
