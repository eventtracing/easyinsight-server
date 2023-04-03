package com.netease.hz.bdms.easyinsight.service.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EnvironmentService {

    @Value("${spring.profiles.active}")
    private String env;

    public boolean isTest() {
        return "test".equals(env);
    }
}
