package com.netease.hz.bdms.easyinsight.web.demo;

import org.springframework.scheduling.annotation.EnableAsync;
import tk.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 主应用入口类
 */
@EnableScheduling
@SpringBootApplication
@EnableAsync
@EnableTransactionManagement
@ComponentScan(basePackages = {
        "com.netease.hz.bdms.easyinsight.web.core",
        "com.netease.hz.bdms.easyinsight.web.demo",
        "com.netease.hz.bdms.easyinsight.common",
        "com.netease.hz.bdms.easyinsight.service",
        "com.netease.hz.bdms.easyinsight.dao",
        "com.netease.hz.bdms.eistest"
})
@MapperScan(basePackages = {
        "com.netease.hz.bdms.easyinsight.dao"
})
public class DemoWebApplication {

  public static void main(String[] args) {
    SpringApplication.run(DemoWebApplication.class, args);
  }

}