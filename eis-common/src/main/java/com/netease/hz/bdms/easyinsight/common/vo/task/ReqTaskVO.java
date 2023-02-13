package com.netease.hz.bdms.easyinsight.common.vo.task;

import lombok.Data;

@Data
public class ReqTaskVO {

    Long id;

    Long reqPoolId;

    String reqIssueKey;

    String taskIssueKey;

    String reqName;

    String taskName;

    Long terminalId;

    String terminal;

    String terminalVersion;

    String terminalVersionLink;

    Integer status;

    String owner;

    String verifier;

    String dataOnwers;

    String sprint;

    TestRecordResultVO testResult;

    /**
     * 是否有基线合并冲突
     */
    private boolean mergeConflict;

    private TaskProcessVO taskProcess;
}
