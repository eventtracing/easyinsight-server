package com.netease.hz.bdms.easyinsight.common.vo.logcheck;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class SpmInfoWithFailKeyVO extends SpmInfoVO {

    /**
     * 错误分类key
     */
    private String failKey;
}
