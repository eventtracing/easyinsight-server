package com.netease.hz.bdms.easyinsight.common.vo.logcheck;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class BucketValueVO {

    private long bucketHour;
    private long time;
    private Number value;
    private String groupBy;
}
