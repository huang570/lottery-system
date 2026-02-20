package com.example.lotterysystem;

import com.example.lotterysystem.controller.param.DrawPrizeParam;
import com.example.lotterysystem.controller.param.ShowWinningRecordsParam;
import com.example.lotterysystem.dao.dataobject.PrizeDO;
import com.example.lotterysystem.dao.dataobject.WinningRecordDO;
import com.example.lotterysystem.service.DrawPrizeService;
import com.example.lotterysystem.service.activitystatus.ActivityStatusManager;
import com.example.lotterysystem.service.dto.ConvertActivityStatusDTO;
import com.example.lotterysystem.service.dto.WinningRecordDTO;
import com.example.lotterysystem.service.enums.ActivityPrizeStatusEnum;
import com.example.lotterysystem.service.enums.ActivityStatusEnum;
import com.example.lotterysystem.service.enums.ActivityUserStatusEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
public class DrawPrizeTest {

    @Autowired
    private DrawPrizeService drawPrizeService;

    @Autowired
    private ActivityStatusManager activityStatusManager;

    @Test
    void dtawPrizeTest(){

//        DrawPrizeParam param = new DrawPrizeParam();
//        param.setActivityId(1L);
//        param.setPrizeId(1L);
//        param.setPrizeTiers("FIRST_PRIZE");
//        param.setWinningTime(new Date());
//
//        List<DrawPrizeParam.Winner> list = new ArrayList<>();
//        DrawPrizeParam.Winner winner = new DrawPrizeParam.Winner();
//        winner.setUserId(1L);
//        winner.setUserName("XXX");
//        list.add(winner);
//        param.setWinnerList(list);
//
//        drawPrizeService.drawPrize(param);


        // 1. 正向流程


        // 2. 处理过程出现异常：回滚

        // 3. 处理过程中出现异常，消息堆积，重新发送
        DrawPrizeParam param = new DrawPrizeParam();
        param.setActivityId(25L);
        param.setPrizeId(19L);
        param.setPrizeTiers("SECOND_PRIZE");
        param.setWinningTime(new Date());

        List<DrawPrizeParam.Winner> list = new ArrayList<>();
        DrawPrizeParam.Winner winner = new DrawPrizeParam.Winner();
        winner.setUserId(49L);
        winner.setUserName("杨康");
        list.add(winner);
        param.setWinnerList(list);

        drawPrizeService.drawPrize(param);
    }

    @Test
    void statusConvert(){

        ConvertActivityStatusDTO convertActivityStatusDTO = new ConvertActivityStatusDTO();
        convertActivityStatusDTO.setActivityId(24L);
        convertActivityStatusDTO.setTargetActivityStatus(ActivityStatusEnum.COMPLETED);
        convertActivityStatusDTO.setPrizeId(19L);
        convertActivityStatusDTO.setTargetPrizeStatus(ActivityPrizeStatusEnum.COMPLETED);

        List<Long> userIds = Arrays.asList(46L);

        convertActivityStatusDTO.setUserIds(userIds);
        convertActivityStatusDTO.setTargetUserStatus(ActivityUserStatusEnum.COMPLETED);

        activityStatusManager.handlerEvent(convertActivityStatusDTO);
    }

    @Test
    void saveWinningRecords(){

        DrawPrizeParam param = new DrawPrizeParam();
        param.setActivityId(24L);
        param.setPrizeId(19L);
        param.setPrizeTiers("SECOND_PRIZE");
        param.setWinningTime(new Date());

        List<DrawPrizeParam.Winner> list = new ArrayList<>();
        DrawPrizeParam.Winner winner = new DrawPrizeParam.Winner();
        winner.setUserId(46L);
        winner.setUserName("郭靖");
        list.add(winner);
        param.setWinnerList(list);

        drawPrizeService.saveWinnerRecords(param);
    }

    @Test
    void showWinningRecords(){

        ShowWinningRecordsParam param = new ShowWinningRecordsParam();
        param.setActivityId(25L);
        param.setPrizeId(18L);

        List<WinningRecordDTO> winningRecordDTOList = drawPrizeService.getRecords(param);
        for (WinningRecordDTO winningRecordDTO : winningRecordDTOList){
            // 中奖者，奖品，等级
            System.out.println(winningRecordDTO.toString());
        }
    }
}
