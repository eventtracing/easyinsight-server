package com.netease.hz.bdms.easyinsight.common.bo.lineage;

import lombok.Data;

import java.util.Map;
import java.util.Set;

/**
 * 血缘图
 */
@Data
public class LinageGraph {
    //血缘图中所有的对象Id
    Set<Long> allObjIds;
    //血缘图中所有对象关联父对象映射，<子对象Id,父对象id集合>
    Map<Long, Set<Long>> parentsMap;
    //血缘图中所有对象关联子对象映射，<父对象Id,子对象id集合>
    Map<Long, Set<Long>> childrenMap;

}
