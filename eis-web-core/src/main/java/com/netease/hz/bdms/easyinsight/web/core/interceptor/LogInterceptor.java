package com.netease.hz.bdms.easyinsight.web.core.interceptor;

import com.google.common.collect.Maps;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.constant.LogConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.util.JsonUtils;
import com.netease.hz.bdms.easyinsight.common.util.HttpUtils;
import com.netease.hz.bdms.easyinsight.web.core.filter.ServletInputStreamFilter.RequestWrapper;

import java.net.URLDecoder;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * description:
 *
 * @author: gaoshuangchao
 * @createDate: 2020-05-21
 * @version: 1.0
 */
public class LogInterceptor extends HandlerInterceptorAdapter {

    private final static Logger log = LoggerFactory.getLogger("ET-TRACE");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String reqId = request.getParameter(LogConstant.REQ_ID);
        if (StringUtils.isBlank(reqId)) {
            reqId = UUID.randomUUID().toString().replace("-", "");
        }
        request.setAttribute(LogConstant.REQ_ID, reqId);
        request.setAttribute(LogConstant.REQ_START_TIME, System.currentTimeMillis());
        MDC.put(LogConstant.REQ_ID, reqId);
        UserDTO user = EtContext.get(ContextConstant.USER);
        String ip = HttpUtils.getClientIP(request);
        Map<String, Object> map = Maps.newHashMap();
        map.put("currentTime", System.currentTimeMillis());
        map.put("ip", ip);
        map.put("reqId", reqId);
        map.put("requestUri", URLDecoder.decode(request.getRequestURI(), "UTF-8"));
        if (null != request.getQueryString()) {
            map.put("queryString", URLDecoder.decode(request.getQueryString(), "UTF-8"));
        }
        if (!checkFileUpload(request)) {
            String bodyString = new RequestWrapper(request).getBodyString();
            if (StringUtils.isNoneBlank(bodyString)) {
                map.put("bodyString", bodyString);
            }
        }
        if (null != user) {
            map.put("email", user.getEmail());
        }
        log.info(JsonUtils.toJson(map));
        return true;
    }

    private boolean checkFileUpload(HttpServletRequest request) {
        if (null == request) {
            return false;
        }
        if (request instanceof MultipartHttpServletRequest) {
            return true;
        }
        String contentType = request.getContentType();
        if (StringUtils.isNoneBlank(contentType) && contentType.toLowerCase().startsWith("multipart/")) {
            return true;
        }
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {

        try {
            long startTime = (long) request.getAttribute(LogConstant.REQ_START_TIME);
            long endTime = System.currentTimeMillis();
            long cost = endTime - startTime;
            String reqId = MDC.get(LogConstant.REQ_ID);
            UserDTO user = EtContext.get(ContextConstant.USER);
            String ip = HttpUtils.getClientIP(request);
            Map<String, Object> map = Maps.newHashMap();
            map.put("currentTime", endTime);
            map.put("ip", ip);
            map.put("reqId", reqId);
            map.put("requestUri", URLDecoder.decode(request.getRequestURI(), "UTF-8"));
            map.put("cost", cost);
            if (null != user) {
                map.put("email", user.getEmail());
            }
            log.info(JsonUtils.toJson(map));
            MDC.remove(LogConstant.REQ_ID);
        } catch (Exception ignored) {
            log.error("", ignored);
        }
    }

}
