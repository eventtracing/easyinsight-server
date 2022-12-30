package com.netease.hz.bdms.easyinsight.common.dto.message;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * 通知内容
 *
 * @author: xumengqiang
 * @date: 2021/10/18 19:07
 */
@Data
@Accessors(chain = true)
public class NotifyContentDTO {
    // 产品ID
    Long appId;

    // 需求ID
    Long requireId;

    // 需求名称
    String requireName;

    // 需求IssueKey
    String requireIssueKey;

    // 任务ID
    Long taskId;

    // 任务信息
    String taskName;

    // 任务状态
    Integer status;

    // 端版本名称
    String terminalVersionName;

    // 终端ID
    Long terminalId;

    // 终端名称
    String terminalName;

    // 接收人邮箱
    String userEmail;

    // 接收人姓名
    String userName;

    // 待办项: key为SPM状态，value为对应SPM数量
    Map<Integer, Integer> backlog;
}
