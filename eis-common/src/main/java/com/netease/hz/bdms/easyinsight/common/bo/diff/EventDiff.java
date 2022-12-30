package com.netease.hz.bdms.easyinsight.common.bo.diff;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 同一对象的某个事件在两个端版本的Diff
 */
@Data
@Accessors(chain = true)
public class EventDiff {
    /**
     * 旧的埋点ID
     */
    private Long oldTrackerId;
    /**
     * 新的埋点ID
     */
    private Long newTrackerId;
    /**
     * 事件类型ID
     */
    private Long eventId;
    /**
     * 事件类型的公参包版本
     * 若是新增，则此字段无效，为默认值0
     * 若是删除，则此字段有效，为oldTrackerId对应的eventId的参数包版本
     * 若是修改（视为先删再增），则此字段有效，为oldTrackerId对应的eventId的参数包版本
     * 若是不变，则此字段有效，为oldTrackerId对应的eventId的参数包版本
     */
    private Long oldEventParamVersionId;
    /**
     * 事件类型的公参包版本
     * 若是新增，则此字段有效，为newTrackerId对应的eventId的参数包版本
     * 若是删除，则此字段无效，为默认值0
     * 若是修改（视为先删再增），则此字段有效，为newTrackerId对应的eventId的参数包版本
     * 若是不变，则此字段有效，为newTrackerId对应的eventId的参数包版本
     */
    private Long newEventParamVersionId;

    /**
     * 变更类型
     * @see com.netease.hz.bdms.easyinsight.common.enums.ChangeTypeEnum
     */
    private Integer changeType;

}
