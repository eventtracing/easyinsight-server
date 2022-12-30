package com.netease.hz.bdms.easyinsight.common.param.spm;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author: xumengqiang
 * @date: 2021/11/11 10:24
 */

@Data
@Accessors(chain = true)
public class SpmMapItemCreateParam {
    /**
     * 新的spm
     */
    private Long spmId;

    /**
     * 旧的spm
     */
    private List<String> spmOldList;
}
