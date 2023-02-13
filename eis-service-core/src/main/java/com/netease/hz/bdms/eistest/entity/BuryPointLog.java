package com.netease.hz.bdms.eistest.entity;

import lombok.Data;

@Data
public class BuryPointLog {
    private String action;
    private String content;
    private Long index;
    private Long logTime;
    private String os;
    private String logtype;
    private Boolean et;
}
