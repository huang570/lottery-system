package com.example.lotterysystem.dao.dataobject;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ActivityUserDO extends BaseDO{

    /**
     * 关联的活动ID
     */
    private Long activityId;

    /**
     * 关联的人员ID
     */
    private Long userId;

    /**
     * 人员姓名
     */
    private String userName;

    /**
     * 人员状态
     */
    private String status;
}
