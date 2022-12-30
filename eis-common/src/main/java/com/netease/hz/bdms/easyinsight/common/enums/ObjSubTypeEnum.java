package com.netease.hz.bdms.easyinsight.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 细分的对象子类型
 */
@AllArgsConstructor
@Getter
public enum ObjSubTypeEnum {
    PAGE("page", ObjTypeEnum.PAGE, "页面"),
    PANEL("panel", ObjTypeEnum.POPOVER, "浮层"),
    MOD("mod", ObjTypeEnum.ELEMENT, "模块"),
    CELL("cell", ObjTypeEnum.ELEMENT, "卡片"),
    BTN("btn", ObjTypeEnum.ELEMENT, "按钮"),
    BRIDGE("bridge", ObjTypeEnum.ELEMENT, "桥梁"),
    LAYER("layer", ObjTypeEnum.ELEMENT, "老浮层"),
    UNKNOWN("unknown", ObjTypeEnum.ELEMENT, "未设置"),
    ;
    private String oidPrefix;
    private ObjTypeEnum defaultParentObjType;
    private String desc;

    public static ObjSubTypeEnum fromPrefix(String name) {
        for (ObjSubTypeEnum objTypeEnum : values()) {
            if (objTypeEnum.getOidPrefix().equals(name)) {
                return objTypeEnum;
            }
        }
        return UNKNOWN;
    }

    public static ObjSubTypeEnum matchOid(String oid) {
        for (ObjSubTypeEnum subTypeEnum : values()) {
            if (StringUtils.startsWith(oid, subTypeEnum.oidPrefix)) {
                return subTypeEnum;
            }
        }
        return UNKNOWN;
    }
}
