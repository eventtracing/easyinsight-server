package com.netease.hz.bdms.easyinsight.common.enums;

import com.netease.hz.bdms.easyinsight.common.exception.ServerException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CheckResultEnum {
  PASS(1),
  NOT_PASS(2),
  NO_MATCH_SPM(3)
  ;
  private Integer result;

  public static CheckResultEnum fromResult(Integer result) {
    for(CheckResultEnum resultEnum : values()) {
      if(resultEnum.getResult().equals(result)) {
        return resultEnum;
      }
    }
    throw new ServerException(result+"不能转换为CheckResultEnum");
  }
}
