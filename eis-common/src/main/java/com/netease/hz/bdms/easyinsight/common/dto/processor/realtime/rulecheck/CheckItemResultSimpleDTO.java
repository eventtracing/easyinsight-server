package com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.rulecheck;

import com.netease.hz.bdms.easyinsight.common.enums.CheckErrorCauseEnum;
import com.netease.hz.bdms.easyinsight.common.enums.CheckItemEnum;
import com.netease.hz.bdms.easyinsight.common.enums.CheckResultEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 一条日志的校验结果
 */
@Data
@Accessors(chain = true)
public class CheckItemResultSimpleDTO {

  /**
   * 校验结果
   */
  private CheckResultEnum checkResult;
  /**
   * 校验出错的原因
   */
  private CheckErrorCauseEnum checkErrorCause;
  /**
   * 出错原因作用到哪一端，日志 or 规则
   */
  private CheckItemEnum checkItem;
  /**
   * key是否匹配
   */
  private Boolean keyMatched;
  /**
   * value是否匹配
   */
  private Boolean valueMatched;
  /**
   * value为取值为空，不包含为null的字段
   */
  private Boolean valueEmpty;
}
