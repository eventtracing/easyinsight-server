package com.netease.hz.bdms.easyinsight.common.dto.compass;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author wangyongliang
 * @version 1.0
 * @description: 分页基础请求
 * @date 2022/4/24 9:56
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class PageBaseReqDTO {

    /**
     * 默认每页25条.
     */
    protected Integer pageSize = 25;

    /**
     * 默认第1页.
     */
    protected Integer pageNum = 1;
}
