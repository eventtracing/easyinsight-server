package com.netease.hz.bdms.easyinsight.dao.model.checkresult;

import lombok.Data;

/**
 * 稽查结果日志，由EisCheckResultLog聚合而成（物化视图）
 * Music_EasyInsight_ClientLogBasic_mv
 */
@Data
public class EisCheckResultAggregation {

    private String buildUUID;     // 所有查询都要带的必传字段，标明是查哪个包
    private String spm;           // 筛选字段，日志中标明的spm
    private String referSpm;      // refer的spm
    private String bizRefer;      // 筛选字段，BI定义的格式，带坑位等信息的SPM
    private String eventCode;     // 筛选字段，日志中标明的eventCode
    private String oid;           // 筛选字段，日志中标明的oid
    private String type;          // 0 普通日志 1 纯事件埋点日志
    private int checkResult;      // 0 校验成功 1 SPM未匹配 2 参数确实 3 参数取值错误 。。。
    private int causeCheckResult; // 0 无refer 1 校验通过 2 spm非法 3 SPM不合理 4 页面降级 。。。
    private long bucketHour;      // 日志时间，按小时取整
    private String bucketDate;    // 写入时间，按天取整，建表滚动需要
    private long totalLogCount;
}
