package com.netease.hz.bdms.easyinsight.common.dto.obj;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class SpmNodeDTO {

    private String oid;
    private ObjectInfoDTO objDetail;

    private String pos;
    private String spmCid;
    private String spmCidName;

    private String spm;
    private String spmNo;
    private String spmName;

    private String scm;
}
