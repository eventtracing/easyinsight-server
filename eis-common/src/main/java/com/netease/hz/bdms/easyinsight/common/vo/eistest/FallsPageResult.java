package com.netease.hz.bdms.easyinsight.common.vo.eistest;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.List;

public class FallsPageResult<T> implements Serializable {
    private static final long serialVersionUID = 3501260691586004504L;
    private List<T> records;
    private FallsPage page;

    public FallsPageResult() {
    }

    public FallsPage getPage() {
        return this.page;
    }

    public void setPage(FallsPage page) {
        this.page = page;
    }

    public List<T> getRecords() {
        return this.records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}