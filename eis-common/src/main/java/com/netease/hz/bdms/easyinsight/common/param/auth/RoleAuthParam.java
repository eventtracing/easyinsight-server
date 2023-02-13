package com.netease.hz.bdms.easyinsight.common.param.auth;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 给角色分配权限的入参
 *
 * @author huzhenhua
 * @modifier wangliangyuan
 * @date 2021-08-04 下午 16:37
 */
@Data
public class RoleAuthParam {
    /**
     * 角色ID
     */
    @NotNull
    private Long roleId;

    /**
     * 权限集合
     */
    @NotNull
    private List<MenuFunctionParam> functions;
}
