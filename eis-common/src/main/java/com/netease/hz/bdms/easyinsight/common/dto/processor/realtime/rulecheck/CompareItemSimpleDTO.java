package com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.rulecheck;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CompareItemSimpleDTO {

  /**
   * 一行记录中的key
   */
  private String key;
  /**
   * 一行记录中的value
   */
  private String value;
  /**
   * 匹配不上的原因，若该字段为空，表示能否匹配上
   */
  private String cause;
  /**
   * 注释
   */
  private String comment;
}
