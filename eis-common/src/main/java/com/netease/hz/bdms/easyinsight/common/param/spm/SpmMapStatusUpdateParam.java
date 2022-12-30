package com.netease.hz.bdms.easyinsight.common.param.spm;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author: xumengqiang
 * @date: 2021/11/11 10:40
 */

@Data
@Accessors(chain = true)
public class SpmMapStatusUpdateParam {

    /**
     * spmId集合
     */
    @NotEmpty(message = "SPM ID集合不能为空")
    private List<Long> spmIds;

    /**
     * 映射状态
     * @see com.netease.hz.bdms.easyinsight.common.enums.SpmMapStatusEnum
     */
    @NotNull(message = "SPM映射状态不能为空")
    private Integer status;
}
