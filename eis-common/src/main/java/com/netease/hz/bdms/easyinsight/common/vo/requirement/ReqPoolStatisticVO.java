package com.netease.hz.bdms.easyinsight.common.vo.requirement;

import lombok.Data;

@Data
public class ReqPoolStatisticVO {

    Integer allSpms;

    //未上线的spm待办数量（现在和字段名无关，之前是已指派的spm待办数量）
    Integer assignedSpms;

    Integer unAssignedSpms;

    Integer allEvents;
    //未上线的event待办数量（现在和字段名无关，之前是已指派的event待办数量）
    Integer assignedEvents;

    Integer unAssignedEvents;

    Integer tasks;

    Integer objCount;

}
