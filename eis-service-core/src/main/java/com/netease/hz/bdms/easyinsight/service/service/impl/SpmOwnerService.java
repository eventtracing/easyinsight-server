package com.netease.hz.bdms.easyinsight.service.service.impl;
import java.util.*;

import com.netease.eis.adapters.CacheAdapter;
import com.netease.hz.bdms.easyinsight.common.enums.ReqTaskStatusEnum;
import com.netease.hz.bdms.easyinsight.common.util.CommonUtil;
import com.netease.hz.bdms.easyinsight.dao.model.EisTaskProcess;
import com.netease.hz.bdms.easyinsight.dao.model.ObjectBasic;
import com.netease.hz.bdms.easyinsight.service.service.ObjectBasicService;
import com.netease.hz.bdms.easyinsight.service.service.requirement.TaskProcessService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.stream.Collectors;

/**
 * SPM负责人查询服务
 */
@Service
public class SpmOwnerService {

    private static final String CACHE_KEY_PREFIX = "SpmOwner_";
    private static final int TTL = 3 * 3600;
    private static final String NULL_PLACE_HOLDER = "NULL";

    @Resource
    private CacheAdapter cacheAdapter;

    @Resource
    private TaskProcessService taskProcessService;

    @Resource
    private ObjectBasicService objectBasicService;

    public Map<String, String> multiGet(Long appId, Set<String> spms) {
        if (appId == null) {
            return new HashMap<>();
        }
        if (CollectionUtils.isEmpty(spms)) {
            return new HashMap<>();
        }
        Set<String> cacheKeys = spms.stream().map(s -> getCacheKey(appId, s)).collect(Collectors.toSet());
        Map<String, String> gets = cacheAdapter.gets(cacheKeys);
        Map<String, String> result = new HashMap<>();
        Set<String> missings = new HashSet<>();
        for (String spm : spms) {
            String cacheKey = getCacheKey(appId, spm);
            String owner = gets.get(cacheKey);
            if (NULL_PLACE_HOLDER.equals(owner)) {
                // 占位符 do nothing
            } else if (StringUtils.isNotBlank(owner)) {
                result.put(spm, owner);
            } else {
                missings.add(spm);
            }
        }

        if (CollectionUtils.isEmpty(missings)) {
            return result;
        }

        // 丢失部分，查询并补充到缓存
        Map<String, String> missingValues = doMultiGet(appId, missings);
        missings.forEach(missingSpm -> {
            String cacheKey = getCacheKey(appId, missingSpm);
            String owner = missingValues.get(missingSpm);
            if (StringUtils.isBlank(owner)) {
                cacheAdapter.setWithExpireTime(cacheKey, NULL_PLACE_HOLDER, TTL);
            } else {
                cacheAdapter.setWithExpireTime(cacheKey, owner, TTL);
                result.put(missingSpm, owner);
            }
        });
        return result;
    }

    private Map<String, String> doMultiGet(Long appId, Set<String> spms) {
        if (CollectionUtils.isEmpty(spms)) {
            return new HashMap<>();
        }
        Set<String> allOids = spms.stream().flatMap(spm -> CommonUtil.transSpmToOidList(spm).stream()).collect(Collectors.toSet());
        List<ObjectBasic> objectBasics = objectBasicService.getByOids(appId, allOids);
        Map<String, Long> oidToObjIdMap = objectBasics.stream().collect(Collectors.toMap(ObjectBasic::getOid, ObjectBasic::getId, (oldV, newV) -> oldV));
        Map<String, String> spmByObjIdToSpmMap = new HashMap<>();

        spms.forEach(spm -> {
            String spmByObjId = CommonUtil.transSpmByOidToSpmByObjId(oidToObjIdMap, spm);
            if (StringUtils.isNotBlank(spmByObjId)) {
                spmByObjIdToSpmMap.put(spmByObjId, spm);
            }
        });

        List<EisTaskProcess> processes = taskProcessService.getBatchBySpmBjObjIds(new HashSet<>(spmByObjIdToSpmMap.keySet()), ReqTaskStatusEnum.ONLINE.getState());
        return processes.stream()
                .filter(process -> StringUtils.isNotBlank(process.getOwnerName()))
                .sorted(Comparator.comparingLong(process -> process.getUpdateTime().getTime())) // 按更新时间升序
                .collect(Collectors.toMap(
                        process -> spmByObjIdToSpmMap.get(process.getSpmByObjId()),
                        EisTaskProcess::getOwnerName,
                        (oldV, newV) -> newV)); // 按更新时间升序，取更新时间更大的
    }

    private String getCacheKey(long appId, String spm) {
        return CACHE_KEY_PREFIX + appId + "_" + spm;
    }
}
