package com.netease.hz.bdms.easyinsight.common.param.param.parampool;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ParamPoolUpdateParam {

  /**
   * 参数池中参数对象ID
   */
  @NotNull(message = "参数ID不能为空")
  private Long id;
  /**
   * 参数名
   */
  @NotBlank(message = "参数名不能为空")
  private String code;
}
