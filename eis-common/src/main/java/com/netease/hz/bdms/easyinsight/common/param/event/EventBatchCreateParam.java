package com.netease.hz.bdms.easyinsight.common.param.event;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class EventBatchCreateParam {

  /**
   * 事件类型列表
   */
  private List<EventCreateParam> params;

}
