package com.netease.hz.bdms.easyinsight.common.vo.logcheck;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class SpmInfoVO {

    private String spm;
    private String spmName;
    private String currentOid;
    private String currentName;
    private String rootOid;
    private String rootName;
    private String ownerName;
    private long count;
}
