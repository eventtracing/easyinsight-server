package com.netease.hz.bdms.easyinsight.dao.model;

import java.sql.Timestamp;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ParamBind {

  /**
   * 自增id
   */
  private Long id;
  /**
   * 参数ID
   */
  private Long paramId;
  /**
   * 关联元素ID，如终端ID，或参数ID，或对象埋点ID
   */
  private Long entityId;
  /**
   * 关联类型，如1终端，2事件类型， 3关联对象埋点， 4参数模板
   * @see com.netease.hz.bdms.easyinsight.common.enums.EntityTypeEnum
   */
  private Integer entityType;
  /**
   * 版本ID
   */
  private Long versionId;
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
   * 是否非空
   */
  private Boolean notEmpty;
  /**
   * 是否必须传
   */
  private Boolean must;
  /**
   * 是否用于测试
   */
  private Boolean needTest;
  /**
   * 参数对应上报日志是否使用urlEncode编码
   */
  private Boolean isEncode;
}
