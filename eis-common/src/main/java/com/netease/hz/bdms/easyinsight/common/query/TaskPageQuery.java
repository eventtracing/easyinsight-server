package com.netease.hz.bdms.easyinsight.common.query;

import lombok.Data;

import java.util.Collection;

@Data
public class TaskPageQuery {

    Long terminalId;

    String terminalVersion;

    String taskName;

    Integer status;

    String ownerEmail;

    String verifierEmail;

    String iteration;

    String orderBy;

    String orderRule;

    Collection<Long> ids;

    Collection<Long> excludeIds;

    Collection<Long> reqIds;

    Long appId;

    Integer currentPage;

    Integer pageSize;

}
