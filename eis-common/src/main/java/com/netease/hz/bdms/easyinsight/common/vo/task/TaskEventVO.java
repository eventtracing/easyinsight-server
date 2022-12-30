package com.netease.hz.bdms.easyinsight.common.vo.task;

import lombok.Data;

@Data
public class TaskEventVO {

    Long id;

    Long eventBuryPointId;

    String eventCode;

    String eventName;

    String owner;

    Integer status;

    String verifier;

    Integer testRecordNum;

    Integer failedTestRecordNum;
}
