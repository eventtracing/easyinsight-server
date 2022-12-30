package com.netease.hz.bdms.easyinsight.common.enums;

/**
 * Overmind任务的状态
 */
public enum TaskSourceStatusEnum {

    OTHER("其他", -9999),
    START("开始", 1),
    TO_REVIEW("待评审", 12600),
    TO_ARRANGE("待排期", 12601),
    ARRANGED("已排期", 11301),
    ONLINE("已上线", 80025),
    LOOKED_BACK("已回顾", 80026),
    LOOKED_BACK_AND_ACCEPTED("已回顾验收", 80027),
    CLOSED("关闭", 6),
    ;

    private String desc;    // 描述
    private int type;   // 枚举值

    TaskSourceStatusEnum(String desc, int type) {
        this.desc = desc;
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public int getType() {
        return type;
    }

    public static TaskSourceStatusEnum valueOfType(Integer type) {
        if (type == null) {
            return OTHER;
        }
        for(TaskSourceStatusEnum value : values()) {
            if (type.equals(value.getType())) {
                return value;
            }
        }
        return OTHER;
    }
}
