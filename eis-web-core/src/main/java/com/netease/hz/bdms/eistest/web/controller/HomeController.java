package com.netease.hz.bdms.eistest.web.controller;

import com.netease.hz.bdms.easyinsight.common.http.HttpResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/processor/v1")
public class HomeController {
    @GetMapping(value = {"/", "/index", "/home"})
    public HttpResult home() {
        return HttpResult.success("Welcome To Use EasyInsight Processor");
    }

    @GetMapping(value = "/health/status")
    public HttpResult health() {
        return HttpResult.success("Server is healthy");
    }
}
