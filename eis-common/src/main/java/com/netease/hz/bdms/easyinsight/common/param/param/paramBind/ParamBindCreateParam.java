package com.netease.hz.bdms.easyinsight.common.param.param.paramBind;

import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ParamBindCreateParam {

  /**
   * 绑定元素ID
   */
  @NotNull(message = "绑定元素ID不能为空")
  private Long entityId;

  /**
   * 绑定元素类型
   * @see com.netease.hz.bdms.easyinsight.common.enums.EntityTypeEnum
   */
  @NotNull(message = "绑定元素类型不能为空")
  private Integer entityType;

  /**
   * 版本名称
   */
  private String version;

  /**
   * 参数绑定情况
   */
  // @NotEmpty(message = "参数绑定不能为空")
  private List<ParamBindItermParam> paramBinds;
}
