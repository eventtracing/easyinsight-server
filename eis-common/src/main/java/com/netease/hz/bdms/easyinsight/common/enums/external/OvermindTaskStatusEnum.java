package com.netease.hz.bdms.easyinsight.common.enums.external;

/**
 * Overmind任务的状态
 */
public enum OvermindTaskStatusEnum {

    UNKNOWN("未知", -9999),
    START("开始", 1),
    DOING("进行中", 10300),
    CANCEL("取消", 11100),
    ARRANGED("已排期", 11301),
    TESTED("测试通过", 12102),
    REVIEWED("review完成", 12200),
    TO_REVIEW("待评审", 12600),
    TO_ARRANGE("待排期", 12601),
    TO_BE_A_PROJECT("待立项", 12819),
    TO_BE_ACCEPTED("待验收", 12820),
    PROJECT_COMPLETED("完成结项", 12821),
    PROJECT_TO_BE_COMPLETED("待结项", 12823),
    RESOLVING("解决中", 3),
    REOPEN("重新打开", 4),
    DONE("已解决", 5),
    CLOSED("关闭", 6),
    ONLINE("已上线", 80025),
    LOOKED_BACK("已回顾", 80026),
    LOOKED_BACK_AND_ACCEPTED("已回顾验收", 80027),
    TERMINATED("终止", 80035),
    NOT_EXECUTED("未执行", 80041),
    IGNORED("忽略", 80042),
    SELF_TESTED("自测通过", 80043),
    TEST_REJECTED("测试不通过", 80044),
    ;

    private String desc;    // 描述
    private int type;   // 枚举值

    OvermindTaskStatusEnum(String desc, int type) {
        this.desc = desc;
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public int getType() {
        return type;
    }

    public static OvermindTaskStatusEnum valueOfType(Integer type) {
        if (type == null) {
            return UNKNOWN;
        }
        for(OvermindTaskStatusEnum value : values()) {
            if (type.equals(value.getType())) {
                return value;
            }
        }
        return UNKNOWN;
    }
}
