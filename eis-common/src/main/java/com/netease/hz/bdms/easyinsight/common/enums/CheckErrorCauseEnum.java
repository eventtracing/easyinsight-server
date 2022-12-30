package com.netease.hz.bdms.easyinsight.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CheckErrorCauseEnum {
    /**
     * 场景：日志中不包含规则中的参数
     */
    LOSS_REQUIRED_PARAM("该参数需要输出（若为空值，请输出空值）"),
    /**
     * 场景：规则中为非空参数，日志中参数值为空
     */
    NOVALUE_FOR_NOTEMPTY_PARAM("该参数值不能为空"),
    /**
     * 场景：规则与日志中的参数值不匹配
     */
    UNMATCHED_PARAM_VALUE("该参数值不匹配"),
    /**
     * 场景：日志中包含不存在的血缘链路节点信息
     */
    REDUNDANT_TRACKER_PARAM_IN_BLOODLINK("日志中包含多余的对象信息"),
    ;
    private String cause;


}
