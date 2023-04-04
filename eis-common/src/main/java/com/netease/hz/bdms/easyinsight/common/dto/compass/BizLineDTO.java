package com.netease.hz.bdms.easyinsight.common.dto.compass;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class BizLineDTO {
    /**
     * 租户ID
     */
    Long tenantId;
    /**
     * 业务线名称
     */
    String businessLine;
    /**
     * 业务线code
     */
    String businessLineCode;
    /**
     * 业务线ID
     */
    Long businessLineId;


}
