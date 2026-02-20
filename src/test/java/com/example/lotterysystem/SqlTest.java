package com.example.lotterysystem;

import com.example.lotterysystem.dao.dataobject.Encrypt;
import com.example.lotterysystem.dao.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SqlTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void mailCount(){
        int count = userMapper.countByMail("123@qq.com");
        System.out.println("mainCount = " + count);
    }

    @Test
    void phoneCount(){
        int count = userMapper.countByPhone(new Encrypt("13111110020"));
        System.out.println("phoneCount = " + count);
    }
}
