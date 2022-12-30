package com.netease.hz.bdms.easyinsight.common.param.param.paramvalue;

import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ParamValueItemParam {

  /**
   * 参数值ID：若是修改时，此ID有值，否则此ID为空
   */
  private Long id;
  /**
   * 参数值code或规则表达式
   */
  @NotBlank(message = "参数值code或规则表达式不能为空")
  private String code;
  /**
   * 中文名称或规则名称
   */
  @NotBlank(message = "中文名称或规则名不能为空")
  private String name;
  /**
   * 描述
   */
  private String description;
}
