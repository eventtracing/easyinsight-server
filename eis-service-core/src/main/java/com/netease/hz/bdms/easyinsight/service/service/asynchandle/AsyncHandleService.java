package com.netease.hz.bdms.easyinsight.service.service.asynchandle;

import com.netease.hz.bdms.easyinsight.dao.model.EisReqTask;
import com.netease.hz.bdms.easyinsight.dao.model.EisTaskProcess;
import com.netease.hz.bdms.easyinsight.service.service.requirement.ReqTaskService;
import com.netease.hz.bdms.easyinsight.service.service.requirement.TaskProcessService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 事件总线
 */
@Slf4j
@Service
public class AsyncHandleService {

    @Resource
    private List<TaskAndProcessUpdateHandler> taskAndProcessUpdateHandlers;

    @Resource
    private List<VersionReleaseHandler> versionReleaseHandlers;

    @Resource
    private TaskProcessService taskProcessService;

    @Resource
    private ReqTaskService reqTaskService;

    private ExecutorService executor = new ThreadPoolExecutor(1, 1, 10L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(500),
            new ThreadPoolExecutor.DiscardPolicy() {
            });

    public void onVersionReleaseSuccess(Long terminalId, Long terminalVersionId) {
        if (CollectionUtils.isNotEmpty(versionReleaseHandlers)) {
            versionReleaseHandlers.forEach(handler -> {
                executor.submit(() -> handler.onTerminalReleaseSuccess(terminalId, terminalVersionId));
            });
        }
    }

    /**
     * 同步任务状态 + 任务进度
     */
    public void onTaskAndProcessUpdate(long taskId) {
       if (CollectionUtils.isNotEmpty(taskAndProcessUpdateHandlers)) {
           EisReqTask task = reqTaskService.getById(taskId);
           if (task == null) {
               return;
           }
           EisTaskProcess query = new EisTaskProcess();
           query.setTaskId(taskId);
           List<EisTaskProcess> processes = taskProcessService.search(query);
           taskAndProcessUpdateHandlers.forEach(handler -> {
               executor.submit(() -> handler.onTaskAndProcessUpdate(task, processes));
           });
       }
    }
}
