package com.netease.hz.bdms.easyinsight.common.vo.release;

import lombok.Data;

import java.util.Set;

@Data
public class ReleaseVO {

    Long terminalId;

    Long terminalVersionId;

    Set<Long> taskIds;

}
