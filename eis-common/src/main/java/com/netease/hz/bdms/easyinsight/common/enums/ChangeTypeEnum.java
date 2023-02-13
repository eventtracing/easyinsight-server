package com.netease.hz.bdms.easyinsight.common.enums;

import com.netease.hz.bdms.easyinsight.common.exception.ServerException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum  ChangeTypeEnum {
  /**
   * 不变
   */
  SAME(0),
  /**
   * 新增
   */
  CREATE(1),
  /**
   * 删除
   */
  DELETE(2),
  ;
  private Integer changeType;
  public static ChangeTypeEnum fromChangeType(Integer changeType) {
    for(ChangeTypeEnum changeTypeEnum : values()) {
      if(changeTypeEnum.getChangeType().equals(changeType)) {
        return changeTypeEnum;
      }
    }
    throw new ServerException(changeType+"不能转换成ChangeTypeEnum");
  }
}
