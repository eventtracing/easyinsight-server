package com.netease.hz.bdms.easyinsight.service.helper;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.netease.hz.bdms.easyinsight.common.enums.ProcessStatusEnum;
import com.netease.hz.bdms.easyinsight.common.enums.ReqTaskStatusEnum;
import com.netease.hz.bdms.easyinsight.common.vo.requirement.TaskDetailVO;
import com.netease.hz.bdms.easyinsight.dao.model.EisReqTask;
import com.netease.hz.bdms.easyinsight.dao.model.EisRequirementInfo;
import com.netease.hz.bdms.easyinsight.dao.model.EisTerminalReleaseHistory;
import com.netease.hz.bdms.easyinsight.service.service.requirement.ReqTaskService;
import com.netease.hz.bdms.easyinsight.service.service.requirement.RequirementInfoService;
import com.netease.hz.bdms.easyinsight.service.service.terminalrelease.TerminalReleaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author: xumengqiang
 * @date: 2021/12/23 21:11
 */
@Slf4j
@Component
public class ReqTaskHelper {

    @Resource
    private ReqTaskService reqTaskService;

    @Resource
    private TerminalReleaseService terminalReleaseService;

    @Resource
    private RequirementInfoService requirementInfoService;

    @Resource
    private MergeConflictHelper mergeConflictHelper;

    public List<TaskDetailVO> getDetailTask(Long appId, Long terminalId, ProcessStatusEnum taskStatus){
        Preconditions.checkArgument(null != appId, "产品ID不能为空");
        Preconditions.checkArgument(null != terminalId, "终端ID不能为空");
        // 1. 查询给定产品下所有任务
        EisReqTask query = new EisReqTask();
        query.setAppId(appId);
        query.setTerminalId(terminalId);
        if (taskStatus != null) {
            query.setStatus(taskStatus.getState());
        }
        List<EisReqTask> reqTaskList = reqTaskService.search(query);
        // 2. 过滤任务
        reqTaskList = reqTaskList.stream()
                .filter(k -> terminalId.equals(k.getTerminalId()))
                .collect(Collectors.toList());
        // 3. 获取任务关联的发布信息
        Set<Long> terminalReleaseIds = reqTaskList.stream()
                .map(EisReqTask::getTerminalReleaseId)
                .collect(Collectors.toSet());
        List<EisTerminalReleaseHistory> terminalReleaseHistoryList = terminalReleaseService
                .getByIds(terminalReleaseIds);
        Map<Long, EisTerminalReleaseHistory> terminalReleaseHistoryMap = terminalReleaseHistoryList.stream()
                .collect(Collectors.toMap(EisTerminalReleaseHistory::getId, Function.identity()));
        // 4. 获取任务关联的需求信息
        Set<Long> requireIds = reqTaskList.stream()
                .map(EisReqTask::getRequirementId)
                .collect(Collectors.toSet());
        List<EisRequirementInfo> requirementList = requirementInfoService.getByIds(requireIds);
        Map<Long, EisRequirementInfo> requirementInfoMap = requirementList.stream()
                .collect(Collectors.toMap(EisRequirementInfo::getId, Function.identity()));
        // 5. 组装冲突状态
        Set<Long> reqPooIds = requirementList.stream().map(EisRequirementInfo::getReqPoolId).collect(Collectors.toSet());
        Set<Long> conflictReqPoolIds = mergeConflictHelper.filterConflictReqPoolIds(reqPooIds);

        // 6. 构建结果集合
        List<TaskDetailVO> result = Lists.newArrayList();
        for (EisReqTask reqTask : reqTaskList) {
            // 聚合信息
            TaskDetailVO taskDetailVO = new TaskDetailVO();
            taskDetailVO.setTaskId(reqTask.getId())
                    .setTaskName(reqTask.getTaskName())
                    .setTaskStatus(reqTask.getStatus())
                    .setOwnerName(reqTask.getOwnerName())
                    .setVerifierName(reqTask.getVerifierName())
                    .setTerminalVersionName(reqTask.getTerminalVersion());
            Long reqId = reqTask.getRequirementId();
            Long terminalReleaseId = reqTask.getTerminalReleaseId();
            if(requirementInfoMap.containsKey(reqId)){
                EisRequirementInfo requirementInfo = requirementInfoMap.get(reqId);
                taskDetailVO.setReqName(requirementInfo.getReqName())
                        .setSource(requirementInfo.getSource())
                        .setReqIssueKey(requirementInfo.getReqIssueKey());
                boolean mergeConflict = conflictReqPoolIds.contains(requirementInfo.getReqPoolId()) && !ReqTaskStatusEnum.ONLINE.getState().equals(reqTask.getStatus());
                taskDetailVO.setMergeConflict(mergeConflict);
            }
            if(terminalReleaseHistoryMap.containsKey(terminalReleaseId)){
                EisTerminalReleaseHistory terminalReleaseHistory = terminalReleaseHistoryMap
                        .get(terminalReleaseId);
                taskDetailVO.setReleaseTime(terminalReleaseHistory.getCreateTime())
                        .setReleaserName(terminalReleaseHistory.getCreateName());
            }
            // 加入结果列表
            result.add(taskDetailVO);
        }
        return result;
    }
}
