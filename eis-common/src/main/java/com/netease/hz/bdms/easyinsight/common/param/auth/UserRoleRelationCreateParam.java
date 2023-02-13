package com.netease.hz.bdms.easyinsight.common.param.auth;

import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.enums.rbac.RoleTypeEnum;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 添加成员的入参
 *
 * @author huzhenhua
 * @modifier wangliangyuan
 * @date 2021-08-03 下午 02:30
 */
@Data
public class UserRoleRelationCreateParam {

    /**
     * {@link RoleTypeEnum}
     */
    @NotNull
    @Min(value = 0)
    @Max(value = 2)
    private Integer range;

    /**
     * 当前角色
     * (查看 角色下的成员列表 时,需要传这个参数)
     */
    private Long currentRole;

    @NotNull
    @Size(min = 1)
    private List<UserSimpleDTO> users;

    /**
     * 分配给用户的角色
     */
    private List<Long> roleIds;
}
