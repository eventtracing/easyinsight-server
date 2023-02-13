package com.netease.hz.bdms.easyinsight.common.enums.rbac;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.EnumSet;

/**
 * 角色等级
 * {@link #level} 越低,等级越高
 *
 * @author wangliangyuan
 * @date 2021-08-04 上午 11:34
 */
@Getter
@AllArgsConstructor
public enum RoleLevelEnum {

    SUPER_ADMIN(0, "超级管理员", 0),

    DOMAIN_PRINCIPAL(1, "域负责人", 1),
    DOMAIN_ADMIN(2, "域管理员", 1),
    DOMAIN_NORMAL_USER(3, "域普通用户", 1),//TODO 这个角色的权限还没确定

    PRODUCT_ADMIN(4, "产品管理员", 2),
    PRODUCT_NORMAL_USER(5, "产品普通用户", 2);

    private final Integer level;
    private final String roleName;
    /**
     * {@link RoleTypeEnum}
     */
    private final Integer roleType;

    /**
     * 根据角色等级匹配
     *
     * @param level 角色等级
     * @return
     */
    public static RoleLevelEnum match(Integer level) {
        return Arrays.stream(values()).filter(roleLevelEnum -> roleLevelEnum.getLevel().equals(level)).findAny().orElse(null);
    }

    /**
     * 根据 level 判断是否是 超级管理员 或 域管理员
     *
     * @param level
     * @return
     */
    public static boolean isSuperAdminOrDomainAdmin(Integer level) {
        return SUPER_ADMIN.getLevel().equals(level) ||
                // 域负责人 的权限和 域管理员 一样,等同于 域管理员
                DOMAIN_PRINCIPAL.getLevel().equals(level) ||
                DOMAIN_ADMIN.getLevel().equals(level);
    }

    /**
     * 域下的默认角色
     *
     * @return
     */
    public static EnumSet<RoleLevelEnum> defaultRolesInDomain() {
        return EnumSet.of(DOMAIN_PRINCIPAL, DOMAIN_ADMIN, DOMAIN_NORMAL_USER);
    }

    /**
     * 产品下的默认角色
     *
     * @return
     */
    public static EnumSet<RoleLevelEnum> defaultRolesInProduct() {
        return EnumSet.of(PRODUCT_ADMIN, PRODUCT_NORMAL_USER);
    }
}
