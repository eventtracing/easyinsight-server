package com.netease.hz.bdms.easyinsight.common.enums.logcheck.monitor;

/**
 * 操作符
 */
public enum OperatorTypeEum {

    SUM("sum", "sumMerge"),
    COUNT_DISTINCT("count distinct", "uniq"),
    ;

    private String v;
    private String dbOperator;

    public String getV() {
        return v;
    }

    public String getDbOperator() {
        return dbOperator;
    }

    OperatorTypeEum(String v, String dbOperator) {
        this.v = v;
        this.dbOperator = dbOperator;
    }

    public static OperatorTypeEum fromValue(String v) {
        for (OperatorTypeEum eum : values()) {
            if (eum.getV().equals(v)) {
                return eum;
            }
        }
        return null;
    }
}
