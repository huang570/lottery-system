package com.example.lotterysystem;

import com.example.lotterysystem.common.pojo.CommonResult;
import com.example.lotterysystem.common.utils.JacksonUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class JacksonTest {

    /**
     * 序列化测试
     */
    @Test
    void JacksonUtilTest(){
        CommonResult<String> result = CommonResult.success("success");
        String str;
        str = JacksonUtil.writeValueAsString(result);
        System.out.println(str);

        result = JacksonUtil.readValue(str,CommonResult.class);
        System.out.println(result);

    }
}
