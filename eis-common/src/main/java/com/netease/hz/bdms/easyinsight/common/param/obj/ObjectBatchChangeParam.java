package com.netease.hz.bdms.easyinsight.common.param.obj;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author: xumengqiang
 * @date: 2021/12/10 19:32
 */
@Data
@Accessors(chain = true)
public class ObjectBatchChangeParam {

    /**
     * 对象oid信息
     */
    List<ObjectBatchParam> objectBatchParams;

    /**
     * reqpoolId
     */
    private Long reqPoolId;

}
