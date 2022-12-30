package com.netease.hz.bdms.easyinsight.common.vo.terminalversion;

import lombok.Data;

@Data
public class VersionEdgeVO {

    Long from;

    Long to;

    Boolean isMaster;
}
