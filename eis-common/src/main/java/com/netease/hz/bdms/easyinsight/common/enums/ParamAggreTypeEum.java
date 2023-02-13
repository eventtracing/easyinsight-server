package com.netease.hz.bdms.easyinsight.common.enums;

import com.netease.hz.bdms.easyinsight.common.exception.ServerException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum  ParamAggreTypeEum {
  CREATOR(1),
  ;

  private Integer type;

  public static ParamAggreTypeEum fromAggreType(Integer type) {
    for(ParamAggreTypeEum paramAggreTypeEum : values()) {
      if(paramAggreTypeEum.getType().equals(type)) {
        return paramAggreTypeEum;
      }
    }
    throw new ServerException(type + "不能转换为ParamAggreTypeEum");
  }
}
