package com.netease.hz.bdms.easyinsight.common.vo.requirement;

import lombok.Data;

@Data
public class ReqSpmEntityVO {
    //spm待办项id
    Long id;
    //需求组id
    Long reqPoolId;

//    Long objId;
    //对象变更历史id
    Long historyId;
    //对象绑定参数实体id
    Long trackerId;
    //对象oid
    String oid;
    //对象名称
    String objName;
    //对象id组成的spm
    String spmByObjId;
    /**
     * @see com.netease.hz.bdms.easyinsight.common.enums.ObjTypeEnum
     */
    Integer objType;

    /**
     * @see com.netease.hz.bdms.easyinsight.common.enums.RequirementTypeEnum
     */
    String reqType;
    //是否包含图片
    Boolean hasPicture;
    //需求名称
    String reqName;
    //任务名称
    String taskName;
    //任务名称
    Long taskId;
    //流程状态
    Integer status;
    /**
     * 存在基线合并冲突
     */
    private boolean mergeConflict = false;

}
