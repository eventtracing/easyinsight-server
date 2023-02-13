package com.netease.hz.bdms.easyinsight.common.vo.logcheck;

import com.netease.hz.bdms.easyinsight.common.enums.BranchCoverageParamTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 分支覆盖
 */
@Accessors(chain = true)
@Data
public class BranchCoverageDetailVO extends SpmInfoVO {

    private String oid;           // 筛选字段，日志中标明的oid
    private String eventCode;           // 筛选字段，日志中标明的eventCode
    private String eventName;
    private String spm;           // 筛选字段，日志中标明的spm
    private String paramCode;          // 参数key
    private String paramName;          // 参数key名字
    /**
     * 覆盖参数类型
     * {@link BranchCoverageParamTypeEnum}
     */
    private Integer paramType;
    private String paramValue;          // 参数取值

    private String objName;
}
