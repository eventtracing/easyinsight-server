package com.netease.hz.bdms.easyinsight.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * @author yangyichun
 */

@Getter
@AllArgsConstructor
public enum ClientTypeEnum {
    ANDROID(10, "android"),
    IOS(11, "iphone"),
    WEB(12, "h5");

    private long type;
    private String code;

    public static ClientTypeEnum from(long type) {
        return Arrays.stream(values())
                .filter(x -> x.getType() == type)
                .findAny()
                .orElse(null);
    }

    public static ClientTypeEnum getByCode(String code) {
        return Arrays.stream(values())
                .filter(x -> x.getCode().equals(code))
                .findAny()
                .orElse(null);
    }
}
