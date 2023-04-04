package com.netease.hz.bdms.eistest.service.es;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.io.IOException;

@Slf4j
@Configuration
public class ElasticsearchClientConfig {

    @Resource
    private ElasticsearchClientBean properties;

    private RestHighLevelClient client;
    private RestClient restClient;

    @Bean
    public RestHighLevelClient getRestHighLevelClient() {
        HttpHost[] hosts = properties.getHosts().stream().map(h -> HttpHost.create(h)).toArray(HttpHost[]::new);
        //基础身份验证 设置账户密码时打开
//        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
//        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(properties.getUsername(), properties.getPassword()));
        client = new RestHighLevelClient(
                RestClient.builder(hosts)
//                        // 身份验证 设置账户密码时打开
//                        .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.disableAuthCaching()
//                                .setDefaultCredentialsProvider(credentialsProvider)
//                                .setDefaultIOReactorConfig(IOReactorConfig.custom().setIoThreadCount(8).build())
//                        )
                        .setRequestConfigCallback(requestConfigBuilder ->
                                requestConfigBuilder.setConnectTimeout(10000)
                                        .setSocketTimeout(300000)
                                        .setConnectionRequestTimeout(10000)
                        )
        );
        int tryLimit = 0;
        while (tryLimit < 3) {
            log.info("Connecting Elasticsearch...");
            try {
                if (client.ping(RequestOptions.DEFAULT)) {
                    log.info("Elasticsearch Connected!");
                    break;
                }
            } catch (Exception e) {
                System.out.println("Connecting Elasticsearch Failed, Retry in 1 seconds...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    e.printStackTrace();
                }
                tryLimit++;
            }
        }
        return client;
    }

    public void close() {
        if(client != null) {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
