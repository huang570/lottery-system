package com.example.lotterysystem;

import com.example.lotterysystem.common.utils.RedisUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
public class RedisTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisUtil redisUtil;
    @Test
    void RedisTest(){
        stringRedisTemplate.opsForValue().set("key","value");
        System.out.println("从redis中获取值" + stringRedisTemplate.opsForValue().get("key"));
    }

    @Test
    void RedisUtil(){
        redisUtil.set("key1","value1");
        redisUtil.set("key2","value2",60L);

        System.out.println("has key1 :" + redisUtil.hasKey("key1"));
        System.out.println("has key2 :" + redisUtil.hasKey("key2"));


        System.out.println("key1: " + redisUtil.get("key1"));
        System.out.println("key2: " + redisUtil.get("key2"));

        redisUtil.del("key1");

        System.out.println("has key1 :" + redisUtil.hasKey("key1"));
        System.out.println("has key2 :" + redisUtil.hasKey("key2"));
    }

}
