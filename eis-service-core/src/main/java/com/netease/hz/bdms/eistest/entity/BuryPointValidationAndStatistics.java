package com.netease.hz.bdms.eistest.entity;

import com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.oldversion.OldVersionLogSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.rulecheck.RuleCheckSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.statistic.*;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BuryPointValidationAndStatistics {
    private String action;
    private Long index;
    private String spm;
    private RuleCheckSimpleDTO ruleCheck;
    private LogStatisticsSimpleDTO statistics;
    private LogStatisticsSimpleDTO oldVersionStatistics;
    private OldVersionLogSimpleDTO oldVersionLog;

    /**
     * 未知(即未定义)的事件类型的统计结果
     */
    private UndefinedEventStatisticsResultDTO undefinedStatistics;

    /**
     * 树模式校验的统计结果
     */
    private List<TreeModeStatisticResultDTO> treeModeStatistic;
    /**
     * 未匹配spm校验的统计结果
     */
    private List<UnMatchSpmStatisticResultDTO> unMatchSpmStatistic;
    /**
     * 纯事件校验的统计结果
     */
    private EventStatisticResultDTO eventStatistic;
}
