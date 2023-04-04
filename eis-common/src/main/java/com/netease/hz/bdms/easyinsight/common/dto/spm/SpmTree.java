package com.netease.hz.bdms.easyinsight.common.dto.spm;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SpmTree {

    private String oid;
    private String spm;
    private String parentOid;
    private String oidName;
    private String spmName;
    private Object value;

    private List<SpmTree> children;
    public SpmTree(String oid, String spm,String oidName, String spmName,Object value,String parentOid) {
        this.oid = oid;
        this.spm = spm;
        this.oidName = oidName;
        this.spmName =spmName;
        this.value=value;
        this.parentOid=parentOid;
    }

}
