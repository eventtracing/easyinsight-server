package com.netease.hz.bdms.easyinsight.common.param.spm;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author: xumengqiang
 * @date: 2021/11/11 10:30
 */

@Data
@Accessors(chain = true)
public class SpmTagBindsParam {
    /**
     * spmId集合
     */
    private List<Long> spmIds;

    /**
     * tagId集合
     */
    private List<Long> tagIds;
}
