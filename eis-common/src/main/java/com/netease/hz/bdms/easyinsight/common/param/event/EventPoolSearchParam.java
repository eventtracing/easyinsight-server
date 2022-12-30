package com.netease.hz.bdms.easyinsight.common.param.event;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @author: xumengqiang
 * @date: 2022/1/13 15:10
 */
@Data
public class EventPoolSearchParam {
    /**
     * 当前页
     */
    @NotNull(message = "当前页号不能为空")
    @Min(value = 1, message = "页号最小为1")
    private Integer currentPage;

    /**
     * 页SIZE
     */
    @NotNull(message = "页规模不能为空")
    @Min(value = 1, message = "业规模最小为1")
    private Integer pageSize;

    /**
     * 排序字段
     */
    private String orderBy;

    /**
     * 排序规则
     */
    private String orderRule;

    /**
     * 终端ID
     */
    private Long terminalId;

    /**
     * 发布版本ID
     */
    private Long releasedId;

    /**
     * 事件code或者中文名称
     */
    private String search;

}
