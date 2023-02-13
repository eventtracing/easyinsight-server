package com.netease.hz.bdms.easyinsight.common.param.param.paramBind;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

@ToString
@Data
@Accessors(chain = true)
public class ParamBindItermParam {

  /**
   * 参数值的取值ID
   */
  @NotEmpty(message = "参数值ID不能为空")
  private List<Long> values;

  /**
   * 参数ID
   */
  @NotNull(message = "参数ID不能为空")
  private Long paramId;

  /**
   * 绑定描述
   */
  private String description;

  /**
   * 是否必须传
   */
  private Boolean must = true;

  /**
   * 是否非空
   */
  @NotNull(message = "非空配置不能为空")
  private Boolean notEmpty;
  /**
   * 是否用于测试
   */
  @NotNull(message = "是否用于测试配置不能为空")
  private Boolean needTest;

  /**
   * 参数对应的上报日志是否使用urlEncode编码
   */
  @NotNull(message = "参数对应上报日志是否使用urlEncode编码不能为空")
  private Boolean isEncode;
}
