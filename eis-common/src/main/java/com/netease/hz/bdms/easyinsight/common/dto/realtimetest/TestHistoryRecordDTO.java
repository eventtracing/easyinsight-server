package com.netease.hz.bdms.easyinsight.common.dto.realtimetest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestHistoryRecordDTO {
    /**
     * session id
     */
    private Long code;

    /**
     * appId
     */
    private Long appId;

    /**
     * 测试uid
     */
    private Long userId;

    /**
     * 测试任务id
     */
    private Long taskId;

    /**
     * 测试用户名
     */
    private String userName;

    /**
     * 测试需求
     */
    private String reqName;

    /**
     * 测试基准版本
     */
    private String baseVersion;

    /**
     * 测试终端
     */
    private String terminal;

    /**
     * app版本
     */
    private String appVersion;

    /**
     * 失败数量
     */
    private Long failedNum;

    /**
     * (1:初始化 2:测试中 3:测试完成)
     */
    private Integer status;

    /**
     * (0:不通过 1:部分通过 2:通过)
     */
    private Integer testResult;

    /**
     * 扩展信息
     */
    private String extInfo;

    /**
     * 记录保存时间
     */
    private Long saveTime;

    /**
     * 跳转链接
     */
    private String targetUrl;

    /**
     * 保存时间
     */
    private Timestamp createTime;
    /**
     * 更新时间
     */
    private Timestamp updateTime;

}
