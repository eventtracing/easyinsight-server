package com.netease.hz.bdms.easyinsight.common.param.template;

import com.netease.hz.bdms.easyinsight.common.param.param.paramBind.ParamBindItermParam;
import java.util.List;
import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TemplateCreateParam {

  /**
   * 模板名称
   */
  @NotBlank(message = "模板名称不能为空")
  private String name;
  /**
   * 模板描述
   */
  private String description;
  /**
   * 默认选中
   */
  private Boolean selectedByDefault;
  /**
   * 参数绑定
   */
  private List<ParamBindItermParam> binds;
}
