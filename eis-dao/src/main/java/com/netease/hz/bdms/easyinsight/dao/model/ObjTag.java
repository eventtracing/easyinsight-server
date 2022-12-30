package com.netease.hz.bdms.easyinsight.dao.model;

import java.sql.Timestamp;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ObjTag {
  /**
   * 标签ID
   */
  private Long id;

  /**
   * 产品ID
   */
  private Long appId;

  /**
   * 对象ID
   */
  private Long objId;

  /**
   * 对象变更ID
   */
  private Long historyId;

  /**
   * 标签ID
   */
  private Long tagId;

  /**
   * 创建者邮箱
   */
  private String createEmail;

  /**
   * 创建者名称
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
