package com.netease.hz.bdms.easyinsight.common.param.terminal;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.netease.hz.bdms.easyinsight.common.enums.TerminalBigTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TerminalCreateParam {

  /**
   * 终端类型
   * @see TerminalBigTypeEnum
   */
  @NotNull(message = "终端类型不能为空")
  private Integer type;

  /**
   * 终端名称
   */
  @NotBlank(message = "终端名称不能为空")
  private String name;

  /**
   * 描述
   */
  private String description;
}
