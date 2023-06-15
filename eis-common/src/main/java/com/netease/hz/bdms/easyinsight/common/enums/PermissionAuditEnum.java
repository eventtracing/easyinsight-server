package com.netease.hz.bdms.easyinsight.common.enums;

import com.netease.hz.bdms.easyinsight.common.exception.ServerException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PermissionAuditEnum {
  /**
   * 未处理
   */
  INIT(0),
  /**
   * 通过
   */
  PASS(1),
  /**
   * 新增
   */
  DENY(-1)
  ;
  private Integer changeType;
  public static PermissionAuditEnum fromChangeType(Integer changeType) {
    for(PermissionAuditEnum changeTypeEnum : values()) {
      if(changeTypeEnum.getChangeType().equals(changeType)) {
        return changeTypeEnum;
      }
    }
    throw new ServerException(changeType+"不能转换成ChangeTypeEnum");
  }
}
