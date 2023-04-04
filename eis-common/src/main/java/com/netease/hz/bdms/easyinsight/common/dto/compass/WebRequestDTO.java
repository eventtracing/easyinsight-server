package com.netease.hz.bdms.easyinsight.common.dto.compass;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * @author wangyongliang
 * @version 1.0
 * @description: pc端流量罗盘查询dto
 * @date 2022/5/19 9:25
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class WebRequestDTO{
    /**
     资源类型
     */
    private String resource;

    /**
     开始时间
     */
    private String startTime;

    /**
     结束时间
     */
    private String endTime;

    /**
     spm 不带位置
     */
    private String spm;

    /**
     * spmName
     */
    private String spmName;

    /**
    维度
   */
    private Map<String,Object> dimension;

    /**
     * 终端id
     */
    private Long terminalId;


    /**
     * 操作系统
     */
    private String os;
    /**
     * 操作系统
     */
    private String appVer;

    /**
     * 实时/离线数据类型
     */
    private Integer status;

    private Integer tag;
    /**
     * 指标类型
     */
    private String indexType;
}
