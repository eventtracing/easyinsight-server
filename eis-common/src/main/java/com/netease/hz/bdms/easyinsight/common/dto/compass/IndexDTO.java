package com.netease.hz.bdms.easyinsight.common.dto.compass;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * @author wangyongliang
 * @version 1.0
 * @description: 指标dto
 * @date 2022/5/19 9:26
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class IndexDTO {

    //指标类型  LP-环比  SP-同比  ACM -累计
    private String indexType;
    // 时间类型，枚举类型
    private String[] TimeSpan;
    //时间窗
    private String windowTime;
    //时间窗格式
    private String timeFormat;
    //时间字段
    private String timeColumn;
    //数据状态 0-实时 1-离线
    private Integer status;
    //统计方式:切片-0, 累积-1
    private int shape;
    //维度字段
    private Map<String,String[]> dimColumn;
    //指标字段
    private String[]  indexColumn;

    private Integer tag;

}