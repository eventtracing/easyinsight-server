package com.netease.hz.bdms.easyinsight.common.dto.obj;

import com.netease.hz.bdms.easyinsight.common.dto.tag.TagSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.enums.ObjSubTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
 * @author: xumengqiang
 * @date: 2021/12/14 18:53
 */

@Data
@Accessors(chain = true)
public class ObjectInfoDTO {
    /**
     * 对象ID
     */
    private Long id;

    /**
     * 对象Oid
     */
    private String oid;

    /**
     * 对象中文名称
     */
    private String name;

    /**
     * 对象类型
     */
    private Integer type;

    /**
     * 对象特殊类型
     */
    private String specialType;

    /**
     * trackerId
     */
    private Long trackerId;

    /**
     * 是否多端一致
     */
    private Boolean consistency;

    /**
     * 对象描述信息
     */
    private String description;

    /**
     * 优先级
     */
    private String priority;

    /**
     * 产品ID
     */
    private Long appId;

    /**
     * 其他空间的appId
     */
    private Long otherAppId;

    /**
     * 变更历史ID
     */
    private Long historyId;

    /**
     * 对象变更对应需求组的基本发布版本
     */
    private Long baseReleaseId;

    /**
     * 需求组ID
     */
    private Long reqPoolId;

    /**
     * 能否变更标识
     */
    private Boolean canChange;

    /**
     * 关联图片的url
     */
    private List<String> imgUrls;

    /**
     * 关联标签信息
     */
    private List<TagSimpleDTO> tags;

    /**
     * 创建人邮箱
     */
    private String createEmail;

    /**
     * 创建人名称
     */
    private String createName;

    /**
     * 最近更新人名称
     */
    private String updateEmail;

    /**
     * 最近更新人名称
     */
    private String updateName;

    /**
     * 子空间ID，桥梁类型对象使用
     */
    private Long bridgeSubAppId;

    /**
     * 子空间ID下的terminal ID，桥梁类型对象使用，如果为0：挂载本端，否则挂载指定端
     */
    private Long bridgeSubTerminalId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 元素类型 {@link ObjSubTypeEnum}
     */
    private String objSubType;

    /**
     * 业务线
     */
    private String bizGroup;

    /**
     * 是否解析cid
     */
    private boolean analyseCid;
}
