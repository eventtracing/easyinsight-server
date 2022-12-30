package com.netease.hz.bdms.easyinsight.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * @author yangyichun
 */

@Getter
@AllArgsConstructor
public enum PackageTypeEnum {
    RELEASE(1, "release","全量包"),
    GRAY(2, "gray","灰度包"),
    BETA(2, "beta","灰度包"),
    DEV(3, "dev","开发包"),
    DEBUG(4, "debug","测试包");

    private long type;
    private String code;
    private String desc;

    public static PackageTypeEnum from(long type) {
        return Arrays.stream(values())
                .filter(x -> x.getType() == type)
                .findAny()
                .orElse(null);
    }

    public static PackageTypeEnum getByCode(String code) {
        return Arrays.stream(values())
                .filter(x -> x.getCode().equals(code))
                .findAny()
                .orElse(null);
    }
}
