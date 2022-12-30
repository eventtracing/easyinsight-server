package com.netease.hz.bdms.easyinsight.web.core.controller;

import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.enums.ProcessStatusEnum;
import com.netease.hz.bdms.easyinsight.common.enums.TaskSourceStatusEnum;
import com.netease.hz.bdms.easyinsight.common.enums.VersionSourceStatusEnum;
import com.netease.hz.bdms.easyinsight.common.http.HttpResult;
import com.netease.hz.bdms.easyinsight.common.vo.release.ReleaseVO;
import com.netease.hz.bdms.easyinsight.common.vo.requirement.AggregatedTaskVO;
import com.netease.hz.bdms.easyinsight.common.vo.requirement.ReleasedTaskVO;
import com.netease.hz.bdms.easyinsight.common.vo.requirement.TaskDetailVO;
import com.netease.hz.bdms.easyinsight.service.facade.ReleaseFacade;
import com.netease.hz.bdms.easyinsight.service.facade.ReqDesignFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 需求管理——发布管理模块 交互接口
 *
 * @author: xumengqiang
 * @date: 2021/12/22 14:18
 */
@Slf4j
@RequestMapping("/eis/v2/release")
@RestController
public class ReleaseController {

    @Resource
    private ReleaseFacade releaseFacade;

    @Resource
    private ReqDesignFacade reqDesignFacade;

    /**
     * 获取端版本聚合任务列表
     *
     * @param terminalId 端版本
     * @param search 搜索条件(需求ID、需求名称、任务名称)
     * @param terminalVersionName 端版本
     * @param taskStatus 按任务 状态 筛选 {@link ProcessStatusEnum}
     * @param taskSourceStatus 按任务 来源状态 筛选 {@link TaskSourceStatusEnum}
     * @param versionDeployedFilter 按版本 是否已上线 筛选
     * @param versionSourceStatus 按版本 来源状态 筛选 {@link VersionSourceStatusEnum}
     * @return
     */
    @GetMapping("/aggregate/tasks/get")
    public HttpResult getAggregatedTaskList(@RequestParam("terminalId") Long terminalId,
                                            @RequestParam(value = "search", required = false) String search,
                                            @RequestParam(value = "terminalVersionName", required = false) String terminalVersionName,
                                            @RequestParam(value = "taskStatus", required = false) Integer taskStatus,
                                            @RequestParam(value = "taskSourceStatus", required = false) Integer taskSourceStatus,
                                            @RequestParam(value = "versionDeployedFilter", required = false) Boolean versionDeployedFilter,
                                            @RequestParam(value = "versionSourceStatus", required = false) Integer versionSourceStatus) {
        List<AggregatedTaskVO>  aggregatedTaskVOList = releaseFacade
                .getTaskListAggregatedByTerminalVersion(terminalId, search, terminalVersionName,
                        taskStatus == null ? null : ProcessStatusEnum.fromState(taskStatus),
                        taskSourceStatus == null ? null : TaskSourceStatusEnum.valueOfType(taskSourceStatus),
                        versionDeployedFilter,
                        versionSourceStatus == null ? null : VersionSourceStatusEnum.valueOfType(versionSourceStatus)
                        );
        return HttpResult.success(aggregatedTaskVOList);
    }

    /**
     * 获取无端版本聚合任务列表
     *
     * @param terminalId 端版本ID
     * @param search 搜索条件(需求ID、需求名称、任务名称)
     * @param taskStatus 按任务 状态 筛选 {@link ProcessStatusEnum}
     * @param taskSourceStatus 按任务 来源状态 筛选 {@link TaskSourceStatusEnum}
     * @return
     */
    @GetMapping("/tasks/get")
    public HttpResult getTaskList(@RequestParam("terminalId") Long terminalId,
                                  @RequestParam(value = "search", required = false) String search,
                                  @RequestParam(value = "taskStatus", required = false) Integer taskStatus,
                                  @RequestParam(value = "taskSourceStatus", required = false) Integer taskSourceStatus) {
        List<TaskDetailVO> taskDetailVOList = releaseFacade.getTaskList(terminalId, search,
                taskStatus == null ? null : ProcessStatusEnum.fromState(taskStatus),
                taskSourceStatus == null ? null : TaskSourceStatusEnum.valueOfType(taskSourceStatus));
        return HttpResult.success(taskDetailVOList);
    }


    /**
     * 获取任务发布历史
     *
     * @param terminalId 终端ID
     * @return
     */
    @GetMapping("/tasks/history/get")
    public HttpResult getReleasedTaskList(@RequestParam("terminalId") Long terminalId,
                                          @RequestParam("ascend") Boolean ascend){
        List<ReleasedTaskVO> releasedTaskVOList = releaseFacade.getReleasedTasks(terminalId, ascend);
        return HttpResult.success(releasedTaskVOList);
    }

    /**
     * 任务发布上线
     *
     * @param releaseVo
     * @return
     */
    @PostMapping("/releaseByVersion")
    public HttpResult release(@RequestBody ReleaseVO releaseVo){
        releaseFacade.releaseByTerminalVersion(releaseVo.getTerminalId(),
                releaseVo.getTerminalVersionId(), releaseVo.getTaskIds());
        // 异步刷新所有需求组基线
        Long appId = EtContext.get(ContextConstant.APP_ID);
        reqDesignFacade.rebaseAllMessageAsync(appId, releaseVo.getTerminalId());
        return HttpResult.success();
    }

    /**
     * 任务发布上线
     *
     * @param taskId
     * @param terminalId
     * @return
     */
    @GetMapping("/releaseByTask")
    public HttpResult release(Long taskId, Long terminalId) {
        releaseFacade.releaseByTaskId(terminalId,taskId);
        // 异步刷新所有需求组基线
        Long appId = EtContext.get(ContextConstant.APP_ID);
        reqDesignFacade.rebaseAllMessageAsync(appId, terminalId);
        return HttpResult.success();
    }

}
