package com.example.lotterysystem;

import com.example.lotterysystem.service.impl.VerificationCodeServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class VerificationCodeServiceTest {

    @Autowired
    private VerificationCodeServiceImpl verificationCodeService;

    @Test
    void test(){
        //verificationCodeService.sendVerificationCode("19746916114");
        System.out.println(verificationCodeService.getVerificationCode("19746916114"));
    }
}
