package com.netease.hz.bdms.easyinsight.dao.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.sql.Timestamp;
import java.util.Date;

@Data
@Accessors(chain = true)
public class EisPermissionApplyRecord {
    /**
     * 角色ID
     */
    private Long id;
    /*
     * appId
     */
    private Long appId;
    /**
     * 角色名称
     */
    private String roleName;
    /**
     * 角色id
     */
    private Long roleId;
    /**
     * 申请人
     */
    private String applyUser;
    /**
     * 处理人
     */
    private String auditUser;
    /**
     * 申请人理由
     */
    private String description;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 创建时间
     */
    private Timestamp createTime;
    /**
     * 更新时间
     */
    private Timestamp updateTime;

}
