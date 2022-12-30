package com.netease.hz.bdms.easyinsight.common.enums;

import com.netease.hz.bdms.easyinsight.common.exception.ServerException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum  ParamTypeEnum {
  /**
   * 全局公参
   */
  GLOBAL_PUBLIC_PARAM(1),
  /**
   * 事件公参
   */
  EVENT_PUBLIC_PARAM(2),
  /**
   * 对象标准私参
   */
  OBJ_NORMAL_PARAM(3),
  /**
   * 对象业务私参
   */
  OBJ_BUSINESS_PRIVATE_PARAM(4),
  ;
  private Integer type;

  public static ParamTypeEnum fromType(Integer type) {
    for (ParamTypeEnum paramTypeEnum : values()) {
      if (paramTypeEnum.getType().equals(type)) {
        return paramTypeEnum;
      }
    }
    throw new ServerException(type + "不能转换为ParamTypeEnum");
  }
}
