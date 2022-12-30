package com.netease.hz.bdms.easyinsight.common.vo.requirement;

import lombok.Data;

import java.util.List;

@Data
public class RebaseVO {

    Long reqPoolId;

    List<TerminalRebaseVO> details;

}
