package com.netease.hz.bdms.eistest.service;

import com.netease.hz.bdms.easyinsight.common.dto.realtimetest.RealTimeTestResourceDTO;
import com.netease.hz.bdms.easyinsight.common.vo.logcheck.BranchCoverageDetailVO;
import com.netease.hz.bdms.easyinsight.service.service.audit.BuryPointRule;
import com.netease.hz.bdms.eistest.entity.BloodLinkQuery;

import java.util.List;
import java.util.Map;

public interface BloodLinkService {
    RealTimeTestResourceDTO getBuryPointResource(BloodLinkQuery query);

    List<BranchCoverageDetailVO> getBranchCoverageIgnoreList(String conversationId);

    Map<String, BuryPointRule> generateBuryPointRule(RealTimeTestResourceDTO realTimeTestResourceDTO);

    BuryPointRule generateEventRule(RealTimeTestResourceDTO realTimeTestResourceDTO);
}
