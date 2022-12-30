package com.netease.hz.bdms.easyinsight.service.service.impl;

import com.netease.hz.bdms.easyinsight.common.enums.VersionSourceEnum;
import com.netease.hz.bdms.easyinsight.common.vo.requirement.VersionBaseVO;
import com.netease.hz.bdms.easyinsight.service.service.VersionSourceComposer;
import org.springframework.stereotype.Service;

@Service
public class DefaultVersionSourceComposer implements VersionSourceComposer {
    @Override
    public VersionSourceEnum getType() {
        return null;
    }

    @Override
    public boolean compose(VersionBaseVO versionBaseVO) {
        return true;
    }
}
