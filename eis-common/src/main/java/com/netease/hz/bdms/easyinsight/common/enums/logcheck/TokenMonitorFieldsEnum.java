package com.netease.hz.bdms.easyinsight.common.enums.logcheck;

public enum TokenMonitorFieldsEnum {

    deviceIdMatch("deviceIdMatch", "deviceId一致性"),
    osMatch("osMatch", "os一致性"),
    tokenSignedDays("tokenSignedDays", "token已签发天数"),
    validType("validType", "token有效性"),
    preKickCause("preKickCause", "踢出预演类型"),
    tokenAccountTokenVersion("tokenAccountTokenVersion", "tokenAccountTokenVersion"),
    tokenProductName("tokenProductName", "tokenProductName"),
    tokenOs("tokenOs", "tokenOs"),
    tokenLoginType("tokenLoginType", "tokenLoginType"),
    tokenTokenType("tokenTokenType", "tokenTokenType"),
    bucketHour("bucketHour", "小时"),
    bucketDate("bucketDate", "日期"),
    uriKey("uriKey", "uriKey"),
    appver("appver", "appver"),
    bizCode("bizCode", "bizCode"),
    sdkVersion("sdkVersion", "sdkVersion"),
    userType("userType", "userType"),
    buildver("buildver", "buildver"),
    os("os", "os"),
    osver("osver", "osver"),
    reqCountSumState("reqCountSumState", "logCount"),
    ;

    private String fieldName;
    private String displayName;

    public String getFieldName() {
        return fieldName;
    }

    public String getDisplayName() {
        return displayName;
    }

    TokenMonitorFieldsEnum(String fieldName) {
        this.fieldName = fieldName;
        this.displayName = fieldName;
    }

    TokenMonitorFieldsEnum(String fieldName, String displayName) {
        this.fieldName = fieldName;
        this.displayName = displayName;
    }

    public static TokenMonitorFieldsEnum fromFieldName(String fieldName) {
        for (TokenMonitorFieldsEnum eum : values()) {
            if (eum.getFieldName().equals(fieldName)) {
                return eum;
            }
        }
        return null;
    }

    public static TokenMonitorFieldsEnum fromDisplayName(String displayName) {
        for (TokenMonitorFieldsEnum eum : values()) {
            if (eum.getDisplayName().equals(displayName)) {
                return eum;
            }
        }
        return null;
    }
}
