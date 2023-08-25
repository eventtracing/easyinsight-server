package com.netease.hz.bdms.easyinsight.common.enums;

import com.netease.hz.bdms.easyinsight.common.exception.ServerException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ObjChangeTypeEnum {
  /**
   * 对象新建
   */
  CREATEOBJ(0, "对象新建"),
  /**
   * 端类型变更
   */
  TERMINALCHANGE(1, "端类型变更"),
  /**
   * 血缘变更
   */
  LINAGECHANGE(2, "血缘变更"),
  /**
   * 私参变更
   */
  PRIVATECHANGE(3, "私参变更"),
  /**
   * 事件变更
   */
  EVENTCHANGE(4, "事件变更"),
  /**
   * 公参包变更
   */
  PUBPARAMCHANGE(5, "公参包变更"),
  /**
   * 基础属性变更
   */
  BASICCHANGE(6, "基础属性变更"),
  /**
   * 基础属性变更
   */
  TASKCHANGE(10, "任务状态变更")
  ;
  private Integer changeType;
  private String name;
  public static ObjChangeTypeEnum fromChangeType(Integer changeType, String name) {
    for(ObjChangeTypeEnum changeTypeEnum : values()) {
      if(changeTypeEnum.getChangeType().equals(changeType)) {
        return changeTypeEnum;
      }
    }
    throw new ServerException(changeType+"不能转换成ChangeTypeEnum");
  }
}
