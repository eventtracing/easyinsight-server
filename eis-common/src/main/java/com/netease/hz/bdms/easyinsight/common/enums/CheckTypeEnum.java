package com.netease.hz.bdms.easyinsight.common.enums;

import com.netease.hz.bdms.easyinsight.common.exception.ServerException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum  CheckTypeEnum {
  REAL_TIME_TEST(1),
  REQUIRE_TEST(2),
    ;
  private Integer type;

  public static CheckTypeEnum fromType(Integer type) {
    for(CheckTypeEnum checkType : values()) {
      if(checkType.getType().equals(type)) {
        return checkType;
      }
    }
    throw new ServerException(type+"不能转换为CheckTypeEnum");
  }
}
