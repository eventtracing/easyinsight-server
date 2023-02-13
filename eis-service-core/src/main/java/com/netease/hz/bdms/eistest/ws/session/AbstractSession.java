package com.netease.hz.bdms.eistest.ws.session;


import com.netease.hz.bdms.eistest.ws.session.WebSocketSessionScope;
import com.netease.hz.bdms.eistest.ws.session.WsSession;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * @author sguo
 */
@Slf4j
@ToString(of = {"scope", "code"})
public abstract class AbstractSession implements WsSession {
    protected final WebSocketSession session;
    protected final WebSocketSessionScope scope;
    protected final String code;

    public AbstractSession(WebSocketSession session, WebSocketSessionScope scope, String code) {
        this.session = session;
        this.scope = scope;
        this.code = code;
    }

    @Override
    public void close(CloseStatus closeStatus) {
        try {
            session.close(closeStatus);
            log.info("raw session close, code:{}, scope:{}", code, scope);
        } catch (Exception exception) {
            log.error("{} close exception", this, exception);
        }
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public void sendData(String text) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(text));
                log.info("send finish");
            }else {
                log.error("websocket已关闭");
            }
        } catch (Exception e) {
            log.error("{} webSocketSession[{}] failed to send message:{}, error:{}",
                    scope, code, text, e);
        }
    }

    @Override
    public WebSocketSession getRaw() {
        return session;
    }
}
