package com.netease.hz.bdms.easyinsight.common.query;

import lombok.Data;

@Data
public class ReqPoolPageQuery {

    private Long appId;

    /**
     * reqPoolId
     */
    private Long id;

    private String dataOwner;

}
