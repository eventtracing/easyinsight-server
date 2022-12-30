package com.netease.hz.bdms.eistest.ws.config;

import com.google.common.collect.Sets;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.socket.server.support.OriginHandshakeInterceptor;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * webSocket 配置类
 *
 * @author wangliangyuan
 * @date 2021-09-10 下午 07:25
 */
@Data
@ConfigurationProperties(prefix = "eis.web-socket")
public class EasyInsightWebSocketProperties {

    /**
     * 允许连接后端 webSocket 的 Origin (request header 里的一个字段)
     * <p>
     * 默认 "*" 表示允许所有的 Origin 连接
     * <p>
     * 元素值必须符合规定的格式: scheme://域名
     * 例如: http://abc.def.com
     * <p>
     * 原理见 {@link OriginHandshakeInterceptor#beforeHandshake(org.springframework.http.server.ServerHttpRequest, org.springframework.http.server.ServerHttpResponse, org.springframework.web.socket.WebSocketHandler, java.util.Map)}
     */
    private Set<String> allowedOrigins = Sets.newHashSet("*");

    /**
     * webSocket Session 的最大闲置时间, 单位:秒
     * <p>
     * 可以查看原理
     *
     * <p>
     * 默认: 28800秒 = 8小时
     */
    private Long maxSessionIdleTimeoutInSecond = 28800L;

    /**
     * 单个 文本消息 的最大字节数限制
     * <p>
     * 默认 8192*2
     */
    private Integer maxTextMessageBufferSize = 8192 * 2;

    /**
     * 单个 二进制 消息的最大字节数限制
     * <p>
     * 默认 8192*2
     */
    private Integer maxBinaryMessageBufferSize = 8192 * 2;


    public String[] allowedOriginsToArray() {
        return this.allowedOrigins.toArray(new String[this.allowedOrigins.size()]);
    }

    /**
     * 获取 webSocket Session 的最大过期时间
     *
     * @return 以 毫秒 为单位返回
     */
    public Long maxSessionIdleTimeoutToMillis() {
        return TimeUnit.SECONDS.toMillis(getMaxSessionIdleTimeoutInSecond());
    }
}
