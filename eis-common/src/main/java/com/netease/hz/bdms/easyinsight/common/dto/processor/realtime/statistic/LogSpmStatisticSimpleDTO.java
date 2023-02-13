package com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.statistic;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
public class LogSpmStatisticSimpleDTO {

  /**
   * spm值
   */
  private String spm;
  /**
   * 对象埋点的数目
   */
  private Integer num;
}
