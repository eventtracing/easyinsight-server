package com.netease.hz.bdms.easyinsight.common.dto.realtimetest;

import lombok.Data;

@Data
public class TestHistorySearchRequestDTO {

    Long code;

    Long userId;

    Long taskId;

    Integer result;

    String reqName;

    String terminal;

    String baseVer;

    Long startTime;

    Long endTime;

    Long domainId;

    Long appId;

    private Integer currentPage;

    private Integer pageSize;

    private String orderBy;

    private String orderRule;

}
