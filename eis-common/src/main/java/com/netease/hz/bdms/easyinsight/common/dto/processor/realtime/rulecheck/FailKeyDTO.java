package com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.rulecheck;

import com.netease.hz.bdms.easyinsight.common.enums.LogCheckResultEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 单条日志校验结果的格式化结构
 */
@Data
@Accessors(chain = true)
public class FailKeyDTO {

    /**
     * 前缀：如对象oid、"全局公参"
     */
    private String prefix;

    /**
     * 字段
     */
    private String key;

    /**
     * 失败类型
     */
    private LogCheckResultEnum type;

    public String toFailKey() {
        if (type == LogCheckResultEnum.PARAM_MISSING || type == LogCheckResultEnum.PARAM_VALUE_INVALID) {
            return prefix + "." + key + type.getDisplayName();
        }
        return type.getDisplayName();
    }
}
