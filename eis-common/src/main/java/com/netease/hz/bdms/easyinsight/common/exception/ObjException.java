package com.netease.hz.bdms.easyinsight.common.exception;

import com.netease.hz.bdms.easyinsight.common.constant.ResponseActionConstant;
import com.netease.hz.bdms.easyinsight.common.constant.ResponseCodeConstant;

public class ObjException extends AbstractCommonException {

  public ObjException() {
    this(ResponseActionConstant.ALERT);
  }

  public ObjException(int action) {
    this.code = ResponseCodeConstant.OBJ_ERROR;
    this.action = action;
  }

  public ObjException(String message) {
    this.code = ResponseCodeConstant.OBJ_ERROR;
    this.message = message;
    this.action = ResponseActionConstant.ALERT;
  }

  public ObjException(int code, String message) {
    this.code = code;
    this.message = message;
  }

  public ObjException(String message, Throwable cause) {
    super(message, cause);
    this.action = ResponseActionConstant.ALERT;
    this.code = ResponseCodeConstant.OBJ_ERROR;
  }
}
