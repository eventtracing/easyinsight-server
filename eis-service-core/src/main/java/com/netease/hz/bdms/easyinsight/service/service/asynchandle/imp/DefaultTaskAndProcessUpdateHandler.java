package com.netease.hz.bdms.easyinsight.service.service.asynchandle.imp;

import com.netease.hz.bdms.easyinsight.dao.model.EisReqTask;
import com.netease.hz.bdms.easyinsight.dao.model.EisTaskProcess;
import com.netease.hz.bdms.easyinsight.service.service.asynchandle.TaskAndProcessUpdateHandler;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefaultTaskAndProcessUpdateHandler implements TaskAndProcessUpdateHandler {
    @Override
    public void onTaskAndProcessUpdate(EisReqTask task, List<EisTaskProcess> processes) {
        // do nothing
    }
}
