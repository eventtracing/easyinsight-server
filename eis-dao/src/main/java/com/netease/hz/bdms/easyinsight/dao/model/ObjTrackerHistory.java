package com.netease.hz.bdms.easyinsight.dao.model;

import java.sql.Timestamp;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ObjTrackerHistory {
  /**
   * 主键ID，无实在含义
   */
  private Long id;
  /**
   * 对象ID,一个对象一个ID
   */
  private Long objId;
  /**
   * 对象历史ID，对象的一个版本是一个ID
   */
  private Long historyId;
  /**
   * 对象埋点ID
   */
  private Long trackerId;
  /**
   * 终端ID
   */
  private Long terminalId;
  /**
   * 对象版本ID
   */
  private Long terminalVersionId;
  /**
   * 产品ID
   */
  private Long appId;
  /**
   * 创建时间
   */
  private Timestamp createTime;
  /**
   * 更新时间
   */
  private Timestamp updateTime;
}
