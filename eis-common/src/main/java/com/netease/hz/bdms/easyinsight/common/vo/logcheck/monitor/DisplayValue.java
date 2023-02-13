package com.netease.hz.bdms.easyinsight.common.vo.logcheck.monitor;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class DisplayValue {

    private String name;
    private String value;
}
