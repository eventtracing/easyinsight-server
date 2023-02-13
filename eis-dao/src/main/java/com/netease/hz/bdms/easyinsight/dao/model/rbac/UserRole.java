package com.netease.hz.bdms.easyinsight.dao.model.rbac;

import com.netease.hz.bdms.easyinsight.common.enums.rbac.RoleLevelEnum;
import lombok.Data;

import javax.persistence.Transient;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * 用户角色表 eis_user_role 对应的实体类
 *
 * @author wangliangyuan
 * @date 2021-08-02 下午 02:27
 */
@Data
public class UserRole {
    /**
     * 自增id
     */
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 角色id
     */
    private Long roleId;

    /**
     * 角色类型
     * 0-platform，1-domain，2-app
     */
    private Integer roleType;

    /**
     * 角色类型ID
     * 如果type为app，则为appId，为domain则为domainId，为platform则默认为-1
     */
    private Long typeId;

    /**
     * 创建时间
     */
    private Timestamp createTime;

    /**
     * 更新时间
     */
    private Timestamp updateTime;

    /**
     * 角色名称
     * {@link Role#roleName}
     * 不是 eis_user_role 表中的列
     */
    @Transient
    private String roleName;

    /**
     * 角色等级
     * {@link Role#roleLevel}
     * 不是 eis_user_role 表中的列
     * <p>
     * {@link RoleLevelEnum}
     */
    @Transient
    private Integer roleLevel;

    /**
     * 用户的邮箱
     * {@link User#email}
     * 不是 eis_user_role 表中的列
     */
    @Transient
    private String email;

    /**
     * 用户名
     * {@link User#userName}
     * 不是 eis_user_role 表中的列
     */
    @Transient
    private String userName;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserRole userRole = (UserRole) o;
        return userId.equals(userRole.userId) &&
                roleId.equals(userRole.roleId) &&
                roleType.equals(userRole.roleType) &&
                typeId.equals(userRole.typeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, roleId, roleType, typeId);
    }

    public static UserRole of(Long userId, Long roleId, Integer roleType, Long typeId) {
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        userRole.setRoleType(roleType);
        userRole.setTypeId(typeId);
        return userRole;
    }
}