package com.example.lotterysystem.service.impl;

import com.example.lotterysystem.common.constant.Constants;
import com.example.lotterysystem.common.errorcode.ServiceErrorCodeConstants;
import com.example.lotterysystem.common.exception.ServiceException;
import com.example.lotterysystem.common.utils.JacksonUtil;
import com.example.lotterysystem.common.utils.RedisUtil;
import com.example.lotterysystem.controller.param.CreateActivityParam;
import com.example.lotterysystem.controller.param.CreatePrizeByActivityParam;
import com.example.lotterysystem.controller.param.CreateUserByActivityParam;
import com.example.lotterysystem.controller.param.PageParam;
import com.example.lotterysystem.dao.dataobject.ActivityDO;
import com.example.lotterysystem.dao.dataobject.ActivityPrizeDO;
import com.example.lotterysystem.dao.dataobject.ActivityUserDO;
import com.example.lotterysystem.dao.dataobject.PrizeDO;
import com.example.lotterysystem.dao.mapper.*;
import com.example.lotterysystem.service.ActivityService;
import com.example.lotterysystem.service.dto.*;
import com.example.lotterysystem.service.enums.ActivityPrizeStatusEnum;
import com.example.lotterysystem.service.enums.ActivityPrizeTiersEnum;
import com.example.lotterysystem.service.enums.ActivityStatusEnum;
import com.example.lotterysystem.service.enums.ActivityUserStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ActivityServiceImpl implements ActivityService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PrizeMapper prizeMapper;

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private ActivityPrizeMapper activityPrizeMapper;

    @Autowired
    private ActivityUserMapper activityUserMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    @Transactional(rollbackFor = Exception.class) // 涉及了多表，需要保证事务
    public CreateActivityDTO createActivity(CreateActivityParam param) {

        // 校验活动信息是否正确
        checkActivityInfo(param);

        // 保存活动信息到库里
        ActivityDO activityDO = new ActivityDO();
        activityDO.setActivityName(param.getActivityName());
        activityDO.setDescription(param.getDescription());
        activityDO.setStatus(ActivityStatusEnum.RUNNING.name());
        activityMapper.insert(activityDO);

        // 保存活动关联的奖品信息
        List<CreatePrizeByActivityParam> prizeParams = param.getActivityPrizeList();
        List<ActivityPrizeDO> activityPrizeDOList = prizeParams
                .stream()
                .map(prizeParam -> {
                    ActivityPrizeDO activityPrizeDO = new ActivityPrizeDO();
                    activityPrizeDO.setActivityId(activityDO.getId());
                    activityPrizeDO.setPrizeId(prizeParam.getPrizeId());
                    activityPrizeDO.setPrizeAmount(prizeParam.getPrizeAmount());
                    activityPrizeDO.setPrizeTiers(prizeParam.getPrizeTiers());
                    activityPrizeDO.setStatus(ActivityPrizeStatusEnum.INIT.name());
                    return activityPrizeDO;
                }).collect(Collectors.toList());
        activityPrizeMapper.batchInsert(activityPrizeDOList);

        // 保存活动关联的人员信息
        List<CreateUserByActivityParam> userParams = param.getActivityUserList();
        List<ActivityUserDO> activityUserDOList = userParams
                .stream()
                .map(userParam -> {
                    ActivityUserDO activityUserDO = new ActivityUserDO();
                    activityUserDO.setActivityId(activityDO.getId());
                    activityUserDO.setUserId(userParam.getUserId());
                    activityUserDO.setUserName(userParam.getUserName());
                    activityUserDO.setStatus(ActivityUserStatusEnum.INIT.name());
                    return activityUserDO;
                }).collect(Collectors.toList());
        activityUserMapper.batchInsert(activityUserDOList);

        // 整合完整的活动信息
        // 将完成的活动信息存放到 redis 缓存中
        // 规定 key：activityId: ActivityDetailDTO: 活动+奖品+人员

        // 需要先获取奖品基本属性列表
        List<Long> prizeIds = param.getActivityPrizeList()
                .stream()
                .map(CreatePrizeByActivityParam::getPrizeId)
                .distinct()
                .collect(Collectors.toList());
        List<PrizeDO> prizeDOList = prizeMapper.batchSelectByIds(prizeIds);

        ActivityDetailDTO detailDTO = converToActivityDetailDTO(activityDO,activityUserDOList
                ,activityPrizeDOList,prizeDOList);

        // 存放进缓存
        cacheActivity(detailDTO);

        // 构造返回
        CreateActivityDTO createActivityDTO = new CreateActivityDTO();
        createActivityDTO.setActivityId(activityDO.getId());
        return createActivityDTO;
    }

    /**
     * 缓存完整的活动信息: ActivityDetailDTO
     *
     * @param detailDTO
     */
    private void cacheActivity(ActivityDetailDTO detailDTO) {
        // key: ACTIVITY_activityId
        // value: ActivityDetailDTO（JSON）

        if(null == detailDTO || null == detailDTO.getActivityId()){
            log.warn("要缓存的信息不存在");
            return;
        }

        try {
            redisUtil.set(Constants.ACTIVITY_PREFIS + detailDTO.getActivityId()
                    , JacksonUtil.writeValueAsString(detailDTO)
                    , Constants.ACTIVITY_TIMEOUT);
        }catch (Exception e){
            log.error("缓存活动异常: ActivityDetailDTO={}"
                    ,JacksonUtil.writeValueAsString(detailDTO)
                    ,e);
        }
    }

    /**
     * 根据活动ID 从缓存中获取活动详细信息
     * @param activityId
     * @return
     */
    private ActivityDetailDTO getActivityFromCache(Long activityId){
        if(null == activityId){
            log.warn("获取缓存活动数据activityId为空");
            return null;
        }
        try {
            String str = redisUtil.get(Constants.ACTIVITY_PREFIS + activityId);
            if(!StringUtils.hasLength(str)){
                log.warn("获取缓存活动数据为空! key: {}",Constants.ACTIVITY_PREFIS);
                return null;
            }
            return JacksonUtil.readValue(str,ActivityDetailDTO.class);
        }catch (Exception e){
            log.error("从缓存中获取活动信息异常,key: {}",Constants.ACTIVITY_PREFIS);
            return null;
        }
    }

    /**
     * 根据基本DO 整合完整的活动信息 ActivityDetailDTO
     * @param activityDO
     * @param activityUserDOList
     * @param activityPrizeDOList
     * @param prizeDOList
     * @return
     */
    private ActivityDetailDTO converToActivityDetailDTO(ActivityDO activityDO, List<ActivityUserDO> activityUserDOList
            , List<ActivityPrizeDO> activityPrizeDOList, List<PrizeDO> prizeDOList) {
        ActivityDetailDTO detailDTO = new ActivityDetailDTO();
        detailDTO.setActivityId(activityDO.getId());
        detailDTO.setActivityName(activityDO.getActivityName());
        detailDTO.setDesc(activityDO.getDescription());
        detailDTO.setStatus(ActivityStatusEnum.forName(activityDO.getStatus()));

        List<PrizeDetailDTO> prizeList = activityPrizeDOList
                .stream()
                .map(activityPrizeDO -> {
                    PrizeDetailDTO prizeDetailDTO = new PrizeDetailDTO();
                    prizeDetailDTO.setPrizeId(activityPrizeDO.getPrizeId());
                    Optional<PrizeDO> optionalPrizeDO = prizeDOList.stream()
                            .filter(prizeDO -> prizeDO.getId().equals(activityPrizeDO.getPrizeId()))
                            .findFirst();

                    // 如果prizeDO为空，则不会传入ifPresent；为空不执行
                    optionalPrizeDO.ifPresent(prizeDO -> {
                        prizeDetailDTO.setName(prizeDO.getName());
                        prizeDetailDTO.setImageUrl(prizeDO.getImageUrl());
                        prizeDetailDTO.setPrice(prizeDO.getPrice());
                        prizeDetailDTO.setDescription(prizeDO.getDescription());
                    });

                    prizeDetailDTO.setTiers(ActivityPrizeTiersEnum.forName(activityPrizeDO.getPrizeTiers()));
                    prizeDetailDTO.setPrizeAmount(activityPrizeDO.getPrizeAmount());
                    prizeDetailDTO.setStatus(ActivityPrizeStatusEnum.forName(activityPrizeDO.getStatus()));

                    return prizeDetailDTO;
                }).collect(Collectors.toList());

        detailDTO.setPrizeDetailDTOList(prizeList);

        List<UserDetaiDTO> userDetaiDTOList = activityUserDOList
                .stream()
                .map(activityUserDO -> {
                    UserDetaiDTO userDetaiDTO = new UserDetaiDTO();
                    userDetaiDTO.setUserId(activityUserDO.getUserId());
                    userDetaiDTO.setUserName(activityUserDO.getUserName());
                    userDetaiDTO.setStatus(ActivityUserStatusEnum.forName(activityUserDO.getStatus()));

                    return userDetaiDTO;
                }).collect(Collectors.toList());

        detailDTO.setUserDetaiDTOList(userDetaiDTOList);
        return detailDTO;
    }

    /**
     * 校验活动信息
     *
     * @param param
     */
    private void checkActivityInfo(CreateActivityParam param) {
        if(null == param){
            throw new ServiceException(ServiceErrorCodeConstants.CREATE_ACTIVITY_INFO_IS_EMPTY);
        }

        // 人员ID 在人员表中存在
        List<Long> userIds = param.getActivityUserList()
                .stream()
                .map(CreateUserByActivityParam::getUserId)
                .distinct() // 去重
                .collect(Collectors.toList());

        List<Long> existUserIds = userMapper.selectExistByIds(userIds);

        if(CollectionUtils.isEmpty(existUserIds)){
            throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_USER_ERROR);
        }

        userIds.forEach(id -> {
            if(!existUserIds.contains(id)){
                throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_USER_ERROR);
            }
        });

        // 奖品ID 在奖品表中存在
        List<Long> prizeIds = param.getActivityPrizeList()
                .stream()
                .map(CreatePrizeByActivityParam::getPrizeId)
                .distinct()
                .collect(Collectors.toList());

        List<Long> existPrizeIds = prizeMapper.selectExistByIds(prizeIds);

        if(CollectionUtils.isEmpty(existPrizeIds)){
            throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_PRIZE_ERROR);
        }

        prizeIds.forEach(id -> {
            if(!existPrizeIds.contains(id)){
                throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_PRIZE_ERROR);
            }
        });

        // 人员数量要大于等于奖品数量
        int userAmount = param.getActivityUserList().size();
        long prizeAmount = param.getActivityPrizeList()
                .stream()
                .mapToLong(CreatePrizeByActivityParam::getPrizeAmount)
                .sum();

        if(userAmount < prizeAmount){
            throw new ServiceException(ServiceErrorCodeConstants.USER_PRIZE_AMOUNT_ERROR);
        }

        // 校验活动奖品等级有效性
        param.getActivityPrizeList().forEach(prize -> {
            if(null == ActivityPrizeTiersEnum.forName(prize.getPrizeTiers())){
                throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_PRIZE_TIERS_ERROR);
            }
        });

    }

    @Override
    public PageListDTO<ActivityDTO> findActivityList(PageParam param) {
        // 获取总量
        int total = activityMapper.count();

        // 获取当前页列表
        List<ActivityDO> activityDOList = activityMapper.selectActivityList(param.offset(),param.getPageSize());

        List<ActivityDTO> activityDTOList = activityDOList
                .stream()
                .map(activityDO -> {
                    ActivityDTO activityDTO = new ActivityDTO();
                    activityDTO.setActivityId(activityDO.getId());
                    activityDTO.setActivityName(activityDO.getActivityName());
                    activityDTO.setDescription(activityDO.getDescription());
                    activityDTO.setStatus(ActivityStatusEnum.forName(activityDO.getStatus()));
                    return activityDTO;
                }).collect(Collectors.toList());

        return new PageListDTO<>(total,activityDTOList);
    }

    @Override
    public ActivityDetailDTO getActivityDetail(Long activityId) {
        if(null == activityId){
            log.info("查询活动详细信息失败, activityId为空！");
            return null;
        }
        // 查redis  活动详细信息
        ActivityDetailDTO detailDTO = getActivityFromCache(activityId);
        if(null != detailDTO){
            log.info("获取到活动详细信息成功！detailDTO: {}",JacksonUtil.writeValueAsString(detailDTO));
            return detailDTO;
        }
        // 如果redis不存在，查表

        // 查活动表
        ActivityDO aDO = activityMapper.selectById(activityId);
        // 活动奖品表
        List<ActivityPrizeDO>  apDOList = activityPrizeMapper.selectByActivityId(activityId);
        // 活动人员表
        List<ActivityUserDO> auDOList = activityUserMapper.seleceByActivityId(activityId);
        // 查奖品表：先获取要查询的奖品id
        List<Long> prizeIds = apDOList
                .stream()
                .map(ActivityPrizeDO::getPrizeId)
                .collect(Collectors.toList());

        List<PrizeDO> pDOList = prizeMapper.batchSelectByIds(prizeIds);
        // 整合表里数据，存放在redis 中
        detailDTO = converToActivityDetailDTO(aDO,auDOList,apDOList,pDOList);

        // 存放在redis 中
        cacheActivity(detailDTO);

        return getActivityFromCache(activityId);
    }

    @Override
    public void cacheActivity(Long activityId) {

        if(null == activityId){
            log.warn("要缓存的活动ID为空");
            throw new ServiceException(ServiceErrorCodeConstants.CACHE_ACTIVITY_ID_IS_EMPTY);
        }

        // 查询表数据：活动表、关联奖品、关联人员、奖品信息表
        // 查活动表
        ActivityDO aDO = activityMapper.selectById(activityId);

        if(null == aDO){
            log.error("要缓存的活动ID有望有误");
            throw new ServiceException(ServiceErrorCodeConstants.CACHE_ACTIVITY_ID_ERROR);
        }
        // 活动奖品表
        List<ActivityPrizeDO>  apDOList = activityPrizeMapper.selectByActivityId(activityId);
        // 活动人员表
        List<ActivityUserDO> auDOList = activityUserMapper.seleceByActivityId(activityId);
        // 查奖品表：先获取要查询的奖品id
        List<Long> prizeIds = apDOList
                .stream()
                .map(ActivityPrizeDO::getPrizeId)
                .collect(Collectors.toList());

        List<PrizeDO> pDOList = prizeMapper.batchSelectByIds(prizeIds);
        // 整合表里数据，存放在redis 中
        cacheActivity(converToActivityDetailDTO(aDO,auDOList
                ,apDOList,pDOList));
    }
}
