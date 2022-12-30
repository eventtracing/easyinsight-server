package com.netease.hz.bdms.easyinsight.common.vo.requirement;

import lombok.Data;

import java.util.List;

@Data
public class ReqPoolPagingListVO {

    Long id;

    String name;

    String dataOwners;

    String creatorName;

    Long createTime;

    List<ReqInfoPagingListVO> requirements;

    /**
     * 是否有合并基线冲突
     */
    private boolean mergeConflict = false;

    /**
     * 对象数
     */
    private int objCount;

    /**
     * 任务数
     */
    private int taskCount;

    /**
     * 事件数
     */
    private int eventCount;

    /**
     * 已经指派数
     */
    private int assignedCount;

    /**
     * 全部指派数
     */
    private int totalAssignCount;
}
