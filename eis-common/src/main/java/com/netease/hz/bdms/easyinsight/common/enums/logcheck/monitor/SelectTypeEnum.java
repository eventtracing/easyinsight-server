package com.netease.hz.bdms.easyinsight.common.enums.logcheck.monitor;

public enum SelectTypeEnum {

    SINGLE("SINGLE"),
    MULTI("MULTI"),
    ;

    private String v;

    public String getV() {
        return v;
    }

    SelectTypeEnum(String v) {
        this.v = v;
    }

    public static SelectTypeEnum fromValue(String v) {
        for (SelectTypeEnum eum : values()) {
            if (eum.getV().equals(v)) {
                return eum;
            }
        }
        return null;
    }
}
