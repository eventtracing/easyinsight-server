package com.netease.hz.bdms.easyinsight.common.enums;

/**
 * 冲突类型
 */
public enum ConflictStatusEnum {
    // 无冲突
    NON("non"),
    // 合并基线冲突
    MERGE_CONFLICT("merge_conflict"),
    RESOLVED("resolved"),
    ;

    ConflictStatusEnum(String status) {
        this.status = status;
    }

    private String status;

    public String getStatus() {
        return status;
    }

    public static ConflictStatusEnum fromStatus(String conflictStatus) {
        for (ConflictStatusEnum resultEnum : values()) {
            if (resultEnum.getStatus().equals(conflictStatus)) {
                return resultEnum;
            }
        }
        return NON;
    }
}
