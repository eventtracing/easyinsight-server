package com.netease.hz.bdms.easyinsight.common.enums;

import com.netease.hz.bdms.easyinsight.common.exception.ServerException;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
public enum LogCheckResultEnum {

    /**
     * 校验成功
     */
    OK(0, "校验成功", CheckResultEnum.PASS),    // 1

    /**
     * SPM未匹配
     */
    SPM_NOT_MATCH(1, "SPM未匹配", CheckResultEnum.NO_MATCH_SPM),   // 3

    /**
     * 参数缺失
     */
    PARAM_MISSING(2, "参数缺失", CheckResultEnum.NOT_PASS), // 2

    /**
     * 参数取值错误
     */
    PARAM_VALUE_INVALID(3, "参数取值错误", CheckResultEnum.NOT_PASS), // 2
    ;

    LogCheckResultEnum(Integer result, String displayName, CheckResultEnum checkResultEnum) {
        this.result = result;
        this.displayName = displayName;
        this.checkResultEnum = checkResultEnum;
    }

    private Integer result;

    private String displayName;

    private CheckResultEnum checkResultEnum;

    public static LogCheckResultEnum fromResult(Integer result) {
        for (LogCheckResultEnum resultEnum : values()) {
            if (resultEnum.getResult().equals(result)) {
                return resultEnum;
            }
        }
        throw new ServerException(result + "不能转换为CheckResultEnum");
    }
}
