package com.netease.hz.bdms.easyinsight.common.dto.rebase;

import lombok.Data;

import java.util.List;

@Data
public class RebaseDTO {

    Long reqPoolId;

    List<TerminalBaseDTO> details;

}
