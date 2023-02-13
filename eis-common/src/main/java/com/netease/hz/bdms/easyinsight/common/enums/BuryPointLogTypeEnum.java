package com.netease.hz.bdms.easyinsight.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * @author yangyichun
 */

@Getter
@AllArgsConstructor
public enum BuryPointLogTypeEnum {
    INSIGHT(1, "曙光日志"),
    OLDVERSION(2, "老版本日志"),
    EXCEPTION(3, "错误日志"),
    UNDEFINED(4, "未定义日志");

    private int code;
    private String desc;

    public static BuryPointLogTypeEnum from(int code) {
        return Arrays.stream(values())
                .filter(x -> x.getCode() == code)
                .findAny()
                .orElse(null);
    }
}
