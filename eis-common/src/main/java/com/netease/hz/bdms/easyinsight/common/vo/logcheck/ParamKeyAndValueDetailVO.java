package com.netease.hz.bdms.easyinsight.common.vo.logcheck;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 参数取值
 */
@Accessors(chain = true)
@Data
public class ParamKeyAndValueDetailVO extends SpmInfoVO {

    private String oid;
    private String objName;
    private String paramCode;
    private String paramName;
    private String paramValue;
}
