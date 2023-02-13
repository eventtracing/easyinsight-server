package com.netease.hz.bdms.easyinsight.common.bo.diff;

import java.util.Set;

import com.netease.hz.bdms.easyinsight.common.dto.param.parambind.ParamBindSimpleDTO;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 同一对象的某个参数在两个端版本的Diff
 */
@Data
@Accessors(chain = true)
public class ParamDiff {
    /**
     * 旧的埋点ID
     */
    private Long oldTrackerId;
    /**
     * 新的埋点ID
     */
    private Long newTrackerId;
    /**
     * 参数ID
     */
    private Long paramId;

    /**
     * 参数值ID（查询eis_param_value表可获取参数值的信息）
     * 若是新增参数，则此字段无效，为空
     * 若是删除参数，则此字段有效，为oldTrackerId在paramId上绑定的参数值
     * 若是修改参数（即修改了参数值，视为先删再增），则此字段有效，为oldTrackerId在paramId上绑定的参数值
     * 若是不变，则此字段有效，为oldTrackerId在paramId上绑定的参数值
     */
    private Set<Long> oldParamValueIds;
    /**
     * 参数值ID（查询eis_param_value表可获取参数值的信息）
     * 若是新增参数，则此字段有效，为newTrackerId在paramId上绑定的参数值
     * 若是删除参数，则此字段无效，为空
     * 若是修改参数（即修改了参数值，视为先删再增），则此字段有效，为newTrackerId在paramId上绑定的参数值
     * 若是不变，则此字段有效，为newTrackerId在paramId上绑定的参数值
     */
    private Set<Long> newParamValueIds;

    /**
     * 绑定描述
     */
    private ParamBindSimpleDTO newData;

    /**
     * 变更类型
     * @see com.netease.hz.bdms.easyinsight.common.enums.ChangeTypeEnum
     */
    private Integer changeType;

}
