package com.netease.hz.bdms.easyinsight.common.dto.obj.tracker.history;

import java.sql.Timestamp;
import java.util.Date;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ObjTrackerHistorySimpleDTO {
  /**
   * 主键ID，无实在含义
   */
  private Long id;
  /**
   * 对象ID,一个对象一个ID
   */
  private Long objId;
  /**
   * 对象历史ID，对象的一个版本是一个ID
   */
  private Long historyId;
  /**
   * 对象埋点ID
   */
  private Long trackerId;
  /**
   * 终端ID
   */
  private Long terminalId;
  /**
   * 对象版本ID
   */
  private Long terminalVersionId;
  /**
   * 产品ID
   */
  private Long appId;
  /**
   * 创建时间
   */
  private Long createTime;
  /**
   * 更新时间
   */
  private Long updateTime;

  public ObjTrackerHistorySimpleDTO setCreateTime(Object createTime) {
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

  public ObjTrackerHistorySimpleDTO setUpdateTime(Object updateTime) {
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
