package com.netease.hz.bdms.easyinsight.common.param.param;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ParamUpdateParam {

  /**
   * 参数ID
   */
  @NotNull(message = "参数ID不能为空")
  private Long id;
  /**
   * 参数类型
   * @see com.netease.hz.bdms.easyinsight.common.enums.ParamTypeEnum
   */
  @NotNull(message = "参数类型不能为空")
  private Integer paramType ;
  /**
   * 参数民
   */
  @NotBlank(message = "参数名不能为空")
  private String code;
  /**
   * 参数中文名称
   */
  @NotBlank(message = "参数中文名称不能为空")
  private String name;
  /**
   * 描述
   */
  private String description;

  /**
   * 参数值类型
   * @see com.netease.hz.bdms.easyinsight.common.enums.ParamValueTypeEnum
   */
  @NotNull(message = "参数值类型不能为空")
  private Integer valueType;
}
