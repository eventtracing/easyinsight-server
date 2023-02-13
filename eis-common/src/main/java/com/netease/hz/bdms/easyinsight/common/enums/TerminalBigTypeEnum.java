package com.netease.hz.bdms.easyinsight.common.enums;

import com.netease.hz.bdms.easyinsight.common.exception.ServerException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TerminalBigTypeEnum {
  /**
   * Android, iPhone, Web
   */
  CLIENT(2),

  /**
   * Server
   */
  SERVER(3);

  private final Integer type;

  private static TerminalBigTypeEnum fromType(Integer type) {
    for (TerminalBigTypeEnum terminalTypeEnum : values()) {
      if (terminalTypeEnum.getType().equals(type)) {
        return terminalTypeEnum;
      }
    }
    throw new ServerException(type + "不能转换为TerminalTypeEnum");
  }
}
