package com.example.lotterysystem.controller;

import com.example.lotterysystem.common.errorcode.ControllerErrorCodeConstants;
import com.example.lotterysystem.common.exception.ControllerException;
import com.example.lotterysystem.common.pojo.CommonResult;
import com.example.lotterysystem.common.utils.JacksonUtil;
import com.example.lotterysystem.controller.param.CreateActivityParam;
import com.example.lotterysystem.controller.param.PageParam;
import com.example.lotterysystem.controller.result.FIndActivityListResult;
import com.example.lotterysystem.controller.result.CreateActivityResult;
import com.example.lotterysystem.controller.result.GetActivityDetailResult;
import com.example.lotterysystem.service.ActivityService;
import com.example.lotterysystem.service.dto.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    /**
     * 创建活动
     *
     * @param param
     * @return
     */
    @PostMapping("/activity/create")
    public CommonResult<CreateActivityResult> createActivity(
            @Validated @RequestBody CreateActivityParam param){

        log.info("方法: createActivity 参数: param : {}", JacksonUtil.writeValueAsString(param));
        return CommonResult.success(
                converToCreateActivityResult(
                        activityService.createActivity(param)));
    }

    /**
     * 转换为 CreateActivityResult
     *
     * @param createActivityDTO
     * @return
     */
    private CreateActivityResult converToCreateActivityResult(CreateActivityDTO createActivityDTO) {
        if(null == createActivityDTO){
            throw new ControllerException(ControllerErrorCodeConstants.CREATE_ACTIVITY_ERROR);
        }
        CreateActivityResult result = new CreateActivityResult();
        result.setActivityId(createActivityDTO.getActivityId());
        return result;
    }

    @RequestMapping("/activity/find-list")
    public CommonResult<FIndActivityListResult> findActivityList(PageParam param){
        log.info("findActivityList param: {}",JacksonUtil.writeValueAsString(param));

        return CommonResult.success(
                convetToFIndActivityListResult(
                        activityService.findActivityList(param)));
    }

    private FIndActivityListResult convetToFIndActivityListResult(PageListDTO<ActivityDTO> activityList) {
        if(null == activityList){
            throw new ControllerException(ControllerErrorCodeConstants.FIND_ACTIVITY_LIST_ERROR);
        }

        FIndActivityListResult result = new FIndActivityListResult();
        result.setTotal(activityList.getTotal());
        result.setRecords(
            activityList.getRecords()
                .stream()
                .map(activityDTO -> {
                    FIndActivityListResult.ActivityInfo activityInfo = new FIndActivityListResult.ActivityInfo();
                    activityInfo.setActivityId(activityDTO.getActivityId());
                    activityInfo.setActivityName(activityDTO.getActivityName());
                    activityInfo.setDescription(activityDTO.getDescription());
                    activityInfo.setValid(activityDTO.valid());
                    return activityInfo;
                }).collect(Collectors.toList())
        );

        return result;
    }

    @GetMapping("/activity-detail/find")
    public CommonResult<GetActivityDetailResult> getActivityDetail(Long activityId){
        log.info("getActivityDetail activityId: {}",activityId);

        ActivityDetailDTO detailDTO = activityService.getActivityDetail(activityId);
        return CommonResult.success(converToGetActivityDetailResult(detailDTO));
    }

    private GetActivityDetailResult converToGetActivityDetailResult(ActivityDetailDTO detailDTO) {
        if(null == detailDTO){
            throw new ControllerException(ControllerErrorCodeConstants.GET_ACTIVITY_DETAIL_ERROR);
        }
        GetActivityDetailResult result = new GetActivityDetailResult();
        result.setActivityId(detailDTO.getActivityId());
        result.setActivityName(detailDTO.getActivityName());
        result.setDescription(detailDTO.getDesc());
        result.setValid(detailDTO.valid());
        // 抽奖顺序，先抽一等奖，然后二、三
        result.setPrizes(
                detailDTO.getPrizeDetailDTOList()
                        .stream()
                        .sorted(Comparator.comparingInt(prizeDTO -> prizeDTO.getTiers().getCode()))
                        .map(prizeDTO -> {
                            GetActivityDetailResult.Prize prize = new GetActivityDetailResult.Prize();
                            prize.setPrizeId(prizeDTO.getPrizeId());
                            prize.setName(prizeDTO.getName());
                            prize.setImageUrl(prizeDTO.getImageUrl());
                            prize.setPrice(prizeDTO.getPrice());
                            prize.setDescription(prizeDTO.getDescription());
                            prize.setPrizeTierName(prizeDTO.getTiers().getMessage());
                            prize.setPrizeAmount(prizeDTO.getPrizeAmount());
                            prize.setValid(prizeDTO.valid());
                            return prize;
                        }).collect(Collectors.toList())
        );
        result.setUsers(
                detailDTO.getUserDetaiDTOList()
                        .stream()
                        .map(userDTO -> {
                            GetActivityDetailResult.User user = new GetActivityDetailResult.User();
                            user.setUserId(userDTO.getUserId());
                            user.setUserName(userDTO.getUserName());
                            user.setValid(userDTO.valid());
                            return user;
                        }).collect(Collectors.toList())
        );

        return result;
    }
}
