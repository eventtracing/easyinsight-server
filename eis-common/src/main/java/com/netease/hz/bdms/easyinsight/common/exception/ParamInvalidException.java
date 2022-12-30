package com.netease.hz.bdms.easyinsight.common.exception;

import com.netease.hz.bdms.easyinsight.common.constant.ResponseActionConstant;
import com.netease.hz.bdms.easyinsight.common.constant.ResponseCodeConstant;

/**
 * description:
 *
 * @author: gaoshuangchao
 * @createDate: 2020-07-08
 * @version: 1.0
 */
public class ParamInvalidException extends AbstractCommonException{

  public ParamInvalidException() {
    this(ResponseActionConstant.ALERT);
  }

  public ParamInvalidException(int action) {
    this.code = ResponseCodeConstant.PARAM_INVALID;
    this.action = action;
  }

  public ParamInvalidException(String message) {
    this.code = ResponseCodeConstant.PARAM_INVALID;
    this.message = message;
    this.action = ResponseActionConstant.ALERT;
  }

  public ParamInvalidException(int code, String message) {
    this.code = code;
    this.message = message;
  }

  public ParamInvalidException(String message, Throwable cause) {
    super(message, cause);
    this.action = ResponseActionConstant.ALERT;
    this.code = ResponseCodeConstant.PARAM_INVALID;
  }

}
