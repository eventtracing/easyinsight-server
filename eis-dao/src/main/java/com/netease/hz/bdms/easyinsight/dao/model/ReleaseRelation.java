package com.netease.hz.bdms.easyinsight.dao.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ReleaseRelation {

  /**
   * 自增id
   */
  private Long id;
  /**
   * 父空间releaseId
   */
  private Long parentReleaseId;
  /**
   * 子空间releaseId
   */
  private Long releaseId;
}
