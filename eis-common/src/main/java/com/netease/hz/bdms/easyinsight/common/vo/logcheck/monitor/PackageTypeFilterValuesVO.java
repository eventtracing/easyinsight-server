package com.netease.hz.bdms.easyinsight.common.vo.logcheck.monitor;

import com.netease.hz.bdms.easyinsight.common.enums.logcheck.LogCheckPackageTypeEum;
import com.netease.hz.bdms.easyinsight.common.vo.logcheck.VersionBuildUUIDVO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Accessors(chain = true)
@Data
public class PackageTypeFilterValuesVO {

    /**
     * {@link LogCheckPackageTypeEum}
     */
    private Integer packageType;
    private String packageDesc;
    private List<VersionBuildUUIDVO> versionValues;
}
