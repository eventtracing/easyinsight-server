package com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.rulecheck;

import lombok.*;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReferCheckSimpleDTO {
  /**
   * type
   */
  private String type;
  /**
   * spm
   */
  private String spm;
  /**
   * scm
   */
  private String scm;
  /**
   * refer
   */
  private String refer;
  /**
   * 验证结果
   * @see com.netease.hz.bdms.easyinsight.common.enums.CauseCheckResultEnum
   */
  private int checkResult;
  /**
   * 验证内容
   */
  private Map<String, Object> checkContent;

}
