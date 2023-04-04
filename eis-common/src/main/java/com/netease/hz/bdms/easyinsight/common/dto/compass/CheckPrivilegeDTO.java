package com.netease.hz.bdms.easyinsight.common.dto.compass;

import lombok.Data;

import java.util.List;

/**
 * @author wangyongliang
 * @version 1.0.0
 * @ClassName BatchCheckPrivilegeDTO.java
 * @Description 鉴权
 * @createTime 2023/3/28 11:23
 */
@Data
public class CheckPrivilegeDTO {
    /**
     * spm
     */
    private String spmNo;
    /**
     * 是否全选
     */
    private Boolean isAll;
    /**
     * 资源类型
     */
    private String resourceType;
    /**
     * 资源列表
     */
    private String[] resourceList;

    /**
     * 用户列表
     */
    private String[] users;
    /**
     * 操作列表
     */
    private String[] actions;
    /**
     * 业务线code
     */
    private String bizCode;

    private List<PermissionItem> permissionList;

}
