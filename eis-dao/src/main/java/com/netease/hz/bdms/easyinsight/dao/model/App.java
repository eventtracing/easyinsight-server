package com.netease.hz.bdms.easyinsight.dao.model;

import java.sql.Timestamp;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class App {

  /**
   * 自增id
   */
  private Long id;
  /**
   * 域ID
   */
  private String code;
  /**
   * 域名称
   */
  private String name;
  /**
   * 域主键ID
   */
  private Long domainId;
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
