package com.netease.hz.bdms.easyinsight.common.vo.logcheck;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 稽查统计报表统计信息
 */
@Accessors(chain = true)
@Data
public class LogCheckStatisticsVO {

    /**
     * 总埋点数及直方图，粒度到小时级
     */
    private HistogramVO totalHistogram;

    /**
     * 校验失败埋点数及直方图，粒度到小时级
     */
    private HistogramVO checkFailedHistogram;

    /**
     * 校验失败涉及SPM列表
     */
    private HistogramVO checkFailedSpms;

    /**
     * 各buildUUID的校验失败涉及SPM列表
     */
    private PieVO buildUUIDcheckFailedSpms;

    /**
     * 全量的无埋点SPM（全量+增量）
     */
    private HistogramVO allNotCoveredSpms;

    /**
     * 增量量的无埋点SPM（本次版本变更的）
     */
    private HistogramVO incrementalNotCoveredSpms;

    /**
     * 全量的未覆盖分支（全量+增量）
     */
    private HistogramVO allNotCoverBranch;

    /**
     * 增量的未覆盖分支（本次版本变更的）
     */
    private HistogramVO incrementalNotCoverBranch;

    /**
     * 基线版本名、和哪些相关任务的合并结果、合并失败的任务列表
     */
    private RuleInfoVO ruleInfo;

    /**
     * 部分失败message
     */
    private String partialFailedMessage;
}
