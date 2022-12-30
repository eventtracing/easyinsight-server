package com.netease.hz.bdms.eistest.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

/**
 * 埋点日志的统计结果
 *
 * @author wangliangyuan
 * @date 2021-09-03 下午 05:33
 */
@Data
@AllArgsConstructor
@ToString
public class BuryPointLogStatisticsResult {

    /**
     * 移动端上报的日志类型
     * <p>
     * {@link AppPushLogAction#getName()} 的值
     */
    private String logType;

    /**
     * logType = log 时的日志统计结果
     */
    private BuryPointValidationAndStatistics logStats;

    /**
     * logType = exception 时的日志统计结果
     */
    private BuryPointLogExceptionStatisticsResult expStats;

    private String status;
}
