package com.netease.hz.bdms.easyinsight.dao.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

@Data
@Accessors(chain = true)
public class CommonKV {

  /**
   * 自增id
   */
  private Long id;
  /**
   * 分类
   */
  private String code;
  /**
   * K
   */
  private String k;

  /**
   * V
   */
  private String v;
}
