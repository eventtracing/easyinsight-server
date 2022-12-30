package com.netease.hz.bdms.easyinsight.dao.model;

import java.sql.Timestamp;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ImageRelation {

  /**
   * 主键ID
   */
  private Integer id;
  /**
   * 服务URL
   */
  private String url;
  /**
   * 关联类型
   * @see com.netease.hz.bdms.easyinsight.common.enums.EntityTypeEnum
   */
  private Integer entityType;
  /**
   * 关联ID
   */
  private Long entityId;
  /**
   * 创建人邮箱
   */
  private String createEmail;
  /**
   * 创建人中文名
   */
  private String createName;
  /**
   * 最近更新人邮箱
   */
  private String updateEmail;
  /**
   * 最近更新人中文民
   */
  private String updateName;
  /**
   * 创建时间
   */
  private Timestamp createTime;
  /**
   * 最近更新时间
   */
  private Timestamp updateTime;
}
