package com.netease.hz.bdms.easyinsight.common.dto.spm;

import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true)
public class SpmMapRelationDTO {

    private Long id;

    /**
     * 对象spm字符串
     */
    private String spm;

    /**
     * 对象spm字符串
     */
    private String spmOld;

}
