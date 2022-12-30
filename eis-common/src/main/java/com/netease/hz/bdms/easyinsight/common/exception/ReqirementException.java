package com.netease.hz.bdms.easyinsight.common.exception;

import com.netease.hz.bdms.easyinsight.common.constant.ResponseActionConstant;
import com.netease.hz.bdms.easyinsight.common.constant.ResponseCodeConstant;

public class ReqirementException extends AbstractCommonException {

    public ReqirementException(String message) {
        this.code = ResponseCodeConstant.REQUIRE_ERROR;
        this.message = message;
        this.action = ResponseActionConstant.ALERT;
    }

    public ReqirementException(String message,Throwable cause) {
        super(message, cause);
        this.code = ResponseCodeConstant.REQUIRE_ERROR;
        this.message = message;
        this.action = ResponseActionConstant.ALERT;
    }
}
