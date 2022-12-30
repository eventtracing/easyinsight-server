package com.netease.hz.bdms.easyinsight.common.dto.logcheck;

import com.netease.hz.bdms.easyinsight.common.enums.BranchCoverageParamTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 分支覆盖
 */
@Accessors(chain = true)
@Data
public class BranchCoverageDTO {

    private String oid;           // 参数是哪个oid的参数
    private String eventCode;     // 日志的eventCode
    private String spm;           // 日志的spm
    /**
     * 覆盖参数类型
     * {@link BranchCoverageParamTypeEnum}
     */
    private Integer paramType;
    private String paramCode;          // 参数key
    private String paramValue;          // 参数取值
}
