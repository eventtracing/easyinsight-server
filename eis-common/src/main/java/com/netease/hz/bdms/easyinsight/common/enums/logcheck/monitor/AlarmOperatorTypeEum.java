package com.netease.hz.bdms.easyinsight.common.enums.logcheck.monitor;

/**
 * 操作符
 */
public enum AlarmOperatorTypeEum {

    LTE("<="),
    GTE(">="),
    LT("<"),
    GT(">"),
    EQUALS("=="),
    ;

    private String v;

    public String getV() {
        return v;
    }
    AlarmOperatorTypeEum(String v) {
        this.v = v;
    }

    public static AlarmOperatorTypeEum fromValue(String v) {
        for (AlarmOperatorTypeEum eum : values()) {
            if (eum.getV().equals(v)) {
                return eum;
            }
        }
        return null;
    }
}
