package com.netease.hz.bdms.easyinsight.common.vo.task;

import lombok.Data;

@Data
public class TaskPagingQueryVO {

    String reqIssueKey;

    String reqName;

    String taskName;

    String dataOwnerEmail;

    Long terminalId;

    String search;

    Integer status;

    String taskOwner;

    String taskVerifier;

    Integer processStatus;

    String processOwner;

    String processVerifier;

    String terminalVersion;

    String sprint;

    Integer currentPage;

    Integer pageSize;

    String orderBy;

    String orderRule;

}
