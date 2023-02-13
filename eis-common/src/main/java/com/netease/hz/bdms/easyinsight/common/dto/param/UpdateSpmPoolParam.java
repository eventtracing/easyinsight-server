package com.netease.hz.bdms.easyinsight.common.dto.param;

import com.netease.hz.bdms.easyinsight.common.enums.OperationTypeEnum;
import lombok.Data;

@Data
public class UpdateSpmPoolParam {

    private Long trackerId;
    private OperationTypeEnum operationTypeEnum;
    boolean isEdit;
}
