package com.netease.hz.bdms.easyinsight.common.param.auth;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserBaseInfoParam {

  /**
   * 用户id
   */
  private Long id;
  /**
   * 用户名
   */
  private String userName;
  /**
   * 邮箱
   */
  private String email;
  /**
   *
   */
  private String terminal;
  /**
   * 会话
   */
  private String conversation;
  /**
   * 需求id
   */
  private String requirements;
  /**
   * tabId
   */
  private String tab;
  /**
   * taskId
   */
  private String taskId;
  /**
   * baseLineName
   */
  private String baseLineName;
  /**
   * terminalId
   */
  private String terminalId;
  /**
   * appId
   */
  private String appId;
  /**
   * reqPoolId
   */
  private String reqPoolId;
  /**
   * reqName
   */
  private String reqName;

}
