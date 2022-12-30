package com.netease.hz.bdms.easyinsight.common.enums;

public enum TerminalCodeTypeEum {

    ANDROID(1, "Android"),
    IPHONE(2, "iPhone"),
    RN_DAWN_VIEW(3, "RN<DawnView>"),
    RN_VIEW(4, "RN<View>"),
    WEB_DAWN_DIV(5, "Web<DawnDiv>"),
    WEB_DIV(6, "Web<Div>"),
    ;

    private Integer result;

    private String desc;

    public Integer getResult() {
        return result;
    }

    public String getDesc() {
        return desc;
    }

    TerminalCodeTypeEum(Integer result, String desc) {
        this.result = result;
        this.desc = desc;
    }

    public static TerminalCodeTypeEum fromValue(Integer result) {
        for (TerminalCodeTypeEum eum : values()) {
            if (eum.getResult().equals(result)) {
                return eum;
            }
        }
        return null;
    }
}
