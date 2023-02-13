package com.netease.hz.bdms.eistest.ws.dto;

import com.netease.hz.bdms.eistest.ws.dto.BuryPointMetaInfo;
import com.netease.hz.bdms.eistest.ws.dto.BuryPointStats;
import com.netease.hz.bdms.eistest.ws.dto.EvictingBlockingQueue;
import com.netease.hz.bdms.eistest.ws.dto.Storage;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author sguo
 */
@Slf4j
public class AppStorage implements Storage {
    @Getter
    private EvictingBlockingQueue<String> queue = new EvictingBlockingQueue<>(1000);
    @Getter
    private String deviceId;
    @Getter
    @Setter
    private BuryPointMetaInfo metaInfo;
    @Getter
    private BuryPointStats stats = new BuryPointStats();

    public AppStorage(String deviceId) {
        this.deviceId = deviceId;
    }

    public void insertMessage(String message) {
        log.info("deviceId={},queueSize={}",deviceId,queue.size());
        queue.offer(message);
    }

    @Override
    public void close() {
        queue.clear();
        stats.clearAllStatisticsResultInTargetConversation();
    }
}
