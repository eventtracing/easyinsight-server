package com.netease.hz.bdms.easyinsight.common.vo.requirement;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
 * 已发布上线任务
 *
 * @author: xumengqiang
 * @date: 2021/12/22 19:17
 */
@Data
@Accessors(chain = true)
public class ReleasedTaskVO {
    /**
     * 端版本
     */
    String terminalVersionName;

    /**
     * 发布ID
     */
    Long releaseId;

    /**
     * 发布者名称
     */
    String releaserName;

    /**
     * 发布时间
     */
    Date releaseTime;

    /**
     * 任务名称列表
     */
    List<String> taskNames;
}
