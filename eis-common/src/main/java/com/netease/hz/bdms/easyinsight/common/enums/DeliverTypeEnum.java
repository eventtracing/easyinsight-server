package com.netease.hz.bdms.easyinsight.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 转交人类型
 */
@AllArgsConstructor
@Getter
public enum DeliverTypeEnum {
    //负责人
    OWNER,
    //验证人
    VERIFIER;
}
