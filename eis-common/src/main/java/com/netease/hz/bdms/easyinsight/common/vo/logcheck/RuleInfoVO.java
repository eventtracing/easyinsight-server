package com.netease.hz.bdms.easyinsight.common.vo.logcheck;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Accessors(chain = true)
@Data
public class RuleInfoVO {

    /**
     * 基线版本名
     */
    private String baseLineVersionName;
    /**
     * 和哪些相关任务的合并结果
     */
    private String relatedTasks;
    /**
     * 合并失败的任务列表
     */
    private List<String> mergeFailedTasks;
}
