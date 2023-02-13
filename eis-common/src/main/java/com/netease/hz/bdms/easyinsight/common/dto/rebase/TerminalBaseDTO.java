package com.netease.hz.bdms.easyinsight.common.dto.rebase;

import lombok.Data;

@Data
public class TerminalBaseDTO {

    Long terminalId;

    Long releaseId;

    boolean autoRebase;
}
