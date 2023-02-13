package com.netease.hz.bdms.eistest.ws.config;

import com.netease.hz.bdms.eistest.ws.SessionManager;
import com.netease.hz.bdms.eistest.ws.common.WebSocketUriTemplate;
import com.netease.hz.bdms.eistest.ws.config.EasyInsightWebSocketProperties;
import com.netease.hz.bdms.eistest.ws.handler.AppWsHandler;
import com.netease.hz.bdms.eistest.ws.handler.PcWsHandler;
import com.netease.hz.bdms.eistest.ws.handler.ScanWsHandler;
import com.netease.hz.bdms.eistest.ws.session.WebSocketSessionScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

/**
 * WebSocket 配置类
 *
 * @author wangliangyuan
 * @date 2021-09-07 下午 02:51
 */
@Slf4j
@Configuration
@EnableWebSocket
@EnableConfigurationProperties({EasyInsightWebSocketProperties.class})
public class WebSocketConfig implements WebSocketConfigurer {
    @Autowired
    private EasyInsightWebSocketProperties webSocketProperties;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        webSocketHandlerRegistry
                // 第二个参数等同于 @ServerEndpoint 这个注解的 value 属性值
                .addHandler(scanWsHandler(), WebSocketUriTemplate.QR_CODE_CONVERSATION_URI_TEMPLATE)
                .addHandler(appWsHandler(), WebSocketUriTemplate.APP_CONVERSATION_URI_TEMPLATE)
                .addHandler(pcWsHandler(), WebSocketUriTemplate.PC_CONVERSATION_URI_TEMPLATE)
                .setAllowedOrigins(webSocketProperties.allowedOriginsToArray())
        ;
    }

    @Bean
    public AppWsHandler appWsHandler() {
        AppWsHandler appWsHandler = new AppWsHandler();
        log.info("AppWsHandler registered. pattern={}", appWsHandler.currentUriTemplate());
        return appWsHandler;
    }

    @Bean
    public PcWsHandler pcWsHandler() {
        PcWsHandler pcWsHandler = new PcWsHandler();
        log.info("PcWsHandler registered. pattern={}", pcWsHandler.currentUriTemplate());
        return pcWsHandler;
    }

    @Bean
    public ScanWsHandler scanWsHandler() {
        ScanWsHandler scanWsHandler = new ScanWsHandler();
        log.info("ScanWsHandler registered. pattern={}", scanWsHandler.currentUriTemplate());
        return scanWsHandler;
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxSessionIdleTimeout(webSocketProperties.maxSessionIdleTimeoutToMillis());
        container.setMaxTextMessageBufferSize(webSocketProperties.getMaxTextMessageBufferSize());
        container.setMaxBinaryMessageBufferSize(webSocketProperties.getMaxBinaryMessageBufferSize());
        return container;
    }

    @Bean
    public SessionManager scanCodeSessionManager() {
        return new SessionManager(WebSocketSessionScope.QR_CODE);
    }

    @Bean
    public SessionManager appSessionManager() {
        return new SessionManager(WebSocketSessionScope.APP);
    }

    @Bean
    public SessionManager pcSessionManager() {
        return new SessionManager(WebSocketSessionScope.PC);
    }
}
