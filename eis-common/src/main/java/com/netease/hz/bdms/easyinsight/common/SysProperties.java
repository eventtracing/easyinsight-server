package com.netease.hz.bdms.easyinsight.common;

import com.netease.hz.bdms.easyinsight.common.util.DateTimeUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@ConfigurationProperties(ignoreInvalidFields = true)
@Component
@Data
@Slf4j
public class SysProperties {

    private String env;
    private String startTime;
    private StoreProperties store;
    private AuthProperties auth;
    private EasyInsightProperties easyinsight;
    private OvermindProperties overmind;
    private HubbleProperties hubble;


    @PostConstruct
    public void init() {
        setStartTime(DateTimeUtils.getCurrent());
    }

    @Data
    public static class StoreProperties {
        private String accessKey;
        private String secretKey;
        /**
         * 桶名
         */
        private String bucketName;
        /**
         * 内网上传地址
         */
        private String internalUrl;
        /**
         * 外网显示地址
         */
        private String externalUrl;

    }

    @Data
    public static class AuthProperties {
        /**
         * 认证服务地址，用户使用
         */
        private String url;
        /**
         * 认证服务地址，内网使用
         */
        private String urlInner;
        /**
         * 本服务地址(用于交给认证服务重定向, 需办公网访问)
         */
        private String appIndex;
        /**
         * 本服务机房网地址, 用于认证服务向本服务发出登出请求
         */
        private String appClearUri;
        /**
         * 本服务地址域名*号后缀部分
         */
        private String appIndexSuffix;
    }

    @Data
    public static class EasyInsightProperties {
        /**
         * Image存储的介质
         * @see com.netease.hz.bdms.easyinsight.common.enums.ImageStoreEnum
         */
        private String imageStore;
        /**
         * 云音乐的路由平台
         */
        private String musicRoutingUrl;
    }

    @Data
    public static class OvermindProperties {
        /**
         * 访问地址
         */
        private String url;
        /**
         * 创建issue的访问地址
         */
        private String newIssueUrl;
        /**
         * 更新issue的访问地址
         */
        private String updateStsUrl;
    }

    @Data
    public static class HubbleProperties{
        private String accessKey;
    }
}
