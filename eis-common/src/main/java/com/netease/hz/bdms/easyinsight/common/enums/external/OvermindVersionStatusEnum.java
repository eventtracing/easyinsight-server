package com.netease.hz.bdms.easyinsight.common.enums.external;

/**
 * Overmind版本的状态
 * https://music-ox.hz.netease.com/ox/music/model/detail/350622
 */
public enum OvermindVersionStatusEnum {

    UNKNOWN("未知", -9999),
    UNUSED("无效",0),
    STARTED("已开始",5),
    WAIT_FOR_TEST("待测试",10),
    TESTED("测试通过",20),
    WAIT_FOR_RELEASE("上线中",25),
    RELEASED("已发布",30),
    TERMINATED("已终止",35),
    ;

    private String desc;    // 描述
    private int type;   // 枚举值

    OvermindVersionStatusEnum(String desc, int type) {
        this.desc = desc;
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public int getType() {
        return type;
    }

    public static OvermindVersionStatusEnum valueOfType(Integer type) {
        if (type == null) {
            return UNKNOWN;
        }
        for(OvermindVersionStatusEnum value : values()) {
            if (type.equals(value.getType())) {
                return value;
            }
        }
        return UNKNOWN;
    }
}
