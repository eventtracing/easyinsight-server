package com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.statistic;

import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Builder
public class EventStatisticResultDTO {

    /**
     * 事件对应的校验详情的集合
     */
    private List<EventCheckResultItemDTO> details;

    /**
     * 基于 事件类型CODE 的校验结果
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class EventCheckResultItemDTO {
        /**
         * 事件类型CODE
         */
        private String eventCode;

        /**
         * 对象埋点的数目(即页面上的日志总数)
         */
        private Integer num;

        /**
         * 校验通过的总数
         */
        private Integer passSum;

        /**
         * 校验不通过的总数
         */
        private Integer failSum;
        /**
         * 覆盖分支数
         */
        private Integer hitSum;

        /**
         * 测试分支数
         */
        private Integer reqSum;
    }
}
