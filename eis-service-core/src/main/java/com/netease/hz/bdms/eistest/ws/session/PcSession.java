package com.netease.hz.bdms.eistest.ws.session;

import com.netease.hz.bdms.eistest.service.BloodLinkService;
import com.netease.hz.bdms.eistest.ws.dto.BuryPointMetaInfo;
import com.netease.hz.bdms.eistest.ws.dto.PcStorage;
import com.netease.hz.bdms.eistest.ws.session.AbstractSession;
import com.netease.hz.bdms.eistest.ws.session.WebSocketSessionScope;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

/**
 * @author sguo
 */
@Slf4j
@ToString(callSuper = true, exclude = "storage")
public class PcSession extends AbstractSession {
    private PcStorage storage = new PcStorage();

    public PcSession(WebSocketSession session, WebSocketSessionScope scope, String code, BloodLinkService bloodLinkService) {
        super(session, scope, code);
        this.storage.setMetaInfo(new BuryPointMetaInfo(bloodLinkService));
    }

    @Override
    public PcStorage getStorage() {
        return storage;
    }
}
