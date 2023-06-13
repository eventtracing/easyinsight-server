package com.netease.hz.bdms.easyinsight.common.dto.rbac;

import lombok.Data;


@Data
public class RoleApplyDTO {
    /**
     * 角色ID
     */
    private Long id;
    /**
     * 角色名称
     */
    private String roleName;
    /**
     * 角色描述
     */
    private String description;
    /**
     * 申请人
     */
    private String applyUser;
    /**
     * 申请人邮箱
     */
    private String applyEmail;
    /**
     * 申请人理由
     */
    private String Reason;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 更新时间
     */
    private Long updateTime;
}
