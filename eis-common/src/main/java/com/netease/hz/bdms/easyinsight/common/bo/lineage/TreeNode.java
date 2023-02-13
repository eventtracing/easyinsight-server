package com.netease.hz.bdms.easyinsight.common.bo.lineage;

import lombok.Data;

import java.util.List;

@Data
public class TreeNode {

    public TreeNode(Long objId){
        this.objId = objId;
    }

    Long objId;

    String spmByObjId;

    List<TreeNode> children;

}
