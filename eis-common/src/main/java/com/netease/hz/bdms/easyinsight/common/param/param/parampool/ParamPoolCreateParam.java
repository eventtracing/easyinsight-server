package com.netease.hz.bdms.easyinsight.common.param.param.parampool;

import com.netease.hz.bdms.easyinsight.common.enums.ParamTypeEnum;
import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ParamPoolCreateParam {

  /**
   * 参数名
   */
  @NotBlank(message = "参数名不能为空")
  private String code;

  // 参数类型，默认业务对象私参类型
  private Integer paramType = ParamTypeEnum.OBJ_BUSINESS_PRIVATE_PARAM.getType();
}
