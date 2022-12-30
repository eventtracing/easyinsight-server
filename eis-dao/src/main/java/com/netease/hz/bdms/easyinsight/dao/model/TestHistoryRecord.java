package com.netease.hz.bdms.easyinsight.dao.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.sql.Timestamp;
import java.util.Date;

@Data
@Accessors(chain = true)
public class TestHistoryRecord {
  /**
   * session id
   */
  private Long id;

  /**
   * appId
   */
  private Long appId;

  /**
   * 测试uid
   */
  private Long userId;

  /**
   * 测试任务id
   */
  private Long taskId;

  /**
   * 测试用户名
   */
  private String userName;

  /**
   * 测试需求
   */
  private String reqName;

  /**
   * 测试基准版本
   */
  private String baseVersion;

  /**
   * 测试终端
   */
  private String terminal;

  /**
   * app版本
   */
  private String appVersion;

  /**
   * 失败数量
   */
  private Long failedNum;

  /**
   * (0:初始化 1:测试中 3:测试完成)
   */
  private Integer status;

  /**
   * (0:不通过 1:部分通过 2:通过)
   */
  private Integer testResult;

  /**
   * 扩展信息
   */
  private String extInfo;

  /**
   * 记录保存时间
   */
  private Long saveTime;

  /**
   * 创建时间
   */
  private Date createTime;

  /**
   * 更新时间
   */
  private Date updateTime;
}
