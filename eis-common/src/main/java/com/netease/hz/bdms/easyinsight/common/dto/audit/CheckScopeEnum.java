package com.netease.hz.bdms.easyinsight.common.dto.audit;

/**
 * 参数校验范围
 */
public enum CheckScopeEnum {

    ALL(1),
    /**
     * 实时校验专用
     */
    REALTIME_ONLY(2),
    /**
     * 稽查专用
     */
    LOG_STATS_ONLY(3),
    ;

    CheckScopeEnum(Integer type) {
        this.type = type;
    }

    private Integer type;

    public static CheckScopeEnum fromType(Integer type) {
        for (CheckScopeEnum checkType : values()) {
            if (checkType.getType().equals(type)) {
                return checkType;
            }
        }
        return ALL;
    }

    public static CheckScopeEnum fromName(String type) {
        for (CheckScopeEnum checkType : values()) {
            if (checkType.name().equals(type)) {
                return checkType;
            }
        }
        return ALL;
    }

    public Integer getType() {
        return type;
    }
}
