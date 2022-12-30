package com.netease.hz.bdms.easyinsight.web.core.advice;

import com.netease.hz.bdms.easyinsight.common.constant.LogConstant;
import com.netease.hz.bdms.easyinsight.common.http.HttpResult;
import javax.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
public class ResponseBodyHandler implements ResponseBodyAdvice {

  @Override
  public boolean supports(MethodParameter methodParameter, Class aClass) {
    // 对所有方法都支持
    return true;
  }

  @Override
  public Object beforeBodyWrite(Object o, MethodParameter methodParameter, MediaType mediaType,
      Class aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
    if (!(o instanceof HttpResult)) {
      return o;
    }
    HttpServletRequest httpRequest = ((ServletServerHttpRequest) serverHttpRequest)
        .getServletRequest();
    HttpResult response = (HttpResult) o;
    String reqId = (String) httpRequest.getAttribute(LogConstant.REQ_ID);
    Object reqStartTime = httpRequest.getAttribute(LogConstant.REQ_START_TIME);
    long startTime = System.currentTimeMillis();
    if(null != reqStartTime) {
      startTime = (long)reqStartTime;
    }
    long cost = System.currentTimeMillis() - startTime;
    response.setReqId(reqId);
    response.setCost(cost);
    return response;
  }

}
