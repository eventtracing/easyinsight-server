package com.netease.hz.bdms.easyinsight.common.param.spm;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author: xumengqiang
 * @date: 2021/11/11 14:56
 */

@Data
@Accessors(chain = true)
public class SpmInfoUpdateParam {


    private Long spmId;

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
     * spm状态
     * @see com.netease.hz.bdms.easyinsight.common.enums.ProcessStatusEnum
     */
    private Integer spmStatus;

    /**
     * 映射生效版本
     */
    private String spmTag;

    /**
     * 老spm列表
     */
    private String spmOldList;

    /**
     * 映射生效版本
     */
    private String version;

    /**
     * spm备注
     */
    private String note;

    /**
     * appId
     */
    private Long appId;

    /**
     * terminalId
     */
    private Long terminalId;
}
