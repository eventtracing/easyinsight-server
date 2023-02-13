package com.netease.hz.bdms.easyinsight.service.helper;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.service.service.obj.ObjRelationReleaseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * @author: xumengqiang
 * @date: 2021/12/9 17:35
 */
@Slf4j
@Component
public class ObjectRelationHelper {

    @Autowired
    private ObjRelationReleaseService objRelationReleaseService;

    /**
     * 检查新增的对象父子关系是否成环
     *
     * @param objId 对象ID
     * @param parentsObjId 父对象ID集合
     * @param baseRelationMap 子父关系集合
     * @return 是否成环
     */
    public Boolean checkLoop(final Long objId, List<Long> parentsObjId,
                             Map<Long, Set<Long>> baseRelationMap){
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "未指定产品信息");
        Preconditions.checkArgument(null != objId, "对象ID不能为空");

        if(CollectionUtils.isNotEmpty(parentsObjId)){
            // 1. 构建全部父子关系映射 (base + diff)

            // 添加新增的父子关系 (diff)
            Set<Long> parentObjIdSet = baseRelationMap
                    .computeIfAbsent(objId, k -> Sets.newHashSet());
            parentObjIdSet.addAll(parentsObjId);

            // 2. 环路检测
            Queue<Long> queue = Lists.newLinkedList();
            queue.offer(objId);
            while(!queue.isEmpty()){
                // 节点出队
                Long currObjId = queue.poll();
                // 获取当前节点的所有父节点
                Set<Long> currParentsObjId = baseRelationMap.getOrDefault(currObjId, Sets.newHashSet());
                for (Long currParentObjId : currParentsObjId) {
                    // 在向上遍历的过程中回到初始节点，说明存在环
                    if(currParentObjId != null && currParentObjId.equals(objId)){
                        return true;
                    }
                    // 父节点入队
                    queue.offer(currParentObjId);
                }
            }
        }
        return false;
    }
}
