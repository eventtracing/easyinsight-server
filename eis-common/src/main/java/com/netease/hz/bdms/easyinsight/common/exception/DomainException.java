package com.netease.hz.bdms.easyinsight.common.exception;

import com.netease.hz.bdms.easyinsight.common.constant.ResponseActionConstant;
import com.netease.hz.bdms.easyinsight.common.constant.ResponseCodeConstant;

public class DomainException extends AbstractCommonException{

  public DomainException() {
    this(ResponseActionConstant.ALERT);
  }

  public DomainException(int action) {
    this.code = ResponseCodeConstant.DOMAIN_ERROR;
    this.action = action;
  }

  public DomainException(String message) {
    this.code = ResponseCodeConstant.DOMAIN_ERROR;
    this.message = message;
    this.action = ResponseActionConstant.ALERT;
  }

  public DomainException(int code, String message) {
    this.code = code;
    this.message = message;
  }

  public DomainException(String message, Throwable cause) {
    super(message, cause);
    this.action = ResponseActionConstant.ALERT;
    this.code = ResponseCodeConstant.DOMAIN_ERROR;
  }
}
