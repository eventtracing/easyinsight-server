package com.netease.hz.bdms.easyinsight.web.core.aop;

import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.enums.rbac.PermissionEnum;
import com.netease.hz.bdms.easyinsight.common.exception.UserManagementException;
import com.netease.hz.bdms.easyinsight.dao.model.rbac.Auth;
import com.netease.hz.bdms.easyinsight.service.service.RbacService;
import com.netease.hz.bdms.easyinsight.common.aop.PermissionAction;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Aspect
@Order
@Component
public class PermissionAspect {

    @Autowired
    private RbacService rbacService;

    @Around("@annotation(com.netease.hz.bdms.easyinsight.common.aop.PermissionAction)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        PermissionAction permissionAction = method.getAnnotation(PermissionAction.class);
        PermissionEnum[] requiredPermission = permissionAction.requiredPermission();

        Set<Auth> requiredAuthSet = Arrays.stream(requiredPermission)
                .map(permissionEnum -> {
                    Auth requiredAuth = new Auth();
                    requiredAuth.setAuthCode(permissionEnum.getCode());
                    requiredAuth.setAuthParentCode(permissionEnum.getParentCode());
                    return requiredAuth;
                }).collect(Collectors.toSet());

        UserDTO user = EtContext.get(ContextConstant.USER);
        Long domainId = EtContext.get(ContextConstant.DOMAIN_ID);
        Long appId = EtContext.get(ContextConstant.APP_ID);

        if (!rbacService.authorize(requiredAuthSet, user.getId(), domainId, appId)) {
            throw new UserManagementException("无操作权限");
        }
        return pjp.proceed();
    }
}
