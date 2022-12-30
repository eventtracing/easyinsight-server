package com.netease.hz.bdms.eistest.ws.handler;

import com.netease.hz.bdms.eistest.ws.BuryPointValidationServiceImpl;

import com.netease.hz.bdms.eistest.ws.SessionManager;
import com.netease.hz.bdms.eistest.ws.handler.LogConsumer;
import com.netease.hz.bdms.eistest.ws.session.AppSession;
import com.netease.hz.bdms.eistest.ws.session.PcSession;
import com.netease.hz.bdms.eistest.ws.session.WebSocketSessionScope;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

/**
 * @author sguo
 */
@Slf4j
@Component
@ToString(of = "futureMap")
public class LogConsumerManager {

    @Autowired
    @Qualifier("appSessionManager")
    protected SessionManager appSessionManager;
    @Autowired
    @Qualifier("scanCodeSessionManager")
    protected SessionManager scanCodeSessionManager;
    @Autowired
    @Qualifier("pcSessionManager")
    protected SessionManager pcSessionManager;
    @Autowired
    private BuryPointValidationServiceImpl bpvs;
//    private ThreadPoolExecutor executor = Executors.newCachedThreadPool(new CustomizableThreadFactory("log-consumer-"));

    private ThreadPoolExecutor executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                      60L, TimeUnit.SECONDS,
                                      new SynchronousQueue<Runnable>(),
            new CustomizableThreadFactory("log-consumer-"));
    private ConcurrentMap<String, Future> futureMap = new ConcurrentHashMap<>();

//    @Scheduled(initialDelay = 10 * 1000, fixedDelay = 10 * 1000)
//    public void watchConsumer(){
//        log.info("线程池信息:{}",executor.toString());
//    }

    public synchronized void start(String code, WebSocketSessionScope scope) {
        AppSession as = (AppSession) appSessionManager.getSessionByCode(code);
        if (as == null) {
            return;
        }

        PcSession ps = (PcSession) pcSessionManager.getSessionByCode(code);
        if (ps == null) {
            return;
        }
        Future future = futureMap.putIfAbsent(code, executor.submit(new LogConsumer(code, as, ps, bpvs)));
        if (future == null) {
            log.info("started log consumer thread {} from {}", code, scope);
        }
    }

    public synchronized void stop(String code, WebSocketSessionScope scope) {
        Future future = futureMap.remove(code);
        if (future != null) {
            log.info("cancel log consumer thread {} from {}", code, scope);
            future.cancel(true);
        }
    }
}
