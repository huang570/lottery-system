package com.example.lotterysystem.controller;

import com.example.lotterysystem.common.pojo.CommonResult;
import com.example.lotterysystem.common.utils.JacksonUtil;
import com.example.lotterysystem.controller.param.DrawPrizeParam;
import com.example.lotterysystem.controller.param.ShowWinningRecordsParam;
import com.example.lotterysystem.controller.result.WinningRecordsResult;
import com.example.lotterysystem.service.DrawPrizeService;
import com.example.lotterysystem.service.dto.WinningRecordDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class DrawPrizeController {

    @Autowired
    private DrawPrizeService drawPrizeService;

    @PostMapping("/draw-prize")
    public CommonResult<Boolean> drawPrize(
            @Validated @RequestBody DrawPrizeParam param){

        log.info("drawPrize DrawPrizeParam: {}",param);

        // service
        drawPrizeService.drawPrize(param);
        return CommonResult.success(true);
    }

    @RequestMapping("/winning-records/show")
    public CommonResult<List<WinningRecordsResult>> showWinningRecords(
            @Validated @RequestBody ShowWinningRecordsParam param){
        log.info("showWinningRecords ShowWinningRecordsParam: {}", JacksonUtil.writeValueAsString(param));

        List<WinningRecordDTO> winningRecordDTOList = drawPrizeService.getRecords(param);
        return CommonResult.success(convertToWinningRecordDTO(winningRecordDTOList));
    }

    private List<WinningRecordsResult> convertToWinningRecordDTO(
            List<WinningRecordDTO> winningRecordDTOList) {

        if(CollectionUtils.isEmpty(winningRecordDTOList)){
            return Arrays.asList();
        }

        return winningRecordDTOList
                .stream()
                .map(winningRecordDTO -> {
                    WinningRecordsResult result = new WinningRecordsResult();
                    result.setWinnerId(winningRecordDTO.getWinnerId());
                    result.setWinnerName(winningRecordDTO.getWinnerName());
                    result.setPrizeName(winningRecordDTO.getPrizeName());
                    result.setPrizeTier(winningRecordDTO.getPrizeTier().getMessage());
                    result.setWinningTime(winningRecordDTO.getWinningTime());
                    return result;
                }).collect(Collectors.toList());
    }
}
