package com.netease.eis.adapters;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 缓存适配器
 */
public interface CacheAdapter {

    /**
     * 设置key的值
     * @param key key
     * @param value value
     * @return 是否设置成功
     */
    boolean set(String key, String value);

    /**
     * 如果key存在，设置key在指定秒数后过期
     * @param key key
     * @param value value
     * @param seconds 秒数
     * @return 是否设置成功
     */
    boolean setWithExpireTime(String key, String value, int seconds);

    /**
     * 获取key的值
     * @param key key
     * @return 值
     */
    String get(String key);

    /**
     * 获取keys的值
     * @param keys kes
     * @return 值
     */
    Map<String, String> gets(Collection<String> keys);

    /**
     * 删除一个key
     * @param key key
     * @return 本次操作是否删除
     */
    boolean del(String key);

    /**
     * 设置key的过期时间
     * @param key key
     * @param seconds 秒数
     * @return 是否设置成功
     */
    boolean expire(String key, int seconds);

    /**
     * 返回Key是否存在
     * @param key
     * @return 是否存在
     */
    boolean exists(String key);

    Long lpush(String key, String value);

    String lpop(String key);

    Long llen(String key);

    List<String> lrange(String key, long start, long end);

    boolean setNXWithExpire(String key, String value, int seconds);

    long ttl(String key);
}
