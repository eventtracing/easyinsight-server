package com.netease.hz.bdms.easyinsight.common.enums;

import com.netease.hz.bdms.easyinsight.common.exception.ServerException;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 对象类型大类
 */
@AllArgsConstructor
@Getter
public enum ObjTypeNamespaceEnum {

    CLIENT("client"),
    SERVER("server"),
    ;

    private String name;

    public static ObjTypeNamespaceEnum fromName(String name) {
        for (ObjTypeNamespaceEnum objTypeEnum : values()) {
            if (objTypeEnum.getName().equals(name)) {
                return objTypeEnum;
            }
        }
        throw new ServerException(name + "不能转换成ObjTypeNamespaceEnum");
    }
}
