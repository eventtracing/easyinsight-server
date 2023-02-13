package com.netease.hz.bdms.easyinsight.common.vo.requirement;

import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import lombok.Data;

import java.util.List;

@Data
public class TaskRelObjTreeLevelVO {

    private Long id;

    private Long taskId;

    private String taskIssueKey;

    private String objName;

    private Long objId;

    private Long trackerId;

    private Long historyId;

    private String spm;

    private String oid;

    private String objType;

    /**
     * 需求类型
     */
    private String reqType;

    private UserSimpleDTO owner;

    private UserSimpleDTO verifier;

    private Integer status;
    /**
     * 当前spm关联对象下所有spm的最高状态
     */
    private Integer relObjMaxStatus;

    private String procInstId;

    private Boolean consistency;

    private Integer checkHistorySum;

    private Boolean containsImg;

    private Boolean canExpandSubTree;

    private List<TaskRelObjTreeLevelVO> childs;

}
