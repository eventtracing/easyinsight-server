package com.netease.hz.bdms.easyinsight.common.param.version;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class VersionCreateParam {

  /**
   * 版本名称
   */
  @NotBlank(message = "版本名称不能为空")
  private String name;

  /**
   * 关联元素ID不能为空
   */
  @NotNull(message = "关联元素ID不能为空")
  private Long entityId;

  /**
   * 关联元素类型不能为空
   * @see com.netease.hz.bdms.easyinsight.common.enums.EntityTypeEnum
   */
  @NotNull(message = "关联元素类型不能为空")
  private Integer entityType;


}
