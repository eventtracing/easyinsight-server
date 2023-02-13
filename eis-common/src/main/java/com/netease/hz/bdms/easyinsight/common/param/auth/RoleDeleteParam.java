package com.netease.hz.bdms.easyinsight.common.param.auth;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 删除角色的入参
 *
 * @author wangliangyuan
 * @date 2021-08-07 下午 06:55
 */
@Data
public class RoleDeleteParam {


    /**
     * 角色ID
     */
    @NotNull
    private Long id;

}
