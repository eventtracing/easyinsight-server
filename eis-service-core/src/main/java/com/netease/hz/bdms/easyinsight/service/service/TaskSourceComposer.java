package com.netease.hz.bdms.easyinsight.service.service;

import com.netease.hz.bdms.easyinsight.common.enums.ReqSourceEnum;
import com.netease.hz.bdms.easyinsight.common.vo.requirement.TaskDetailVO;

public interface TaskSourceComposer {

    ReqSourceEnum getType();

    /**
     * @return 是否组装了数据
     */
    boolean compose(TaskDetailVO taskDetailVO);
}
