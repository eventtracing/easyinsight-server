package com.netease.hz.bdms.easyinsight.dao.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author: xumengqiang
 * @date: 2021/12/9 14:40
 */
@Data
@Accessors(chain = true)
public class ObjectBasic {
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
     * @see com.netease.hz.bdms.easyinsight.common.enums.ObjSpecialTypeEnum
     */
    private String specialType;

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
     * 扩展字段
     */
    private String ext;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;


    /**
     * 新增冗余字段-流量罗盘事件分析使用
     **/

    private String tag;
}
