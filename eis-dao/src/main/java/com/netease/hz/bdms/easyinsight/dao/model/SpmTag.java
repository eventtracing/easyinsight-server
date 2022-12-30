package com.netease.hz.bdms.easyinsight.dao.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

/**
 * @author: xumengqiang
 * @date: 2021/11/10 14:09
 */

@Data
@Accessors(chain = true)
public class SpmTag {
    /**
     * 自增ID
     */
    private Long id;

    /**
     * SPM ID
     */
    private Long spmId;

    /**
     * 标签ID
     */
    private Long tagId;

    /**
     * 终端ID
     */
    private Long terminalId;

    /**
     * 创建者邮箱
     */
    private String createEmail;

    /**
     * 创建者名称
     */
    private String createName;

    /**
     * 更新人邮箱
     */
    private String updateEmail;

    /**
     * 更新人名称
     */
    private String updateName;

    /**
     * 创建时间
     */
    private Timestamp createTime;

    /**
     * 更新时间
     */
    private Timestamp updateTime;
}
