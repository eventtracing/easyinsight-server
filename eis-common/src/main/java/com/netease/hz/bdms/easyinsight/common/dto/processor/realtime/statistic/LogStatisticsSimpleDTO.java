package com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.statistic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class LogStatisticsSimpleDTO {
  /**
   * 日志总数目
   */
  private Integer logNum;
  /**
   * 未通过日志总数目
   */
  private Integer checkFailedlogNum;
  /**
   * 对象埋点总数目
   */
  private Integer objTrackerNum;
  /**
   * 日志统计信息
   */
  private List<LogEventStatisticSimpleDTO> eventStatistics;
}