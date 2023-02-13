package com.netease.hz.bdms.easyinsight.common.bo.diff;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 同一对象的某个关联血缘对象在两个端版本的Diff
 */
@Data
@Accessors(chain = true)
public class RelationObjDiff {
    /**
     * 旧的埋点ID
     */
    private Long oldTrackerId;
    /**
     * 新的埋点ID
     */
    private Long newTrackerId;

    /**
     * 涉及的父对象ID
     */
    private Long parentObjId;
    /**
     * 涉及的父对象oid
     */
    private String parentOid;

    /**
     * 变更类型
     * @see com.netease.hz.bdms.easyinsight.common.enums.ChangeTypeEnum
     */
    private Integer changeType;

}
