package com.netease.hz.bdms.easyinsight.common.enums;

/**
 * 时间粒度
 */
public enum TimeGranularityEnum {

    HOUR(3600000L, 1),
    DAY(86400000L, 24),
    ;

    long millis;
    long hours;

    TimeGranularityEnum(long millis, long hours) {
        this.millis = millis;
        this.hours = hours;
    }

    public long getMillis() {
        return millis;
    }

    public void setMillis(long millis) {
        this.millis = millis;
    }

    public long getHours() {
        return hours;
    }

    public void setHours(long hours) {
        this.hours = hours;
    }
}
