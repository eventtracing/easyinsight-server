package com.netease.hz.bdms.easyinsight.common.enums.logcheck.monitor;

/**
 * 操作符
 */
public enum ConditionTypeEum {

    FIXED("固定阈值", 0, ""),
    COMPARE_TO_YESTERDAY("同比昨日", 1, "说明：计算方式为当日值/昨日值，若相等则比值为1"),
    COMPARE_TO_LAST_WEEK("环比上周", 7, "说明：计算方式为当日值/7天前值，若相等则则比值为1"),
    ;

    private String v;
    private int compareDayOffset;
    private String desc;

    public String getV() {
        return v;
    }

    public String getDesc() {
        return desc;
    }

    public int getCompareDayOffset() {
        return compareDayOffset;
    }

    ConditionTypeEum(String v, int compareDayOffset, String desc) {
        this.v = v;
        this.compareDayOffset = compareDayOffset;
        this.desc = desc;
    }

    public static ConditionTypeEum fromValue(String v) {
        for (ConditionTypeEum eum : values()) {
            if (eum.getV().equals(v)) {
                return eum;
            }
        }
        return null;
    }
}
