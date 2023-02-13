package com.netease.hz.bdms.easyinsight.common.enums;

import com.netease.hz.bdms.easyinsight.common.exception.ServerException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ParamValueTypeEnum {
  /**
   * 常量
   */
  CONSTANT(1),
  /**
   * 变量
   */
  VARIABLE(2),

  ;
  private Integer type;

  public static ParamValueTypeEnum fromType(Integer type) {
    for (ParamValueTypeEnum paramValueTypeEnum : values()) {
      if (paramValueTypeEnum.getType().equals(type)) {
        return paramValueTypeEnum;
      }
    }
    throw new ServerException(type + "不能转换为ParamValueTypeEnum");
  }
}
