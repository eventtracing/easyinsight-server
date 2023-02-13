package com.netease.hz.bdms.easyinsight.dao.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class EisRealtimeBranchIgnore {

  /**
   * 自增id
   */
  private Long id;
  /**
   * 会话ID
   */
  private String conversationId;
  /**
   * 分支
   */
  private String branchKey;

  /**
   * 内容
   */
  private String content;
}
