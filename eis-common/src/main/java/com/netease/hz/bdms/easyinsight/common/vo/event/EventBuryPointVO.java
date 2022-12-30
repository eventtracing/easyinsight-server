package com.netease.hz.bdms.easyinsight.common.vo.event;

import com.netease.hz.bdms.easyinsight.common.dto.param.ParamDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.parambind.ParamBindItemDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.parambind.ParamBindSimpleDTO;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 已上线事件埋点详情
 *
 * @author: xumengqiang
 * @date: 2022/1/12 10:16
 */

@Data
public class EventBuryPointVO {
    /**
     * 终端ID
     */
    private Long terminalId;

    /**
     * 终端名称
     */
    private String terminalName;

    /**
     * 全局公参参数包ID
     */
    private Long pubParamPackageId;

    /**
     * 事件ID
     */
    private Long eventId;

    /**
     * 事件名称
     */
    private String eventName;

    /**
     * 事件code
     */
    private String eventCode;

    /**
     * 事件公参参数包ID
     */
    private Long eventParamPackageId;

    /**
     * 创建人
     */
    private String createName;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 最近更新人
     */
    private String updateName;

    /**
     * 最近更新时间
     */
    private Date updateTime;

}
