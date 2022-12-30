package com.netease.hz.bdms.eistest.entity;

import com.netease.hz.bdms.easyinsight.common.enums.BuryPointErrorCategoryEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * {@link BuryPointErrorContent}的扩展类
 *
 * @author wangliangyuan
 * @date 2021-09-08 下午 07:54
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BuryPointErrorContentExpand {

    /**
     * 异常日志 json 字段里的 code
     */
    private Integer code;
    /**
     * {@link BuryPointErrorCategoryEnum}
     */
    private String category;
    /**
     * 时间戳
     */
    private Long timestamp;
    /**
     * 原始的埋点日志
     */
    private BuryPointErrorContent log;
}
