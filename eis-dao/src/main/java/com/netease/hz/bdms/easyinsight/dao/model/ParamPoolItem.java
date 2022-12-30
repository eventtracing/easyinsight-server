package com.netease.hz.bdms.easyinsight.dao.model;

import java.sql.Timestamp;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ParamPoolItem {

  /**
   * 自增id
   */
  private Long id;
  /**
   * 参数名
   */
  private String code;
  /**
   * 参数类型：1 全局公参，2事件公参，3对象标准私参，4.对象业务私参
   * @see com.netease.hz.bdms.easyinsight.common.enums.ParamTypeEnum
   */
  private Integer paramType;
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
