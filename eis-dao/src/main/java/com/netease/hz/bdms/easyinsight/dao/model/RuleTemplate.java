package com.netease.hz.bdms.easyinsight.dao.model;

import java.sql.Timestamp;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RuleTemplate {

  /**
   * 规则模板ID
   */
  private Long id;
  /**
   * 规则模板名称
   */
  private String name;
  /**
   * 规则模板内容
   */
  private String rule;
  /**
   * 创建人邮箱
   */
  private String createEmail;
  /**
   * 创建人名称
   */
  private String createName;
  /**
   * 更新人邮箱
   */
  private String updateEmail;
  /**
   * 更新人名称
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
