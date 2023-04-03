package com.netease.hz.bdms.easyinsight.common.vo.logcheck;

import com.netease.hz.bdms.easyinsight.common.enums.logcheck.LogCheckPackageTypeEum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Set;

@Accessors(chain = true)
@Data
public class RuleVersionVO {
    /**
     * 包build号
     */
    private String buildUUid;
    /**
     * 规则版本列表
     */
    private List<Long> ruleVersionIds;
}
