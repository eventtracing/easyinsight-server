package com.netease.hz.bdms.easyinsight.common.dto.param.paramvalue;

import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import java.sql.Timestamp;
import java.util.Date;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RuleTemplateSimpleDTO {

  /**
   * 规则模板ID
   */
  private Long id;
  /**
   * 规则模板名称
   */
  private String name;
  /**
   * 规则模板内容
   */
  private String rule;
  /**
   * 创建人
   */
  private UserSimpleDTO creator;
  /**
   * 更新人
   */
  private UserSimpleDTO updater;
  /**
   * 创建时间
   */
  private Long createTime;
  /**
   * 更新时间
   */
  private Long updateTime;
}
