package com.netease.hz.bdms.easyinsight.service.service.impl;

import com.netease.eis.adapters.CacheAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class LockService {

    @Resource
    private CacheAdapter cacheAdapter;


    /**
     * 自旋锁，阻止进一步操作，直到获取到锁，或者超过重试次数
     * @param lockKey
     */
    public void tryLock(String lockKey) {
        int times = 50;
        while (times-- > 0) {
            boolean setSuccess = cacheAdapter.setNXWithExpire(lockKey, "1", 10);
            // 抢到了锁
            if (setSuccess) {
                return;
            }
            try {
                long millis = times > 45 ? 10L : 200L;
                Thread.sleep(millis);
            } catch (Exception e) {
                log.debug("", e);
            }
        }
    }

    /**
     * 自旋锁，阻止进一步操作，直到获取到锁，或者超过重试次数
     * @param timeout 超时次数
     */
    public boolean tryLock(String lockKey, long timeout) {
        int triedTimes = 0;
        while (timeout > 0) {
            boolean setSuccess = cacheAdapter.setNXWithExpire(lockKey, "1", (int) (timeout / 1000L + 1L));
            // 抢到了锁
            if (setSuccess) {
                return true;
            }
            try {
                // 重试次数较少时，快速重试；较多时，提高重试间隔
                long millis = triedTimes < 5L ? 10L : 200L;
                Thread.sleep(millis);
                triedTimes++;
                timeout -= millis;
            } catch (Exception e) {
                log.debug("", e);
            }
        }
        return false;
    }

    public void releaseLock(String lockKey) {
        cacheAdapter.del(lockKey);
    }
}
