package com.netease.hz.bdms.easyinsight.common.param.auth;

import com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.statistic.BaseCoverInfoDTO;
import com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.statistic.ReqCoverInfoDTO;
import com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.statistic.ReqTestInfoDTO;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TestStatisticInfoParam {
  /**
   * 会话id
   */
  private String code;
  /**
   * 测试结果
   */
  private Integer testResult;
  /**
   * 需求待测试信息
   */
  private ReqTestInfoDTO reqTestInfoDTO;
  /**
   * 需求测试分支
   */
  private ReqCoverInfoDTO reqCoverInfoDTO;
  /**
   * 全量测试分支
   */
  private BaseCoverInfoDTO baseCoverInfoDTO;

}
