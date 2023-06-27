package com.netease.hz.bdms.easyinsight.common.dto.diff;

import com.netease.hz.bdms.easyinsight.common.bo.diff.EventDiff;
import com.netease.hz.bdms.easyinsight.common.bo.diff.ParamDiff;
import com.netease.hz.bdms.easyinsight.common.bo.diff.RelationDiff;
import com.netease.hz.bdms.easyinsight.common.enums.ObjChangeTypeEnum;
import javafx.util.Pair;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Accessors(chain = true)
@Data
public class TrackerTerminalDiffDTO {

    private List<ObjChangeTypeEnum> changeTypeEnums;
    private Pair<Long, Long> pubParamDiffs;
    private Pair<String, String> terminalDiffs;
    private List<ParamDiff> paramDiffs;
    private Pair<String, String> eventDiffs;
    private RelationDiff relationDiff;
    //
    private Long newTrackerId;
    private Long preTrackerId;
    private Long terminalId;

}
