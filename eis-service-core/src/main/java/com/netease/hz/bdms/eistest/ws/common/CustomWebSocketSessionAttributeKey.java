package com.netease.hz.bdms.eistest.ws.common;

import com.netease.hz.bdms.eistest.ws.session.WebSocketSessionScope;
import org.springframework.web.socket.WebSocketSession;

/**
 * 自定义 {@link WebSocketSession#getAttributes()} 这个 map 的 key
 *
 * @author wangliangyuan
 * @date 2021-09-08 下午 07:24
 */
public interface CustomWebSocketSessionAttributeKey {

    /**
     * 会话 ID 的路径变量
     */
    String CONVERSATION = "conversation";

    /**
     * {@link WebSocketSessionScope}
     */
    String SCOPE = "scope";

    /**
     * 移动端的设备 ID
     * {@link WebSocketSessionScope}
     */
    String DEVICE_ID = "deviceId";

    /**
     * "在相同的会话和相同的 scope的情况下,是否存在重复的连接" 的标识位
     */
    String REPEATED_CONNECTION_IN_SAME_CONVERSATION_AND_SCOPE = "repeatedConnectionInSameConversationAndScope";

    /**
     * "在会话不同的情况下,是否存在重复的设备 ID" 的标识位
     */
    String REPEATED_DEVICE_ID_IN_DIFFERENT_CONVERSATION = "repeatedDeviceIdInDifferentConversation";

    /**
     * WebSocketSession 关闭时, “是否清除存储” 的标识位
     */
    String CLEAN_STORAGE_ON_WEB_SOCKET_SESSION_CLOSED = "cleanStorageOnWebSocketSessionClosed";

}
