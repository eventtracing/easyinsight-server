package com.netease.hz.bdms.easyinsight.common.aop;

import com.netease.hz.bdms.easyinsight.common.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Component
@Aspect
@Slf4j
public class LogAspect {

    @Pointcut("@annotation(com.netease.hz.bdms.easyinsight.common.aop.MethodLog)")
    private void pointCut() {

    }

    @Before("pointCut()")
    public void doBeforeAdvice(JoinPoint joinPoint) {
        // 解析入口controller方法入参
        Map<String, Object> params = getFieldsName(joinPoint);
        log.info("方法名:" + joinPoint.getSignature() + ",入参:" + params);
    }

    private Map<String, Object> getFieldsName(JoinPoint joinPoint) {
        // 参数值
        Object[] args = joinPoint.getArgs();
        ParameterNameDiscoverer pnd = new DefaultParameterNameDiscoverer();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String[] parameterNames = pnd.getParameterNames(method);
        Map<String, Object> paramMap = new HashMap<>(32);
        for (int i = 0; i < parameterNames.length; i++) {
            paramMap.put(parameterNames[i], args[i]);
        }
        return paramMap;
    }
}
