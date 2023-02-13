package com.netease.hz.bdms.easyinsight.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BranchCoverageParamTypeEnum {

    /**
     * 无参数
     */
    NON(0),
    /**
     * 对象参数
     */
    OBJECT_PARAM(1),
    /**
     * 事件参数
     */
    EVENT_PARAM(2),
    ;
    private Integer type;

    public static BranchCoverageParamTypeEnum fromType(Integer type) {
        for (BranchCoverageParamTypeEnum paramTypeEnum : values()) {
            if (paramTypeEnum.getType().equals(type)) {
                return paramTypeEnum;
            }
        }
        return NON;
    }
}
