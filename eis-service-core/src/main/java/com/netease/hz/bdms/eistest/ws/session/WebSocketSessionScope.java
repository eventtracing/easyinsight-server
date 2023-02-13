package com.netease.hz.bdms.eistest.ws.session;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * WebSocketSession 的作用域
 *
 * @author wangliangyuan
 * @date 2021-09-08 上午 10:53
 */
@Getter
@AllArgsConstructor
public enum WebSocketSessionScope {
    QR_CODE("二维码"),
    PC("PC 端"),
    APP("移动端");

    /**
     * 作用域
     */
    private final String scope;
}
