package com.netease.hz.bdms.easyinsight.common.bo.lineage;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LineageForest {

    List<TreeNode> roots = new ArrayList<>();

}
