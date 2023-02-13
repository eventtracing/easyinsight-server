package com.netease.hz.bdms.easyinsight.common.vo.logcheck.monitor;

import com.netease.hz.bdms.easyinsight.common.enums.logcheck.monitor.SelectTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;

@Accessors(chain = true)
@Data
public class DataFieldFilterValuesVO {

    private String fieldName;

    private String displayName;

    /**
     * {@link SelectTypeEnum}
     */
    private String type;

    private Set<DisplayValue> values;
}
