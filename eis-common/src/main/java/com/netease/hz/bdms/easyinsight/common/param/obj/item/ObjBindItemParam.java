package com.netease.hz.bdms.easyinsight.common.param.obj.item;

import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ObjBindItemParam {

  /**
   * 埋点ID
   */
  @NotNull(message = "埋点ID不能为空")
  private Long trackerId;

  /**
   * 标签ID集合
   */
  private List<Long> tagIds;
  /**
   * 路由路径ID集合
   */
  private List<Long> routePathIds;
}
