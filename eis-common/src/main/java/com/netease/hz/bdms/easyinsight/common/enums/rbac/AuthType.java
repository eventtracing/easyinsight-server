package com.netease.hz.bdms.easyinsight.common.enums.rbac;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 权限类型
 *
 * @author wangliangyuan
 * @date 2021-08-04 上午 11:56
 */
@Getter
@AllArgsConstructor
public enum AuthType {

    MENU(0, "菜单"),
    BUTTON(1, "按钮");

    private final Integer code;
    private final String desc;
}
