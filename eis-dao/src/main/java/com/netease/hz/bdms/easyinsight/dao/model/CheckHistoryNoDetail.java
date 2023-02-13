package com.netease.hz.bdms.easyinsight.dao.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

@Data
@Accessors(chain = true)
public class CheckHistoryNoDetail {

    /**
     * 测试历史记录ID
     */
    private Long id;
    /**
     * 埋点ID
     */
    private Long trackerId;
    /**
     * 验证结果，1表示通过，2表示不通过
     *
     * @see com.netease.hz.bdms.easyinsight.common.enums.CheckResultEnum
     */
    private Integer checkResult;
    /**
     * spm
     */
    private String spm;
    /**
     * 事件类型Code
     */
    private String eventCode;
    /**
     * 事件类型名称
     */
    private String eventName;
    /**
     * 日志获取时间
     */
    private Timestamp logServerTime;
    /**
     * 测试类型，1表示实时测试，2表示需求测试
     *
     * @see com.netease.hz.bdms.easyinsight.common.enums.CheckTypeEnum
     */
    private Integer type;
    /**
     * 保存人邮箱
     */
    private String saverEmail;
    /**
     * 保存人名称
     */
    private String saverName;
    /**
     * 保存时间
     */
    private Timestamp saveTime;
    /**
     * 更新时间
     */
    private Timestamp updateTime;

    public CheckHistoryNoDetail(CheckHistory checkHistory) {
        this.id = checkHistory.getId();
        this.trackerId = checkHistory.getTrackerId();
        this.checkResult = checkHistory.getCheckResult();
        this.spm = checkHistory.getSpm();
        this.eventCode = checkHistory.getEventCode();
        this.eventName = checkHistory.getEventName();
        this.logServerTime = checkHistory.getLogServerTime();
        this.type = checkHistory.getType();
        this.saverEmail = checkHistory.getSaverEmail();
        this.saverName = checkHistory.getSaverName();
        this.saveTime = checkHistory.getSaveTime();
        this.updateTime = checkHistory.getUpdateTime();
    }
}
