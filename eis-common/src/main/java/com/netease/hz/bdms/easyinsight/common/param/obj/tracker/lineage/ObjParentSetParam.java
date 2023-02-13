package com.netease.hz.bdms.easyinsight.common.param.obj.tracker.lineage;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ObjParentSetParam {

  /**
   * 对象主键ID
   */
  private Long objId;
  /**
   * 终端ID
   */
  private Long terminalId;
  /**
   * 对象版本ID
   */
  private Long terminalVersionId;
  /**
   * 子对象ID集合
   */
  private List<Long> parentObjs;
}
