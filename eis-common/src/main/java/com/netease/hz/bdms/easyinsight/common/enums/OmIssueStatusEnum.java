package com.netease.hz.bdms.easyinsight.common.enums;

import com.netease.hz.bdms.easyinsight.common.exception.ServerException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OmIssueStatusEnum {

    NOT_START("未开始",-1),
    START("开始", 1),
    FIXING("解决中", 3),
    UN_FIX("未解决", 4),
    FIXED("已解决", 5),
    CLOSED("关闭", 6),
    FINISHED("已完成", 12102),
    ARRANGED("已排期", 11301),
    PLANNING("策划中", 10616),
    INTERACTING("交互中", 10617),
    VISION("视觉中", 10301),
    VERIFYING("验证中", 10303),
    REQUIREMENT_POOL("需求池", 10410),
    DEVELOPING("开发中", 10600),
    WAITING_RELEASE("待上线", 10605),
    CODE_REVIEW("代码审核", 11113),
    WAITING_REVIEW("待评审", 12600),
    WAITING_ARRANGE("待排期", 12601),
    CANCELLED("已取消", 11100);

    private String statusName;

    private Integer statusCode;

    public static OmIssueStatusEnum fromType(Integer statusCode) {
        for (OmIssueStatusEnum omIssueStatusEnum : values()) {
            if (omIssueStatusEnum.getStatusCode().equals(statusCode)) {
                return omIssueStatusEnum;
            }
        }
        throw new ServerException(statusCode + "不能转换成OmIssueStatusEnum");
    }

}
