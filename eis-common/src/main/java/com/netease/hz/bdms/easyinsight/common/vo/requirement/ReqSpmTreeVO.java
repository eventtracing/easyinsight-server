package com.netease.hz.bdms.easyinsight.common.vo.requirement;

import com.netease.hz.bdms.easyinsight.common.bo.lineage.TreeNode;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * spm待指派项视图
 */
@Data
public class ReqSpmTreeVO {
    //终端id
    Long terminalId;
    //终端名称
    String terminalName;
    //血缘树层级结构
    List<TreeNode> roots = new ArrayList<>();
    //血缘树上每个节点的具体信息——需求对象池涉及的对象
    List<ReqSpmEntityVO> reqDevSpmEntities = new ArrayList<>();
    //血缘树上每个节点的具体信息——需求对象池不涉及的对象
    List<BaseObjEntityVO> baseObjEntities = new ArrayList<>();
    // 树展开到此SPM
    List<String> spmsToExpand;
}
