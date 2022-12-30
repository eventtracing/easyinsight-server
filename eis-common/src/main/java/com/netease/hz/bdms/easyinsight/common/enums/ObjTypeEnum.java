package com.netease.hz.bdms.easyinsight.common.enums;

import com.netease.hz.bdms.easyinsight.common.exception.ServerException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ObjTypeEnum {
    PAGE(1, "page", "页面"),
    ELEMENT(2, "element","元素"),
    POPOVER(3, "popover", "浮层"),
    ;
    private Integer type;
    private String name;
    private String chineseName;

    public static ObjTypeEnum fromType(Integer type) {
        for (ObjTypeEnum objTypeEnum : values()) {
            if (objTypeEnum.getType().equals(type)) {
                return objTypeEnum;
            }
        }
        throw new ServerException(type + "不能转换成ObjTypeEnum");
    }

    @Override
    public String toString() {
        return "{type: " + type +
                ", name: " + name +
                ", chineseName: " + chineseName + "}";
    }
}
