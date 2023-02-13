package com.netease.hz.bdms.easyinsight.common.enums;

/**
 * Overmind任务的状态
 */
public enum VersionSourceStatusEnum {

    OTHER("其他", -9999),
    WAIT_FOR_TEST("待上线",10),
    WAIT_FOR_RELEASE("上线中",25),
    RELEASED("已上线",30),
    TERMINATED("已终止",35)
    ;

    private String desc;    // 描述
    private int type;   // 枚举值

    VersionSourceStatusEnum(String desc, int type) {
        this.desc = desc;
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public int getType() {
        return type;
    }

    public static VersionSourceStatusEnum valueOfType(Integer type) {
        if (type == null) {
            return OTHER;
        }
        for(VersionSourceStatusEnum value : values()) {
            if (type.equals(value.getType())) {
                return value;
            }
        }
        return OTHER;
    }
}
