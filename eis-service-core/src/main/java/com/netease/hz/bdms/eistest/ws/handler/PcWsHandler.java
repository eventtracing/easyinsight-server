package com.netease.hz.bdms.eistest.ws.handler;

import com.alibaba.fastjson.JSON;
import com.netease.hz.bdms.easyinsight.common.constant.GlobalConst;
import com.netease.hz.bdms.easyinsight.common.constant.ResponseCodeConstant;
import com.netease.hz.bdms.easyinsight.common.exception.ParamException;
import com.netease.hz.bdms.easyinsight.common.exception.RealTimeTestException;
import com.netease.hz.bdms.easyinsight.common.http.HttpResult;
import com.netease.hz.bdms.eistest.service.BloodLinkService;
import com.netease.hz.bdms.eistest.ws.common.WebSocketUriTemplate;

import com.netease.hz.bdms.eistest.ws.dto.PcStorage;
import com.netease.hz.bdms.eistest.ws.handler.AbstractWsHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.adapter.standard.StandardWebSocketSession;

import java.util.List;
import java.util.Map;

/**
 * @author sguo
 */
@Slf4j
public class PcWsHandler extends AbstractWsHandler {
    @Autowired
    private BloodLinkService bloodLinkService;

    @Override
    public synchronized void afterConnectionEstablished(WebSocketSession newSession) throws Exception {
//        fillAttr(newSession);
//        String code = helper.extractConversation(newSession);
//        log.info("try to start pc session with code {}", code);
//        WsSession oldSession = pcSessionManager.getSessionByCode(code);
//        if (oldSession != null) {
//            log.info("already has pc session, close old session: {}", code);
//            oldSession.getRaw().close(CloseStatus.GOING_AWAY);
//        }
//
//        log.info("create new pc session, code: {}", code);
//        PcSession ps = new PcSession(newSession, WebSocketSessionScope.PC, code, bloodLinkService);
//        pcSessionManager.addSession(code, ps);
//
//        log.info("init metadata, code : {}", code);
//        initMetadata(code, newSession);
//
//        log.info("try start consume thread,  code: {}", code);
//        logConsumerManager.start(code, WebSocketSessionScope.PC);
//
//        if (appSessionManager.getSessionByCode(code) != null) {
//            sendMessageToPc(code, "设备已连接");
//        } else {
//            sendMessageToPc(code, "设备已断开");
//        }
//        log();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
//        log.info("on afterConnectionClosed pc");
//        boolean closed = closeSession(pcSessionManager, session, WebSocketSessionScope.PC);
//        if (closed) {
//            String code = helper.extractConversation(session);
//            logConsumerManager.stop(code, WebSocketSessionScope.PC);
//
//            if (status.getCode() == 3001) {
//                WsSession appSession = appSessionManager.getSessionByCode(code);
//                if (appSession != null) {
//                    appSession.getRaw().close(CloseStatus.GOING_AWAY);
//                }
//            }
//        }
//        log();
    }


    @Override
    public String currentUriTemplate() {
        return WebSocketUriTemplate.PC_CONVERSATION_URI_TEMPLATE;
    }

    public void initMetadata(String conversation, WebSocketSession webSocketSession) {
        try {
            initBuryPointRule(conversation, webSocketSession);
        } catch (ParamException | RealTimeTestException e) {
            log.error("initMetadata error", e);

            sendJsonTextMessage(webSocketSession, e.getMessage(),
                    ResponseCodeConstant.REAL_TIME_TEST_ERROR);
            throw e;
        } catch (RuntimeException e) {
            log.error("initMetadata2 error", e);
            sendJsonTextMessage(webSocketSession,
                    GlobalConst.DEFAULT_INTERNAL_SERVER_ERROR_MESSAGE, ResponseCodeConstant.REAL_TIME_TEST_ERROR);
            throw e;
        }
    }

    public void sendJsonTextMessage(WebSocketSession webSocketSession, String message, Integer resultCode) {
        if (resultCode == null) {
            resultCode = ResponseCodeConstant.OK;
        }
        HttpResult<String> result = new HttpResult<>(resultCode);
        result.setMsg(message);
        String jsonResult = JSON.toJSONString(result);
        sendTextMessage(webSocketSession, jsonResult);
    }


    private void initBuryPointRule(String conversation, WebSocketSession webSocketSession) {
        StandardWebSocketSession standardWebSocketSession = (StandardWebSocketSession) webSocketSession;
        Map<String, List<String>> requestParameterMap = standardWebSocketSession.getNativeSession().getRequestParameterMap();

        List<String> domainIds = requestParameterMap.get("domainId");
        Long domainId = null;
        if (CollectionUtils.isNotEmpty(domainIds)) {
            domainId = Long.parseLong(domainIds.get(0));
        }
        if (domainId == null) {
            throw new ParamException("domainId 不能为空");
        }

        List<String> appIds = requestParameterMap.get("appId");
        Long appId = null;
        if (CollectionUtils.isNotEmpty(appIds)) {
            appId = Long.parseLong(appIds.get(0));
        }
        if (appId == null) {
            throw new ParamException("appId 不能为空");
        }

        List<String> logOnlyList = requestParameterMap.get("logOnly");
        boolean logOnly = false;
        if (CollectionUtils.isNotEmpty(logOnlyList)) {
            logOnly = Boolean.parseBoolean(logOnlyList.get(0));
        }

        List<String> taskIdList = requestParameterMap.get("taskId");
        Long taskId = null;
        if (CollectionUtils.isNotEmpty(taskIdList)) {
            String taskIdStr = taskIdList.get(0);
            if(!StringUtils.isEmpty(taskIdStr)){
                taskId = Long.parseLong(taskIdList.get(0));
            }
        }

        List<String> terminalIds = requestParameterMap.get("terminalId");
        Long terminalId = null;
        if(CollectionUtils.isNotEmpty(terminalIds)){
            terminalId = Long.parseLong(terminalIds.get(0));
        }

        PcStorage pcStorage = (PcStorage) pcSessionManager.getSessionByCode(conversation).getStorage();
        pcStorage.getMetaInfo().initBuryPointRule(taskId,terminalId, domainId, appId);
        pcStorage.setLogOnly(logOnly);
    }
}
