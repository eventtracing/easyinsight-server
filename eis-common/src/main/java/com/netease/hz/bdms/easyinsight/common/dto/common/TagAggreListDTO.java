package com.netease.hz.bdms.easyinsight.common.dto.common;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TagAggreListDTO {

  /**
   * 创建人
   */
  private List<CommonAggregateDTO> creators;
}
