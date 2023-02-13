package com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.rulecheck;

import com.netease.hz.bdms.easyinsight.common.enums.CheckResultEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 单条日志校验结果的格式化结构
 */
@Data
@Accessors(chain = true)
public class CheckItemResultFormatSimpleDTO {

  /**
   * 格式化后的日志
   */
  private CompareItemSimpleDTO log;
  /**
   * 格式好后的规则
   */
  private CompareItemSimpleDTO rule;
  /**
   * key是否匹配
   */
  private Boolean keyMatched;
  /**
   * value是否匹配
   */
  private Boolean valueMatched;
  /**
   * value是否为空
   */
  private Boolean valueEmpty;

  /**
   * 校验结果
   */
  private CheckResultEnum checkResult;
}
