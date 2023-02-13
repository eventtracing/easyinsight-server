package com.netease.hz.bdms.easyinsight.common.param.param.paramvalue;

import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ParamValueUpdateParam {

  /**
   * 参数名
   */
  @NotNull(message = "参数ID不能为空")
  private Long paramId;

  /**
   * 参数取值
   */
  private List<ParamValueItemParam> values;
}
