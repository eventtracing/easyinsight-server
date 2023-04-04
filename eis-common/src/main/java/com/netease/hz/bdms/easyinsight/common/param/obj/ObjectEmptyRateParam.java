package com.netease.hz.bdms.easyinsight.common.param.obj;

import lombok.Data;
import lombok.experimental.Accessors;
import javax.validation.constraints.NotNull;

/**
 * 对象空值率设置
 * @author: yangyichun
 * @date: 2023/03/21 17:28
 */
@Data
@Accessors(chain = true)
public class ObjectEmptyRateParam {
    /**
     * 对象Id
     */
    @NotNull(message = "对象Id不能为空")
    private Long objId;

    /**
     * 参数id
     */
    @NotNull(message = "参数id不能为空")
    private Long paramId;

    /**
     * 空值率
     */
    @NotNull(message = "空值率设置不能为空")
    private Integer setRate;

}
