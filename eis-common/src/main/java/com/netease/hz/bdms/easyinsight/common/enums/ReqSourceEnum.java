package com.netease.hz.bdms.easyinsight.common.enums;

import com.netease.hz.bdms.easyinsight.common.exception.ServerException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReqSourceEnum {

    OVER_MIND(1,"Overmind"),
    CUSTOM(2,"自定义");

    private Integer type;
    private String name;

    public static ReqSourceEnum fromType(Integer type) {
        for (ReqSourceEnum reqSourceEnum : values()) {
            if (reqSourceEnum.getType().equals(type)) {
                return reqSourceEnum;
            }
        }
        throw new ServerException(type + "不能转换成OmPriorityEnum");
    }

}
