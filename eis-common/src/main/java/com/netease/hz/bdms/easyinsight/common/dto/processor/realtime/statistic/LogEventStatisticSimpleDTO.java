package com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.statistic;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class LogEventStatisticSimpleDTO {

  /**
   * 日志数目
   */
  private Integer logNum;
  /**
   * 对象埋点数目
   */
  private Integer objTrackerNum;
  /**
   * spm的统计明细
   */
  private List<LogSpmStatisticSimpleDTO> spmStatistics;

  /**
   * 事件类型CODE
   */
  private String eventCode;
  /**
   * 事件类型名称
   */
  private String eventName;
}
