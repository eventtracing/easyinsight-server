package com.netease.hz.bdms.easyinsight.common.param.tag;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class CidTagInfo {

    private String cid;
    private String name;
}
