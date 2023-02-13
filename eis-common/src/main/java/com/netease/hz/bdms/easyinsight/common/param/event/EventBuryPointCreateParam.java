package com.netease.hz.bdms.easyinsight.common.param.event;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * 事件埋点池——新建事件埋点时的传入参数
 *
 * @author: xumengqiang
 * @date: 2021/12/24 11:10
 */
@Data
@Accessors(chain = true)
public class EventBuryPointCreateParam {
    /**
     * 需求组ID
     */
    @NotNull(message = "需求组ID不能为空")
    private Long reqPoolId;

    /**
     * 事件ID
     */
    @NotNull(message = "事件ID不能为空")
    private Long eventId;

    /**
     * 事件参数包ID
     */
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
}
