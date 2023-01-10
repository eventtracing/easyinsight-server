package com.netease.hz.bdms.eistest.ws.common;

/**
 * WebSocket Uri 里的路径变量,
 * 例如 ws://xxx.com/process/realtime/scancode/22076 里的 22076 就是一个路径变量
 *
 * @author wangliangyuan
 * @date 2021-09-07 下午 05:12
 */
public interface WebSocketUriTemplate {

    /**
     * PC 端二维码关联的 WebSocket 会话的 uri 模板
     */
    String QR_CODE_CONVERSATION_URI_TEMPLATE = "/process/realtime/scancode/{" + CustomWebSocketSessionAttributeKey.CONVERSATION + "}";

    /**
     * 移动端关联的 WebSocket 会话的 uri 模板
     */
    String APP_CONVERSATION_URI_TEMPLATE = "/process/realtime/app/{" + CustomWebSocketSessionAttributeKey.CONVERSATION + "}/{" + "domainId" + "}/{" + "appId" + "}/{" + "taskId" + "}/{" + "terminalId" + "}/{" + CustomWebSocketSessionAttributeKey.DEVICE_ID + "}";

    /**
     * PC 端关联的 WebSocket 会话的 uri 模板
     */
    String PC_CONVERSATION_URI_TEMPLATE = "/process/realtime/fronted/{" + CustomWebSocketSessionAttributeKey.CONVERSATION + "}";

}