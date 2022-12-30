package com.netease.hz.bdms.eistest.ws.session;

import com.netease.hz.bdms.eistest.ws.dto.Storage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

/**
 * @author sguo
 */
public interface WsSession {
    void close(CloseStatus closeStatus);

    String getCode();

    Storage getStorage();

    void sendData(String text);

    WebSocketSession getRaw();
}
