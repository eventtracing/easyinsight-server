package com.netease.hz.bdms.easyinsight.common.vo.logcheck.monitor;

import com.netease.hz.bdms.easyinsight.common.vo.logcheck.HistogramVO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Accessors(chain = true)
@Data
public class LogCheckMonitorHistogramVO {
    /**
     * key : groupby组合 value: 图线
     */
    private Map<String, HistogramVO> histograms;

    /**
     * 表格：key为纵坐标（指标），value为横坐标及图线
     */
    private Map<String, HistogramVO> forms;

    /**
     * 可支持的筛选groupBy及其取值列表
     */
    private  Map<String, Set<String>> groupByValues;

    /**
     * 可支持的筛选metric列表
     */
    private Set<String> filterMetrics;
}
