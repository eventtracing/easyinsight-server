package com.netease.hz.bdms.easyinsight.common.enums;

import com.netease.hz.bdms.easyinsight.common.exception.ServerException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TestResultEnum {
  UNPASS(0),
  PASS(1),
  PARTPASS(2),
    ;
  private Integer type;

  public static TestResultEnum fromType(Integer type) {
    for(TestResultEnum checkType : values()) {
      if(checkType.getType().equals(type)) {
        return checkType;
      }
    }
    throw new ServerException(type+"不能转换为TestResultEnum");
  }
}
