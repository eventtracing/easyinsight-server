package com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.oldversion;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OldVersionLogSimpleDTO {

  /**
   * 事件类型code
   */
  private String action;
  /**
   * mspm值
   */
  private String mspm;
  /**
   * 日志获取时间
   */
  private Long logServerTime;
  /**
   * 去向
   */
  private String targetId;
  /**
   * 资源类型
   */
  private String resourceType;
  /**
   * 资源ID
   */
  private String resourceId;
  /**
   * 日志
   */
  private Map<String, Object> log;
}
