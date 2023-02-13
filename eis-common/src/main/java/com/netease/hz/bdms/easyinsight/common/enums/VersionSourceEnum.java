package com.netease.hz.bdms.easyinsight.common.enums;

import com.netease.hz.bdms.easyinsight.common.exception.ServerException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum VersionSourceEnum {
  /**
   * JIRA
   */
  JIRA(1),
  /**
   * Overmind
   */
  OVERMIND(2),
  /**
   * 对象
   */
  MANUAL(3),
  ;
  private Integer type;

  public static VersionSourceEnum fromType(Integer type) {
    for (VersionSourceEnum versionSourceEnum : values()) {
      if (versionSourceEnum.getType().equals(type)) {
        return versionSourceEnum;
      }
    }
    throw new ServerException(type + "不能转换为VersionSourceEnum");
  }
}
