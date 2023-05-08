package com.netease.hz.bdms.easyinsight.common.dto.obj;

import lombok.Data;

/**
 * 事件埋点详情
 */
@Data
public class ObjectEventRelationDTO {

    /**
     * 表`eis_req_pool_event`的主键ID
     */
    Long reqPoolEventId;

    /**
     * 事件code
     */
    String eventCode;

    /**
     * 事件名称
     */
    String eventName;

    /**
     * 终端
     */
    String terminalName;

    /**
     * 需求名称
     */
    String reqName;

    /**
     * 任务名称
     */
    String taskName;

    /**
     * 任务id
     */
    Long taskId;

    /**
     * 任务状态
     */
    String status;

}
