package com.netease.hz.bdms.easyinsight.common.enums;

import com.netease.hz.bdms.easyinsight.common.exception.ServerException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RealTestResultEnum {
  PASS(1),
  NOT_PASS(2),
  PART_PASS(3)
  ;
  private Integer result;

  public static RealTestResultEnum fromResult(Integer result) {
    for(RealTestResultEnum resultEnum : values()) {
      if(resultEnum.getResult().equals(result)) {
        return resultEnum;
      }
    }
    throw new ServerException(result+"不能转换为CheckResultEnum");
  }
}
