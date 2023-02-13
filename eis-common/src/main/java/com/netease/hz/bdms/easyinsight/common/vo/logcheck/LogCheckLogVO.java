package com.netease.hz.bdms.easyinsight.common.vo.logcheck;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.Map;

/**
 * 稽查统计日志信息
 */
@Accessors(chain = true)
@Data
public class LogCheckLogVO {

    private String buildUUID;     // 所有查询都要带的必传字段，标明是查哪个包
    private String buildType;     // 可由buildUUID确定，放这里只是冗余一份
    private String spm;           // 筛选字段，日志中标明的spm
    private String spmName;       // spm名
    private String referSpm;      // refer的spm
    private String bizRefer;      // 筛选字段，BI定义的格式，带坑位等信息的SPM
    private String uid;           // 用户userId
    private String eventCode;     // 筛选字段，日志中标明的eventCode
    private String eventName;     // 日志中标明的eventCode对应的名字
    private String oid;           // 筛选字段，日志中标明的oid
    private String objName;       // 对象名称
    private String type;          // referType。如：e/p/orphues。。。
    /**
     * {@link} LogCheckResultEnum
     */
    private int checkResult;      // 0 校验成功 1 SPM未匹配 2 参数确实 3 参数取值错误 。。。
    private int causeCheckResult; // 0 无refer 1 校验通过 2 spm非法 3 SPM不合理 4 页面降级 。。。
    private long logTime;
    private long bucketHour;      // 日志时间，按小时取整
    private String bucketDate;    // 写入时间，按天取整，建表滚动需要
    private long createTime;      // 创建时间，写入DB时间
    private String logUUID;       // 日志唯一UUID，写入时由程序生成好，确保全局唯一
    private String os;            // os
    private String failKey;       // 日志失败分类标志
    private String appver;       // appver
    private Map<String, Object> content;       // 日志具体内容 JSON
}
