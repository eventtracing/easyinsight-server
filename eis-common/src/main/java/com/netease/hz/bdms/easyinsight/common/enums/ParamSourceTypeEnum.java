package com.netease.hz.bdms.easyinsight.common.enums;

import com.netease.hz.bdms.easyinsight.common.exception.ServerException;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 参数由谁打
 */
@AllArgsConstructor
@Getter
public enum ParamSourceTypeEnum {
  /**
   * 来源于本身
   */
  SELF("self"),
  /**
   * 来源于外部透传
   */
  TRP("trp"),
  ;
  private String type;

  public static ParamSourceTypeEnum fromType(String type) {
    for (ParamSourceTypeEnum paramTypeEnum : values()) {
      if (paramTypeEnum.getType().equals(type)) {
        return paramTypeEnum;
      }
    }
    throw new ServerException(type + "不能转换为ParamSourceTypeEnum");
  }
}
