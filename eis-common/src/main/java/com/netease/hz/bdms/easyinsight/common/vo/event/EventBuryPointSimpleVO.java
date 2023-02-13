package com.netease.hz.bdms.easyinsight.common.vo.event;

import lombok.Data;

import java.util.Date;

/**
 * 已上线事件埋点信息
 *
 * @author: xumengqiang
 * @date: 2022/1/18 14:22
 */
@Data
public class EventBuryPointSimpleVO {

    /**
     * 事件埋点ID
     */
    private Long eventBuryPointId;

    /**
     * 事件code
     */
    private String eventCode;

    /**
     * 事件名称
     */
    private String eventName;

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
