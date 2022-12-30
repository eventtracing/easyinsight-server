package com.netease.hz.bdms.easyinsight.web.demo.adapters;

import com.netease.eis.adapters.CacheAdapter;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DemoJedisCacheAdapter implements CacheAdapter {

    JedisCluster jedis = new JedisCluster(parseHostAndPorts());

    private Set<HostAndPort> parseHostAndPorts() {
        String s = "10.197.112.51:32538;10.197.112.52:32538;10.197.112.53:32538;10.197.112.54:32538;10.197.112.55:32538;10.197.112.56:32538;10.197.112.57:32538;10.197.112.58:32538;10.197.112.59:32538;10.197.112.60:32538";
        return Arrays.stream(s.split(";")).map(o -> {
            String[] split = o.split(":");
            return new HostAndPort(split[0], Integer.parseInt(split[1]));
        }).collect(Collectors.toSet());
    }

    @Override
    public boolean set(String key, String value) {
        jedis.set(key, value);
        return true;
    }

    @Override
    public boolean setWithExpireTime(String key, String value, int seconds) {
        jedis.setex(key, seconds, value);
        return true;
    }

    @Override
    public String get(String key) {
        return jedis.get(key);
    }

    @Override
    public Map<String, String> gets(Collection<String> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return new HashMap<>();
        }
        Map<String, String> result = new HashMap<>();
        keys.forEach(key -> {
            String v = get(key);
            if (v != null) {
                result.put(key, v);
            }
        });
        return result;
    }

    @Override
    public boolean del(String key) {
        Long del = jedis.del(key);
        return del != null && del > 0L;
    }

    @Override
    public boolean expire(String key, int seconds) {
        jedis.expire(key, seconds);
        return true;
    }

    @Override
    public boolean exists(String key) {
        Boolean exists = jedis.exists(key);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public Long lpush(String key, String value) {
        return jedis.lpush(key, value);
    }

    @Override
    public String lpop(String key) {
        return jedis.lpop(key);
    }

    @Override
    public Long llen(String key) {
        return jedis.llen(key);
    }

    @Override
    public List<String> lrange(String key, long start, long end) {
        return jedis.lrange(key, start, end);
    }

    @Override
    public boolean setNXWithExpire(String key, String value, int seconds) {
        Long ret = jedis.setnx(key, value);
        boolean success = ret != null && ret > 0L;
        if (!success) {
            return false;
        }
        expire(key, seconds);
        return true;
    }

    @Override
    public long ttl(String key) {
        Long ttl = jedis.ttl(key);
        return ttl == null ? 0L : ttl;
    }
}
