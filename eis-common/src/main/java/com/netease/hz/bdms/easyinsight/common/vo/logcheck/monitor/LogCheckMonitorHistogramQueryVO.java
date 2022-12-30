package com.netease.hz.bdms.easyinsight.common.vo.logcheck.monitor;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class LogCheckMonitorHistogramQueryVO {

    private Long monitorItemId;

    private Map<String, Set<String>> groupByValueFilters;

    private List<String> metrics;

    private long startTime;

    private long endTime;
}
