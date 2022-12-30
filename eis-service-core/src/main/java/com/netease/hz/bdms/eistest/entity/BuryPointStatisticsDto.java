package com.netease.hz.bdms.eistest.entity;

import com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.statistic.*;
import com.netease.hz.bdms.easyinsight.common.param.auth.TestStatisticInfoParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BuryPointStatisticsDto implements Serializable {
    private static final long serialVersionUID = -2623929303787330464L;
    private String action;
    private boolean appStatus;
    private long index;
    private long logUpdateNum;
    private long oldLogUpdateNum;
    private long exceptionLogNum;
    private LogStatisticsSimpleDTO statistics;
    private LogStatisticsSimpleDTO oldVersionStatistics;
    private ReqTestInfoDTO reqTestInfoStatistic;
    private TestStatisticInfoParam testStatisticInfoParam;
    /**
     * 未知(即未定义)的事件类型的统计结果
     */
    private UndefinedEventStatisticsResultDTO undefinedStatistics;

    /**
     * 树模式校验的统计结果
     */
    private List<TreeModeStatisticResultDTO> treeModeStatistic;

    /**
     * 错误日志的统计结果
     */
    private List<ErrorMessageSimpleDTO> errorStatistic;

    /**
     * 纯事件校验的统计结果
     */
    private EventStatisticResultDTO eventStatistic;

    /**
     * 未匹配spm校验的统计结果
     */
    private List<UnMatchSpmStatisticResultDTO> unMatchSpmStatistic;

}
