package com.netease.hz.bdms.easyinsight.common.vo.auth;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class RoleVO {
    private Long id;
    private String roleName;
    private String description;
    private Timestamp createTime;
    private Timestamp updateTime;
}
