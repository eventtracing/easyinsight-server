package com.netease.hz.bdms.easyinsight.service.helper;

import com.netease.hz.bdms.easyinsight.common.enums.ConflictStatusEnum;
import com.netease.hz.bdms.easyinsight.common.util.CommonUtil;
import com.netease.hz.bdms.easyinsight.dao.model.EisObjChangeHistory;
import com.netease.hz.bdms.easyinsight.service.service.ObjChangeHistoryService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MergeConflictHelper {

    @Resource
    private ObjChangeHistoryService objChangeHistoryService;

    public boolean hasMergeConflict(Long reqPoolId) {
        Set<Long> set = new HashSet<>();
        set.add(reqPoolId);
        return CollectionUtils.isNotEmpty(filterConflictReqPoolIds(set));
    }

    public Set<Long> filterConflictReqPoolIds(Set<Long> reqPoolIds) {
        List<EisObjChangeHistory> byConflictStatus = objChangeHistoryService.getByConflictStatus(reqPoolIds, ConflictStatusEnum.MERGE_CONFLICT.getStatus());
        if (CollectionUtils.isEmpty(byConflictStatus)) {
            return new HashSet<>();
        }
        return byConflictStatus.stream().map(EisObjChangeHistory::getReqPoolId).collect(Collectors.toSet());
    }

    public Set<Long> getMergeConflictReqPoolIds() {
        return objChangeHistoryService.getDistinctReqPoolIdByConflictStatus(ConflictStatusEnum.MERGE_CONFLICT.getStatus());
    }

    public Set<Long> getMergeConflictObjIdsOfReqPool(Long reqPoolId) {
        return objChangeHistoryService.getByConflictStatus(reqPoolId, ConflictStatusEnum.MERGE_CONFLICT.getStatus()).stream()
                .map(EisObjChangeHistory::getObjId).collect(Collectors.toSet());
    }

    public Map<Long, Set<Long>> getAllMergeConflictObjIdsGroupByReqPoolId() {
        List<EisObjChangeHistory> all = objChangeHistoryService.getAllByConflictStatus(ConflictStatusEnum.MERGE_CONFLICT.getStatus());
        Map<Long, Set<Long>> result = new HashMap<>();
        if (CollectionUtils.isNotEmpty(all)) {
            all.forEach(o -> {
                Set<Long> objIds = result.computeIfAbsent(o.getReqPoolId(), k -> new HashSet<>());
                objIds.add(o.getObjId());
            });
        }
        return result;
    }

    public static boolean hasMergeConflict(String spmByObjId, Set<Long> objIdsInMergeConflict) {
        if (CollectionUtils.isEmpty(objIdsInMergeConflict)) {
            return false;
        }
        List<Long> objIds = CommonUtil.transSpmToObjIdList(spmByObjId);
        if (CollectionUtils.isEmpty(objIds)) {
            return false;
        }
        for (Long objId : objIds) {
            if (objIdsInMergeConflict.contains(objId)) {
                return true;
            }
        }
        return false;
    }

}
