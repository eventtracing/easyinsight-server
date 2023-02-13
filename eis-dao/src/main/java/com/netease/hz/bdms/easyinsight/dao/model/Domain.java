package com.netease.hz.bdms.easyinsight.dao.model;

import java.sql.Timestamp;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Domain {

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
   * 负责人邮箱
   */
  private String ownerEmail;
  /**
   * 负责人中文名
   */
  private String ownerName;
  /**
   * 管理员列表
   * 格式：[{"email":"","userName":"" }]
   */
  private String admins;
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
