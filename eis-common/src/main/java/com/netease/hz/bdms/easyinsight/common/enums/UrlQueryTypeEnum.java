package com.netease.hz.bdms.easyinsight.common.enums;

/**
 * Overmind任务的状态
 */
public enum UrlQueryTypeEnum {

    REQUIREMENT("查询需求链接", 1),
    TASK("查询任务链接", 2),
    ;

    private String desc;    // 描述
    private int type;   // 枚举值

    UrlQueryTypeEnum(String desc, int type) {
        this.desc = desc;
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public int getType() {
        return type;
    }

    public static UrlQueryTypeEnum valueOfType(Integer type) {
        if (type == null) {
            return REQUIREMENT;
        }
        for (UrlQueryTypeEnum value : values()) {
            if (type.equals(value.getType())) {
                return value;
            }
        }
        return REQUIREMENT;
    }
}
