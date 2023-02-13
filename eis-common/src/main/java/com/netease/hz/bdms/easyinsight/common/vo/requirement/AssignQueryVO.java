package com.netease.hz.bdms.easyinsight.common.vo.requirement;

import lombok.Data;

import java.util.List;

@Data
public class AssignQueryVO {

    Long reqPoolId;

    /**
     * 是否勾选了"开启多端同步"
     */
    boolean syncAllTerminal;

    //待办列表
    List<AssignEntityVO> assignEntities;
}
