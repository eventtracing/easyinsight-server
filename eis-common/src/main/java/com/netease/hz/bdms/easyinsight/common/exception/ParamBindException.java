package com.netease.hz.bdms.easyinsight.common.exception;

import com.netease.hz.bdms.easyinsight.common.constant.ResponseActionConstant;
import com.netease.hz.bdms.easyinsight.common.constant.ResponseCodeConstant;

public class ParamBindException extends AbstractCommonException{

  public ParamBindException() {
    this(ResponseActionConstant.ALERT);
  }

  public ParamBindException(int action) {
    this.code = ResponseCodeConstant.PARAM_BIND_ERROR;
    this.action = action;
  }

  public ParamBindException(String message) {
    this.code = ResponseCodeConstant.PARAM_BIND_ERROR;
    this.message = message;
    this.action = ResponseActionConstant.ALERT;
  }

  public ParamBindException(int code, String message) {
    this.code = code;
    this.message = message;
  }

  public ParamBindException(String message, Throwable cause) {
    super(message, cause);
    this.action = ResponseActionConstant.ALERT;
    this.code = ResponseCodeConstant.PARAM_BIND_ERROR;
  }
}
