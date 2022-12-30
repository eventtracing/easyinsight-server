package com.netease.hz.bdms.easyinsight.common.vo.requirement;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
public class TaskQueryVO {

    List<String> reqIssueKeys;

    String taskIssueKey;
    /**
     * 对象id或对象名称
     */
    String search;
    /**
     * 老埋点spm搜索
     */
    String oldSpm;

    Integer status;
    /**
     * 对象类型
     * @see com.netease.hz.bdms.easyinsight.common.enums.ObjTypeEnum
     */
    String objType;
    /**
     * 负责人
     */
    String owner;
    /**
     * 验证人
     */
    String verifier;
    /**
     * 关联版本
     */
    String version;

}
