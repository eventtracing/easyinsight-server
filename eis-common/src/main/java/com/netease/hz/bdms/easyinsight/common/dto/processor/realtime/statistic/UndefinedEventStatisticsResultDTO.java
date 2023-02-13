package com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.statistic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 未定义事件的统计结果
 *
 * @author wangliangyuan
 * @date 2021-09-06 下午 03:40
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UndefinedEventStatisticsResultDTO {

    /**
     * 未定义事件的总数目
     */
    private Integer eventNum;

    /**
     * 日志总数目
     */
    private Integer logNum;

    /**
     * 统计详情
     */
    private List<UndefinedEventStatisticsItemDTO> details;

    /**
     * 未定义事件的统计结果
     */
    @Data
    public static class UndefinedEventStatisticsItemDTO {
        /**
         * 事件类型CODE
         */
        private String eventCode;

        /**
         * 日志数量
         */
        private Integer logCount;
    }
}
