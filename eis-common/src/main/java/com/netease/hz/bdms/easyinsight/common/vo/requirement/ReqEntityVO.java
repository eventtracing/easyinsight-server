package com.netease.hz.bdms.easyinsight.common.vo.requirement;

import lombok.Data;

import java.util.List;

@Data
public class ReqEntityVO {

    Long reqPoolId;

    RequirementInfoVO requirement;

    List<TaskEntityVO> tasks;

}
