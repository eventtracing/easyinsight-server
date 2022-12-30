package com.netease.hz.bdms.easyinsight.common.vo.eistest;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

public class FallsPageRequest implements Serializable {
    private static final long serialVersionUID = 335278388705747622L;
    private Integer size;
    private String cursor;
    private Integer page;
    private Integer total;

    public FallsPageRequest() {
    }

    public Integer getSize() {
        return this.size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getCursor() {
        return this.cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    public Integer getLimit() {
        return this.size;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
