package com.netease.hz.bdms.eistest.entity;

import lombok.Data;

@Data
public class ClientBasicInfo {
    private Boolean appStore;
    private Boolean release;
    private String appVer;
    private Boolean dev;
    //用户id
    private String userid;
    //是否匿名
    private Boolean anonymous;
    private String username;
    private String platform;
    //设备型号
    private String deviceName;
    //设备品牌
    private String channel;
    private String appName;
    private String sysVer;
}
