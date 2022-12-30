package com.netease.hz.bdms.easyinsight.common.dto.realtimetest;

import com.netease.hz.bdms.easyinsight.common.bo.lineage.NodeOfTestTree;
import com.netease.hz.bdms.easyinsight.common.vo.realtimetest.ReqNode;
import lombok.Data;

import java.util.List;

@Data
public class TestTreeVO {

    List<ReqNode> nodesOfReq;

    List<NodeOfTestTree> roots;

    String reqName;

    String taskName;

    Long reqPoolId;

}
