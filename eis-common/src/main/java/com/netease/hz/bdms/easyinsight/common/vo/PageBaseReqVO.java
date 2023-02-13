package com.netease.hz.bdms.easyinsight.common.vo;

import lombok.Data;

/**
 * @author: xumengqiang
 * @date: 2022/2/28 1:09
 */
@Data
public class PageBaseReqVO {
    /**
     * 默认每页25条.
     */
    protected Integer pageSize = 25;

    /**
     * 默认第1页.
     */
    protected Integer pageNum = 1;
}
