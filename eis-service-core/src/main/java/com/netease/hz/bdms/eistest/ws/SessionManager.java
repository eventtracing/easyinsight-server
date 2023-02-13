package com.netease.hz.bdms.eistest.ws;


import com.netease.hz.bdms.eistest.ws.dto.AppStorage;
import com.netease.hz.bdms.eistest.ws.session.WebSocketSessionScope;
import com.netease.hz.bdms.eistest.ws.session.WsSession;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author sguo
 */
@Slf4j
@ToString(exclude = "executor")
public class SessionManager {
    private Map<String, WsSession> sessionMap = new ConcurrentHashMap<>();
    private WebSocketSessionScope type;
    private ScheduledExecutorService executor;

    public SessionManager(WebSocketSessionScope type) {
        this.type = type;
        executor = Executors.newSingleThreadScheduledExecutor(new CustomizableThreadFactory("ws-ping-" + type.name() + "-"));
        executor.scheduleAtFixedRate(this::processPing, 3, 10, TimeUnit.SECONDS);
    }

    private void processPing() {
        sessionMap.values().forEach(wsSession -> {
            try {
                WebSocketSession rawSession = wsSession.getRaw();
                if (rawSession.isOpen()) {
                    log.debug("sending ping {}", wsSession);
                    rawSession.sendMessage(new PingMessage());
                }
            } catch (IOException e) {
                log.warn("sending ping message error, {}", wsSession, e);
            }
        });
    }

    public WsSession getSessionByCode(String code) {
        return sessionMap.get(code);
    }

    public Optional<WsSession> getSessionByDeviceId(String deviceId) {
        return sessionMap.values().stream().filter(s -> ((AppStorage) s.getStorage()).getDeviceId().equals(deviceId)).findAny();
    }

    public synchronized void addSession(String code, WsSession appSession) {
        sessionMap.put(code, appSession);
    }

    public synchronized void removeSession(String code, CloseStatus closeStatus) {
        WsSession wsSession = sessionMap.remove(code);
        if (wsSession != null) {
            wsSession.close(closeStatus);
        }
    }

    public synchronized void removeSession(String code) {
        removeSession(code, CloseStatus.NORMAL);
    }
}
