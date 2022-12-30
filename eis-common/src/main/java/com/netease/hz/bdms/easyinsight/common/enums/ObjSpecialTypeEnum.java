package com.netease.hz.bdms.easyinsight.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ObjSpecialTypeEnum {
    NORMAL("normal", "普通对象"),
    BRIDGE("bridge", "桥梁"),
    ;
    private String name;
    private String chineseName;

    public static ObjSpecialTypeEnum fromName(String name) {
        for (ObjSpecialTypeEnum aEnum : values()) {
            if (aEnum.getName().equals(name)) {
                return aEnum;
            }
        }
        return NORMAL;
    }
}
