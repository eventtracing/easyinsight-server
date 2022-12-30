package com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.rulecheck;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class IndicatorSimpleDTO {

  /**
   * 分子
   */
  private Integer numerator;
  /**
   * 分母
   */
  private Integer denominator;
  /**
   * 比值
   */
  private Double ratio;
  /**
   * 是否需要标红关注,为true表示标红，不存在或为false表示不标红
   * 完整度和匹配度 小于100标红，空值率 大于0标红
   */
  private Boolean focus;
}
