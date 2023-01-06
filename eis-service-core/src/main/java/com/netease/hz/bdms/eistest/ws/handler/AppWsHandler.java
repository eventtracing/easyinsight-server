package com.netease.hz.bdms.eistest.ws.handler;

import com.netease.hz.bdms.easyinsight.common.dto.realtimetest.RealTimeTestResourceDTO;
import com.netease.hz.bdms.easyinsight.common.exception.ParamException;
import com.netease.hz.bdms.easyinsight.common.exception.RealTimeTestException;
import com.netease.hz.bdms.eistest.service.BloodLinkService;
import com.netease.hz.bdms.eistest.ws.handler.AbstractWsHandler;
import com.netease.hz.bdms.eistest.service.impl.BuryPointAlertService;
import com.netease.hz.bdms.eistest.service.BuryPointAnaysisService;
import com.netease.hz.bdms.eistest.service.impl.ConversationBasicInfoService;
import com.netease.hz.bdms.eistest.ws.common.WebSocketUriTemplate;
import com.netease.hz.bdms.eistest.ws.dto.AppStorage;
import com.netease.hz.bdms.eistest.ws.dto.BuryPointMetaInfo;
import com.netease.hz.bdms.eistest.ws.session.AppSession;
import com.netease.hz.bdms.eistest.ws.session.WebSocketSessionScope;
import com.netease.hz.bdms.eistest.ws.session.WsSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Optional;

/**
 * @author sguo
 */
@Slf4j
public class AppWsHandler extends AbstractWsHandler {

    @Resource
    private BuryPointAnaysisService buryPointAnaysisService;
    @Autowired
    private BloodLinkService bloodLinkService;
    @Resource
    private BuryPointAlertService buryPointAlertService;
    @Resource
    private ConversationBasicInfoService conversationBasicInfoService;

    @Override
    public synchronized void afterConnectionEstablished(WebSocketSession newSession) throws Exception {
        fillAttr(newSession);

        String code = helper.extractConversation(newSession);
        WsSession appSession = appSessionManager.getSessionByCode(code);
        if (appSession != null) {
            cleanDataWhenClosed(newSession, false);
            newSession.close(CloseStatus.NORMAL);
            log.info("already has app session, close incoming session: {}", code);
            return;
        }
        String deviceId = helper.extractDeviceId(newSession);
        Optional<WsSession> deviceSession = appSessionManager.getSessionByDeviceId(deviceId);
        if (deviceSession.isPresent()) {
            log.info("same device has app session before, close old one, old code: {}, new code: {}", deviceSession.get().getCode(), code);
            appSessionManager.removeSession(deviceSession.get().getCode());
        }

        log.info("create new app session, code: {}, deviceid: {}", code, deviceId);
        AppSession as = new AppSession(newSession, WebSocketSessionScope.APP, code, deviceId);
        appSessionManager.addSession(code, as);

        //send scan code message
        WsSession scs = scanCodeSessionManager.getSessionByCode(code);
        if (scs != null) {
            log.info("send completion message to scancode session: {}", code);
            scs.sendData("completion");
        }

        //init metadata
        try {
            initMetadata(code, newSession);
        }catch (Exception e){
            log.error("init meta data failed! code:{}", code, e);
        }

        //send status
        sendMessageToPc(code, "设备已连接");
//        logConsumerManager.start(code, WebSocketSessionScope.APP);
        log();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("on afterConnectionClosed app");
        boolean closed = closeSession(appSessionManager, session, WebSocketSessionScope.APP);
        if (closed) {
            String code = helper.extractConversation(session);
            appSessionManager.removeSession(code);
            sendMessageToPc(code, "设备已断开");
            //调用报警接口
            buryPointAlertService.pointTestAlert(code);
//            logConsumerManager.stop(code, WebSocketSessionScope.APP);
        }
        log();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String s = helper.extractConversation(session);
        WsSession sessionByCode = appSessionManager.getSessionByCode(s);
        if (sessionByCode != null) {
            //parse and cache
            try {
                buryPointAnaysisService.parseBuryPointResource(s, message.getPayload(), (AppSession) sessionByCode);
            }catch (Exception e){
                log.error("parse bury point data failed! code:{}", s, e);
            }
//            ((AppStorage) sessionByCode.getStorage()).insertMessage(message.getPayload());
        }
    }

    @Override
    public String currentUriTemplate() {
        return WebSocketUriTemplate.APP_CONVERSATION_URI_TEMPLATE;
    }


    public void initMetadata(String conversation, WebSocketSession webSocketSession) {
        try {
            initBuryPointRule(conversation, webSocketSession);
        } catch (ParamException | RealTimeTestException e) {
            log.error("initMetadata error", e);
            throw e;
        } catch (RuntimeException e) {
            log.error("initMetadata2 error", e);
            throw e;
        }
    }

    private void initBuryPointRule(String conversation, WebSocketSession webSocketSession) {
        AppStorage appStorage = (AppStorage) appSessionManager.getSessionByCode(conversation).getStorage();
        // 非首次进入页面，无需重新初始化规则
        if (appStorage.getMetaInfo() != null) {
            log.info("非首次进入页面，conversation={}", conversation);
            return;
        }
        Map<String, Object> requestParameterMap = webSocketSession.getAttributes();
        String domainIdStr = requestParameterMap.get("domainId").toString();
        Long domainId = null;
        if (StringUtils.isNotEmpty(domainIdStr)) {
            domainId = Long.parseLong(domainIdStr);
        }
        if (domainId == null) {
            throw new ParamException("domainId 不能为空");
        }

        String appIdStr = requestParameterMap.get("appId").toString();
        Long appId = null;
        if (StringUtils.isNotEmpty(appIdStr)) {
            appId = Long.parseLong(appIdStr);
        }
        if (appId == null) {
            throw new ParamException("appId 不能为空");
        }


        String taskIdStr = requestParameterMap.get("taskId").toString();
        Long taskId = null;
        try {
            if (StringUtils.isNotEmpty(taskIdStr)) {
                taskId = Long.parseLong(taskIdStr);
            }
        }catch (Exception e){
            log.warn("taskId 不存在 conversation={} taskId={}", conversation, taskId, e);
        }


        String terminalIdStr = requestParameterMap.get("terminalId").toString();
        Long terminalId = null;
        if (StringUtils.isNotEmpty(terminalIdStr)) {
            terminalId = Long.parseLong(terminalIdStr);
        }

//        BuryPointMetaInfo buryPointMetaInfo = new BuryPointMetaInfo(bloodLinkService);
//        buryPointMetaInfo.initBuryPointRule(taskId, terminalId, domainId, appId);
//        BuryPointMetaInfoDto buryPointMetaInfoDto = BuryPointMetaInfoDto.builder()
//                .popovers(buryPointMetaInfo.getPopovers())
//                .conversationRuleMap(buryPointMetaInfo.getConversationRuleMap())
//                .conversationObjectMap(buryPointMetaInfo.getConversationObjectMap())
//                .conversationEventCodeToNameMap(buryPointMetaInfo.getConversationEventCodeToNameMap())
//                .conversationRoutePathToOidMap(buryPointMetaInfo.getConversationRoutePathToOidMap())
//                .eventRule(buryPointMetaInfo.getEventRule())
//                .build();
//        String key = BuryPointProcessorKey.getBuryPointMetaKey(conversation);
//        redisClusterService.set(key, JsonUtils.toJson(buryPointMetaInfoDto));

        BuryPointMetaInfo buryPointMetaInfo = new BuryPointMetaInfo(bloodLinkService);
        RealTimeTestResourceDTO realTimeTestResourceDTO = buryPointMetaInfo.initBuryPointRule(taskId, terminalId, domainId, appId);
        if (realTimeTestResourceDTO != null && StringUtils.isNotBlank(realTimeTestResourceDTO.getBaseLineName())) {
            log.info("conversation={} 基线名={}", conversation, realTimeTestResourceDTO.getBaseLineName());
            conversationBasicInfoService.setBaseLineName(conversation, realTimeTestResourceDTO.getBaseLineName());
        }
        appStorage.setMetaInfo(buryPointMetaInfo);
    }
}
