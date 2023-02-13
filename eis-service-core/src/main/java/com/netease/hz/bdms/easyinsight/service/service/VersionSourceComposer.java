package com.netease.hz.bdms.easyinsight.service.service;

import com.netease.hz.bdms.easyinsight.common.enums.VersionSourceEnum;
import com.netease.hz.bdms.easyinsight.common.vo.requirement.VersionBaseVO;

public interface VersionSourceComposer {

    VersionSourceEnum getType();

    /**
     * @return 是否组装了数据
     */
    boolean compose(VersionBaseVO versionBaseVO);
}
