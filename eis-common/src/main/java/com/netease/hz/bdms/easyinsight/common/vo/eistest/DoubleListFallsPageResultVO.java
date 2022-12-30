package com.netease.hz.bdms.easyinsight.common.vo.eistest;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.List;

public class DoubleListFallsPageResultVO<T, R, U> implements Serializable {
    private static final long serialVersionUID = 3501260691586004504L;
    private List<T> records;
    private List<R> extraRecords;
    private List<R> needCover;
    private List<U> shouldCoverData;
    private List<R> covered;
    private FallsPage page;

    public DoubleListFallsPageResultVO() {
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

    public List<R> getExtraRecords() {
        return extraRecords;
    }

    public void setExtraRecords(List<R> extraRecords) {
        this.extraRecords = extraRecords;
    }


    public List<R> getNeedCover() {
        return needCover;
    }

    public void setNeedCover(List<R> needCover) {
        this.needCover = needCover;
    }

    public List<R> getCovered() {
        return covered;
    }

    public void setCovered(List<R> covered) {
        this.covered = covered;
    }

    public List<U> getShouldCoverData() {
        return shouldCoverData;
    }

    public void setShouldCoverData(List<U> shouldCoverData) {
        this.shouldCoverData = shouldCoverData;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}