package com.netease.hz.bdms.easyinsight.common.dto.spm;

import lombok.Data;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

/**
 * @author: xumengqiang
 * @date: 2021/11/9 11:37
 */

@Data
@Accessors(chain = true)
public class SpmInfoDTO {
    /**
     * 自增ID
     */
    private Long id;

    /**
     * spm
     */
    private String spm;

    /**
     * spm名称
     */
    private String name;

    /**
     * 映射生效状态
     * @see com.netease.hz.bdms.easyinsight.common.enums.SpmMapStatusEnum
     */
    private Integer status;

    /**
     * 映射生效版本
     */
    private String version;

    /**
     * spm备注
     */
    private String note;

    /**
     * 产品信息
     */
    private Long appId;

    /**
     * 终端ID
     */
    private Long terminalId;

    /**
     * 来源 0-同步 1-手动添加
     */
    private Integer source;

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
