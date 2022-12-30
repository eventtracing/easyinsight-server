package com.netease.hz.bdms.easyinsight.common.enums.logcheck.monitor;

/**
 * 定时执行模式
 */
public enum ExecuteAlarmTypeEnum {

    EVERYDAY,   // 每日
    EVERY_DAY_OF_WEEK,  // 每周指定周几执行
    ;

    public static ExecuteAlarmTypeEnum valueOfName(String name) {
        for (ExecuteAlarmTypeEnum value : ExecuteAlarmTypeEnum.values()) {
            if (value.name().equals(name)) {
                return value;
            }
        }
        return ExecuteAlarmTypeEnum.EVERYDAY;
    }
}
