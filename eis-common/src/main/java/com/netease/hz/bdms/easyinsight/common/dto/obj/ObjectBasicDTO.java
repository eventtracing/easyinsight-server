package com.netease.hz.bdms.easyinsight.common.dto.obj;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author: xumengqiang
 * @date: 2021/12/15 15:52
 */

@Data
@Accessors(chain = true)
public class ObjectBasicDTO {

    /**
     * 自增id
     */
    private Long id;

    /**
     * 对象oid
     */
    private String oid;

    /**
     * 对象名称
     */
    private String name;

    /**
     * 对象类型，1表示页面，2表示元素
     * @see com.netease.hz.bdms.easyinsight.common.enums.ObjTypeEnum
     */
    private Integer type;

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
     * 创建时间
     */
    private Long createTime;

    /**
     * 更新时间
     */
    private Long updateTime;

    /**
     * {@link com.netease.hz.bdms.easyinsight.common.enums.DiffTypeEnum}
     */
    private String diffType;
}
