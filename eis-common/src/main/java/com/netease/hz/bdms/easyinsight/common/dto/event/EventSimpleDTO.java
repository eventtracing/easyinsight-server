package com.netease.hz.bdms.easyinsight.common.dto.event;

import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Data
@Accessors(chain = true)
@ToString
public class EventSimpleDTO {
  /**
   * ID
   */
  private Long id;
  /**
   * 域ID
   */
  private String code;
  /**
   * 域中文名
   */
  private String name;

  /**
   * 产品ID
   */
  private Long appId;
  /**
   * 描述
   */
  private String description;
  /**
   * 域创建者
   */
  private UserSimpleDTO creator;
  /**
   * 域最近更新人
   */
  private UserSimpleDTO updater;
  /**
   * 域创建时间
   */
  private Long createTime;
  /**
   * 域最近更新时间
   */
  private Long updateTime;

  /**
   * 事件变化情况 {@link com.netease.hz.bdms.easyinsight.common.enums.DiffTypeEnum}
   */
  private String diffType;

  /**
   * 适用的对象类型， 格式为[1,2]
   * 其中的objType
   * @see com.netease.hz.bdms.easyinsight.common.enums.ObjTypeEnum
   */
  private List<Integer> applicableObjTypes;
  /**
   * 是否默认选中, 作用于对象新建
   */
  private Boolean selectedByDefault;

  public EventSimpleDTO setCreateTime(Object createTime) {
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

  public EventSimpleDTO setUpdateTime(Object updateTime) {
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
