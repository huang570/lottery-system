package com.example.lotterysystem.controller;

import com.example.lotterysystem.common.errorcode.ControllerErrorCodeConstants;
import com.example.lotterysystem.common.exception.ControllerException;
import com.example.lotterysystem.common.pojo.CommonResult;
import com.example.lotterysystem.common.utils.JacksonUtil;
import com.example.lotterysystem.controller.param.CreatePrizeParam;
import com.example.lotterysystem.controller.param.PageParam;
import com.example.lotterysystem.controller.result.FindPrizeListResult;
import com.example.lotterysystem.service.PictureService;
import com.example.lotterysystem.service.PrizeService;
import com.example.lotterysystem.service.dto.PageListDTO;
import com.example.lotterysystem.service.dto.PrizeDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class PrizeController {

    @Autowired
    private PictureService pictureService;

    @Autowired
    private PrizeService prizeService;

    @RequestMapping("/pic/upload")
    public String uploadPic(MultipartFile file){
        return pictureService.savePicture(file);
    }

    /**
     * 创建奖品
     * @RequestPart 用于接收表单数据 表单数据编码格式 multipart/from-data
     *              对于multipart/from-data 都可以使用RequestPart接收
     *
     * @param param 表单数据
     * @param picFile
     * @return
     */
    @PostMapping("/prize/create")
    public CommonResult<Long> createPrize(@Validated @RequestPart("param") CreatePrizeParam param,
                                          @RequestPart("prizePic") MultipartFile picFile){
        log.info("类：createPrize 参数：CreatePrizeParam picFile 创建奖品:{}", JacksonUtil.writeValueAsString(param));
        return CommonResult.success(prizeService.createPrize(param,picFile));
    }

    /**
     * 查询奖品列表
     *
     * @param param
     * @return
     */
    @GetMapping("/prize/find-list")
    private CommonResult<FindPrizeListResult> findPrizeList(PageParam param){
        log.info("类：findPrizeList 参数：param: {}",JacksonUtil.writeValueAsString(param));
        PageListDTO<PrizeDTO> pageListDTO = prizeService.findPrizeList(param);

        return CommonResult.success(converToFindPrizeListResult(pageListDTO));
    }

    private FindPrizeListResult converToFindPrizeListResult(PageListDTO<PrizeDTO> pageListDTO) {
        if(null == pageListDTO){
            throw new ControllerException(ControllerErrorCodeConstants.FIND_PRIZE_LIST_ERROR);
        }

        FindPrizeListResult result = new FindPrizeListResult();
        result.setTotal(pageListDTO.getTotal());
        result.setRecords(
                pageListDTO.getRecords().stream()
                    .map(prizeDTO -> {
                        FindPrizeListResult.PrizeInfo prizeInfo = new FindPrizeListResult.PrizeInfo();
                        prizeInfo.setPrizeId(prizeDTO.getPrizeId());
                        prizeInfo.setPrizeName(prizeDTO.getName());
                        prizeInfo.setDescription(prizeDTO.getDescription());
                        prizeInfo.setPrice(prizeDTO.getPrice());
                        prizeInfo.setImageUrl(prizeDTO.getImageUrl());
                        return prizeInfo;
                }).collect(Collectors.toList())
        );
        return result;
    }

}
