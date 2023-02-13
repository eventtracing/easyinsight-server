package com.netease.hz.bdms.easyinsight.common.param.spm;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author: xumengqiang
 * @date: 2021/11/11 14:56
 */

@Data
@Accessors(chain = true)
public class SpmMapNoteUpdateParam {


    private Long spmId;

    /**
     * spm备注
     */
    private String note;
}
