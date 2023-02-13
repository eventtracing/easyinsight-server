package com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.statistic;

import lombok.Data;

/**
 * 事件类型的校验统计结果
 *
 * @author wangliangyuan
 * @date 2021-08-30 下午 06:47
 */
@Data
public class EventCheckResultDTO {

    /**
     * 校验通过的总数
     */
    private Integer passSum;

    /**
     * 校验不通过的总数
     */
    private Integer failSum;

    public static EventCheckResultDTO init() {
        EventCheckResultDTO eventCheckResultDTO = new EventCheckResultDTO();
        eventCheckResultDTO.setPassSum(0);
        eventCheckResultDTO.setFailSum(0);
        return eventCheckResultDTO;
    }
}
