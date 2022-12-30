package com.netease.hz.bdms.eistest.ws.handler;

import com.netease.hz.bdms.eistest.ws.WsHelper;
import com.netease.hz.bdms.eistest.ws.common.WebSocketUriTemplate;

import com.netease.hz.bdms.eistest.ws.handler.AbstractWsHandler;
import com.netease.hz.bdms.eistest.ws.session.ScanSession;
import com.netease.hz.bdms.eistest.ws.session.WebSocketSessionScope;
import com.netease.hz.bdms.eistest.ws.session.WsSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

/**
 * @author sguo
 */
@Slf4j
public class ScanWsHandler extends AbstractWsHandler {

    @Autowired
    private WsHelper helper;

    @Override
    public synchronized void afterConnectionEstablished(WebSocketSession newSession) throws Exception {
        fillAttr(newSession);
        String code = helper.extractConversation(newSession);
        WsSession appSession = scanCodeSessionManager.getSessionByCode(code);
        if (appSession != null) {
            log.info("already has scan code session, close old session: {}", code);
            scanCodeSessionManager.removeSession(code);
        }

        log.info("create new scan code session: {}", code);
        ScanSession scs = new ScanSession(newSession, WebSocketSessionScope.QR_CODE, code);
        scanCodeSessionManager.addSession(code, scs);
        log();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        closeSession(scanCodeSessionManager, session, WebSocketSessionScope.QR_CODE);
        log();
    }

    @Override
    public String currentUriTemplate() {
        return WebSocketUriTemplate.QR_CODE_CONVERSATION_URI_TEMPLATE;
    }
}
