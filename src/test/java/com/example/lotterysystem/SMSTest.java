package com.example.lotterysystem;

import com.example.lotterysystem.common.utils.SMSUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SMSTest {

    @Autowired
    private SMSUtil smsUtil;

    @Test
    void SMSTest(){
        Boolean bool = smsUtil.sendVerifyCode("19211406216","5418");
        System.out.println(bool);

    }
}
