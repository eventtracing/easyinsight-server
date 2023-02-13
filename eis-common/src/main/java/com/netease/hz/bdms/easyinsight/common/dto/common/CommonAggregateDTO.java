package com.netease.hz.bdms.easyinsight.common.dto.common;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author: xumengqiang
 * @date: 2021/10/12 13:39
 */
@Data
@Accessors(chain = true)
public class CommonAggregateDTO {
    /**
     * 选中时，传递给后端的值
     */
    private String key;
    /**
     * 前端显示值
     */
    private String value;
}
