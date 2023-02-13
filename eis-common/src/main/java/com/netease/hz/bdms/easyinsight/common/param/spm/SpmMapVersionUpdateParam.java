package com.netease.hz.bdms.easyinsight.common.param.spm;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author: xumengqiang
 * @date: 2021/11/11 13:45
 */

@Data
@Accessors(chain = true)
public class SpmMapVersionUpdateParam {
    /**
     * spmId集合
     */
    private List<Long> spmIds;

    /**
     * 生效版本
     */
    private String version;
}
