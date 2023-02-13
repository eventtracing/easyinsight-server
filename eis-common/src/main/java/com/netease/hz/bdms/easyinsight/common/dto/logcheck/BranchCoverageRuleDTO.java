package com.netease.hz.bdms.easyinsight.common.dto.logcheck;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.Set;

@Accessors(chain = true)
@Data
public class BranchCoverageRuleDTO {

    /**
     * 全部
     */
    private Set<BranchCoverageDTO> all;
    /**
     * 本次变更增量部分
     */
    private Set<BranchCoverageDTO> increment;

    /**
     * code到name的映射
     */
    private Map<String, String> paramCodeToParamNameMap;
    /**
     * oid到name的映射
     */
    private Map<String, String> oidToNameMap;
    /**
     * eventCode到name的映射
     */
    private Map<String, String> eventNameMap;
}
