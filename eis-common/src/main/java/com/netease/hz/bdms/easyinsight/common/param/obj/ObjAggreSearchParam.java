package com.netease.hz.bdms.easyinsight.common.param.obj;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ObjAggreSearchParam {

  /**
   * 搜索类别集合
   */
  private List<Integer> aggreTypes;
}
