package com.netease.hz.bdms.easyinsight.common.vo.logcheck;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 参数key
 */
@Accessors(chain = true)
@Data
public class ParamKeyVO {

    private String oid;
    private String spm;
    private String paramCode;
}
