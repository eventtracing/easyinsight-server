package com.netease.hz.bdms.easyinsight.common.vo.requirement;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class CancelAssignBatchVO {

    List<OidAssignVO> oidAssignVOS;

    /**
     * 是否改动同步到所有端（按spmByObjId匹配）
     */
    private boolean syncAllTerminal;

    private Long reqPoolId;
}
