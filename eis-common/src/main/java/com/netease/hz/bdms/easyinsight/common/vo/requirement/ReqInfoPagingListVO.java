package com.netease.hz.bdms.easyinsight.common.vo.requirement;

import lombok.Data;

@Data
public class ReqInfoPagingListVO {

    Long id;

    String issueKey;

    String name;

    String from;

    Integer omState;

    String priority;

    String views;

    String businessArea;

    String team;

    String creatorName;

    Long createTime;

    String url;

    /**
     * 是否有合并基线冲突
     */
    private boolean mergeConflict = false;

}
