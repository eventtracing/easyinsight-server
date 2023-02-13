package com.netease.hz.bdms.easyinsight.common.param.version;

import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class VersionSetParam {

  /**
   * 版本ID
   */
  @NotNull(message = "版本ID不能为空")
  private Long versionId;

  /**
   * 关联元素ID
   */
  @NotNull(message = "关联元素ID不能为空")
  private Long entityId;

  /**
   * 关联元素类型
   * @see com.netease.hz.bdms.easyinsight.common.enums.EntityTypeEnum
   */
  @NotNull(message = "关联元素类型不能为空")
  private Integer entityType;
}
