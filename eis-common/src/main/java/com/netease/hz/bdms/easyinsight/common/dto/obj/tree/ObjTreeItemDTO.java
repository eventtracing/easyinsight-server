package com.netease.hz.bdms.easyinsight.common.dto.obj.tree;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ObjTreeItemDTO {
  /**
   * 对象ID， objId
   */
  private Long id;
  /**
   * 对象名称，objName
   */
  private String name;
  /**
   * 对象oid
   */
  private String oid;
  /**
   * 对象类型
   * @see com.netease.hz.bdms.easyinsight.common.enums.ObjTypeEnum
   */
  private Integer type;
  /**
   * 是否能变更
   */
  private Boolean canChange;
  /**
   * 子对象
   */
  private Boolean child;
}
