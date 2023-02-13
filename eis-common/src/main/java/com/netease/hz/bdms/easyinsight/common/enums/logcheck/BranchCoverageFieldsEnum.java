package com.netease.hz.bdms.easyinsight.common.enums.logcheck;

public enum BranchCoverageFieldsEnum {

    OID("oid"),
    EVENT_CODE("eventCode"),
    SPM("spm"),
    PRIORITY("priority", "对象优先级"),
    TAG("tag", "对象标签"),
    PARAM_CODE("paramCode", "paramCode"),
    PARAM_VALUE("paramValue", "paramValue"),
    BUCKET_DATE("bucketDate", "日期"),
    LOG_COUNT("logCountSumState", "logCount"),
    ;

    private String fieldName;
    private String displayName;

    public String getFieldName() {
        return fieldName;
    }

    public String getDisplayName() {
        return displayName;
    }

    BranchCoverageFieldsEnum(String fieldName) {
        this.fieldName = fieldName;
        this.displayName = fieldName;
    }

    BranchCoverageFieldsEnum(String fieldName, String displayName) {
        this.fieldName = fieldName;
        this.displayName = displayName;
    }

    public static BranchCoverageFieldsEnum fromFieldName(String fieldName) {
        for (BranchCoverageFieldsEnum eum : values()) {
            if (eum.getFieldName().equals(fieldName)) {
                return eum;
            }
        }
        return null;
    }

    public static BranchCoverageFieldsEnum fromDisplayName(String displayName) {
        for (BranchCoverageFieldsEnum eum : values()) {
            if (eum.getDisplayName().equals(displayName)) {
                return eum;
            }
        }
        return null;
    }
}
