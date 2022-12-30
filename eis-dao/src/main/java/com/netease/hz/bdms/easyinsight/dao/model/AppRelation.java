package com.netease.hz.bdms.easyinsight.dao.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AppRelation {

  /**
   * 自增id
   */
  private Long id;
  /**
   * 父空间appId
   */
  private Long parentAppId;
  /**
   * 子空间appId
   */
  private Long appId;
}
