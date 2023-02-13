package com.netease.hz.bdms.easyinsight.common.vo.release;

import lombok.Data;

/**
 * @author: xumengqiang
 * @date: 2022/1/12 14:55
 */
@Data
public class BaseReleaseVO {

    /**
     * 终端ID
     */
    private Long terminalId;

    /**
     * 端版本ID
     */
    private Long terminalVersionId;

    /**
     * 基线版本ID
     */
    private Long baseReleaseId;

    /**
     * 终端名称
     */
    private String terminalName;

    /**
     * 端版本名称
     */
    private String terminalVersionName;
}
