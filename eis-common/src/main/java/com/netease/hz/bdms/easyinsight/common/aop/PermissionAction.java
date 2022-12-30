package com.netease.hz.bdms.easyinsight.common.aop;

import com.netease.hz.bdms.easyinsight.common.enums.rbac.PermissionEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PermissionAction {
    /**
     * 需要的权限, 可能有多个, 但是绝大多数情况下只有一个
     *
     * @return
     */
    PermissionEnum[] requiredPermission();
}
