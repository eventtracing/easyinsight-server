package com.netease.hz.bdms.easyinsight.common.dto.realtimetest;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Accessors(chain = true)
@Data
public class ResourceGenerateDTO {
    private Long appId;
    private List<String> reqList;
    private Long terminalId;
    private String versionId;
    private Long ruleVerion;
}
