package com.netease.hz.bdms.easyinsight.dao.model.logcheck;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * eis_logcheck_alarm_rule
 * 报警规则表
 */
@Data
@Accessors(chain = true)
public class EisLogCheckAlarmRule {

    /**
     * 自增ID
     */
    private Long id;

    /**
     * appId
     */
    private Long appId;

    /**
     * 监控ID
     */
    private Long monitorItemId;

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
