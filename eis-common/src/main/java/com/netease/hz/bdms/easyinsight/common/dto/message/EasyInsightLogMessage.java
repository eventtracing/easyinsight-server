package com.netease.hz.bdms.easyinsight.common.dto.message;

import lombok.Data;

import java.util.Map;

@Data
public class EasyInsightLogMessage extends EasyInsightLogMessageLogCheckMeta {

    Map<String, Object> props;  //原始props
}
