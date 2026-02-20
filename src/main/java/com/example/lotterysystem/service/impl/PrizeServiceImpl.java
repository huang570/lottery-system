package com.example.lotterysystem.service.impl;

import com.example.lotterysystem.controller.param.CreatePrizeParam;
import com.example.lotterysystem.controller.param.PageParam;
import com.example.lotterysystem.dao.dataobject.PrizeDO;
import com.example.lotterysystem.dao.mapper.PrizeMapper;
import com.example.lotterysystem.service.PictureService;
import com.example.lotterysystem.service.PrizeService;
import com.example.lotterysystem.service.dto.PageListDTO;
import com.example.lotterysystem.service.dto.PrizeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class PrizeServiceImpl implements PrizeService {

    @Autowired
    private PrizeMapper prizeMapper;

    @Autowired
    private PictureService pictureService;

    /**
     * 创建奖品
     *
     * @param param 奖品属性
     * @param picFile 奖品图片
     * @return 返回奖品ID
     */
    @Override
    public Long createPrize(CreatePrizeParam param, MultipartFile picFile) {
        // 上传图片
        String fileName = pictureService.savePicture(picFile);
        // 存储库中
        PrizeDO prizeDO = new PrizeDO();
        prizeDO.setName(param.getPrizeName());
        prizeDO.setDescription(param.getDescription());
        prizeDO.setImageUrl(fileName);
        prizeDO.setPrice(param.getPrice());

        prizeMapper.insert(prizeDO);

        return prizeDO.getId();
    }

    /**
     * 返回奖品当前页列表和奖品总量
     *
     * @param param
     * @return
     */
    @Override
    public PageListDTO<PrizeDTO> findPrizeList(PageParam param) {

        // 设置总量
        int total = prizeMapper.selectPrizeCount();

        // 查询列表
        List<PrizeDTO> prizeDTOList = new ArrayList<>();

        List<PrizeDO> prizeDOList = prizeMapper.selectPrizeList(param.offset(),param.getPageSize());

        for (PrizeDO prizeDO : prizeDOList){
            PrizeDTO prizeDTO = new PrizeDTO();
            prizeDTO.setPrizeId(prizeDO.getId());
            prizeDTO.setDescription(prizeDO.getDescription());
            prizeDTO.setName(prizeDO.getName());
            prizeDTO.setPrice(prizeDO.getPrice());
            prizeDTO.setImageUrl(prizeDO.getImageUrl());
            prizeDTOList.add(prizeDTO);
        }
        return new PageListDTO<>(total,prizeDTOList);
    }
}
