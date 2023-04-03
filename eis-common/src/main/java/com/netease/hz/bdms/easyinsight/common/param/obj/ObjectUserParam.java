package com.netease.hz.bdms.easyinsight.common.param.obj;

import lombok.Data;
import lombok.experimental.Accessors;
import java.util.List;


@Data
@Accessors(chain = true)
public class ObjectUserParam {

    /**
     * 埋点列表
     */
    private List<UserBuryPointParam> pointParams;

}
