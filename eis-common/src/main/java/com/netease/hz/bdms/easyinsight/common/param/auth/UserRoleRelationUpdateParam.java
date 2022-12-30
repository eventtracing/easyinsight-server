package com.netease.hz.bdms.easyinsight.common.param.auth;

import com.netease.hz.bdms.easyinsight.common.enums.rbac.RoleTypeEnum;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
/**
 * 更新成员的入参
 *
 * @author huzhenhua
 * @modifier wangliangyuan
 * @date 2021-08-03 下午 05:41
 */
@Data
public class UserRoleRelationUpdateParam {

    /**
     * {@link RoleTypeEnum}
     */
    @NotNull
    @Min(value = 0)
    @Max(value = 2)
    private Integer range;

    @NotNull
    private Long userId;

    @NotBlank
    private String email;

    private String userName;

    @NotNull
    @Size(min = 1)
    private List<Long> roleIds;
}
