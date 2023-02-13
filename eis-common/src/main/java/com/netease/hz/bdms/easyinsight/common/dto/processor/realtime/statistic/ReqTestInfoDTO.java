package com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.statistic;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class ReqTestInfoDTO {

  /**
   * spm数目
   */
  private Integer spmNum;
  /**
   * 事件数目
   */
  private Integer actionNum;
  /**
   * 参数数目
   */
  private Integer paramNum;

  /**
   * 待测分支数
   */
  private Integer branchNum;

}
