package com.netease.hz.bdms.eistest.ws.handler;

import com.alibaba.fastjson.JSON;
import com.netease.hz.bdms.eistest.entity.BuryPointLogStatisticsResult;
import com.netease.hz.bdms.eistest.ws.SessionManager;
import com.netease.hz.bdms.eistest.ws.WsHelper;
import com.netease.hz.bdms.eistest.entity.AppPushLogAction;
import com.netease.hz.bdms.eistest.ws.common.CustomWebSocketSessionAttributeKey;
import com.netease.hz.bdms.eistest.ws.handler.LogConsumerManager;
import com.netease.hz.bdms.eistest.ws.session.WebSocketSessionScope;
import com.netease.hz.bdms.eistest.ws.session.WsSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.PathMatcher;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

/**
 * @author sguo
 */
@Slf4j
public abstract class AbstractWsHandler extends TextWebSocketHandler {
    @Autowired
    protected WsHelper helper;
    @Autowired
    protected PathMatcher pathMatcher;
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
    protected LogConsumerManager logConsumerManager;

    protected void fillAttr(WebSocketSession newSession) {
        URI uri = Objects.requireNonNull(newSession.getUri());
        Map<String, String> uriTemplateVariableMap = extractUriTemplateVariables(uri);
        Map<String, Object> webSocketSessionAttributes = newSession.getAttributes();
        copyUriTemplateVariableIntoWebSocketSessionAttributes(uriTemplateVariableMap, webSocketSessionAttributes);
        helper.saveWebSocketSessionAttribute(newSession,
                CustomWebSocketSessionAttributeKey.CLEAN_STORAGE_ON_WEB_SOCKET_SESSION_CLOSED, true);
    }

    private Map<String, String> extractUriTemplateVariables(URI uri) {
        return pathMatcher.extractUriTemplateVariables(currentUriTemplate(), uri.getPath());
    }

    public abstract String currentUriTemplate();

    /**
     * 将所有路径参数复制到 webSocket Session attributes 这个 map 里
     *
     * @param uriTemplateVariableMap     路径参数 map
     * @param webSocketSessionAttributes webSocket Session attributes
     */
    private void copyUriTemplateVariableIntoWebSocketSessionAttributes(Map<String, String> uriTemplateVariableMap,
                                                                       Map<String, Object> webSocketSessionAttributes) {
        uriTemplateVariableMap.forEach(webSocketSessionAttributes::put);
    }

    protected void cleanDataWhenClosed(WebSocketSession session, boolean clean) {
        helper.saveWebSocketSessionAttribute(session,
                CustomWebSocketSessionAttributeKey.CLEAN_STORAGE_ON_WEB_SOCKET_SESSION_CLOSED, clean);
    }

    protected boolean closeSession(SessionManager sessionManager, WebSocketSession session, WebSocketSessionScope scope) {
        boolean clean = helper.extractCleanStorage(session);
        if (clean) {
            String code = helper.extractConversation(session);
            log.info("remove {} session {}", scope, code);
            sessionManager.removeSession(code);
        }
        return clean;
    }

    protected void sendMessageToPc(String code, String msg) {
        WsSession pcs = pcSessionManager.getSessionByCode(code);
        if (pcs != null) {
            log.info("send status message to frontend: {}", msg);
            BuryPointLogStatisticsResult result = new BuryPointLogStatisticsResult(AppPushLogAction.STATUS.getName(), null, null, msg);
            pcs.sendData(JSON.toJSONString(result));
        }
    }

    protected void sendTextMessage(WebSocketSession webSocketSession, String message) {
        helper.sendTextMessage(webSocketSession, message);
    }

    protected void log() {
        log.info("<<< \n{} \n{} \n{} \n{}", appSessionManager, pcSessionManager, scanCodeSessionManager, logConsumerManager);
    }
}
