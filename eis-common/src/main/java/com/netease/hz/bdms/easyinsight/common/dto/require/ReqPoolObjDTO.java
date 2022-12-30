package com.netease.hz.bdms.easyinsight.common.dto.require;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class ReqPoolObjDTO {

    private Long reqPoolId;

    private Long objId;
}
