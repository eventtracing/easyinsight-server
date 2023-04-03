package com.netease.hz.bdms.easyinsight.dao.model.checkresult;

import com.netease.hz.bdms.easyinsight.common.enums.BranchCoverageParamTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 稽查结果日志
 */
@Accessors(chain = true)
@Data
public class EisBranchCoverage {

    private String buildUUID;      // 所有查询都要带的必传字段，标明是查哪个包
    private long bucketHour;      // 日志时间，按小时取整
    private String bucketDate;    // 写入时间，按天取整，建表滚动需要
    private String oid;           // 筛选字段，日志中标明的oid
    private String eventCode;           // 筛选字段，日志中标明的eventCode
    private String spm;           // 筛选字段，日志中标明的spm
    private String ruleVer;
    /**
     * 覆盖参数类型
     * {@link BranchCoverageParamTypeEnum}
     */
    private Integer paramType;
    private String paramCode;          // 参数key
    private String paramValue;          // 参数取值
    private long logCount;          // 总数
}
