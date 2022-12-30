package com.netease.hz.bdms.easyinsight.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ReqPoolTypeEnum {

    /**
     * 待开发spm池
     */
    SPM_DEV(1),
    /**
     * 待删除smp池
     */
    SPM_DELETE(2),
    /**
     * 事件池
     */
    EVENT(3);

    private Integer reqPoolType;

    public static ReqPoolTypeEnum valueOfType(Integer type) {
        for (ReqPoolTypeEnum value : values()) {
            if (value.getReqPoolType().equals(type)) {
                return value;
            }
        }
        return null;
    }

}
