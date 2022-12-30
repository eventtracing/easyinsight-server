package com.netease.hz.bdms.easyinsight.dao.model;

import java.sql.Timestamp;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CheckHistory {

  /**
   * 测试历史记录ID
   */
  private Long id;
  /**
   * 埋点ID
   */
  private Long trackerId;
  /**
   * 日志
   */
  private String log;
  /**
   * 规则
   */
  private String rule;
  /**
   * 验证结果，1表示通过，2表示不通过
   * @see com.netease.hz.bdms.easyinsight.common.enums.CheckResultEnum
   */
  private Integer checkResult;
  /**
   * 验证指标
   */
  private String indicators;
  /**
   * spm
   */
  private String spm;
  /**
   * 事件类型Code
   */
  private String eventCode;
  /**
   * 事件类型名称
   */
  private String eventName;
  /**
   * 日志获取时间
   */
  private Timestamp logServerTime;
  /**
   * 测试类型，1表示实时测试，2表示需求测试
   * @see com.netease.hz.bdms.easyinsight.common.enums.CheckTypeEnum
   */
  private Integer type;
  /**
   * 保存人邮箱
   */
  private String saverEmail;
  /**
   * 保存人名称
   */
  private String saverName;
  /**
   * 保存时间
   */
  private Timestamp saveTime;
  /**
   * 更新时间
   */
  private Timestamp updateTime;
}
