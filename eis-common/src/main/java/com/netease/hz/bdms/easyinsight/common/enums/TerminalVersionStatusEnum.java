package com.netease.hz.bdms.easyinsight.common.enums;

import com.netease.hz.bdms.easyinsight.common.exception.ServerException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TerminalVersionStatusEnum {
    /**
     * 待上线
     */
    DEVELOP(1),

    /**
     * 已上线
     */
    ONLINE(2),
    ;

    private Integer status;

    public static TerminalVersionStatusEnum fromStatus(Integer status) {
        for (TerminalVersionStatusEnum statusEnum : values()) {
            if (statusEnum.getStatus().equals(status)) {
                return statusEnum;
            }
        }
        throw new ServerException(status + "不能转换成TerminalVersionStatusEnum");
    }
}
