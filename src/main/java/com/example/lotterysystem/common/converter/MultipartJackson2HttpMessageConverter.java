package com.example.lotterysystem.common.converter;

import com.example.lotterysystem.common.pojo.CommonResult;
import com.example.lotterysystem.controller.param.CreatePrizeParam;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Type;

/**
 * 转换器
 */
@Component
public class MultipartJackson2HttpMessageConverter extends AbstractJackson2HttpMessageConverter {
    protected MultipartJackson2HttpMessageConverter(ObjectMapper objectMapper) {
        // MediaType.APPLICATION_OCTET_STREAM 表示这个转换器用于处理二进制流数据，通常用于文件上传。
        super(objectMapper, MediaType.APPLICATION_OCTET_STREAM);
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        // 转换器不用于写入（即不用于响应的序列化）
        return false;
    }

    @Override
    public boolean canWrite(Type type, Class<?> clazz, MediaType mediaType) {
        return false;
    }

    @Override
    protected boolean canWrite(MediaType mediaType) {
        return false;
    }
}

//    public CommonResult<Long> createPrize(@Validated @RequestPart("param") CreatePrizeParam param,
//                                          @RequestPart("prizePic") MultipartFile picFile){
//    在遇到类似文件类二进制流上传可以使用该转换器