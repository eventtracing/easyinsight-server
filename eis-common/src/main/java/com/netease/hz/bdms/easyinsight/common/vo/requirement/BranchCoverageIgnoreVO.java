package com.netease.hz.bdms.easyinsight.common.vo.requirement;

import com.netease.hz.bdms.easyinsight.common.vo.logcheck.BranchCoverageDetailVO;
import lombok.Data;

import java.util.List;

@Data
public class BranchCoverageIgnoreVO {

    private String conversationId;

    private List<BranchCoverageDetailVO> branches;

}
