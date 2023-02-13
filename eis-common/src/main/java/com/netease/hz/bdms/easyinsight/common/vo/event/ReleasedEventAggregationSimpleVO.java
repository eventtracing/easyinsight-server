package com.netease.hz.bdms.easyinsight.common.vo.event;

import com.netease.hz.bdms.easyinsight.common.dto.common.CommonAggregateDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.CommonRelationAggregateDTO;
import lombok.Data;

import java.util.List;

/**
 * @author: xumengqiang
 * @date: 2022/1/19 11:03
 */
@Data
public class ReleasedEventAggregationSimpleVO {
    /**
     * 终端信息
     */
    CommonAggregateDTO terminal;

    /**
     * 发布版本信息
     */
    List<CommonRelationAggregateDTO> releases;
}
