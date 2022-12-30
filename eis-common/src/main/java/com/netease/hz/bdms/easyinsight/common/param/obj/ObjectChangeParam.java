package com.netease.hz.bdms.easyinsight.common.param.obj;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import java.util.List;

/**
 * @author: xumengqiang
 * @date: 2021/12/10 19:32
 */
@Data
@Accessors(chain = true)
public class ObjectChangeParam {
    /**
     * 对象Id
     */
    @NotNull(message = "对象Id不能为空")
    private Long id;

    /**
     * 对象Oid
     */
    private String oid;

    /**
     * 对象描述
     */
    private String description;

    /**
     * 图片链接
     */
    private List<String> imgUrls;

    /**
     * 标签ID集合
     */
    private List<Long> tagIds;

    /**
     * 优先级，默认为P1
     */
    private String priority;

    /**
     * 是否多端一致
     */
    @NotNull(message = "多端是否一致不能为空")
    Boolean consistency;

    /**
     * 需求组ID
     */
    @NotNull(message = "需求组ID不能为空")
    private Long reqPoolId;

    /**
     * 变更历史ID
     */
    private Long historyId;

    /**
     * 对象埋点信息
     */
    @NotEmpty(message = "埋点变更信息不能为空")
    @Valid
    List<ObjectTrackerChangeParam> trackers;
}
