package com.netease.hz.bdms.easyinsight.common.exception;

import com.netease.hz.bdms.easyinsight.common.constant.ResponseActionConstant;
import com.netease.hz.bdms.easyinsight.common.constant.ResponseCodeConstant;
import lombok.Getter;
import lombok.Setter;

/**
 * description:
 *
 * @author: gaoshuangchao
 * @createDate: 2020-06-02
 * @version: 1.2.0
 */
@Setter
@Getter
public class ServerException extends AbstractCommonException {

  public ServerException() {
    this(ResponseActionConstant.ALERT);
  }

  public ServerException(int action) {
    this.code = ResponseCodeConstant.SYSTEM_ERROR;
    this.action = action;
  }

  public ServerException(String message) {
    this.code = ResponseCodeConstant.SYSTEM_ERROR;
    this.message = message;
    this.action = ResponseActionConstant.ALERT;
  }

  public ServerException(int code, String message) {
    this.code = code;
    this.message = message;
  }

  public ServerException(String message, Throwable cause) {
    super(message, cause);
    this.action = ResponseActionConstant.ALERT;
    this.code = ResponseCodeConstant.SYSTEM_ERROR;
  }

}
