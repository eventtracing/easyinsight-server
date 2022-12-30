package com.netease.hz.bdms.easyinsight.common.param.obj;

import com.netease.hz.bdms.easyinsight.common.param.tag.CidTagInfo;
import com.netease.hz.bdms.easyinsight.common.param.tag.ObjBasicTagDTO;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author: xumengqiang
 * @date: 2021/12/13 20:49
 */
@Data
@Accessors(chain = true)
public class ObjectEditParam {
    /**
     * 对象Id
     */
    @NotNull(message = "对象Id不能为空")
    private Long id;

    /**
     * 对象Oid
     */
    @NotBlank(message = "对象Oid不能为空")
    private String oid;

    /**
     * 对象名称
     */
    @NotBlank(message = "对象name不能为空")
    private String name;

    /**
     * 对象类型
     */
    @NotNull(message = "对象类型不能为空")
    private Integer type;

    /**
     * 对象特殊类型
     */
    private String specialType;

    /**
     * 子空间ID，桥梁类型对象使用
     */
    private Long bridgeSubAppId;

    /**
     * 子空间ID下的terminal ID，桥梁类型对象使用，如果为0：挂载本端，否则挂载指定端
     */
    private Long bridgeSubTerminalId;

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
    private Boolean consistency;

    /**
     * 需求ID
     */
    @NotNull(message = "需求组ID不能为空")
    private Long reqPoolId;

    /**
     * 对象埋点信息
     */
    @NotEmpty(message = "埋点变更信息不能为空")
    @Valid
    private List<ObjectTrackerEditParam> trackers;

    /**
     * 是否为解决冲突的编辑
     */
    private boolean resolveConflict = false;

    /**
     * 对象基本标签
     */
    private ObjBasicTagDTO basicTag;

    /**
     * CID标签信息
     */
    private List<CidTagInfo> cidTagInfos;

    /**
     * 是否解析CID信息
     */
    private boolean analyseCid;
}
