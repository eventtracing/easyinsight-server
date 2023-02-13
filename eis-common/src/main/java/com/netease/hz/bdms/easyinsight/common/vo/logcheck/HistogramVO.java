package com.netease.hz.bdms.easyinsight.common.vo.logcheck;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Accessors(chain = true)
@Data
public class HistogramVO {

    private String sql; // debug用
    private long total;
    private List<BucketValueVO> values;
}
