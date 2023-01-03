# 服务端埋点平台说明文档

## 0. 中间件依赖准备，并编写对应的Adapter代码适配该中间件

什么是Adapter?
每个公司的基建选型不一样，因此同一个类型的中间件也有多个实现方式。EasyInsight服务端剥离了具体实现，在埋点平台业务代码中使用Adapter模式。您使用时，只需要适配现有能力，实现Adapter的接口，就可以使用了。

#### 数据库准备

* MySQL数据库：IP、PORT、用户名、密码
* ElasticSearch：IP、PORT、用户名、密码

#### Redis或类似缓存组件，实现CacheAdapter

* 用于提高系统性能、实现分布式锁
* 示例中自带的DemoJedisCacheAdapter使用Jedis客户端实现了缓存

#### 通知中心，实现NotifyUserAdapter

* 用于给用户推送邮件、IM消息通知。若没有，则无消息通知功能
* 示例中自带的DemoUserNotifyAdapter为一个空实现，无任何消息通知功能。

#### 配置中心，实现RealtimeConfigAdapter

* 项目部分配置可支持实时修改
* 示例中自带的DemoRealtimeConfigAdapter为一个基于代码中硬编码返回固定配置的实现，没有使用配置中心。

#### 文件存储，实现FileUploadAdapter

* 项目中图片存储
* 示例中自带的DemoFileUploadAdapter未实现图床，而是直接返回了固定图片。

## 1. 域名准备

* 用户域名
* 后台域名

## 2. 后端DEMO部署

* 修改application.yml，替换MySQL数据库IP、PORT、用户名、密码，替换ElasticSearch IP、PORT、用户名、密码
* 修改eis-web-demo中的Adapter，形成自己的Java项目
* 在RealtimeConfigAdapter实现的对应配置中心中，把域名相关设置为上述域名：eis.http.host=用户域名 eis.backend-http.host 后台域名
* 基于该项目部署启动服务

## 3. 前端DEMO部署

* 请参考前端工程 music-easy-insight 部分

## 4. 域名及NGINX配置

* 转发规则如下
    * /* -> 前端
    * /api/* -> 去除/api后，发送到服务端
    * WebSocket相关API：去除/api后，发送到固定某一台服务端机器。（这是因为该功能基于内存，暂不支持分布式）
    * 参考配置如下:

```
     server {
        listen       80;   #监听端口
        
        server_name  easyinsight-demo.com;  #域名信息

        location / {
            proxy_set_header Host $host;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-From-IP $remote_addr;
            proxy_set_header X-Forwarded-Proto $scheme;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection "upgrade";
            proxy_pass http://localhost:8088;
        }

        location ^~ /api {
            rewrite ^/api/(.*) /$1 break;
            proxy_pass http://localhost:8081;
            proxy_set_header Host $host;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-From-IP $remote_addr;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        if ($request_uri ~* "^\/process\/realtime\/[\w]+\/([0-9]+).*") {
            set $defaultkey $1;
        }
        location ~* ^\/process\/realtime\/[\w]+\/([0-9]+).* {
            proxy_pass http://localhost:8081;
            proxy_http_version 1.1;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection "upgrade";
            proxy_read_timeout 1200s; 
        }
        location ^~ /api/processor {
            rewrite ^/api/(.*) /$1 break;
            proxy_pass http://localhost:8081;
            proxy_set_header Host $host;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-From-IP $remote_addr;
            proxy_set_header X-Forwarded-Proto $scheme;
        }
        location ^~ /api/processor/realtime/exam {
            rewrite ^/api/(.*) /$1 break;
            proxy_pass http://localhost:8081;
            proxy_set_header Host $host;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-From-IP $remote_addr;
            proxy_set_header X-Forwarded-Proto $scheme;
        }
    }
```

* 后台域名
    * /* -> 发送到API集群
    * 参考：
    *
```
 server {
    server_name easyinsight-backend;

    location / {
        proxy_pass http://localhost:8081
    }
}
```

## 5. 数据初始化

* A. MySQL
  * 方式1：
    * 直接向数据库导入建表语句+数据：执行demo中的init-with-demo-data.sql
  * 方式2：
    * 初始化数据库，执行DEMO中的建表语句：执行demo中的init-tables-only.sql
    * 初始化域、产品、权限等配置，并增加SYSTEM账号，用于快速体验功能：访问${后台域名}/api/init
    * 给产品增加DEMO内置参数：访问${后台域名}/api/init-meta?appId=${appId}，其中appId是/api/init生成的，一般第一次生成是1
* B. ElasticSearch（用于实时测试）
  *  请参考 elastic-search-mappings.txt 进行索引创建

## 6.访问用户域名，使用平台

* 提示插入数据库行报错，因为创建时间字段为null
  * 1、需要修改MySQL配置, 在MySQL的配置文件，在mysqld下添加 explicit_defaults_for_timestamp=OFF
  * 2、或者执行SQL，临时修改该配置`SET @@global.explicit_defaults_for_timestamp = off`