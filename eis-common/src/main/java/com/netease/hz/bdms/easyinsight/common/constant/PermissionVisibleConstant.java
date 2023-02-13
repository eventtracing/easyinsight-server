package com.netease.hz.bdms.easyinsight.common.constant;

import com.netease.hz.bdms.easyinsight.common.enums.rbac.RoleLevelEnum;

import java.util.EnumSet;

/**
 * 权限可见性常量
 *
 * @author wangliangyuan
 * @date 2021-08-20 下午 12:20
 */
public class PermissionVisibleConstant {

    /**
     * 能看见某个域下所有 app 的角色集合
     */
    public static final EnumSet<RoleLevelEnum> PRIVILEGED_ROLES_OF_VIEW_ALL_APPS = EnumSet.of(
            RoleLevelEnum.SUPER_ADMIN,
            RoleLevelEnum.DOMAIN_PRINCIPAL,
            RoleLevelEnum.DOMAIN_ADMIN
    );
}
