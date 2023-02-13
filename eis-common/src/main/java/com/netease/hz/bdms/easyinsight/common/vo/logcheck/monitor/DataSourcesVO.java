package com.netease.hz.bdms.easyinsight.common.vo.logcheck.monitor;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Accessors(chain = true)
@Data
public class DataSourcesVO {

    private List<PackageTypeFilterValuesVO> packageFilterValues;

    private List<DataSourceVO> dataSources;
}
