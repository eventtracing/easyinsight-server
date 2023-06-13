package com.netease.hz.bdms.easyinsight.common.enums;

import com.netease.hz.bdms.easyinsight.common.exception.ServerException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ObjChangeTypeEnum {
  /**
   * 无变更
   */
  NOCHANGE(0),
  /**
   * 端类型变更
   */
  TERMINALCHANGE(1),
  /**
   * 血缘变更
   */
  LINAGECHANGE(2),
  /**
   * 私参变更
   */
  PRIVATECHANGE(3),
  /**
   * 事件变更
   */
  EVENTCHANGE(4),
  /**
   * 公参包变更
   */
  PUBPARAMCHANGE(4)
  ;
  private Integer changeType;
  public static ObjChangeTypeEnum fromChangeType(Integer changeType) {
    for(ObjChangeTypeEnum changeTypeEnum : values()) {
      if(changeTypeEnum.getChangeType().equals(changeType)) {
        return changeTypeEnum;
      }
    }
    throw new ServerException(changeType+"不能转换成ChangeTypeEnum");
  }
}
