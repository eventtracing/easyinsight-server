package com.netease.hz.bdms.easyinsight.common.param.obj;

import com.netease.hz.bdms.easyinsight.common.param.tag.CidTagInfo;
import com.netease.hz.bdms.easyinsight.common.param.tag.ObjBasicTagDTO;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author: xumengqiang
 * @date: 2021/12/9 9:45
 */
@Data
@Accessors(chain = true)
public class ObjectBasicCreateParam {
    /**
     * 对象oid
     */
    @NotBlank(message = "对象oid不能为空")
    private String oid;

    /**
     * 对象中文名称
     */
    @NotBlank(message = "对象中文名称不能为空")
    private String name;

    /**
     * 对象描述
     */
    private String description;

    /**
     * 对象类型
     * @see com.netease.hz.bdms.easyinsight.common.enums.ObjTypeEnum
     */
    @NotNull(message = "对象类型不能为空")
    private Integer type;

    /**
     * 对象类型
     * @see com.netease.hz.bdms.easyinsight.common.enums.ObjSpecialTypeEnum
     */
    private String specialType;

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
    @NotBlank(message = "优先级不能为空")
    private String priority;

    /**
     * 子空间ID，桥梁类型对象使用
     */
    private Long bridgeSubAppId;

    /**
     * 子空间ID下的terminal ID，桥梁类型对象使用，如果为0：挂载本端，否则挂载指定端
     */
    private Long bridgeSubTerminalId;

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
