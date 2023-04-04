package com.netease.hz.bdms.easyinsight.common.enums;

import com.netease.hz.bdms.easyinsight.common.exception.CommonException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TrackerContentTypeEnum {

    /**
     * 服务端API信息
     */
    SERVER_API_INFO("serverAPIInfo"),
    ;

    private final String type;

    public static TrackerContentTypeEnum of(String t) {
        for (TrackerContentTypeEnum value : TrackerContentTypeEnum.values()) {
            if (value.getType().equals(t)) {
                return value;
            }
        }
        throw new CommonException("转换TrackerContentTypeEnum失败 : " + t);
    }

}
