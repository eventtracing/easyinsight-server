package com.netease.hz.bdms.easyinsight.common.dto.obj.param;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class GlobalPublicParamItemDTO {

  /**
   * 版本名称
   */
  private String versionName;

  /**
   * 具体参数信息
   */
  List<ParamWithValueItemDTO> params;
}
