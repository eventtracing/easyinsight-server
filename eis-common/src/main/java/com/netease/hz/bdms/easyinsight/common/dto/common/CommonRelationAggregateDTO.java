package com.netease.hz.bdms.easyinsight.common.dto.common;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author: xumengqiang
 * @date: 2022/1/4 11:05
 */
@Data
@Accessors(chain = true)
public class CommonRelationAggregateDTO {
    /**
     * 关联信息的值
     */
    private String associatedKey;

    /**
     * 传给后端的值
     */
    private String key;

    /**
     * 返回给前端的名称
     */
    private String value;
}
