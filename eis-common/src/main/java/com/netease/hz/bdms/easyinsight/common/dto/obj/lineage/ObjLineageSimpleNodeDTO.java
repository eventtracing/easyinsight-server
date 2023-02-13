package com.netease.hz.bdms.easyinsight.common.dto.obj.lineage;

import java.util.Objects;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ObjLineageSimpleNodeDTO {

  /**
   * 对象ID
   */
  private Long id;

  /**
   * 对象oid
   */
  private String oid;

  /**
   * 参数中文名称
   */
  private String name;

  /**
   * 类别
   *
   * @see com.netease.hz.bdms.easyinsight.common.enums.ObjTypeEnum
   */
  private Integer type;
  /**
   * 版本ID
   */
  private Long versionId;
  /**
   * 端版本ID
   */
  private Long terminalVersionId;
  /**
   * 对象版本ID
   */
  private Long objVersionId;
  /**
   * 终端ID
   */
  private Long terminalId;
  /**
   * 是否能展开父节点， 父节点才能展开父，中心节点都能展开
   */
  private Boolean expandParent;
  /**
   * 是否能展开子节点， 子节点才能展开子，中心节点都能展开
   */
  private Boolean expandSon;

  /**
   * 对象埋点ID
   */
  private Long trackerId;
  /**
   * 对象变更ID
   */
  private Long historyId;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ObjLineageSimpleNodeDTO that = (ObjLineageSimpleNodeDTO) o;
    return Objects.equals(id, that.id) &&
        Objects.equals(objVersionId, that.objVersionId) &&
        Objects.equals(terminalId, that.terminalId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, objVersionId, terminalId);
  }
}
