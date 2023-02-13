package com.netease.hz.bdms.easyinsight.dao.model;

import java.sql.Timestamp;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
@Accessors(chain = true)
public class Param {

  /**
   * 自增id
   */
  @Id  // 声明此属性为主键
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  /**
   * 参数名
   */
  private String code;
  /**
   * 参数中文名
   */
  private String name;
  /**
   * 参数类型：1 全局公参，2事件公参，3对象标准私参，4.对象业务私参
   * @see com.netease.hz.bdms.easyinsight.common.enums.ParamTypeEnum
   */
  private Integer paramType;
  /**
   * 参数值类型:值类型，如1常量，2变量， 0不确定
   * @see com.netease.hz.bdms.easyinsight.common.enums.ParamValueTypeEnum
   */
  private Integer valueType;
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
