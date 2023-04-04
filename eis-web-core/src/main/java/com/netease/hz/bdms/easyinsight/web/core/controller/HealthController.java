package com.netease.hz.bdms.easyinsight.web.core.controller;

import com.netease.hz.bdms.easyinsight.common.http.HttpResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class HealthController {

    @RequestMapping("/connection/test")
    public HttpResult<String> testConnection() {
        return HttpResult.success("OK");
    }

    @RequestMapping("/health/online")
    public HttpResult<String> online() {
        log.info("应用上线");
        return HttpResult.success("OK");
    }

    @RequestMapping("/health/offline")
    public HttpResult<String> offline() {
        log.info("应用下线");
        return HttpResult.success("OK");
    }
}
