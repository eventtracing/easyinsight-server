package com.netease.hz.bdms.easyinsight.common.vo.task;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class TaskProcessViewQueryVO {

    Long taskId;

    Integer status;

    String owner;

    String verifier;

    String objSearch;

}
