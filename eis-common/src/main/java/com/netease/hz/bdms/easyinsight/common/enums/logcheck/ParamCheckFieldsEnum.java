package com.netease.hz.bdms.easyinsight.common.enums.logcheck;

public enum ParamCheckFieldsEnum {

    EVENT_CODE("eventCode"),
    SPM("spm"),
    OID("oid"),
    PRIORITY("priority", "对象优先级"),
    TAG("tag", "对象标签"),
    PARAM_CODE("paramCode"),
    PARAM_VALUE("paramValue"),
    ;

    private String fieldName;
    private String displayName;

    public String getFieldName() {
        return fieldName;
    }

    public String getDisplayName() {
        return displayName;
    }

    ParamCheckFieldsEnum(String fieldName) {
        this.fieldName = fieldName;
        this.displayName = fieldName;
    }

    ParamCheckFieldsEnum(String fieldName, String displayName) {
        this.fieldName = fieldName;
        this.displayName = displayName;
    }

    public static ParamCheckFieldsEnum fromFieldName(String fieldName) {
        for (ParamCheckFieldsEnum eum : values()) {
            if (eum.getFieldName().equals(fieldName)) {
                return eum;
            }
        }
        return null;
    }
}
