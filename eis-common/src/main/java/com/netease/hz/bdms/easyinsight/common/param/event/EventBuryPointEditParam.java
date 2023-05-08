package com.netease.hz.bdms.easyinsight.common.param.event;

import com.netease.hz.bdms.easyinsight.common.param.obj.server.ServerApiInfo;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author: xumengqiang
 * @date: 2022/1/21 14:40
 */
@Data
public class EventBuryPointEditParam {
    /**
     * 事件埋点ID
     */
    @NotNull(message = "事件埋点ID不能为空")
    private Long eventBuryPointId;
    /**
     * 事件池事件ID
     */
    private Long reqPoolEventId;
    /**
     * 事件ID
     */
    private Long eventId;

    /**
     * 事件公参参数包版本
     */
    @NotNull(message = "事件公参参数包ID不能为空")
    private Long eventParamPackageId;

    /**
     * 终端ID
     */
    private Long terminalId;

    /**
     * 全局公参参数包版本
     */
    @NotNull(message = "全局公参参数包ID不能为空")
    private Long pubParamPackageId;


    /**
     * 事件埋点对象关联关系（terminalId -> objId）
     */
    private List<EventObjRelation> relationList;

    /**
     * 事件埋点关联的api信息
     */
    private List<ServerApiInfo> apiInfos;
}
