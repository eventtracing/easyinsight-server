package com.netease.hz.bdms.easyinsight.common.dto.diff;

import com.netease.hz.bdms.easyinsight.common.bo.diff.EventDiff;
import com.netease.hz.bdms.easyinsight.common.bo.diff.ParamDiff;
import com.netease.hz.bdms.easyinsight.common.bo.diff.RelationDiff;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Accessors(chain = true)
@Data
public class TrackerDiffDTO {

    /**
     * 变更后的pubParamPackageId
     */
    private Long pubParamPackageId;
    private boolean pubParamPackageChange;
    private List<ParamDiff> paramDiffs;
    private List<EventDiff> eventDiffs;
    private RelationDiff relationDiff;

    // 合并diff后用的字段
    private boolean acceptBase = false;     // 直接使用baseLine的
    private boolean acceptReqPool = false;  // 直接使用需求池的
}
