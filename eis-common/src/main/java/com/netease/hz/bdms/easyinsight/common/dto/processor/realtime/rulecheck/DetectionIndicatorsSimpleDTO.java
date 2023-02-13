package com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.rulecheck;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ToString
public class DetectionIndicatorsSimpleDTO {

  /**
   * 私参完整度：日志中所有层级对象的私参命中规则中所有层级对象的私参数量/规则中所有层级对象的私参数量；
   */
  private IndicatorSimpleDTO privateParamCompletion;

  /**
   * 私参匹配度：日志中所有层级对象的私参取值命中规则中对应对象对应参数的取值范围（或枚举值）的私参数量/日志中所有层级对象的私参命中规则中所有对象的私参数量；
   */
  private IndicatorSimpleDTO privateParamSuitability;
  /**
   * 私参空值率：日志中所有层级对象的私参命中规则中所有层级对象的非空私参，且日志中私参取值为空的私参数量/日志中所有层级对象的私参取值命中规则中所有层级对象的非空私参数量；
   */
  private IndicatorSimpleDTO privateParamNullRate;
  /**
   * 公参完整度：日志中所有全局公参和事件公参命中规则中的公参数量/规则中的公参数量；
   */
  private IndicatorSimpleDTO publicParamCompletion;
  /**
   * 公参匹配度：日志中所有全局公参和事件公参取值命中规则中对应参数的取值范围（或枚举值）的公参数量/日志中所有全局公参和事件公参命中规则中的公参数量；
   */
  private IndicatorSimpleDTO publicParamSuitability;
  /**
   * 公参空值率：日志中所有全局公参和事件公参命中规则中的非空公参，且日志中公参取值为空的参数数量/日志中所有全局公参和事件公参命中规则中的非空公参数量；
   */
  private IndicatorSimpleDTO publicParamNullRate;
}
