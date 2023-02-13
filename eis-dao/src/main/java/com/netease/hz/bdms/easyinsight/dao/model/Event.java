package com.netease.hz.bdms.easyinsight.dao.model;

import java.sql.Timestamp;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Event {

  /**
   * 自增id
   */
  private Long id;
  /**
   * 事件类型ID
   */
  private String code;
  /**
   * 事件类型名称
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
   * 创建人邮箱
   */
  private String createEmail;
  /**
   * 创建人名称
   */
  private String createName;
  /**
   * 最近更新人的邮箱
   */
  private String updateEmail;
  /**
   * 最近更新人的名称
   */
  private String updateName;
  /**
   * 创建时间
   */
  private Timestamp createTime;
  /**
   * 更新时间
   */
  private Timestamp updateTime;
  /**
   * 适用的对象类型， 格式为[1,2]
   * 其中的objType
   * @see com.netease.hz.bdms.easyinsight.common.enums.ObjTypeEnum
   */
  private String applicableObjTypes;
  /**
   * 是否默认选中, 作用于对象新建
   */
  private Boolean selectedByDefault;
}
