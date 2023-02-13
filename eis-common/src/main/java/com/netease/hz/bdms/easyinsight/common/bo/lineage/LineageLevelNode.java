package com.netease.hz.bdms.easyinsight.common.bo.lineage;

import lombok.Data;

import java.util.List;

/**
 * 血缘层级关系
 */
@Data
public class LineageLevelNode {

    Long trackerId;

    Long objId;

    Long historyId;

    String oid;

    String objName;

    String spm;

    /**
     * 类别
     *
     * @see com.netease.hz.bdms.easyinsight.common.enums.ObjTypeEnum
     */
    private Integer type;

    List<LineageLevelNode> children;

}
