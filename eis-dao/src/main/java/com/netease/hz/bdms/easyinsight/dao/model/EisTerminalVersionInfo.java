package com.netease.hz.bdms.easyinsight.dao.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author: xumengqiang
 * @date: 2021/12/22 20:49
 */
@Data
@Accessors(chain = true)
public class EisTerminalVersionInfo {
    /**
     * 自增id
     */
    private Long id;

    /**
     * 端版本名称
     */
    private String name;

    /**
     * 端版本号
     */
    private String num;

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
    private Date createTime;

    /**
     * 更新时间
     */
    private Long updateTime;
}
