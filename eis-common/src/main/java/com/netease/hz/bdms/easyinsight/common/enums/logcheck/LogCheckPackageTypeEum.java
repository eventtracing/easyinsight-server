package com.netease.hz.bdms.easyinsight.common.enums.logcheck;

public enum LogCheckPackageTypeEum {

    ANDROID_GRAY(1, "Android灰度包"),
    ANDROID_TEST(2, "Android测试包"),
    ANDROID_RELEASE(3, "Android正式包"),
    IPHONE_TEST(4, "IPHONE测试包"),
    IPHONE_RELEASE(5, "IPHONE正式包"),
    WEB(6, "WEB"),
    ;

    private Integer result;

    private String desc;

    public Integer getResult() {
        return result;
    }

    public String getDesc() {
        return desc;
    }

    LogCheckPackageTypeEum(Integer result, String desc) {
        this.result = result;
        this.desc = desc;
    }

    public static LogCheckPackageTypeEum fromValue(Integer result) {
        for (LogCheckPackageTypeEum eum : values()) {
            if (eum.getResult().equals(result)) {
                return eum;
            }
        }
        return null;
    }
}
