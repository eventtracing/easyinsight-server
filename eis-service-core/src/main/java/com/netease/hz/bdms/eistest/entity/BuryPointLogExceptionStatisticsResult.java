package com.netease.hz.bdms.eistest.entity;

import com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.statistic.ErrorMessageSimpleDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 埋点日志中错误信息的统计结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BuryPointLogExceptionStatisticsResult {

    private String action;

    private long index;

    /**
     * 错误信息的统计结果
     */
    private List<ErrorMessageSimpleDTO> exceptions;

    /**
     * 移动端上报的错误日志内容
     */
    private BuryPointErrorContentExpand expLogContent;
}
