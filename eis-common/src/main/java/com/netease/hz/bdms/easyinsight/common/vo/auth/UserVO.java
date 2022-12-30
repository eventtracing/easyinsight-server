package com.netease.hz.bdms.easyinsight.common.vo.auth;

import lombok.Data;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

@Data
public class UserVO {
    private Long userId;
    private String email;
    private String userName;
    private Set<String> apps;
    private List<RoleVO> roles;
    private Long musicUserId;
    private Timestamp createTime;
    private Timestamp updateTime;
}
