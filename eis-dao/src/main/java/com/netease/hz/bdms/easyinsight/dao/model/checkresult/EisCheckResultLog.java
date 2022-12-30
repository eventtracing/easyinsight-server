package com.netease.hz.bdms.easyinsight.dao.model.checkresult;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 稽查结果日志
 * Music_EasyInsight_ClientLogBasic
 */
@Accessors(chain = true)
@Data
public class EisCheckResultLog {

    private String buildUUID;     // 所有查询都要带的必传字段，标明是查哪个包
    private String buildType;     // 可由buildUUID确定，放这里只是冗余一份
    private String spm;           // 筛选字段，日志中标明的spm
    private String referSpm;      // refer的spm
    private String bizRefer;      // 筛选字段，BI定义的格式，带坑位等信息的SPM
    private String uid;           // 用户userId
    private String eventCode;     // 筛选字段，日志中标明的eventCode
    private String oid;           // 筛选字段，日志中标明的oid
    private String type;          // 0 普通日志 1 纯事件埋点日志
    private int checkResult;      // 0 校验成功 1 SPM未匹配 2 参数确实 3 参数取值错误 。。。
    private int causeCheckResult; // 0 无refer 1 校验通过 2 spm非法 3 SPM不合理 4 页面降级 。。。
    private long logTime;       // 日志时间，秒级，用作筛选条件以及TTL
    private long logTimeMs;     // 日志时间，精确到ms，用作排序
    private long bucketHour;      // 日志时间，按小时取整
    private String bucketDate;    // 写入时间，按天取整，建表滚动需要
    private Date createTime;      // 创建时间，写入DB时间
    private String logUUID;       // 日志唯一UUID，写入时由程序生成好，确保全局唯一
    private long logCount;        // 详情表中，一定是1
    private String content;       // 日志具体内容 JSON
    private String failKey;       // 失败描述
    private String appver;       // appver
    private String os;            // os
}
