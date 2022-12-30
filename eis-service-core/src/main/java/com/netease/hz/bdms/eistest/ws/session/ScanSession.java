package com.netease.hz.bdms.eistest.ws.session;


import com.netease.hz.bdms.eistest.ws.dto.Storage;
import com.netease.hz.bdms.eistest.ws.session.AbstractSession;
import com.netease.hz.bdms.eistest.ws.session.WebSocketSessionScope;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

/**
 * @author sguo
 */
@Slf4j
@ToString(callSuper = true)
public class ScanSession extends AbstractSession {

    public ScanSession(WebSocketSession session, WebSocketSessionScope scope, String code) {
        super(session,scope,code);
    }

    @Override
    public Storage getStorage() {
        return null;
    }
}
