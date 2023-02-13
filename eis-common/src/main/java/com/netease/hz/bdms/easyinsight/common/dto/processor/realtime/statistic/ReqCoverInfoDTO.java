package com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.statistic;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ReqCoverInfoDTO {

  /**
   * 已通过数目
   */
  private Integer passNum;
  /**
   * 未通过数目
   */
  private Integer disPassNum;
  /**
   * 未覆盖数目
   */
  private Integer unCoveredNum;

}
