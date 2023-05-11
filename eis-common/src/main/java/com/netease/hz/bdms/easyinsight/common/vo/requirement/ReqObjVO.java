package com.netease.hz.bdms.easyinsight.common.vo.requirement;

import lombok.Data;

import java.util.Date;

@Data
public class ReqObjVO {

    Long objId;

    String oid;

    String objName;

    Integer objType;

    /**
     * 对象类型，1表示页面，2表示元素
     * @see com.netease.hz.bdms.easyinsight.common.enums.ObjTypeEnum
     */
    private String specialType;

    String terminals;

    Boolean hasPicture;

    Integer operatorType;

    Boolean editable;

    Date createTime;

    Date updateTime;

    String updateName;

    /**
     * 是否有合并基线冲突
     */
    Boolean mergeConflict;

}
