package com.netease.hz.bdms.easyinsight.common.enums;

import com.netease.hz.bdms.easyinsight.common.exception.ServerException;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 服务端接口类型
 */
@AllArgsConstructor
@Getter
public enum ServerAPITypeEnum {

    HTTP("HTTP"),
    RPC("RPC"),
    ;

    private String name;

    public static ServerAPITypeEnum fromName(String name) {
        for (ServerAPITypeEnum objTypeEnum : values()) {
            if (objTypeEnum.getName().equals(name)) {
                return objTypeEnum;
            }
        }
        throw new ServerException(name + "不能转换成ServerAPITypeEnum");
    }
}
