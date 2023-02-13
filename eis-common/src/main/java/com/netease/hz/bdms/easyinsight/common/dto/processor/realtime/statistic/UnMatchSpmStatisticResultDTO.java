package com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.statistic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 校验的统计结果(在前端页面上以树形结构展示)
 *
 * @author wangliangyuan
 * @date 2021-08-30 下午 06:17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnMatchSpmStatisticResultDTO {
    /**
     * spm值
     */
    private String spm;
    /**
     * spm名称
     */
    private String spmName;
    /**
     * 未匹配数目
     */
    private Long num;

}
