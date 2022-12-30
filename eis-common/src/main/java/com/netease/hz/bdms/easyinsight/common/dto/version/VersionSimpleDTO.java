package com.netease.hz.bdms.easyinsight.common.dto.version;

import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import java.sql.Timestamp;
import java.util.Date;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class VersionSimpleDTO {

  /**
   * 自增id
   */
  private Long id;
  /**
   * 版本名称
   */
  private String name;

  /**
   * 版本来源，如1表示jira, 2表示overmind, 3表示手动创建
   * @see com.netease.hz.bdms.easyinsight.common.enums.VersionSourceEnum
   */
  private Integer versionSource;

  /**
   * 关联元素ID(部分版本非全局概念，而是与关联元素构成版本),如终端ID，或参数ID，或对象ID, 默认为0
   */
  private Long entityId;
  /**
   * 关联元素类型(部分版本非全局概念，而是与关联元素构成版本),如1终端，2事件类型， 3关联对象， 4参数模板，默认为0
   * @see com.netease.hz.bdms.easyinsight.common.enums.EntityTypeEnum
   */
  private Integer entityType;
  /**
   * 是否是当前使用版本
   */
  private Boolean currentUsing;
  /**
   * 是否是预置
   */
  private Boolean preset;

  /**
   * 产品ID
   */
  private Long appId;
  /**
   * 描述
   */
  private String description;
  /**
   * 创建人
   */
  private UserSimpleDTO creator;

  /**
   * 最近更新人
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

  public VersionSimpleDTO setCreateTime(Object createTime) {
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

  public VersionSimpleDTO setUpdateTime(Object updateTime) {
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
