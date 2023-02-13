package com.netease.hz.bdms.easyinsight.common.enums;

public enum DiffTypeEnum {

    /**
     * 新增
     */
    NEW("new"),
    /**
     * 修改
     */
    MOD("mod"),
    /**
     * 删除
     */
    DEL("del")
    /**
     * 未改变
     */
    ,
    NON("non"),
    ;

    private String fieldName;

    public String getFieldName() {
        return fieldName;
    }

    DiffTypeEnum(String fieldName) {
        this.fieldName = fieldName;
    }

    public static DiffTypeEnum fromFieldName(String fieldName) {
        for (DiffTypeEnum eum : values()) {
            if (eum.getFieldName().equals(fieldName)) {
                return eum;
            }
        }
        return null;
    }
}
