package com.netease.hz.bdms.easyinsight.common.vo.task;

import lombok.Data;

import java.util.List;

@Data
public class TransStatusVO {

    List<Long> processIds;

    List<Long> taskIds;

    Integer targetStatus;

}
