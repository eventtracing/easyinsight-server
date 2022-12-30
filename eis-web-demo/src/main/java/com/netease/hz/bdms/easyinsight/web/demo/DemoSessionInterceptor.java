package com.netease.hz.bdms.easyinsight.web.demo;

import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.constant.DemoConst;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.util.FormaterUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 猛犸登陆拦截
 */
public class DemoSessionInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        EtContext.put(ContextConstant.DOMAIN_ID, DemoConst.DEMO_DOMAIN_ID);
        // 用户信息维护到上下文中
        EtContext.put(ContextConstant.USER, DemoConst.SYSTEM_USER_DTO);
        EtContext.put(ContextConstant.SESSION, DemoConst.SYSTEM_SESSION_DTO);
        //     产品ID
        String appIdStr = request.getParameter(ContextConstant.APP_ID);
        if (StringUtils.isNotBlank(appIdStr)) {
            Long appId = FormaterUtils.parseLong(appIdStr);
            EtContext.put(ContextConstant.APP_ID, appId);
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest,
                           HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse, Object o, Exception e) {

    }

    private void setResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(message);
        response.setStatus(HttpStatus.SC_OK);
    }

}
