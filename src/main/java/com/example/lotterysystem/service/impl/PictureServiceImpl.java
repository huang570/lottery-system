package com.example.lotterysystem.service.impl;

import com.example.lotterysystem.common.errorcode.ServiceErrorCodeConstants;
import com.example.lotterysystem.common.exception.ServiceException;
import com.example.lotterysystem.service.PictureService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 保存图片
 */
@Service
public class PictureServiceImpl implements PictureService {

    @Value("${pic.local-path}")
    private String localPath;

    @Override
    public String savePicture(MultipartFile multipartFile) {

        // 创建目录
        File dir = new File(localPath);
        if(!dir.exists()){
            dir.mkdirs();
        }
        // 创建索引

        // 1. 获取文件全名称
        String filename = multipartFile.getOriginalFilename();
        assert filename != null;

        // 2. 拿到文件后缀
        String suffix = filename.substring(
                filename.lastIndexOf("."));

        // 3. 生成索引
        filename = UUID.randomUUID() + suffix;

        // 图片保存
        try {
            multipartFile.transferTo(new File(localPath + "/" + filename));
        } catch (IOException e) {
            throw new ServiceException(ServiceErrorCodeConstants.PIC_UPLOAD_ERROR);
        }
        return filename;
    }
}
