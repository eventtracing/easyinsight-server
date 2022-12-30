package com.netease.hz.bdms.eistest.entity;

/**
 * 埋点测试页中展示
 */
public class TestDetailInfo extends ClientBasicInfo {

    private String tester;
    private String testerEmail;
    private String baseLineName;

    public String getTester() {
        return tester;
    }

    public void setTester(String tester) {
        this.tester = tester;
    }

    public String getTesterEmail() {
        return testerEmail;
    }

    public void setTesterEmail(String testerEmail) {
        this.testerEmail = testerEmail;
    }

    public String getBaseLineName() {
        return baseLineName;
    }

    public void setBaseLineName(String baseLineName) {
        this.baseLineName = baseLineName;
    }
}
