package com.netease.hz.bdms.easyinsight.common.vo.logcheck;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * 稽查统计报表统计信息
 */
@Accessors(chain = true)
@Data
public class CauseCheckStatisticsVO {

    /**
     * 总埋点数及直方图
     */
    private HistogramVO totalHistogram;

    /**
     * SPM非法埋点数及直方图
     */
    private HistogramVO spmInvalidHistogram;

    /**
     * SPM非法埋点SPM数
     */
    private HistogramVO spmInvalidSpmHistogram;

    /**
     * SPM不合理埋点数及直方图
     */
    private HistogramVO spmUnreasonableHistogram;

    /**
     * SPM不合理埋点SPM数
     */
    private HistogramVO spmUnreasonableSpmHistogram;

    /**
     * 降级到页面埋点数及直方图
     */
    private HistogramVO fallbackToPageHistogram;

    /**
     * 降级到页面埋点SPM数
     */
    private HistogramVO fallbackToPageSpmHistogram;

    /**
     * type为e/p/xx/xxx的分布
     */
    private Map<String, HistogramVO> typeHistograms;

    /**
     * 规则信息
     * eg. 当前对比的埋点基准为Android_20220224和XX、XX的合并结果，其中任务XX自动合并失败，故未包含其变更埋点信息
     */
    private RuleInfoVO ruleInfo;
}
