package com.netease.hz.bdms.easyinsight.common.vo.task;

import lombok.Data;

import java.util.List;

@Data
public class TaskProcessVO {

    String reqName;

    String taskName;

    TaskSpmTreeVO devSpmTree;

    TaskSpmTreeVO deleteSpmTree;

    List<TaskEventVO> events;

}
