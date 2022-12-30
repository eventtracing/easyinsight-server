package com.netease.hz.bdms.easyinsight.common.enums.rbac;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 角色类型
 *
 * @author wangliangyuan
 * @date 2021-08-02 下午 05:22
 */
@Getter
@AllArgsConstructor
public enum RoleTypeEnum {

    PLATFORM(0, "平台角色"),
    DOMAIN(1, "域角色"),
    APP(2, "应用(产品)角色");

    private final Integer code;
    private final String desc;

    public static RoleTypeEnum match(Integer code) {
        return Arrays.stream(values())
                .filter(item -> code.equals(item.getCode()))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("无效的code:" + code));
    }
}
