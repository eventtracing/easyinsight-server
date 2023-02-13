package com.netease.hz.bdms.easyinsight.common.dto.require;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class ReqPoolCountsDTO {


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

    /**
     * 过期时间，单位毫秒
     */
    private long expire;

    /**
     * 需求池id
     */
    private long reqPoolId;
}
