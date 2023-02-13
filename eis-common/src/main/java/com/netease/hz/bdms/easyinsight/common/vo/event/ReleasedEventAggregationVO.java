package com.netease.hz.bdms.easyinsight.common.vo.event;

import lombok.Data;

import java.util.List;

/**
 * @author: xumengqiang
 * @date: 2022/1/19 11:03
 */
@Data
public class ReleasedEventAggregationVO {
    /**
     * 每个端的版本列表
     */
    List<ReleasedEventAggregationSimpleVO> list;
}
