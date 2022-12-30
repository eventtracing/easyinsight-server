package com.netease.hz.bdms.easyinsight.service.service.asynchandle;

import com.netease.hz.bdms.easyinsight.dao.model.EisReqTask;
import com.netease.hz.bdms.easyinsight.dao.model.EisTaskProcess;

import java.util.List;

public interface TaskAndProcessUpdateHandler {
    /**
     * 同步任务状态 + 任务进度
     */
    void onTaskAndProcessUpdate(EisReqTask task, List<EisTaskProcess> processes);
}
