package com.netease.hz.bdms.easyinsight.common.vo.requirement;

import lombok.Data;

@Data
public class BaseObjEntityVO {
    //对象id
    Long objId;
    // 改对象属于父空间
    Long otherAppId;
    //对象变更历史id
    Long historyId;
    //对象oid
    String oid;
    //对象名称
    String objName;

    /**
     * 对象类型
     * @see com.netease.hz.bdms.easyinsight.common.enums.ObjTypeEnum
     */
    Integer objType;

    /**
     * 是否有合并基线冲突
     */
    private boolean mergeConflict = false;

}
