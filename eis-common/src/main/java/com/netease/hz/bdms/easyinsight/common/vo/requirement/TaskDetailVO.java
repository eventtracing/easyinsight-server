package com.netease.hz.bdms.easyinsight.common.vo.requirement;

import com.netease.hz.bdms.easyinsight.common.enums.TaskSourceStatusEnum;
import com.netease.hz.bdms.easyinsight.common.enums.ReqSourceEnum;
import com.netease.hz.bdms.easyinsight.common.enums.VersionSourceStatusEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author: xumengqiang
 * @date: 2021/12/23 13:54
 */
@Data
@Accessors(chain = true)
public class TaskDetailVO {
    /**
     * 需求 issueKey
     */
    private String reqIssueKey;

    /**
     * 需求名称
     */
    private String reqName;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 发布人
     */
    private String releaserName;

    /**
     * 发布时间
     */
    private Date releaseTime;

    /**
     * 端版本名称
     */
    private String terminalVersionName;

    /**
     * 任务状态
     */
    private Integer taskStatus;

    /**
     * 负责人
     */
    private String ownerName;

    /**
     * 验证人
     */
    private String verifierName;

    /**
     * 需求来源 {@link ReqSourceEnum}
     */
    private Integer source;

    /**
     * 来源的创建时间
     */
    private Date sourceCreateTime;

    /**
     * 来源的状态 {@link VersionSourceStatusEnum}
     */
    private Integer sourceStatus;

    /**
     * 来源的状态的文字描述
     * 如果source是 x
     */
    private String sourceStatusDesc;

    /**
     * 计划发布时间
     */
    private String planReleaseTime;

    /**
     * 是否有合并基线冲突
     */
    private boolean mergeConflict = false;
}
