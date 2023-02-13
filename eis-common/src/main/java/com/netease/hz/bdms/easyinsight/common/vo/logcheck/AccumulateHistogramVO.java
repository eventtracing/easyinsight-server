package com.netease.hz.bdms.easyinsight.common.vo.logcheck;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;

/**
 * 参数取值
 */
@Accessors(chain = true)
@Data
public class AccumulateHistogramVO<T> {

    /**
     * 最近一天的情况
     */
    private Set<T> latest;

    private HistogramVO histogram;
}
