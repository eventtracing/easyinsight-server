package com.netease.hz.bdms.easyinsight.common.vo.requirement;

import lombok.Data;

import java.util.List;

@Data
public class ReqEditShowVO {

    ReqShowVO reqInfo;

    List<TaskShowVO> tasks;

}
