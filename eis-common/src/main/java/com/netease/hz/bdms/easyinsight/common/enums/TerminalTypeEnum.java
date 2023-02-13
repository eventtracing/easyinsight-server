package com.netease.hz.bdms.easyinsight.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TerminalTypeEnum {

    /**
     * 手机端，iPhone / android，有双端同步需求
     */
    APP("app"),
    /**
     * 非手机端
     */
    NON_APP("non_app"),
    ;

    private final String type;

    public static TerminalTypeEnum of(String terminalName) {
        if ("iphone".equalsIgnoreCase(terminalName)) {
            return APP;
        }
        if ("android".equalsIgnoreCase(terminalName)) {
            return APP;
        }
        return NON_APP;
    }

}
