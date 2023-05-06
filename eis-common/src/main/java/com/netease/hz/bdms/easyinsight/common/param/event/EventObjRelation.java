package com.netease.hz.bdms.easyinsight.common.param.event;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class EventObjRelation {

  /**
   * 终端Id
   */
  private Long terminalId;
  /**
   * 对象id
   */
  private Long objId;
  /**
   * 对象oid
   */
  private String oid;

}
