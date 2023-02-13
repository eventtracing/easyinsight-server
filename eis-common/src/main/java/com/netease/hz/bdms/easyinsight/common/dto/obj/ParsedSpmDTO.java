package com.netease.hz.bdms.easyinsight.common.dto.obj;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

@Accessors(chain = true)
@Data
public class ParsedSpmDTO {

    /**
     * 注意是从根节点开始向下，与SPM中OID顺序相反
     */
    private List<SpmNodeDTO> nodes;

    private String spm;

    private String spmNo;

    private String spmName;

}
