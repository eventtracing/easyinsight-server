package com.netease.hz.bdms.easyinsight.dao.model.logcheck;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * eis_logcheck_user_monitor_item
 * 用户和监控项关系表
 */
@Data
@Accessors(chain = true)
public class EisLogCheckUserMonitorItem {

    /**
     * 自增ID
     */
    private Long id;

    /**
     * appId
     */
    private Long appId;

    /**
     * 用户
     */
    private String userEmail;

    /**
     * 监控ID
     */
    private Long monitorItemId;

    /**
     * 类型
     */
    private String type;

    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 修改时间
     */
    private Date updateTime;

}
