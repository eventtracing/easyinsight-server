package com.netease.hz.bdms.easyinsight.common.vo.task;

import lombok.Data;

@Data
public class TaskProcessSpmEntityVO {

    Long id;

    String spmByObjId;

    String oid;

    String objName;

    /**
     * @see com.netease.hz.bdms.easyinsight.common.enums.ObjTypeEnum
     */
    Integer objType;

    String reqType;

    String owner;

    String verifier;

    Integer status;

    Boolean hasPicture;

    Integer testRecordNum;

    Integer failedTestRecordNum;

    /**
     * 是否有合并基线冲突
     */
    private boolean mergeConflict = false;

}
