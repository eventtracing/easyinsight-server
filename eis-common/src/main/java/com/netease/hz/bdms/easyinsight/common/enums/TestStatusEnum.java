package com.netease.hz.bdms.easyinsight.common.enums;

import com.netease.hz.bdms.easyinsight.common.exception.ServerException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TestStatusEnum {

  INIT(1),

  START(2),

  RESULT(3),
  ;
  private Integer status;
  public static TestStatusEnum fromChangeType(Integer status) {
    for(TestStatusEnum testStatusEnum : values()) {
      if(testStatusEnum.getStatus().equals(status)) {
        return testStatusEnum;
      }
    }
    throw new ServerException(status+"不能转换成TestStatusEnum");
  }
}
