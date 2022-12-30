package com.netease.hz.bdms.easyinsight.common.param.auth;

import com.netease.hz.bdms.easyinsight.common.dto.common.PagingSortDTO;
import com.netease.hz.bdms.easyinsight.common.enums.rbac.RoleTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 产品下成员列表的分页查询参数
 *
 * @author wangliangyuan
 * @date 2021-08-02 下午 04:52
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MemberListPageableParam extends PagingSortDTO {

    /**
     * {@link RoleTypeEnum}
     */
    @NotNull
    @Min(value = 0)
    @Max(value = 2)
    private Integer range;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 搜索关键字
     */
    private String search;

    @Override
    public String getOrderBy() {
        String orderBy = super.getOrderBy();
        if (StringUtils.isNotBlank(orderBy)) {
            // u. 是 sql 里表名的前缀
            orderBy = "u." + orderBy;
        }
        return orderBy;
    }
}
