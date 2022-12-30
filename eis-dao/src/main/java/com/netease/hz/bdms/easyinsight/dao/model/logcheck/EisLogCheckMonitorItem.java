package com.netease.hz.bdms.easyinsight.dao.model.logcheck;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * eis_logcheck_monitor_item
 * 监控项表
 */
@Data
@Accessors(chain = true)
public class EisLogCheckMonitorItem {

    /**
     * 自增ID
     */
    private Long id;

    /**
     * appId
     */
    private Long appId;

    /**
     * 监控名
     */
    private String itemName;

    /**
     * 监控内容JSON
     */
    private String content;

    /**
     * 监控创建人
     */
    private String creator;

    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 修改时间
     */
    private Date updateTime;

}
