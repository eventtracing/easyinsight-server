package com.netease.hz.bdms.easyinsight.common.dto.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class PagingSortDTO {
  /**
   * 当前页
   */
  @NotNull(message = "当前页号不能为空")
  @Min(value = 1, message = "页号最小为1")
  private Integer currentPage;

  /**
   * 每页记录数
   */
  @NotNull(message = "每页记录数不能为空")
  @Min(value = 1, message = "每页记录数最小为1")
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
   * 计算偏移量
   */
  private Integer offset;

  public PagingSortDTO(Integer currentPage, Integer pageSize) {
    this(currentPage, pageSize, null, null);
  }

  public PagingSortDTO(Integer currentPage, Integer pageSize, String orderBy, String orderRule) {
    this.currentPage = currentPage;
    this.pageSize = pageSize;
    this.orderBy = orderBy;
    this.orderRule = orderRule;

    if(currentPage != null && currentPage >= 1 && pageSize != null && pageSize >= 1) {
      this.offset = (currentPage - 1) * pageSize;
    }
  }



}
