package com.netease.hz.bdms.easyinsight.common.dto.checkhistory;

import com.netease.hz.bdms.easyinsight.common.dto.common.CommonAggregateDTO;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CheckHistoryAggreDTO {

  /**
   * spm
   */
  private List<CommonAggregateDTO> spms;
  /**
   * 事件类型
   */
  private List<CommonAggregateDTO> events;
}
