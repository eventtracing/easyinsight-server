package com.netease.hz.bdms.easyinsight.common.vo.logcheck;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 参数取值
 */
@Accessors(chain = true)
@Data
public class ParamKeyAndValueVO {

    private ParamKeyVO key;
    private String value;
}
