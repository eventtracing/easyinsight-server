package com.netease.hz.bdms.easyinsight.web.core.controller;

import com.netease.hz.bdms.easyinsight.common.http.HttpResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用于衔接发布系统，做健康检查用
 */
@Slf4j
@RequestMapping("/eis/v1")
@RestController
public class ServerStatusController {

  @GetMapping(value = {"/", "/index", "/home"})
  public HttpResult home() {
    return HttpResult.success("Welcome To Use EasyInsight");
  }

  @GetMapping(value = "/health/status")
  public HttpResult health() {
    return HttpResult.success("Server is healthy");
  }

}
