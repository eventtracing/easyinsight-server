package com.netease.hz.bdms.easyinsight.common.vo.requirement;

import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import lombok.Data;

@Data
public class TaskRelObjVO {

    private Long id;

    private Long taskId;

    private String taskIssuekey;

    private String name;

    private Long objId;

    private Long trackerId;

    private Long historyId;

    private String oid;

    private String objType;

    private String reqType;

    private UserSimpleDTO owner;

    private UserSimpleDTO verifier;

    private Integer status;

    private String procInstId;

    private Boolean consistency;

    private Integer checkHistorySum;

    private Boolean containsImg;
}
