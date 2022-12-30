package com.netease.hz.bdms.easyinsight.common.dto.tag;

import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.ParamValueSimpleDTO;
import java.sql.Timestamp;
import java.util.Date;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ToString
public class ObjTagSimpleDTO {
  /**
   * 标签ID
   */
  private Long id;
  /**
   * 产品ID
   */
  private Long appId;
  /**
   * 对象ID
   */
  private Long objId;
  /**
   * 对象变更ID
   */
  private Long historyId;
  /**
   * 标签ID
   */
  private Long tagId;
  /**
   * 创建者
   */
  private UserSimpleDTO creator;
  /**
   * 更新人
   */
  private UserSimpleDTO updater;
  /**
   * 创建时间
   */
  private Long createTime;
  /**
   * 更新时间
   */
  private Long updateTime;

  public ObjTagSimpleDTO setCreateTime(Object createTime) {
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

  public ObjTagSimpleDTO setUpdateTime(Object updateTime) {
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