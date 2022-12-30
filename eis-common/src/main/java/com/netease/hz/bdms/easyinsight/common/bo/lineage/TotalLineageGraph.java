package com.netease.hz.bdms.easyinsight.common.bo.lineage;

import lombok.Data;

import java.util.Map;
import java.util.Set;

/**
 * 全量血缘图（只增不删）
 *
 * @author: xumengqiang
 * @date: 2021/12/30 17:04
 */

@Data
public class TotalLineageGraph extends LinageGraph{
    Map<Long, Set<Long>> addedRelationMap;

    Map<Long, Set<Long>> deletedRelationMap;
}
