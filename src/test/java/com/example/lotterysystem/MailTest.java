package com.example.lotterysystem;

import cn.hutool.core.date.DateUtil;
import com.example.lotterysystem.common.utils.MailUtil;
import com.example.lotterysystem.service.enums.ActivityPrizeTiersEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MailTest {

    @Autowired
    private MailUtil mailUtil;

    @Test
    void sendMessage(){

        String context = "Hi，乔治鹏先生。您的1500米通关了吗？";

        mailUtil.sendSampleMail("1926522425@qq.com","中奖通知",context);
    }
}
