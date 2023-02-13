package com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.statistic;

import com.netease.hz.bdms.easyinsight.common.enums.BuryPointErrorCategoryEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实时测试中的错误信息
 *
 * @author wangliangyuan
 * @date 2021-08-26 下午 05:44
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorMessageSimpleDTO {

    /**
     * 这三个字段取自 {@link BuryPointErrorCategoryEnum}
     */
    private String key;
    private Integer code;
    private String category;

    /**
     * 总数
     */
    private Integer count;
}
