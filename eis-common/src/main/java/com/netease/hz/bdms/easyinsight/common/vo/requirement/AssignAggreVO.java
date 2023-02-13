package com.netease.hz.bdms.easyinsight.common.vo.requirement;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class AssignAggreVO {

    Long terminalId;

    String terminalName;

    List<AssignTargetTask> targetTasks;

    @Data
    public static class AssignTargetTask{
        Long id;
        String taskName;
    }

}
