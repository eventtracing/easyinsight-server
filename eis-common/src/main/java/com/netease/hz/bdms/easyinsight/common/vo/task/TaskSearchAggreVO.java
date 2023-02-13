package com.netease.hz.bdms.easyinsight.common.vo.task;

import com.netease.hz.bdms.easyinsight.common.vo.requirement.RequirementInfoVO;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class TaskSearchAggreVO {

    Set<String> reqKeys;

    Set<String> reqName;

    List<RequirementInfoVO> reqInfos;

    List<TerminalAggre> terminals;

    Set<String> termnialVersions;


    Set<String> sprints;

    @Data
    public static class TerminalAggre {
        Long id;
        String name;
    }

}
