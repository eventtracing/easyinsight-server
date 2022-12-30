package com.netease.hz.bdms.easyinsight.common.param.obj;

import com.netease.hz.bdms.easyinsight.common.param.tag.CidTagInfo;
import com.netease.hz.bdms.easyinsight.common.param.tag.ObjBasicTagDTO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author: xumengqiang
 * @date: 2021/12/10 19:36
 */
@Data
@Accessors(chain = true)
public class ObjectBasicChangeParam {

    /**
     * 对象ID，不可变更
     */
    private Long id;

    /**
     * 对象oid，不可变更
     */
    private String oid;

    /**
     * 对象中文名称，不可变更
     */
    private String name;

    /**
     * 对象类型，不可变更
     * @see com.netease.hz.bdms.easyinsight.common.enums.ObjTypeEnum
     */
    private Integer type;

    /**
     * 对象特殊类型
     * @see com.netease.hz.bdms.easyinsight.common.enums.ObjSpecialTypeEnum
     */
    private String specialType;

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
     * 需求ID
     */
    private Long reqPoolId;

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
