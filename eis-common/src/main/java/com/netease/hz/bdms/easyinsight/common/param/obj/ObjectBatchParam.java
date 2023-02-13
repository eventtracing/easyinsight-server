package com.netease.hz.bdms.easyinsight.common.param.obj;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author: xumengqiang
 * @date: 2021/12/10 19:32
 */
@Data
@Accessors(chain = true)
public class ObjectBatchParam {

    /**
     * 对象id
     */
    private Long objId;

    /**
     * 变更历史ID
     */
    private Long historyId;

}
