package com.netease.hz.bdms.easyinsight.common.exception;

import com.netease.hz.bdms.easyinsight.common.constant.ResponseActionConstant;
import com.netease.hz.bdms.easyinsight.common.constant.ResponseCodeConstant;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AuthException extends AbstractCommonException {

    public AuthException(int action) {
        this.code = ResponseCodeConstant.NO_PERMISSION;
        this.action = action;
    }

    public AuthException(String message) {
        this.code = ResponseCodeConstant.NO_PERMISSION;
        this.message = message;
        this.action = ResponseActionConstant.ALERT;
    }

    public AuthException(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
        this.action = ResponseActionConstant.ALERT;
        this.code = ResponseCodeConstant.NO_PERMISSION;
    }
}
