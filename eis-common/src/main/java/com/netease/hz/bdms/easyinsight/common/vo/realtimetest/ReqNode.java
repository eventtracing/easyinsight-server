package com.netease.hz.bdms.easyinsight.common.vo.realtimetest;

import lombok.Data;

import java.util.List;

@Data
public class ReqNode {

    Long objId;

    String spm;

    /**
     * @see com.netease.hz.bdms.easyinsight.common.enums.RequirementTypeEnum
     */
    String reqType;
}
