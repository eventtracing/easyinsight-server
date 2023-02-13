package com.netease.hz.bdms.eistest.service.impl;

import com.netease.eis.adapters.CacheAdapter;
import com.netease.hz.bdms.easyinsight.common.util.JsonUtils;
import com.netease.hz.bdms.eistest.entity.ClientBasicInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 将测试会话基本信息存到redis
 */
@Slf4j
@Service
public class ConversationBasicInfoService {

    private static final String KEY_PREFIX = "eis_realtime_test_conversation_";
    private static final String BASE_LINE_NAME_KEY_PREFIX = "eis_realtime_test_baseline_name_conversation_";

    @Resource
    private CacheAdapter cacheAdapter;

    public ClientBasicInfo getClientBasicInfo(String conversation) {
        String s = cacheAdapter.get(getCacheKey(conversation));
        if (StringUtils.isEmpty(s)) {
            return null;
        }
        return JsonUtils.parseObject(s, ClientBasicInfo.class);
    }

    public void setClientBasicInfo(String conversation, ClientBasicInfo clientBasicInfo) {
        String cacheKey = getCacheKey(conversation);
        cacheAdapter.setWithExpireTime(cacheKey, JsonUtils.toJson(clientBasicInfo), 86400 * 30); // 存个30天
    }

    public String getBaseLineName(String conversation) {
        return cacheAdapter.get(getBaseLineNameCacheKey(conversation));
    }

    public void setBaseLineName(String conversation, String baseLineName) {
        String cacheKey = getBaseLineNameCacheKey(conversation);
        cacheAdapter.setWithExpireTime(cacheKey, baseLineName, 86400 * 30); // 存个30天
    }

    private static String getCacheKey(String conversation) {
        return KEY_PREFIX + conversation;
    }

    private static String getBaseLineNameCacheKey(String conversation) {
        return BASE_LINE_NAME_KEY_PREFIX + conversation;
    }
}
