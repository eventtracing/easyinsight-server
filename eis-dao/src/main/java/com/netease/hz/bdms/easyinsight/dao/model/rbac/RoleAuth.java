package com.netease.hz.bdms.easyinsight.dao.model.rbac;

import lombok.Data;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * 角色权限表 eis_role_auth 对应的实体类
 *
 * @author wangliangyuan
 * @date 2021-08-02 下午 02:27
 */
@Data
public class RoleAuth {
    /**
     * 自增id
     */
    private Long id;

    /**
     * 角色id
     */
    private Long roleId;

    /**
     * 权限id
     */
    private Long authId;

    /**
     * 标志位，用以细分权限，将来扩展用，比如需要细分某个资源的增删改查权限时，可以存4位二进制
     */
    private Integer flag;

    /**
     * 创建时间
     */
    private Timestamp createTime;

    /**
     * 更新时间
     */
    private Timestamp updateTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RoleAuth roleAuth = (RoleAuth) o;
        return roleId.equals(roleAuth.roleId) &&
                authId.equals(roleAuth.authId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleId, authId);
    }
}