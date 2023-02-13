package com.netease.hz.bdms.eistest.service.es;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ElasticsearchClientBean {

    @Value("${elasticsearch-client.hosts}")
    private String hosts;

    @Value("${elasticsearch-client.username}")
    private String username;

    @Value("${elasticsearch-client.password}")
    private String password;

    public List<String> getHosts() {
        return Arrays.asList(hosts.split(","));
    }

    public void setHosts(String hosts) {
        this.hosts = hosts;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "ElasticsearchClientBean{" +
                "hosts=" + hosts +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}