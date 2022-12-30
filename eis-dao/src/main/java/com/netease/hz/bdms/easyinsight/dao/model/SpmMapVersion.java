package com.netease.hz.bdms.easyinsight.dao.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * SPM映射生效版本信息
 *
 * @author: xumengqiang
 * @date: 2022/3/2 18:09
 */
@Data
@Accessors(chain = true)
public class SpmMapVersion {
    /**
     * 自增ID
     */
    private Long id;

    /**
     * 关联表 eis_spm_map_info 中spm对应的主键ID
     */
    private Long spmId;

    /**
     * spm映射生效版本
     */
    private String version;

    /**
     * 终端ID
     */
    private Long terminalId;

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
    private Date updateTime;
}
