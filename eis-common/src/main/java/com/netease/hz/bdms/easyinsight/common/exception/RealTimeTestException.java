package com.netease.hz.bdms.easyinsight.common.exception;

import com.netease.hz.bdms.easyinsight.common.constant.ResponseActionConstant;
import com.netease.hz.bdms.easyinsight.common.constant.ResponseCodeConstant;

public class RealTimeTestException extends AbstractCommonException {

    public RealTimeTestException(String message) {
        this.code = ResponseCodeConstant.REAL_TIME_TEST_ERROR;
        this.message = message;
        this.action = ResponseActionConstant.ALERT;
    }

    public RealTimeTestException(String message,Throwable cause) {
        super(message, cause);
        this.code = ResponseCodeConstant.REAL_TIME_TEST_ERROR;
        this.message = message;
        this.action = ResponseActionConstant.ALERT;
    }
}
