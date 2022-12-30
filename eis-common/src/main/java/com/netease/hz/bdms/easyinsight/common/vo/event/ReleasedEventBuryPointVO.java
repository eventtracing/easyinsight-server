package com.netease.hz.bdms.easyinsight.common.vo.event;

import lombok.Data;

import java.util.Date;

/**
 * 事件埋点发布历史信息
 *
 * @author: xumengqiang
 * @date: 2022/1/18 10:31
 */
@Data
public class ReleasedEventBuryPointVO {

    /**
     * 事件埋点ID
     */
    private Long eventBuryPointId;

    /**
     * 发布ID
     */
    private Long terminalReleaseId;

    /**
     * 终端ID
     */
    private Long terminalId;

    /**
     * 终端名称
     */
    private String terminalName;

    /**
     * 端版本ID
     */
    private Long terminalVersionId;

    /**
     * 端版本名称
     */
    private String terminalVersionName;

    /**
     * 发布时间
     */
    private Date releaseTime;

    /**
     * 发布人
     */
    private String releaser;


}
