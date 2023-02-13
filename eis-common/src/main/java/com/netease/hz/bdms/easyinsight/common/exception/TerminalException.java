package com.netease.hz.bdms.easyinsight.common.exception;

import com.netease.hz.bdms.easyinsight.common.constant.ResponseActionConstant;
import com.netease.hz.bdms.easyinsight.common.constant.ResponseCodeConstant;

public class TerminalException extends AbstractCommonException{

  public TerminalException() {
    this(ResponseActionConstant.ALERT);
  }

  public TerminalException(int action) {
    this.code = ResponseCodeConstant.TERMINAL_ERROR;
    this.action = action;
  }

  public TerminalException(String message) {
    this.code = ResponseCodeConstant.TERMINAL_ERROR;
    this.message = message;
    this.action = ResponseActionConstant.ALERT;
  }

  public TerminalException(int code, String message) {
    this.code = code;
    this.message = message;
  }

  public TerminalException(String message, Throwable cause) {
    super(message, cause);
    this.action = ResponseActionConstant.ALERT;
    this.code = ResponseCodeConstant.TERMINAL_ERROR;
  }
}
