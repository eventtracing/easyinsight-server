package com.netease.hz.bdms.easyinsight.common.dto.spm;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author: xumengqiang
 * @date: 2021/11/10 19:11
 */

@Data
@Accessors(chain = true)
public class SpmMapItemDTO {
    /**
     * spmId
     */
    private Long spmId;

    /**
     * 老SPM字符串
     */
    private String spmOld;

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
