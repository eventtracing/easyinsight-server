package com.netease.hz.bdms.easyinsight.common.param.terminal;

import java.util.List;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.netease.hz.bdms.easyinsight.common.enums.TerminalBigTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TerminalSearchParam {
  /**
   * 当前页
   */
  @NotNull(message = "当前页号不能为空")
  @Min(value = 1, message = "页号最小为1")
  private Integer currentPage;

  /**
   * 页SIZE
   */
  @NotNull(message = "页规模不能为空")
  @Min(value = 1, message = "业规模最小为1")
  private Integer pageSize;

  /**
   * 排序字段
   */
  private String orderBy;

  /**
   * 排序规则
   */
  private String orderRule;

  /**
   * 终端类型
   * @see TerminalBigTypeEnum
   */
  private List<Integer> terminalTypes;
  /**
   * 描述
   */
  private String search;

}
