package com.netease.hz.bdms.easyinsight.common.vo.logcheck;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;

/**
 * 参数取值
 */
@Accessors(chain = true)
@Data
public class ParamBindDetail {

    private String paramCode;
    private Set<String> paramValues;
}
