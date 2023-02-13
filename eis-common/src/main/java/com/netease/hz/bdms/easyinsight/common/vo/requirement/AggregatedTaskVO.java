package com.netease.hz.bdms.easyinsight.common.vo.requirement;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
 * @author: xumengqiang
 * @date: 2021/12/23 17:24
 */
@Data
@Accessors(chain = true)
public class AggregatedTaskVO extends VersionBaseVO {
    /**
     * 端版本ID
     */
    private Long terminalVersionId;

    /**
     * 手动维护的buildVersion
     */
    private String buildVersion;

    /**
     * 端版本名称
     */
    private String terminalVersionName;

    /**
     * 端版本链接
     */
    private String terminalVersionLink;

    /**
     * 端版本号
     */
    private String terminalVersionNum;

    /**
     * 端版本下的任务是否全部已上线
     */
    private Boolean deployed;

    /**
     * 端版本 创建人
     */
    private String createName;

    /**
     * 端版本 创建时间
     */
    private Date createTime;

    /**
     * 最近发布人
     */
    private String latestReleaserName;

    /**
     * 最近发布时间
     */
    private Date latestReleaseTime;

    /**
     * 当前端版本下的任务列表
     */
    private List<TaskDetailVO> tasks;

    /**
     * 是否有合并基线冲突
     */
    private boolean mergeConflict = false;
}
