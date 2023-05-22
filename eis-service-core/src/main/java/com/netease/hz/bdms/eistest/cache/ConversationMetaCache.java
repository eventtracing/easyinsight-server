package com.netease.hz.bdms.eistest.cache;

import com.netease.eis.adapters.CacheAdapter;
import com.netease.hz.bdms.easyinsight.common.util.JsonUtils;
import com.netease.hz.bdms.eistest.service.BloodLinkService;
import com.netease.hz.bdms.eistest.ws.dto.BuryPointMetaInfo;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
public class ConversationMetaCache {

    private static final String CACHE_KEY = "meta_cache_";

    @Resource
    private CacheAdapter cacheAdapter;

    @Autowired
    private BloodLinkService bloodLinkService;

    private Map<String, BuryPointMetaInfo> localCache = new HashMap<>();

    public void set(String conversation, Long taskId, Long terminalId, Long domainId, Long appId) {
        String cacheKey = CACHE_KEY + conversation;
        CacheData cacheData = new CacheData();
        cacheData.setTaskId(taskId);
        cacheData.setTerminalId(terminalId);
        cacheData.setDomainId(domainId);
        cacheData.setAppId(appId);
        // 每个会话的meta生成规则保存30天
        cacheAdapter.setWithExpireTime(cacheKey, JsonUtils.toJson(cacheData), 86400 * 30);
    }

    public BuryPointMetaInfo get(String conversation) {
        BuryPointMetaInfo localCacheData = localCache.get(conversation);
        if (localCacheData != null) {
            return localCacheData;
        }
        String cacheKey = CACHE_KEY + conversation;
        String s = cacheAdapter.get(cacheKey);
        if (StringUtils.isBlank(s)) {
            return null;
        }
        CacheData cacheData = JsonUtils.parseObject(s, CacheData.class);
        if (cacheData == null) {
            return null;
        }
        // 重新生成
        synchronized (this) {
            // Double Check
            BuryPointMetaInfo l = localCache.get(conversation);
            if (l != null) {
                return l;
            }
            // 抢到了锁，而且此时conversation对应meta数据可初始化、且并未初始化
            BuryPointMetaInfo buryPointMetaInfo = new BuryPointMetaInfo(bloodLinkService);
            buryPointMetaInfo.initBuryPointRule(cacheData.getTaskId(), cacheData.getTerminalId(), cacheData.getDomainId(), cacheData.getAppId());
            localCache.put(cacheKey, buryPointMetaInfo);
            return buryPointMetaInfo;
        }
    }

    @Data
    public static class CacheData {
        private Long taskId;
        private Long terminalId;
        private Long domainId;
        private Long appId;
    }
}
