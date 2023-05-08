package com.netease.hz.bdms.easyinsight.common.vo.requirement;

import com.netease.hz.bdms.easyinsight.common.param.event.EventObjRelation;
import lombok.Data;

import java.util.EventObject;
import java.util.List;

/**
 * 待开发事件埋点详情
 */
@Data
public class UnDevelopedEventVO {

    /**
     * 表`eis_req_pool_event`的主键ID
     */
    Long reqPoolEventId;

    /**
     * 事件埋点ID 表`eis_event_bury_point`的主键ID
     */
    Long eventBuryPointId;

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
     * 适用的对象类型， 格式为[1,2]
     * 其中的objType
     * @see com.netease.hz.bdms.easyinsight.common.enums.ObjTypeEnum
     */
    List<Integer> applicableObjTypes;

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

    /**
     * 关联对象
     */
    List<EventObjRelation> relations;

}
