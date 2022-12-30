package com.netease.hz.bdms.easyinsight.dao.model.checkresult;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 稽查结果日志
 */
@Accessors(chain = true)
@Data
public class EisParamValueAggregation {

    private String buildUUID;      // 所有查询都要带的必传字段，标明是查哪个包
    private long bucketHour;      // 日志时间，按小时取整
    private String bucketDate;    // 写入时间，按天取整，建表滚动需要
    private String oid;           // 筛选字段，日志中标明的oid
    private String paramKey;          // 枚举Key值
    private String paramValue;          // 枚举Value值
    private long logCount;          // 总数
}
