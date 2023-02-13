package com.netease.hz.bdms.easyinsight.dao.model;

import java.sql.Timestamp;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ToString
public class ObjTrackerEvent {
  /**
   * 自增id
   */
  private Long id;

  /**
   * 对象埋点ID
   */
  private Long trackerId;
  /**
   * 事件类型ID
   */
  private Long eventId;
  /**
   * （（事件类型）事件公参参数包的版本ID
   */
  private Long eventParamVersionId;
  /**
   * 创建时间
   */
  private Timestamp createTime;
  /**
   * 更新时间
   */
  private Timestamp updateTime;
}
