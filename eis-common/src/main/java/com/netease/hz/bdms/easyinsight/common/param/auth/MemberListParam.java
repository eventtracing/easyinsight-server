package com.netease.hz.bdms.easyinsight.common.param.auth;

import com.netease.hz.bdms.easyinsight.common.enums.rbac.RoleTypeEnum;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 查询成员列表的入参
 *
 * @author wangliangyuan
 * @date 2021-08-04 下午 04:46
 */
@Data
public class MemberListParam {

    /**
     * {@link RoleTypeEnum}
     */
    @NotNull
    @Min(value = 0)
    @Max(value = 2)
    private Integer range;
}
