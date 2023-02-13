package com.netease.hz.bdms.easyinsight.common.param.auth;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 创建角色的入参
 *
 * @author huzhenhua
 * @modifier wangliangyuan
 * @date 2021-08-04 上午 09:41
 */
@Data
public class RoleCreateParam {

    /**
     * 产品ID
     */
    @NotNull
    private Long appId;

    /**
     * 角色名称
     */
    @NotBlank
    private String roleName;

    /**
     * 角色描述
     */
    private String description;
}
