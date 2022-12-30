package com.netease.hz.bdms.easyinsight.common.dto.param;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ParamListItemDTO {

  /**
   * 参数名code
   */
  private String code;
  /**
   * 参数类型
   * @see com.netease.hz.bdms.easyinsight.common.enums.ParamTypeEnum
   */
  private Integer paramType;
  /**
   * 参数对象
   */
  private List<ParamSimpleDTO> items;
}
