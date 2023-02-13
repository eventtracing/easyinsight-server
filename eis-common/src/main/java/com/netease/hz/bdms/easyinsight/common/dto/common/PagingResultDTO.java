package com.netease.hz.bdms.easyinsight.common.dto.common;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class PagingResultDTO <T>{

  /**
   * 总数目
   */
  private Integer totalNum;
  /**
   * 当前页码
   */
  private Integer pageNum;
  /**
   * 元素个数
   */
  private List<T> list;
}
