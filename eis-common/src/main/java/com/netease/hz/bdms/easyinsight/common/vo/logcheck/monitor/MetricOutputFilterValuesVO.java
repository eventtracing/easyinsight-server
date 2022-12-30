package com.netease.hz.bdms.easyinsight.common.vo.logcheck.monitor;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Accessors(chain = true)
@Data
public class MetricOutputFilterValuesVO {
    
    private List<OperatorSupportFields> operatorSupportFields;
}
