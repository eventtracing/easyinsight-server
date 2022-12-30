package com.netease.hz.bdms.easyinsight.common.vo.requirement;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class AssignVO {

    /**
     * 需求池ID
     */
    private Long reqPoolId;

    /**
     * 同步指派多个端
     */
    private boolean syncAllTerminal = false;

    //待办列表
    List<AssignEntityVO> assignEntities;

    Set<Long> taskIds;

}
