package com.netease.hz.bdms.easyinsight.common.vo.task;

import lombok.Data;

import java.util.List;

@Data
public class ReqTaskWithProcessViewVO {

    private ReqTaskVO reqTask;

    private List<TaskProcessSpmEntityVO> entities;

}
