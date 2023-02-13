package com.netease.hz.bdms.easyinsight.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * @author yangyichun
 */

@Getter
@AllArgsConstructor
public enum ReferCheckResultEnum {
    NO_REFER(1, "归因缺失"),
    FORM_INVALID(2, "非法格式"),
    SPM_INVALID(3, "非法spm"),
    SPM_IRRATIONAL(4, "不合理spm"),
    PAGE_DEGRADE(5, "页面降级");

    private int type;
    private String desc;

    public static ReferCheckResultEnum from(long type) {
        return Arrays.stream(values())
                .filter(x -> x.getType() == type)
                .findAny()
                .orElse(null);
    }
}
