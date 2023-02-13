package com.netease.hz.bdms.easyinsight.common.enums.logcheck;

public enum LogCheckFieldsEnum {

    EVENT_CODE("eventCode"),
    SPM("spm"),
    OID("oid"),
    PRIORITY("priority", "对象优先级"),
    TAG("tag", "对象标签"),
    CHECK_RESULT("checkResult", "规则校验结果"),
    CAUSE_CHECK_RESULT("causeCheckResult", "归因校验结果"),
    UID("uid", "uid"),
    LOG_COUNT("logCountSumState", "logCount"),
    BUCKET_DATE("bucketDate", "日期"),
    OS("os", "os"),
    FAIL_KEY("failKey", "校验失败分类Key"),
    BUILD_UUID("buildUUID", "buildUUID"),
    APP_VER("appver", "appver"),
    ;

    private String fieldName;
    private String displayName;

    public String getFieldName() {
        return fieldName;
    }

    public String getDisplayName() {
        return displayName;
    }

    LogCheckFieldsEnum(String fieldName) {
        this.fieldName = fieldName;
        this.displayName = fieldName;
    }

    LogCheckFieldsEnum(String fieldName, String displayName) {
        this.fieldName = fieldName;
        this.displayName = displayName;
    }

    public static LogCheckFieldsEnum fromFieldName(String fieldName) {
        for (LogCheckFieldsEnum eum : values()) {
            if (eum.getFieldName().equals(fieldName)) {
                return eum;
            }
        }
        return null;
    }

    public static LogCheckFieldsEnum fromDisplayName(String displayName) {
        for (LogCheckFieldsEnum eum : values()) {
            if (eum.getDisplayName().equals(displayName)) {
                return eum;
            }
        }
        return null;
    }
}
