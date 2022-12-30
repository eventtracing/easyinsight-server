package com.netease.hz.bdms.eistest.ws;

import com.netease.hz.bdms.eistest.ws.common.CustomWebSocketSessionAttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.server.WebServerException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;

/**
 * WebSocketSession Helper
 *
 * @author wangliangyuan
 * @date 2021-09-09 下午 04:20
 */
@Slf4j
@Component
public class WsHelper {

    /**
     * 发送 文本 格式的消息
     *
     * @param webSocketSession 会话
     * @param message          消息内容
     */
    public void sendTextMessage(WebSocketSession webSocketSession, String message) {
        sendWebSocketMessage(webSocketSession, new TextMessage(message));
    }

    private void sendWebSocketMessage(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage) {
        if (webSocketSession.isOpen()) {
            try {
                webSocketSession.sendMessage(webSocketMessage);
            } catch (IOException e) {
                log.error("webSocketSession[{}] failed to send message:{}, error:{}",
                        webSocketSession, webSocketMessage.getPayload(), e);
                throw new WebServerException("WebSocket 推送消息失败", e.getCause());
            }
        }
    }

    public String extractConversation(WebSocketSession webSocketSession) {
        Map<String, Object> attributes = webSocketSession.getAttributes();
        return attributes.get(CustomWebSocketSessionAttributeKey.CONVERSATION).toString();
    }

    public String extractScope(WebSocketSession webSocketSession) {
        return webSocketSession.getAttributes().get(CustomWebSocketSessionAttributeKey.SCOPE).toString();
    }

    public String extractDeviceId(WebSocketSession webSocketSession) {
        return webSocketSession.getAttributes().get(CustomWebSocketSessionAttributeKey.DEVICE_ID).toString();
    }

    public boolean extractCleanStorage(WebSocketSession webSocketSession) {
        return (boolean) webSocketSession.getAttributes()
                .get(CustomWebSocketSessionAttributeKey.CLEAN_STORAGE_ON_WEB_SOCKET_SESSION_CLOSED);
    }

    public void saveWebSocketSessionAttribute(WebSocketSession webSocketSession, String key, Object value) {
        this.saveWebSocketSessionAttribute(webSocketSession.getAttributes(), key, value);
    }

    public void saveWebSocketSessionAttribute(Map<String, Object> attributes, String key, Object value) {
        attributes.put(key, value);
    }
}
