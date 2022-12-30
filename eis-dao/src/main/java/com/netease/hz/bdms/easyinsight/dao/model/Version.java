package com.netease.hz.bdms.easyinsight.dao.model;

import java.sql.Timestamp;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Version {

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
}
