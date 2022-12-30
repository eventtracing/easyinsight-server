package com.netease.hz.bdms.eistest.service.impl;

import com.netease.hz.bdms.eistest.service.es.ElasticsearchWriteService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Service
public class ESTaskConsumerService {

    @Resource
    private ElasticsearchWriteService elasticsearchWriteService;

    private static final String logIndexName = "insight_eslog";

    private static final String staIndexName = "insight_esparam";

    private ExecutorService executor = new ThreadPoolExecutor(1, 1, 10L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(200),
            new ThreadPoolExecutor.DiscardPolicy() {
            });


    private static long preWriteTime = System.currentTimeMillis();

    public void submitWriteCkTask(ArrayBlockingQueue<Map<String, Object>> logQueue, ArrayBlockingQueue<Map<String, Object>> paramQueue) {

        if (preWriteTime + 1000 < System.currentTimeMillis() || logQueue.size() >= 100 || paramQueue.size() >= 100) {
            executor.execute(() -> {
                // executor被设置为单线程的，因此无并发问题
                try {
                    if(preWriteTime + 1000 < System.currentTimeMillis() || logQueue.size() >= 100 || paramQueue.size() >= 100) {
                        List<Map<String, Object>> buryPointLogs = new ArrayList<>();
                        logQueue.forEach(eisCheckResultLog -> {
                            try {
                                Map<String, Object> checkResultLog = logQueue.take();
                                buryPointLogs.add(checkResultLog);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        });
                        if(buryPointLogs.size() > NumberUtils.LONG_ZERO) {
                            boolean ret = elasticsearchWriteService.insertIntoEsBatch(logIndexName, buryPointLogs);
                            if (ret) {
                                log.info("log批量写入ES成功，{}", buryPointLogs.size());
                                preWriteTime = System.currentTimeMillis();
                            }
                        }

                        List<Map<String, Object>> statisticLogs = new ArrayList<>();
                        paramQueue.forEach(eisCheckResultLog -> {
                            try {
                                Map<String, Object> staResultLog = paramQueue.take();
                                statisticLogs.add(staResultLog);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        });
                        if(statisticLogs.size() > NumberUtils.LONG_ZERO) {
                            boolean staRet = elasticsearchWriteService.insertIntoEsBatch(staIndexName, statisticLogs);
                            if (staRet) {
                                log.info("日志统计批量写入ES成功，{}", statisticLogs.size());
                                preWriteTime = System.currentTimeMillis();
                            }
                        }

                    }
                } catch (Exception e) {
                    log.error("批量写入ES failed", e);
                }
            });
        }
    }

}

