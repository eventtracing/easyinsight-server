package com.netease.hz.bdms.eistest.entity;


import com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.oldversion.OldVersionLogSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.rulecheck.RuleCheckSimpleDTO;
import com.netease.hz.bdms.eistest.entity.BuryPointErrorContentExpand;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuryPointLogRuleCheckDto {
    private long index;
    private int type;
    private RuleCheckSimpleDTO ruleCheck;
    private OldVersionLogSimpleDTO oldVersionLog;
    private BuryPointErrorContentExpand exceptionStatisticsResult;
}
