package com.netease.hz.bdms.easyinsight.common.exception;

import com.netease.hz.bdms.easyinsight.common.constant.ResponseActionConstant;
import com.netease.hz.bdms.easyinsight.common.constant.ResponseCodeConstant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UploadException extends AbstractCommonException{

  public UploadException(){this(ResponseActionConstant.ALERT);}

  public UploadException(int action) {
    this.code = ResponseCodeConstant.UPLOAD_ERROR;
    this.action = action;
  }

  public UploadException(String message) {
    this.code = ResponseCodeConstant.UPLOAD_ERROR;
    this.message = message;
    this.action = ResponseActionConstant.ALERT;
  }

  public UploadException(int code, String message) {
    this.code = code;
    this.message = message;
  }

  public UploadException(String message, Throwable cause) {
    super(message, cause);
    this.action = ResponseActionConstant.ALERT;
    this.code = ResponseCodeConstant.UPLOAD_ERROR;
  }
}
