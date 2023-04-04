package com.netease.hz.bdms.easyinsight.dao.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

@Data
@Accessors(chain = true)
public class TrackerContent {

  /**
   * 自增id
   */
  private Long id;

  /**
   * trackerId
   */
  private Long trackerId;

  /**
   * 类型
   */
  private String type;

  /**
   * 类型
   */
  private String content;

  /**
   * 创建时间
   */
  private Timestamp createTime;

  /**
   * 更新时间
   */
  private Timestamp updateTime;

}
