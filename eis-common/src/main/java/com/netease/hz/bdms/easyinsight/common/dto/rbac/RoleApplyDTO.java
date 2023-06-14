package com.netease.hz.bdms.easyinsight.common.dto.rbac;

import lombok.Data;


@Data
public class RoleApplyDTO {
    /**
     * id
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
     * 申请人名字
     */
    private String applyUserName;
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
    private Long createTime;
    /**
     * 更新时间
     */
    private Long updateTime;
}
