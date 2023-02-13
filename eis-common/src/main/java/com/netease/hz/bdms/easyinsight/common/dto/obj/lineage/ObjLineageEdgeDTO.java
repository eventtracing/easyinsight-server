package com.netease.hz.bdms.easyinsight.common.dto.obj.lineage;

import java.util.Objects;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ObjLineageEdgeDTO {

  /**
   * 来源
   */
  private Long fromObjId;
  /**
   * 去向
   */
  private Long toObjId;

  /**
   * 变更类型
   * @see com.netease.hz.bdms.easyinsight.common.enums.ChangeTypeEnum
   */
  private Integer changeType;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ObjLineageEdgeDTO that = (ObjLineageEdgeDTO) o;
    return Objects.equals(fromObjId, that.fromObjId) &&
        Objects.equals(toObjId, that.toObjId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(fromObjId, toObjId);
  }
}
