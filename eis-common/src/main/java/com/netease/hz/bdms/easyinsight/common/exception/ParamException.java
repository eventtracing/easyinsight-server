package com.netease.hz.bdms.easyinsight.common.exception;

import com.netease.hz.bdms.easyinsight.common.constant.ResponseActionConstant;
import com.netease.hz.bdms.easyinsight.common.constant.ResponseCodeConstant;

public class ParamException extends AbstractCommonException{

  public ParamException() {
    this(ResponseActionConstant.ALERT);
  }

  public ParamException(int action) {
    this.code = ResponseCodeConstant.PARAM_ERROR;
    this.action = action;
  }

  public ParamException(String message) {
    this.code = ResponseCodeConstant.PARAM_ERROR;
    this.message = message;
    this.action = ResponseActionConstant.ALERT;
  }

  public ParamException(int code, String message) {
    this.code = code;
    this.message = message;
  }

  public ParamException(String message, Throwable cause) {
    super(message, cause);
    this.action = ResponseActionConstant.ALERT;
    this.code = ResponseCodeConstant.PARAM_ERROR;
  }
}
