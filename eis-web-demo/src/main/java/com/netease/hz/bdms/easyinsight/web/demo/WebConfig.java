package com.netease.hz.bdms.easyinsight.web.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.hz.bdms.easyinsight.common.util.JsonUtils;
import com.netease.hz.bdms.easyinsight.web.core.filter.ServletInputStreamFilter;
import com.netease.hz.bdms.easyinsight.web.core.interceptor.LogInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.Filter;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  /**
   * SpringBoot 的 Jackson 配置与静态工具类 {@link JsonUtils} 保持一致.
   */
  @Bean
  @Primary
  @ConditionalOnMissingBean(ObjectMapper.class)
  public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
    ObjectMapper objectMapper = builder.createXmlMapper(false).build();
    JsonUtils.init(objectMapper);
    return objectMapper;
  }

  @Bean
  public LogInterceptor getLogInterceptor() {
    return new LogInterceptor();
  }

  @Bean
  public DemoSessionInterceptor getSessionInterceptor() {
    return new DemoSessionInterceptor();
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    String[] excludeSessionCheckUrls = {
        "/", "/eis/v1/health/status",
        "/login", "/logon", "/check/login", "/logout", "/clear-token",
        "/eis/v1/config/get",
        "/eis/backdoor/v1/**",
        "/backend/**",
        "/et/v1/realtime/in/*",
        "/et/v1/realtime/branchCover/ignore/get",
        "/et/v1/realtime/checkresult/list",
        "/et/v1/realtime/validate/save",
        "/eis/test/*",
        "/connection/test",
        "/webhook/**",
        "/openapi/**",
        "/opt/*",
        "/et/v1/notify/message",
        "/realtime/test/history/save"
    };
    registry.addInterceptor(getLogInterceptor())
        .addPathPatterns("/**")
        .excludePathPatterns("/eis/v1/health/status")
        .order(1);
    // 越低优先级越高
    registry.addInterceptor(getSessionInterceptor())
        .addPathPatterns("/**")
        .excludePathPatterns(excludeSessionCheckUrls)
        .order(0);
  }
//
//  /**
//   * 配置 Spring Session 默认序列化方案
//   */
//  @Bean("springSessionDefaultRedisSerializer")
//  public RedisSerializer<Object> defaultRedisSession() {
//    return new GenericJackson2JsonRedisSerializer();
//  }

  /**
   * 注册过滤器
   *
   * @return FilterRegistrationBean
   */
  @Bean
  public FilterRegistrationBean someFilterRegistration() {
    FilterRegistrationBean registration = new FilterRegistrationBean();
    registration.setFilter(replaceStreamFilter());
    registration.addUrlPatterns("/*");
    registration.setName("streamFilter");
    return registration;
  }

  /**
   * 实例化StreamFilter
   *
   * @return Filter
   */
  @Bean(name = "streamFilter")
  public Filter replaceStreamFilter() {
    return new ServletInputStreamFilter();
  }
}
