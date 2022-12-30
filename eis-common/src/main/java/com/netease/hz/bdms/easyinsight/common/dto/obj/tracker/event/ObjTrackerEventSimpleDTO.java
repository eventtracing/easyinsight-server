package com.netease.hz.bdms.easyinsight.common.dto.obj.tracker.event;

import java.sql.Timestamp;
import java.util.Date;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ObjTrackerEventSimpleDTO {
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
  private Long createTime;
  /**
   * 更新时间
   */
  private Long updateTime;


  public ObjTrackerEventSimpleDTO setCreateTime(Object createTime) {
    if(createTime != null) {
      if(createTime instanceof Timestamp) {
        this.createTime = ((Timestamp) createTime).getTime();
      }else if(createTime instanceof Date) {
        this.createTime = ((Date) createTime).getTime();
      }else if(createTime instanceof Long) {
        this.createTime = (Long)createTime;
      }else if(createTime instanceof Integer) {
        this.createTime = (Integer)createTime*1000l;
      }else if(createTime instanceof String) {
        this.createTime = Long.parseLong((String)createTime);
      }
    }
    return this;
  }

  public ObjTrackerEventSimpleDTO setUpdateTime(Object updateTime) {
    if(updateTime != null) {
      if(updateTime instanceof Timestamp) {
        this.updateTime = ((Timestamp) updateTime).getTime();
      }else if(updateTime instanceof Date) {
        this.updateTime = ((Date) updateTime).getTime();
      }else if(updateTime instanceof Long) {
        this.updateTime = (Long)updateTime;
      }else if(updateTime instanceof Integer) {
        this.updateTime = (Integer)updateTime*1000l;
      }else if(updateTime instanceof String) {
        this.updateTime = Long.parseLong((String)updateTime);
      }
    }
    return this;
  }
}
