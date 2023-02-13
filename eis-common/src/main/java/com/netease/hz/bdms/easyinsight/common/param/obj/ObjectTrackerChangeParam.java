package com.netease.hz.bdms.easyinsight.common.param.obj;

import com.netease.hz.bdms.easyinsight.common.param.param.paramBind.ParamBindItermParam;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * @author: xumengqiang
 * @date: 2021/12/13 10:08
 */
@Data
@Accessors(chain = true)
public class ObjectTrackerChangeParam {

    /**
     * 先前埋点ID
     */
    private Long id;

    /**
     * 终端ID
     */
    @NotNull(message = "终端ID不能为空")
    private Long terminalId;

    /**
     * 全局公参的参数包版本ID
     */
    private Long pubParamPackageId;

    /**
     * 事件类型ID集合
     */
    private List<Long> eventIds;

    /**
     * 事件id——参数版本id map
     */
    private Map<Long, Long> eventParamVersionIdMap;

    /**
     * 关联父对象集合
     */
    private List<Long> parentObjs;

    /**
     * 对象标准私参、业务私参信息
     */
    private List<ParamBindItermParam> paramBinds;

}
