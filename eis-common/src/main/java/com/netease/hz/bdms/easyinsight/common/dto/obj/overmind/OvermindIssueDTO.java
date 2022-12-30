package com.netease.hz.bdms.easyinsight.common.dto.obj.overmind;

import com.netease.hz.bdms.easyinsight.common.vo.TerminalVersion;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class OvermindIssueDTO {

    private Integer id;
    private String title;
    private Integer status;
    private Long createTime;
    private Long updateTime;
    private Long resolveTime;
    private Long closeTime;
    private String planStartTime;
    private String planReleaseTime;
    private String planSubmitTestTime;
    private String creator;
    private String creatorName;
    private String assignee;
    private String assigneeName;
    private String reporter;
    private String reporterName;
    private String verifier;
    private String verifierName;
    private String team;
    private String component;
    private String issueType;
    private Long createdAt;
    private Long updatedAt;
    private List<TerminalVersion> versionDTOS;
}
