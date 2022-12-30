package com.netease.hz.bdms.easyinsight.dao.model;

import java.sql.Timestamp;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ToString
public class ParamBindValue {

  /**
   * 自增id
   */
  private Long id;
  /**
   * 参数绑定ID
   */
  private Long bindId;
  /**
   * 参数值ID
   */
  private Long paramValueId;
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
