package com.netease.hz.bdms.easyinsight.common.vo.eistest;

import lombok.Data;

import java.io.Serializable;

@Data
public class FallsPage implements Serializable {
    private Integer size;
    private Integer page;
    private Integer total;
    private String cursor;
    private Boolean more = true;

    public FallsPage() {
    }

}
