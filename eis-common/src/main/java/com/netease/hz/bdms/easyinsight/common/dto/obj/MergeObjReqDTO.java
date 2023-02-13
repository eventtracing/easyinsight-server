package com.netease.hz.bdms.easyinsight.common.dto.obj;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class MergeObjReqDTO {

    /**
     * 对象ID
     */
    private Long objId;

    /**
     * 对象所处需求池ID
     */
    private Long reqPoolId;

    /**
     * 需求池下对象historyId
     */
    private Long objHistoryIdOfReqPool;

    /**
     * 目标基线ID
     */
    private Long targetReleaseId;
}
