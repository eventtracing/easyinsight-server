package com.netease.hz.bdms.easyinsight.common.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: xumengqiang
 * @date: 2022/2/28 1:11
 */
@Data
public class PageResultVO<T> {
    /**
     * 每页条数.
     */
    private Integer pageSize;

    /**
     * 当前页码.
     */
    private Integer pageNum;

    /**
     * 总条数.
     */
    private Integer totalCount;

    /**
     * 总页数.
     */
    private Integer totalPage;

    /**
     * 结果.
     */
    private List<T> list = new ArrayList<>();

    public PageResultVO(List<T> data, Integer pageSize, Integer pageNum){
        this.pageSize = pageSize;
        this.totalCount = data.size();
        this.totalPage = this.totalCount / pageSize + 1;
        this.pageNum = (pageNum - 1) * pageSize > totalCount ? 1 : pageNum;
        this.list = data.subList((this.pageNum-1)*pageSize, Math.min(totalCount, this.pageNum*pageSize));
    }

    public PageResultVO(List<T> data){
        this.pageSize = 25;
        this.pageNum = 1;
        this.totalCount = data.size();
        this.totalPage = totalCount / pageSize + 1;
        this.list = data.subList((pageNum-1)*pageSize, Math.min(totalCount, this.pageNum*pageSize));
    }
}
