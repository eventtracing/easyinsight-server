package com.netease.hz.bdms.easyinsight.common.exception;

import com.netease.hz.bdms.easyinsight.common.constant.ResponseActionConstant;
import com.netease.hz.bdms.easyinsight.common.constant.ResponseCodeConstant;

public class CommonException extends AbstractCommonException {

    public CommonException(String message) {
        this.message = message;
        this.action = ResponseActionConstant.ALERT;
    }


}
