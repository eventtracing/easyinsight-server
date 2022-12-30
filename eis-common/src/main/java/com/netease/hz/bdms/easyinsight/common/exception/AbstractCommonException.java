package com.netease.hz.bdms.easyinsight.common.exception;

import com.netease.hz.bdms.easyinsight.common.constant.ResponseActionConstant;
import com.netease.hz.bdms.easyinsight.common.constant.ResponseCodeConstant;
import lombok.Getter;
import lombok.Setter;

/**
 * description:
 *
 * @author: gaoshuangchao
 * @createDate: 2020-05-25
 * @version: 1.0
 */
@Getter
@Setter
public class AbstractCommonException extends RuntimeException {

  protected int code = ResponseCodeConstant.SYSTEM_ERROR;

  protected int action = ResponseActionConstant.NO_ACTION;

  protected String message;

  protected AbstractCommonException() {
  }

  protected AbstractCommonException(String message) {
    super(message);
    this.message = message;
  }

  public AbstractCommonException(String message, Throwable cause) {
    super(message, cause);
    this.message = message;
  }

}
