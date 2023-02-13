package com.netease.hz.bdms.easyinsight.common.vo.logcheck.monitor;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Set;

@Accessors(chain = true)
@Data
public class DataSourceVO {

    private String name;

    private String value;

    private List<DataFieldFilterValuesVO> dataFields;

    private MetricOutputFilterValuesVO metricOutput;

    private Set<String> groupBys;
}
