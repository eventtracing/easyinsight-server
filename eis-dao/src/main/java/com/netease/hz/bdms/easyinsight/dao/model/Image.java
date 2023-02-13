package com.netease.hz.bdms.easyinsight.dao.model;

import java.sql.Timestamp;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Image {

  /**
   * 主键ID
   */
  private Long id;
  /**
   * 图片内容
   */
  private byte[] content;
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
   * 开始时间
   */
  private Timestamp createTime;
  /**
   * 结束时间
   */
  private Timestamp updateTime;

}
