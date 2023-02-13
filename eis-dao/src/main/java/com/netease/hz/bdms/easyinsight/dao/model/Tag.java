package com.netease.hz.bdms.easyinsight.dao.model;

import java.sql.Timestamp;

import com.netease.hz.bdms.easyinsight.common.enums.TagTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Tag {
  /**
   * 标签ID
   */
  private Long id;

  /**
   * 标签名称
   */
  private String name;

  /**
   * 产品ID
   */
  private Long appId;

  /**
   * 标签类型
   * @see TagTypeEnum
   */
  private Integer type;

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
