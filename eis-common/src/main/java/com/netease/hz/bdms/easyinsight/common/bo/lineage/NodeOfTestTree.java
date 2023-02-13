package com.netease.hz.bdms.easyinsight.common.bo.lineage;

import lombok.Data;

import java.util.List;

@Data
public class NodeOfTestTree {

    Long objId;

    String oid;

    String objName;

    Integer objType;

    String spm;

    List<NodeOfTestTree> children;

}
