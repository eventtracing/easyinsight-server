package com.netease.hz.bdms.easyinsight.common.dto.realtimetest;

import lombok.Data;

import java.util.Map;

@Data
public class ServerLogParam {

    /**
     * realtimeTestCode
     */
    private Long code;
    /**
     * action
     */
    private String action;
    /**
     * data
     */
    private Map<String, Object> data;

}
