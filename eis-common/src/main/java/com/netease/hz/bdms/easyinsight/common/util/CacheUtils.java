package com.netease.hz.bdms.easyinsight.common.util;


import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 封装穿透缓存
 */
@Slf4j
public class CacheUtils {

    private static final ExecutorService executor = new ThreadPoolExecutor(1, 1, 10L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            new ThreadPoolExecutor.DiscardPolicy() {
            });

    /**
     * 从cacheGetFunction获取
     * 如果未获取到，从dataSupplier中获取，并使用cacheSetConsumer写入
     */
    public static <T> T getAndSetIfAbsent(Supplier<String> keyGenerator,
                                          Supplier<T> dataSupplier,
                                          Function<String, String> cacheGetter,
                                          BiConsumer<String, String> cacheSetter, Class<T> tClass) {
        String cacheKey = keyGenerator.get();
        String cacheValue = cacheGetter.apply(cacheKey);
        // 命中cache
        if (cacheValue != null) {
            if ("N".equals(cacheValue)) {
                return null;
            }
            return JsonUtils.parseObject(cacheValue, tClass);
        }
        // 未命中cache
        T data = dataSupplier.get();
        if (data == null) {
            cacheSetter.accept(cacheKey, "N");
            return null;
        }
        cacheSetter.accept(cacheKey, JsonUtils.toJson(data));
        return data;
    }

    /**
     * 从cacheGetFunction获取
     * 如果未获取到，从dataSupplier中获取，阻塞当前线程
     * 如果数据过期，从dataSupplier中获取，更新缓存是异步的，不会阻塞当前线程
     */
    public static <T> T getAndSetIfAbsent(Supplier<String> keyGenerator,
                                          Function<String, Boolean> needRefreshCache,
                                          Supplier<T> dataSupplier,
                                          Function<String, String> cacheGetter,
                                          BiConsumer<String, String> cacheSetter, Class<T> tClass) {
        String cacheKey = keyGenerator.get();
        String cacheValue = cacheGetter.apply(cacheKey);
        // 命中cache
        if (cacheValue != null) {
            if (Boolean.TRUE.equals(needRefreshCache.apply(cacheKey))) {
                // 异步刷新，不阻塞
                executor.submit(() -> {
                    try {
                        T data = dataSupplier.get();
                        if (data == null) {
                            cacheSetter.accept(cacheKey, "N");
                        }
                        cacheSetter.accept(cacheKey, JsonUtils.toJson(data));
                    } catch (Exception e) {
                        log.error("refresh cache failed, cacheKey={}", cacheKey, e);
                    }
                });
            }
            if ("N".equals(cacheValue)) {
                return null;
            }
            return JsonUtils.parseObject(cacheValue, tClass);
        }
        // 未命中cache
        T data = dataSupplier.get();
        if (data == null) {
            cacheSetter.accept(cacheKey, "N");
            return null;
        }
        cacheSetter.accept(cacheKey, JsonUtils.toJson(data));
        return data;
    }

    /**
     *  dataSupplier中数据覆盖到memcache
     */
    public static <T> void update(Supplier<String> keyGenerator, Supplier<T> dataSupplier, BiConsumer<String, String> cacheSetter) {
        String cacheKey = keyGenerator.get();
        T data = dataSupplier.get();
        if (data == null) {
            cacheSetter.accept(cacheKey, "N");
            return;
        }
        cacheSetter.accept(cacheKey, JsonUtils.toJson(data));
    }
}
