package com.netease.hz.bdms.easyinsight.common.enums;

import com.netease.hz.bdms.easyinsight.common.exception.ServerException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OmPriorityEnum {

    PS("-1"), P0("1"), P1("3"), P2("4"), P3("5");

    String priorityNum;

    public static OmPriorityEnum fromType(String priorityNum) {
        for (OmPriorityEnum omPriority : values()) {
            if (omPriority.getPriorityNum().equals(priorityNum)) {
                return omPriority;
            }
        }
        throw new ServerException(priorityNum + "不能转换成OmPriorityEnum");
    }

}
