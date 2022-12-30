package com.netease.hz.bdms.easyinsight.common.dto.rbac;

import lombok.Data;


@Data
public class RoleDTO {
    /**
     * 角色ID
     */
    private Long id;
    /**
     * 角色名称
     */
    private String roleName;
    /**
     * 角色等级
     */
    private Integer roleLevel;

    /**
     * 是否是内置角色
     * true-内置角色，false-自定义角色(非内置角色)
     */
    private Boolean builtin;
    /**
     * 角色描述
     */
    private String description;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 更新时间
     */
    private Long updateTime;
}
