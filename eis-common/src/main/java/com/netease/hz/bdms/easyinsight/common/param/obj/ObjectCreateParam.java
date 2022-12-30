package com.netease.hz.bdms.easyinsight.common.param.obj;

import com.netease.hz.bdms.easyinsight.common.param.obj.tracker.ObjTrackerCreateParam;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author: xumengqiang
 * @date: 2021/12/8 16:04
 */
@Data
@Accessors(chain = true)
public class ObjectCreateParam {
    /**
     * 对象埋点需求
     */
    @NotEmpty(message = "对象埋点需求不能为空")
    @Valid
    private List<ObjectTrackerCreateParam> trackers;

    /**
     * 对象基本信息
     */
    @NotEmpty(message = "对象基础信息不能为空")
    @Valid
    private List<ObjectBasicCreateParam> basics;

    /**
     * 多端是否一致：true多端一致，false表示多端不一致
     */
    @NotNull(message = "多端是否一致不能为空")
    private Boolean consistency;

    /**
     * 需求组ID
     */
    @NotNull(message = "需求组ID不能为空")
    private Long reqPoolId;

}
