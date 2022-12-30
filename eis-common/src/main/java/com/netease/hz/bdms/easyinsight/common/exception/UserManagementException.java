package com.netease.hz.bdms.easyinsight.common.exception;

import com.netease.hz.bdms.easyinsight.common.constant.ResponseActionConstant;
import com.netease.hz.bdms.easyinsight.common.constant.ResponseCodeConstant;

public class UserManagementException extends AbstractCommonException{
    public UserManagementException() {
        this(ResponseActionConstant.ALERT);
    }

    public UserManagementException(int action) {
        this.code = ResponseCodeConstant.USER_MANAGEMENT_ERROR;
        this.action = action;
    }

    public UserManagementException(String message) {
        this.code = ResponseCodeConstant.USER_MANAGEMENT_ERROR;
        this.action = ResponseActionConstant.ALERT;
        this.message = message;
    }

    public UserManagementException(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public UserManagementException(String message, Throwable cause) {
        super(message, cause);
        this.action = ResponseActionConstant.ALERT;
        this.code = ResponseCodeConstant.USER_MANAGEMENT_ERROR;
    }
}
