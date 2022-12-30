package com.netease.hz.bdms.easyinsight.dao.model;

import java.sql.Timestamp;

import com.netease.hz.bdms.easyinsight.common.enums.TerminalBigTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Terminal {

  /**
   * 自增id
   */
  private Long id;

  /**
   * 终端名称
   */
  private String name;
  /**
   * 终端类型，1表示PC, 2表示无线
   * @see TerminalBigTypeEnum
   */
  private Integer type;
  /**
   * 是否预置
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
