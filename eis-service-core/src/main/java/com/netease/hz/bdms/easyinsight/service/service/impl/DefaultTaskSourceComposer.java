package com.netease.hz.bdms.easyinsight.service.service.impl;

import com.netease.hz.bdms.easyinsight.common.enums.ReqSourceEnum;
import com.netease.hz.bdms.easyinsight.common.vo.requirement.TaskDetailVO;
import com.netease.hz.bdms.easyinsight.service.service.TaskSourceComposer;
import org.springframework.stereotype.Service;

@Service
public class DefaultTaskSourceComposer implements TaskSourceComposer {

    @Override
    public ReqSourceEnum getType() {
        return null;
    }

    @Override
    public boolean compose(TaskDetailVO taskDetailVO) {
        return true;
    }
}
