package com.netease.hz.bdms.eistest.ws.session;


import com.netease.hz.bdms.eistest.ws.dto.AppStorage;
import com.netease.hz.bdms.eistest.ws.session.AbstractSession;
import com.netease.hz.bdms.eistest.ws.session.WebSocketSessionScope;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

/**
 * @author sguo
 */
@Slf4j
@ToString(callSuper = true, exclude = "storage")
public class AppSession extends AbstractSession {

    private AppStorage storage;

    public AppSession(WebSocketSession session, WebSocketSessionScope scope, String code, String deviceId) {
        super(session, scope, code);
        this.storage = new AppStorage(deviceId);
    }

    public void setStorage(AppStorage storage) {
        this.storage = storage;
    }

    @Override
    public AppStorage getStorage() {
        return storage;
    }

    @Override
    public void close(CloseStatus closeStatus) {
        super.close(closeStatus);
        try {
            if (storage != null) {
                storage.close();
            }
        } catch (Exception exception) {
            log.error("{} data close exception", this, exception);
        }
    }
}
