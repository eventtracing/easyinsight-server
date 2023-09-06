package com.netease.hz.bdms.easyinsight.service.facade;

import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.netease.hz.bdms.easyinsight.common.aop.MethodLog;
import com.netease.hz.bdms.easyinsight.common.bo.lineage.LineageForest;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.common.PagingResultDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.event.EventSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.image.ImageRelationDTO;
import com.netease.hz.bdms.easyinsight.common.dto.realtimetest.TestHistoryRecordDTO;
import com.netease.hz.bdms.easyinsight.common.dto.terminal.TerminalSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.enums.*;
import com.netease.hz.bdms.easyinsight.common.exception.CommonException;
import com.netease.hz.bdms.easyinsight.common.query.Search;
import com.netease.hz.bdms.easyinsight.common.query.TaskPageQuery;
import com.netease.hz.bdms.easyinsight.common.util.JsonUtils;
import com.netease.hz.bdms.easyinsight.common.vo.obj.ObjDetailsVO;
import com.netease.hz.bdms.easyinsight.common.vo.requirement.RequirementInfoVO;
import com.netease.hz.bdms.easyinsight.common.vo.task.*;
import com.netease.hz.bdms.easyinsight.dao.model.*;
import com.netease.hz.bdms.easyinsight.dao.model.ObjMappings;
import com.netease.hz.bdms.easyinsight.service.helper.*;
import com.netease.hz.bdms.easyinsight.service.service.*;
import com.netease.hz.bdms.easyinsight.service.service.impl.EventCheckHistoryService;
import com.netease.hz.bdms.easyinsight.service.service.impl.VersionLinkService;
import com.netease.hz.bdms.easyinsight.service.service.asynchandle.AsyncHandleService;
import com.netease.hz.bdms.easyinsight.service.service.obj.EventBuryPointService;
import com.netease.hz.bdms.easyinsight.service.service.obj.ObjTerminalTrackerService;
import com.netease.hz.bdms.easyinsight.service.service.requirement.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReqTaskFacade {

    @Resource
    private TaskProcessService taskProcessService;

    @Resource
    private ReqPoolBasicService reqPoolBasicService;

    @Resource
    private ObjTerminalTrackerService terminalTrackerService;

    @Resource
    private RequirementInfoService requirementInfoService;

    @Resource
    private  ReqTaskService reqTaskService;

    @Resource
    private ReqSpmPoolService reqSpmPoolService;

    @Resource
    private ReqEventPoolService reqEventPoolService;

    @Resource
    private ObjectBasicService objectBasicService;

    @Resource
    private ObjChangeHistoryService objChangeHistoryService;

    @Resource
    private ReqPoolRelBaseService reqPoolRelBaseService;

    @Resource
    private TerminalService terminalService;

    @Resource
    private LineageHelper lineageHelper;

    @Resource
    private ImageRelationService imageRelationService;

    @Resource
    private  EventBuryPointService eventBuryPointService;

    @Resource
    private EventService eventService;

    @Resource
    private TerminalVersionInfoHelper terminalVersionInfoHelper;

    @Resource
    private ObjectHelper objectHelper;

    @Resource
    private EventCheckHistoryService eventCheckHistoryService;

    @Resource
    private NotifyHelper notifyHelper;

    @Resource
    private VersionLinkService versionLinkService;

    @Resource
    private AsyncHandleService asyncHandleService;

    @Resource
    private RealTimeTestRecordService realTimeTestRecordService;

    @Resource
    private MergeConflictHelper mergeConflictHelper;

    @Autowired
    private ReqObjChangeHistoryService reqObjChangeHistoryService;

    @MethodLog
    public PagingResultDTO<ReqTaskVO> queryPagingList(TaskPagingQueryVO queryVo){
        PagingResultDTO<ReqTaskVO> result = new PagingResultDTO<>();
        result.setTotalNum(0);
        result.setPageNum(0);
        result.setList(new ArrayList<>());
        Long appId = EtContext.get(ContextConstant.APP_ID);
        boolean useRequirementCondition = false;
        List<EisRequirementInfo> requirementInfos = new ArrayList<>();
        if(!StringUtils.isEmpty(queryVo.getDataOwnerEmail()) ||
                !StringUtils.isEmpty(queryVo.getReqIssueKey()) || !StringUtils.isEmpty(queryVo.getReqName())){
            boolean useReqPoolCondition = false;
            List<EisReqPool> reqPools = new ArrayList<>();
            if(!StringUtils.isEmpty(queryVo.getDataOwnerEmail())){
                EisReqPool reqPoolQuery = new EisReqPool();
                reqPoolQuery.setDataOwners(queryVo.getDataOwnerEmail());
                reqPools = reqPoolBasicService.search(reqPoolQuery);
                useReqPoolCondition = true;
                if(CollectionUtils.isEmpty(reqPools)){
                    return result;
                }
            }
            EisRequirementInfo reqQuery = new EisRequirementInfo();
            reqQuery.setReqIssueKey(queryVo.getReqIssueKey());
            reqQuery.setReqName(queryVo.getReqName());
            requirementInfos = requirementInfoService.search(reqQuery);
            if(useReqPoolCondition){
                Set<Long> reqPoolIds = reqPools.stream().map(EisReqPool::getId).collect(Collectors.toSet());
                requirementInfos = requirementInfos.stream().filter(e -> reqPoolIds.contains(e.getReqPoolId())).collect(Collectors.toList());
            }
            useRequirementCondition = true;
        }
        if(useRequirementCondition && CollectionUtils.isEmpty(requirementInfos)){
            return result;
        }

        List<EisTaskProcess> processes = new ArrayList<>();
        boolean useProcessCondition = false;
        if(queryVo.getProcessStatus() != null || !StringUtils.isEmpty(queryVo.getProcessOwner())
                || !StringUtils.isEmpty(queryVo.getProcessVerifier()) || !StringUtils.isEmpty(queryVo.getSearch())){
            List<EisReqPoolSpm> reqPoolSpms = new ArrayList<>();
            List<EisReqPoolEvent> reqPoolEvents = new ArrayList<>();
            boolean useObjCondition = false;
            if(!StringUtils.isEmpty(queryVo.getSearch())){
                String searchStr = queryVo.getSearch().replace("_","\\_");
                List<ObjectBasic> objs = new ArrayList<>();
                Search search = new Search();
                search.setSearch(searchStr);
                search.setAppId(appId);
                objs.addAll(objectBasicService.searchLike(search));
                if(!CollectionUtils.isEmpty(objs)){
                    Set<Long> objIds = objs.stream().map(ObjectBasic::getId).collect(Collectors.toSet());
                    reqPoolSpms = reqSpmPoolService.getBatchByObjIds(objIds);
                }
                useObjCondition = true;
                //
                List<EventSimpleDTO> eventsOfCurrentApp = eventService.searchEvent(searchStr,appId,null,null,null,null);
                if(!CollectionUtils.isEmpty(eventsOfCurrentApp)) {
                    Set<Long> eventIds = eventsOfCurrentApp.stream().map(EventSimpleDTO::getId).collect(Collectors.toSet());
                    reqPoolEvents = reqEventPoolService.getBatchByEventIds(eventIds);
                }
                if(CollectionUtils.isEmpty(reqPoolSpms) && CollectionUtils.isEmpty(reqPoolEvents)){
                    return result;
                }
            }
            EisTaskProcess processQuery = new EisTaskProcess();
            processQuery.setStatus(queryVo.getProcessStatus());
            processQuery.setOwnerEmail(queryVo.getProcessOwner());
            processQuery.setVerifierEmail(queryVo.getProcessVerifier());
            processes = taskProcessService.search(processQuery);
            if(useObjCondition){
                Set<Long> reqSpmPoolEntityIds = reqPoolSpms.stream().map(EisReqPoolSpm::getId).collect(Collectors.toSet());
                Set<Long> reqEventPoolEntityIds = reqPoolEvents.stream().map(EisReqPoolEvent::getId).collect(Collectors.toSet());
                processes = processes.stream().filter(
                        e -> (reqSpmPoolEntityIds.contains(e.getReqPoolEntityId()) && e.getReqPoolType().equals(1)) ||
                                (reqEventPoolEntityIds.contains(e.getReqPoolEntityId()) && e.getReqPoolType().equals(3))
                ).collect(Collectors.toList());
            }
            useProcessCondition = true;
        }
        if(useProcessCondition && CollectionUtils.isEmpty(processes)){
            return result;
        }

        Set<Long> reqIdsForFilter = new HashSet<>();
        Set<Long> taskIdsForFilter = new HashSet<>();
        if(useProcessCondition){
            taskIdsForFilter = processes.stream().map(EisTaskProcess::getTaskId).collect(Collectors.toSet());
        }
        if(useRequirementCondition){
            reqIdsForFilter = requirementInfos.stream().map(EisRequirementInfo::getId).collect(Collectors.toSet());
        }
        Set<Long> taskIdsWithoutProcecsses = getTaskIdsWithOutProcesses();
        TaskPageQuery pageQuery = new TaskPageQuery();

        pageQuery.setAppId(appId);
        pageQuery.setTerminalId(queryVo.getTerminalId());
        pageQuery.setStatus(queryVo.getStatus());
        pageQuery.setOwnerEmail(queryVo.getTaskOwner());
        pageQuery.setVerifierEmail(queryVo.getTaskVerifier());
        pageQuery.setTerminalVersion(queryVo.getTerminalVersion());
        pageQuery.setTaskName(queryVo.getTaskName());
        pageQuery.setIteration(queryVo.getSprint());
        pageQuery.setReqIds(reqIdsForFilter);
        pageQuery.setIds(taskIdsForFilter);
        pageQuery.setExcludeIds(taskIdsWithoutProcecsses);
        pageQuery.setOrderBy(queryVo.getOrderBy());
        pageQuery.setOrderRule(queryVo.getOrderRule());
        pageQuery.setCurrentPage(queryVo.getCurrentPage());
        pageQuery.setPageSize(queryVo.getPageSize());
        PageInfo tasksPage = reqTaskService.queryPagingList(pageQuery);
        List<EisReqTask> tasks = tasksPage.getList();
        if(CollectionUtils.isEmpty(tasks)){
            return result;
        }

        List<TerminalSimpleDTO> terminals = terminalService.getByAppId(appId);
            Map<Long,String> terminalMap = new HashMap<>();
        Map<Long,Long> reqIdToPoolIdMap = new HashMap<>();
        Map<Long,EisRequirementInfo> reqMap = new HashMap<>();
        Set<Long> reqIds = new HashSet<>();
        for (EisReqTask task : tasks) {
            reqIds.add(task.getRequirementId());
        }
        List<EisRequirementInfo> relReqs = requirementInfoService.getByIds(reqIds);
        for (EisRequirementInfo requirementInfo : relReqs) {
            reqIdToPoolIdMap.put(requirementInfo.getId(),requirementInfo.getReqPoolId());
            reqMap.put(requirementInfo.getId(),requirementInfo);
        }

        EisReqPool reqPoolQuery = new EisReqPool();
        reqPoolQuery.setAppId(appId);
        List<EisReqPool> reqPools = reqPoolBasicService.search(reqPoolQuery);
        Map<Long,EisReqPool> reqPoolMap = new HashMap<>();
        for (EisReqPool reqPool : reqPools) {
            reqPoolMap.put(reqPool.getId(),reqPool);
        }
        for (TerminalSimpleDTO terminal : terminals) {
            terminalMap.put(terminal.getId(),terminal.getName());
        }

        Set<Long> allConflictReqPoolIds = mergeConflictHelper.getMergeConflictReqPoolIds();

        List<ReqTaskVO> taskVos = new ArrayList<>();
        for (EisReqTask task : tasks) {
            ReqTaskVO vo = new ReqTaskVO();
            EisRequirementInfo requirementInfo = reqMap.get(task.getRequirementId());
            EisReqPool reqPool = reqPoolMap.get(requirementInfo.getReqPoolId());
            vo.setId(task.getId());
            vo.setTerminalId(task.getTerminalId());
            vo.setTerminal(terminalMap.get(task.getTerminalId()));
            vo.setTerminalVersion(task.getTerminalVersion());
            long reqPoolId = reqIdToPoolIdMap.get(task.getRequirementId());
            vo.setReqPoolId(reqPoolId);
            vo.setMergeConflict(allConflictReqPoolIds.contains(reqPoolId));
            if(reqPool != null){
                String dataOwnersString = reqPool.getDataOwners();
                if(!StringUtils.isEmpty(dataOwnersString)){
                    String ownerNames = "";
                    List<UserSimpleDTO> dataOwners = JsonUtils.parseList(reqPool.getDataOwners(),UserSimpleDTO.class);
                    if(dataOwners != null){
                        List<String> ownerNameList = dataOwners.stream().map(UserSimpleDTO::getUserName).collect(Collectors.toList());
                        ownerNames = String.join(",",ownerNameList);
                        vo.setDataOnwers(ownerNames);
                    }
                }
            }
            //任务测试记录
            TestRecordResultVO testResult = new TestRecordResultVO();
            int sum = 0;
            int passNum = 0;
            int unPassNum = 0;
            int partPassNum = 0;
            List<TestHistoryRecordDTO> recordDTOS = realTimeTestRecordService.getTestHistoryByTaskId(task.getId());
            for (TestHistoryRecordDTO historyRecord : recordDTOS){
                if(historyRecord.getTestResult().equals(TestResultEnum.PASS.getType())){
                    passNum ++;
                }else if(historyRecord.getTestResult().equals(TestResultEnum.UNPASS.getType())){
                    unPassNum ++;
                }else if(historyRecord.getTestResult().equals(TestResultEnum.PARTPASS.getType())){
                    partPassNum ++;
                }
                sum ++;
            }
            testResult.setSum(sum);
            testResult.setPassNum(passNum);
            testResult.setUnPassNum(unPassNum);
            testResult.setPartPassNum(partPassNum);
            vo.setTestResult(testResult);
            vo.setOwner(task.getOwnerName());
            vo.setVerifier(task.getVerifierName());
            vo.setReqIssueKey(requirementInfo.getReqIssueKey());
            vo.setReqName(requirementInfo.getReqName());
            vo.setTaskName(task.getTaskName());
            vo.setStatus(task.getStatus());
            vo.setSprint(task.getIteration());
            taskVos.add(vo);
        }
        tasksPage.setList(taskVos);
        result.setList(tasksPage.getList());
        result.setPageNum(tasksPage.getPageNum());
        result.setTotalNum(Long.valueOf(tasksPage.getTotal()).intValue());

        // 组装版本链接
        Set<String> versionNames = taskVos.stream().map(ReqTaskVO::getTerminalVersion).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<String, String> links = versionLinkService.getLinks(versionNames);
        taskVos.forEach(vo -> {
            String versionName = vo.getTerminalVersion();
            if (StringUtils.isNotBlank(versionName)) {
                vo.setTerminalVersionLink(links.get(versionName));
            }
        });

        return result;
    }

    private Set<Long> getTaskIdsWithOutProcesses(){
        EisReqTask taskQuery = new EisReqTask();
        Long appId = EtContext.get(ContextConstant.APP_ID);
        taskQuery.setAppId(appId);
        List<EisReqTask> allTasks = reqTaskService.search(taskQuery);
        Set<Long> allTaskIds = allTasks.stream().map(e -> e.getId()).collect(Collectors.toSet());
        List<EisTaskProcess> processes = taskProcessService.getBatchByTaskIds(allTaskIds);
        if(CollectionUtils.isEmpty(processes)){
            return allTaskIds;
        }
        Set<Long> taskIdsWithProcesses = processes.stream().map(e -> e.getTaskId()).collect(Collectors.toSet());
        Set<Long> taskIdsWithoutProcecsses = Sets.difference(allTaskIds,taskIdsWithProcesses);
        return taskIdsWithoutProcecsses;
    }

    public void setTaskStatus(Long taskId, ReqTaskStatusEnum statusEnum){
        EisReqTask task = reqTaskService.getById(taskId);
        int targetStatus = statusEnum.getState();
        boolean isTransferToTestFinished = ReqTaskStatusEnum.TEST_FINISHED.getState().equals(targetStatus);
        task.setStatus(targetStatus);
        reqTaskService.updateById(task);

        EisTaskProcess query = new EisTaskProcess();
        query.setTaskId(task.getId());
        List<EisTaskProcess> processes = taskProcessService.search(query);
        if(CollectionUtils.isEmpty(processes)){
            throw new CommonException("任务没有待办流程，无法流转");
        }
        // 父任务的状态是Min(子流程状态)。父任务往后流转时，要流转子流程中状态不大于父任务的状态
        List<EisTaskProcess> toUpdateProcesses = processes.stream().filter(p -> p.getStatus() < targetStatus).collect(Collectors.toList());
        for (EisTaskProcess process : toUpdateProcesses) {
            process.setStatus(targetStatus);
        }
        taskProcessService.updateBatch(toUpdateProcesses);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void transTaskStatusToNext(Long id, boolean isInTestFinishPage){
        EisReqTask task = reqTaskService.getById(id);
        Integer status = task.getStatus();
        // 测试完成后，无法通过此接口流转
        if(status >= ReqTaskStatusEnum.TEST_FINISHED.getState()){
            return;
        }
        // 从'开发完成'流转到'测试完成'，必须在测试完成页面
        if(ReqTaskStatusEnum.DEV_FINISHED.getState().equals(status)) {
            if (!isInTestFinishPage) {
                throw new CommonException("请点击'测试完成'按钮操作");
            }
        }

        // 检查是否有基线合并冲突，有冲突要卡住
        EisRequirementInfo requirementInfo = requirementInfoService.getById(task.getRequirementId());
        if (requirementInfo != null) {
            if (mergeConflictHelper.hasMergeConflict(requirementInfo.getReqPoolId())) {
                throw new CommonException("当前任务关联需求组" + requirementInfo.getReqPoolId() + "存在基线合并冲突，请联系需求设计者解决冲突");
            }
        }

        int targetStatus = status + 1;
        task.setStatus(targetStatus);
        reqTaskService.updateById(task);

        EisTaskProcess query = new EisTaskProcess();
        query.setTaskId(task.getId());
        List<EisTaskProcess> processes = taskProcessService.search(query);
        if(CollectionUtils.isEmpty(processes)){
            throw new CommonException("任务没有待办流程，无法流转");
        }
        // 父任务的状态是Min(子流程状态)。父任务往后流转时，要流转子流程中状态不大于父任务的状态
        List<EisTaskProcess> toUpdateProcesses = processes.stream().filter(p -> p.getStatus() < targetStatus).collect(Collectors.toList());
        for (EisTaskProcess process : toUpdateProcesses) {
            process.setStatus(targetStatus);
            if(process.getReqPoolType().equals(ReqPoolTypeEnum.EVENT.getReqPoolType())){
                continue;
            }
            // 记录变更记录
            EisReqObjChangeHistory objReqChangeHistory = new EisReqObjChangeHistory();
            objReqChangeHistory.setReqPoolId(process.getReqPoolId());
            objReqChangeHistory.setObjId(process.getObjId());
            objReqChangeHistory.setNewTrackerInfo(task.getTaskName() + process.getSpmByObjId() + "变更为" + ProcessStatusEnum.fromState(targetStatus).getDesc());
            objReqChangeHistory.setChangeType(JsonUtils.toJson(Collections.singletonList(ObjChangeTypeEnum.TASKCHANGE.getName())));
            reqObjChangeHistoryService.insert(objReqChangeHistory);
        }
        taskProcessService.updateBatch(toUpdateProcesses);
        // 及时通知
        Set<Long> taskProcessIds = processes.stream()
                .map(EisTaskProcess::getId).collect(Collectors.toSet());
        notifyHelper.notifyAfterUpdateSpmStatus(taskProcessIds);
        // 同步任务状态到三方
        asyncHandleService.onTaskAndProcessUpdate(task.getId());
    }

    @Transactional(rollbackFor = Throwable.class)
    public void transProcessStatusToNext(Long id){
        EisTaskProcess taskProcess = taskProcessService.getById(id);
        if (mergeConflictHelper.hasMergeConflict(taskProcess.getReqPoolId())) {
            throw new CommonException("当前任务关联需求组" + taskProcess.getReqPoolId() + "存在基线合并冲突，请联系需求设计者解决冲突");
        }

        Integer status = taskProcess.getStatus();
        if(status >= 5){
            throw new CommonException("状态为测试完成/上线，无法在当前页面操作流转");
        }
        Long taskId = taskProcess.getTaskId();
        EisReqTask reqTask = reqTaskService.getById(taskId);
        if (reqTask == null) {
            throw new CommonException("taskId不存在：" + taskId);
        }

        int targetStatus = status + 1;
        EisTaskProcess processUpdateQuery = new EisTaskProcess();
        processUpdateQuery.setId(taskProcess.getId());
        processUpdateQuery.setStatus(targetStatus);
        taskProcessService.updateById(processUpdateQuery);
        Integer taskStatus = taskProcessService.getTaskNewStatusByProcesses(taskProcess.getTaskId());
        EisReqTask taskUpdateQuery = new EisReqTask();
        taskUpdateQuery.setId(taskProcess.getTaskId());
        taskUpdateQuery.setStatus(taskStatus);
        reqTaskService.updateById(taskUpdateQuery);
        // 记录变更记录
        if(!taskProcess.getReqPoolType().equals(ReqPoolTypeEnum.EVENT.getReqPoolType())) {
            EisReqObjChangeHistory objReqChangeHistory = new EisReqObjChangeHistory();
            objReqChangeHistory.setReqPoolId(taskProcess.getReqPoolId());
            objReqChangeHistory.setObjId(taskProcess.getObjId());
            objReqChangeHistory.setNewTrackerInfo(reqTask.getTaskName() + taskProcess.getSpmByObjId() + "变更为" + ProcessStatusEnum.fromState(targetStatus).getDesc());
            objReqChangeHistory.setChangeType(JsonUtils.toJson(Collections.singletonList(ObjChangeTypeEnum.TASKCHANGE.getName())));
            reqObjChangeHistoryService.insert(objReqChangeHistory);
        }
        // 及时通知
        notifyHelper.notifyAfterUpdateSpmStatus(Collections.singleton(id));
        // 同步任务状态到三方
        asyncHandleService.onTaskAndProcessUpdate(taskProcess.getTaskId());
    }

    @Transactional(rollbackFor = Throwable.class)
    public void transToNextStatusBatch(TransStatusVO transStatusVo){
        Set<Long> processIds = new HashSet<>(transStatusVo.getProcessIds());
        Set<Long> taskIds = new HashSet<>(transStatusVo.getTaskIds());
        if(!CollectionUtils.isEmpty(processIds)){
            List<EisTaskProcess> processes = taskProcessService.getBatchByTaskIds(processIds);
            processes = processes.stream().filter(e -> !taskIds.contains(e.getTaskId())).collect(Collectors.toList());
            for (EisTaskProcess taskProcess : processes) {
                if(taskProcess.getStatus() >= 5){
                    throw new CommonException("存在状态为测试完成/上线的流程，无法在当前页面操作流转");
                }
                taskProcess.setStatus(taskProcess.getStatus() + 1);
                taskProcessService.updateBatch(processes);
                // 记录变更记录
                if(!taskProcess.getReqPoolType().equals(ReqPoolTypeEnum.EVENT.getReqPoolType())) {
                    EisReqTask reqTask = reqTaskService.getById(taskProcess.getTaskId());
                    EisReqObjChangeHistory objReqChangeHistory = new EisReqObjChangeHistory();
                    objReqChangeHistory.setReqPoolId(taskProcess.getReqPoolId());
                    objReqChangeHistory.setObjId(taskProcess.getObjId());
                    objReqChangeHistory.setNewTrackerInfo(reqTask.getTaskName() + taskProcess.getSpmByObjId() + "变更为" + ProcessStatusEnum.fromState(taskProcess.getStatus() + 1).getDesc());
                    objReqChangeHistory.setChangeType(JsonUtils.toJson(Collections.singletonList(ObjChangeTypeEnum.TASKCHANGE.getName())));
                    reqObjChangeHistoryService.insert(objReqChangeHistory);
                }
                // 及时通知
                notifyHelper.notifyAfterUpdateSpmStatus(processIds);
            }
        }
        if(!CollectionUtils.isEmpty(taskIds)){
            for (Long taskId : taskIds) {
                transTaskStatusToNext(taskId, false);
            }
        }
    }

    public void backwardTaskStatus(Long id){
        EisReqTask task = reqTaskService.getById(id);
        Integer status = task.getStatus();
        if(status.equals(1)){
            return;
        }
        task.setStatus(status - 1);
        reqTaskService.updateById(task);

        EisTaskProcess query = new EisTaskProcess();
        query.setTaskId(task.getId());
        List<EisTaskProcess> processes = taskProcessService.search(query);
        for(EisTaskProcess process : processes){
            process.setStatus(status - 1);
            // 记录变更记录
            if(process.getReqPoolType().equals(ReqPoolTypeEnum.EVENT.getReqPoolType())){
                continue;
            }
            EisReqTask reqTask = reqTaskService.getById(process.getTaskId());
            EisReqObjChangeHistory objReqChangeHistory = new EisReqObjChangeHistory();
            objReqChangeHistory.setReqPoolId(process.getReqPoolId());
            objReqChangeHistory.setObjId(process.getObjId());
            objReqChangeHistory.setNewTrackerInfo(reqTask.getTaskName() + process.getSpmByObjId() + "变更为" + ProcessStatusEnum.fromState(process.getStatus() + 1).getDesc());
            objReqChangeHistory.setChangeType(JsonUtils.toJson(Collections.singletonList(ObjChangeTypeEnum.TASKCHANGE.getName())));
            reqObjChangeHistoryService.insert(objReqChangeHistory);
        }
        taskProcessService.updateBatch(processes);
        // 及时通知
        Set<Long> taskProcessIds = processes.stream()
                .map(EisTaskProcess::getId).collect(Collectors.toSet());
        notifyHelper.notifyAfterUpdateSpmStatus(taskProcessIds);
        // 同步任务状态到三方
        asyncHandleService.onTaskAndProcessUpdate(task.getId());
    }

    public void backwardProcessStatus(Long id){
        EisTaskProcess taskProcess = taskProcessService.getById(id);
        Integer status = taskProcess.getStatus();
        if(status == 1){
            return;
        }
        EisTaskProcess processUpdateQuery = new EisTaskProcess();
        processUpdateQuery.setId(id);
        processUpdateQuery.setStatus(status - 1);
        taskProcessService.updateById(processUpdateQuery);
        Integer taskStatus = taskProcessService.getTaskNewStatusByProcesses(taskProcess.getTaskId());
        EisReqTask taskUpdateQuery = new EisReqTask();
        taskUpdateQuery.setId(taskProcess.getTaskId());
        taskUpdateQuery.setStatus(taskStatus);
        reqTaskService.updateById(taskUpdateQuery);
        // 记录变更记录
        if(!taskProcess.getReqPoolType().equals(ReqPoolTypeEnum.EVENT.getReqPoolType())) {
            EisReqTask reqTask = reqTaskService.getById(taskProcess.getTaskId());
            EisReqObjChangeHistory objReqChangeHistory = new EisReqObjChangeHistory();
            objReqChangeHistory.setReqPoolId(taskProcess.getReqPoolId());
            objReqChangeHistory.setObjId(taskProcess.getObjId());
            objReqChangeHistory.setNewTrackerInfo(reqTask.getTaskName() + taskProcess.getSpmByObjId() + "变更为" + ProcessStatusEnum.fromState(taskProcess.getStatus() + 1).getDesc());
            objReqChangeHistory.setChangeType(JsonUtils.toJson(Collections.singletonList(ObjChangeTypeEnum.TASKCHANGE.getName())));
            reqObjChangeHistoryService.insert(objReqChangeHistory);
        }
        // 及时通知
        notifyHelper.notifyAfterUpdateSpmStatus(Collections.singleton(id));
        // 同步任务状态到三方
        asyncHandleService.onTaskAndProcessUpdate(taskProcess.getTaskId());
    }

    @Transactional(rollbackFor = Throwable.class)
    public void backwardStatusBatch(TransStatusVO transStatusVo){
        List<Long> processIds = transStatusVo.getProcessIds();
        if(!CollectionUtils.isEmpty(processIds)){
            List<EisTaskProcess> taskProcesses = new ArrayList<>();
            for (Long processId : processIds) {
                EisTaskProcess taskProcess = new EisTaskProcess();
                taskProcess.setId(processId);
                taskProcess.setStatus(transStatusVo.getTargetStatus());
                taskProcesses.add(taskProcess);
            }
            taskProcessService.updateBatch(taskProcesses);
            // 及时通知
            notifyHelper.notifyAfterUpdateSpmStatus(Sets.newHashSet(processIds));
        }
        List<Long> taskIds = transStatusVo.getTaskIds();
        if(!CollectionUtils.isEmpty(taskIds)){
            List<EisReqTask> tasks = new ArrayList<>();
            List<EisTaskProcess> taskProcesses = new ArrayList<>();
            for (Long taskId : taskIds) {
                EisReqTask task = new EisReqTask();
                task.setId(taskId);
                task.setStatus(transStatusVo.getTargetStatus());
                tasks.add(task);
                EisTaskProcess processQuery = new EisTaskProcess();
                processQuery.setTaskId(taskId);
                taskProcesses.addAll(taskProcessService.search(processQuery));
            }
            reqTaskService.updateBatch(tasks);
            for (EisTaskProcess taskProcess : taskProcesses) {
                taskProcess.setStatus(transStatusVo.getTargetStatus());
                if(taskProcess.getReqPoolType().equals(ReqPoolTypeEnum.EVENT.getReqPoolType())){
                    continue;
                }
                EisReqTask reqTask = reqTaskService.getById(taskProcess.getTaskId());
                EisReqObjChangeHistory objReqChangeHistory = new EisReqObjChangeHistory();
                objReqChangeHistory.setReqPoolId(taskProcess.getReqPoolId());
                objReqChangeHistory.setObjId(taskProcess.getObjId());
                objReqChangeHistory.setNewTrackerInfo(reqTask.getTaskName() + taskProcess.getSpmByObjId() + "变更为" + ProcessStatusEnum.fromState(taskProcess.getStatus() + 1).getDesc());
                objReqChangeHistory.setChangeType(JsonUtils.toJson(Collections.singletonList(ObjChangeTypeEnum.TASKCHANGE.getName())));
                reqObjChangeHistoryService.insert(objReqChangeHistory);
            }
            taskProcessService.updateBatch(taskProcesses);
            // 及时通知
            Set<Long> taskProcessIds = taskProcesses.stream()
                    .map(EisTaskProcess::getId).collect(Collectors.toSet());
            notifyHelper.notifyAfterUpdateSpmStatus(taskProcessIds);
            // 同步到三方
            taskIds.forEach(taskId -> asyncHandleService.onTaskAndProcessUpdate(taskId));
        }

    }

    public void deliverBatch(DeliverVO deliverVO){
        Set<Long> taskIds = deliverVO.getTaskIds();
        Set<Long> processIds = deliverVO.getProcessIds();
        if(!CollectionUtils.isEmpty(taskIds)){
            List<EisReqTask> tasks = reqTaskService.getByIds(taskIds);
            if (CollectionUtils.isEmpty(tasks)) {
                throw new CommonException("没有需要流转的任务");
            }
            Set<Long> requirementIds = tasks.stream().map(EisReqTask::getRequirementId).collect(Collectors.toSet());
            List<EisRequirementInfo> requirementInfos = requirementInfoService.getByIds(requirementIds);
            if (CollectionUtils.isNotEmpty(requirementInfos)) {
                Set<Long> reqPoolIds = requirementInfos.stream().map(o -> o.getReqPoolId()).collect(Collectors.toSet());
                Set<Long> conflictReqPoolIds = mergeConflictHelper.filterConflictReqPoolIds(reqPoolIds);
                if (CollectionUtils.isNotEmpty(conflictReqPoolIds)) {
                    throw new CommonException("需求组" + JsonUtils.toJson(conflictReqPoolIds) + "下存在合并冲突，请先解决冲突");
                }
            }

            List<EisReqTask> updateList = new ArrayList<>();
            for (Long taskId : taskIds) {
                EisReqTask task = new EisReqTask();
                task.setId(taskId);
                if(deliverVO.getDeliverType().equalsIgnoreCase(DeliverTypeEnum.OWNER.name())){
                    task.setOwnerEmail(deliverVO.getUserDTO().getEmail());
                    task.setOwnerName(deliverVO.getUserDTO().getUserName());
                }else {
                    task.setVerifierEmail(deliverVO.getUserDTO().getEmail());
                    task.setVerifierName(deliverVO.getUserDTO().getUserName());
                }
                updateList.add(task);
            }
            reqTaskService.updateBatch(updateList);
        }
        if(!CollectionUtils.isEmpty(processIds)){

            List<EisTaskProcess> processes = taskProcessService.getBatchByIds(processIds);
            if (CollectionUtils.isEmpty(processes)) {
                throw new CommonException("没有需要流转的任务");
            }
            Set<Long> reqPoolIds = processes.stream().map(EisTaskProcess::getReqPoolId).collect(Collectors.toSet());
            Set<Long> conflictReqPoolIds = mergeConflictHelper.filterConflictReqPoolIds(reqPoolIds);
            if (CollectionUtils.isNotEmpty(conflictReqPoolIds)) {
                throw new CommonException("需求组" + JsonUtils.toJson(conflictReqPoolIds) + "下存在合并冲突，请先解决冲突");
            }

            if (deliverVO.getDeliverType().equalsIgnoreCase(DeliverTypeEnum.OWNER.name())) {
                taskProcessService.updateOwner(new ArrayList<>(processIds), deliverVO.getUserDTO().getUserName(), deliverVO.getUserDTO().getEmail());
            }
            if (deliverVO.getDeliverType().equalsIgnoreCase(DeliverTypeEnum.VERIFIER.name())) {
                taskProcessService.updateVerifier(new ArrayList<>(processIds), deliverVO.getUserDTO().getUserName(), deliverVO.getUserDTO().getEmail());
            }
        }
    }

    public void setTerminalVersion(Long taskId, String terminalVersion){
        EisReqTask task = new EisReqTask();
        task.setId(taskId);
        task.setTerminalVersion(terminalVersion);
        reqTaskService.updateById(task);
        //调用端版本service并尝试插入
        terminalVersionInfoHelper.checkAndInsert(terminalVersion);
    }

    public void setSprint(Long taskId, String sprint){
        EisReqTask task = new EisReqTask();
        task.setId(taskId);
        task.setIteration(sprint);
        reqTaskService.updateById(task);
    }

    public TaskSearchAggreVO getSearchAggre(){
        EisRequirementInfo reqQuery = new EisRequirementInfo();
        Long appId = EtContext.get(ContextConstant.APP_ID);
        reqQuery.setAppId(appId);
        List<EisRequirementInfo> reqList = requirementInfoService.search(reqQuery);
        List<RequirementInfoVO> reqInfos = new ArrayList<>(reqList.size());
        Set<String> reqKeys = new HashSet<>();
        Set<String> reqNames = new HashSet<>();
        for (EisRequirementInfo requirementInfo : reqList) {
            if(requirementInfo.getReqIssueKey() != null){
                reqKeys.add(requirementInfo.getReqIssueKey());
            }
            reqNames.add(requirementInfo.getReqName());

            RequirementInfoVO vo = new RequirementInfoVO();
            vo.setReqId(requirementInfo.getId());
            vo.setReqIssueKey(requirementInfo.getReqIssueKey());
            vo.setName(requirementInfo.getReqName());
            vo.setPriority(requirementInfo.getPriority());
            vo.setTeam(requirementInfo.getTeam());
            vo.setBusinessArea(requirementInfo.getBusinessArea());
            vo.setViews(requirementInfo.getViews());
            vo.setOmState(requirementInfo.getOmState());
            vo.setDesc(requirementInfo.getDescription());
            reqInfos.add(vo);
        }
        List<TerminalSimpleDTO> terminals = terminalService.getByAppId(appId);
        List<TaskSearchAggreVO.TerminalAggre> terminalAggres = new ArrayList<>();
        for (TerminalSimpleDTO terminal : terminals) {
            TaskSearchAggreVO.TerminalAggre terminalAggre = new TaskSearchAggreVO.TerminalAggre();
            terminalAggre.setId(terminal.getId());
            terminalAggre.setName(terminal.getName());
            terminalAggres.add(terminalAggre);
        }
        Set<String> termnialVersions = new HashSet<>();
        Set<String> sprints = new HashSet<>();
        EisReqTask taskQuery = new EisReqTask();
        taskQuery.setAppId(appId);
        List<EisReqTask> tasks = reqTaskService.search(taskQuery);
        for (EisReqTask task : tasks) {
            if(task.getTerminalVersion() != null){
                termnialVersions.add(task.getTerminalVersion());
            }
            if(task.getIteration() != null){
                sprints.add(task.getIteration());
            }
        }
        TaskSearchAggreVO vo = new TaskSearchAggreVO();
        vo.setReqKeys(reqKeys);
        vo.setReqName(reqNames);
        vo.setReqInfos(reqInfos);
        vo.setTerminals(terminalAggres);
        vo.setTermnialVersions(termnialVersions);
        vo.setSprints(sprints);
        return vo;
    }

    @MethodLog
    public TaskProcessVO getProcessVo(TaskProcessViewQueryVO queryVo){
        Long appId = EtContext.get(ContextConstant.APP_ID);
        ObjMappings objMappings = objectBasicService.getMapping(EtContext.get(ContextConstant.APP_ID), null);
        Map<Long,String> objIdToOidMap = objMappings.getObjIdToOidMap();
        Map<Long,String> objIdToNameMap = objMappings.getObjIdToNameMap();
        List<EventSimpleDTO> eventsOfCurrentApp = eventService.searchEvent(null,appId,null,null,null,null);
        Map<Long,String> eventIdToCodeMap = new HashMap<>();
        Map<Long,String> eventIdToNameMap = new HashMap<>();
        for (EventSimpleDTO event : eventsOfCurrentApp) {
            eventIdToCodeMap.put(event.getId(),event.getCode());
            eventIdToNameMap.put(event.getId(),event.getName());
        }
        EisReqTask task = reqTaskService.getById(queryVo.getTaskId());
        if (task == null) {
            log.error("taskId " + queryVo.getTaskId() + " 不存在");
            return new TaskProcessVO();
        }
        EisRequirementInfo requirementInfo = requirementInfoService.getById(task.getRequirementId());
        EisTaskProcess processQuery = new EisTaskProcess();
        processQuery.setTaskId(queryVo.getTaskId());
        processQuery.setOwnerEmail(queryVo.getOwner());
        processQuery.setVerifierEmail(queryVo.getVerifier());
        processQuery.setStatus(queryVo.getStatus());
        List<EisTaskProcess> taskProcesses = taskProcessService.search(processQuery);
        if(!StringUtils.isEmpty(queryVo.getObjSearch())){
            taskProcesses = taskProcesses.stream().filter(e ->{
                String oid = objIdToOidMap.get(e.getObjId());
                String oidName = objIdToNameMap.get(e.getObjId());

                String eventCode = eventIdToCodeMap.get(e.getEventId());
                String eventName = eventIdToNameMap.get(e.getEventId());

                return (!StringUtils.isEmpty(oid) && oid.contains(queryVo.getObjSearch())) ||
                       (!StringUtils.isEmpty(oidName) && oidName.contains(queryVo.getObjSearch())) ||
                       (!StringUtils.isEmpty(eventCode) && eventCode.contains(queryVo.getObjSearch())) ||
                       (!StringUtils.isEmpty(eventName) && eventName.contains(queryVo.getObjSearch()));
            }).collect(Collectors.toList());
        }
        TaskSpmTreeVO devSpmTreeVo = getTaskSpmTreeVo(task.getTerminalId(),taskProcesses,ReqPoolTypeEnum.SPM_DEV);
        TaskSpmTreeVO deleteSpmTreeVo = getTaskSpmTreeVo(task.getTerminalId(),taskProcesses,ReqPoolTypeEnum.SPM_DELETE);
        List<TaskEventVO> taskEventVos = getTaskEventVos(taskProcesses);
        TaskProcessVO taskProcessVo = new TaskProcessVO();
        taskProcessVo.setDevSpmTree(devSpmTreeVo);
        taskProcessVo.setDeleteSpmTree(deleteSpmTreeVo);
        taskProcessVo.setEvents(taskEventVos);
        taskProcessVo.setTaskName(task.getTaskName());
        taskProcessVo.setReqName(requirementInfo.getReqName());
        return taskProcessVo;
    }

    public Map<Long, TaskProcessVO> getProcessVosBatch(List<Long> taskIds){
        List<EisReqTask> tasks = reqTaskService.getByIds(new HashSet<>(taskIds));
        if (CollectionUtils.isEmpty(tasks)) {
            return new HashMap<>();
        }

        List<EisRequirementInfo> requirementInfos = requirementInfoService.getByIds(tasks.stream().map(EisReqTask::getRequirementId).collect(Collectors.toSet()));
        Map<Long, EisRequirementInfo> requirementInfoMap = requirementInfos.stream().collect(Collectors.toMap(EisRequirementInfo::getId, o -> o, (oldV, newV) -> newV));

        List<EisTaskProcess> processes = taskProcessService.getBatchByTaskIds(new HashSet<>(taskIds));
        Map<Long, List<EisTaskProcess>> processesOfTaskIds = processes.stream().collect(Collectors.groupingBy(EisTaskProcess::getTaskId));

        Set<Long> allReqPoolIds = processes.stream().map(EisTaskProcess::getReqPoolId).collect(Collectors.toSet());
        List<EisReqPoolRelBaseRelease> allCurrentUse = reqPoolRelBaseService.batchGetCurrentUse(allReqPoolIds);
        Map<Long, List<EisReqPoolRelBaseRelease>> reqPoolRelBaseReleaseGroupByReqPoolId = allCurrentUse.stream().collect(Collectors.groupingBy(EisReqPoolRelBaseRelease::getReqPoolId));
        Set<Long> allBaseReleaseIds = allCurrentUse.stream().map(EisReqPoolRelBaseRelease::getBaseReleaseId).collect(Collectors.toSet());
        Map<Long, List<EisObjTerminalTracker>> trackersOfBaseGroupByBaseReleaseId = objectHelper.getTrackersOfReqPoolBases(allBaseReleaseIds);
        Map<Long, Set<Long>> mergeConflictObjIdsGroupByReqPoolId = mergeConflictHelper.getAllMergeConflictObjIdsGroupByReqPoolId();

        Map<Long, TaskProcessVO> result = new HashMap<>();
        tasks.forEach(task -> {
            EisRequirementInfo requirementInfo = requirementInfoMap.get(task.getRequirementId());
            List<EisTaskProcess> taskProcesses = processesOfTaskIds.get(task.getId());
            if (taskProcesses == null) {
                taskProcesses = new ArrayList<>(0);
            }
            TaskSpmTreeVO devSpmTreeVo = getTaskSpmTreeVo(task.getTerminalId(), taskProcesses, ReqPoolTypeEnum.SPM_DEV,
                    trackersOfBaseGroupByBaseReleaseId, mergeConflictObjIdsGroupByReqPoolId, reqPoolRelBaseReleaseGroupByReqPoolId);
            TaskSpmTreeVO deleteSpmTreeVo = getTaskSpmTreeVo(task.getTerminalId(), taskProcesses, ReqPoolTypeEnum.SPM_DELETE,
                    trackersOfBaseGroupByBaseReleaseId, mergeConflictObjIdsGroupByReqPoolId, reqPoolRelBaseReleaseGroupByReqPoolId);
            List<TaskEventVO> taskEventVos = getTaskEventVos(taskProcesses);
            TaskProcessVO taskProcessVo = new TaskProcessVO();
            taskProcessVo.setDevSpmTree(devSpmTreeVo);
            taskProcessVo.setDeleteSpmTree(deleteSpmTreeVo);
            taskProcessVo.setEvents(taskEventVos);
            taskProcessVo.setTaskName(task.getTaskName());
            taskProcessVo.setReqName(requirementInfo == null ? "" : requirementInfo.getReqName());
            result.put(task.getId(), taskProcessVo);
        });

        return result;
    }

    private TaskSpmTreeVO getTaskSpmTreeVo(Long terminalId, List<EisTaskProcess> taskProcesses, ReqPoolTypeEnum reqPoolTypeEnum) {
        return getTaskSpmTreeVo(terminalId, taskProcesses, reqPoolTypeEnum, null, null, null);
    }

    private TaskSpmTreeVO getTaskSpmTreeVo(Long terminalId, List<EisTaskProcess> taskProcesses, ReqPoolTypeEnum reqPoolTypeEnum,
                                           Map<Long, List<EisObjTerminalTracker>> trackersOfBaseGroupByBaseReleaseId,
                                           Map<Long, Set<Long>> mergeConflictObjIdsGroupByReqPoolId,
                                           Map<Long, List<EisReqPoolRelBaseRelease>> reqPoolRelBaseReleaseGroupByReqPoolId) {
        Map<Long,EisTaskProcess> poolEntityIdToProcessMap = new HashMap<>();
        for (EisTaskProcess taskProcess : taskProcesses) {
            poolEntityIdToProcessMap.put(taskProcess.getReqPoolEntityId(),taskProcess);
        }
        List<EisTaskProcess> spmProcessesOfType = taskProcesses.stream()
                .filter(e -> e.getReqPoolType().equals(reqPoolTypeEnum.getReqPoolType()))
                .collect(Collectors.toList());
        if(CollectionUtils.isEmpty(spmProcessesOfType)){
            return new TaskSpmTreeVO();
        }
        Long reqPoolId = spmProcessesOfType.get(0).getReqPoolId();
        // 性能优化，因为此方法可能被循环调，可以由上层批量传入mergeConflictObjIdsMap
        Set<Long> mergeConflictObjIds = mergeConflictObjIdsGroupByReqPoolId == null ? mergeConflictHelper.getMergeConflictObjIdsOfReqPool(reqPoolId) : mergeConflictObjIdsGroupByReqPoolId.get(reqPoolId);
        if (mergeConflictObjIds == null) {
            mergeConflictObjIds = new HashSet<>();
        }
        // 性能优化，因为此方法可能被循环调，可以由上层批量传入trackersOfBaseMap
        EisReqPoolRelBaseRelease reqPoolRelBaseRelease;
        if (reqPoolRelBaseReleaseGroupByReqPoolId == null) {
            reqPoolRelBaseRelease = reqPoolRelBaseService.getCurrentUse(reqPoolId, terminalId);
        } else {
            List<EisReqPoolRelBaseRelease> byReqPoolId = reqPoolRelBaseReleaseGroupByReqPoolId.get(reqPoolId);
            reqPoolRelBaseRelease = byReqPoolId.stream().filter(o -> o.getTerminalId().equals(terminalId)).findFirst().orElse(null);
        }
        Long baseReleaseId = 0L;
        if(reqPoolRelBaseRelease != null){
            baseReleaseId = reqPoolRelBaseRelease.getBaseReleaseId();
        }
        // 性能优化，因为此方法可能被循环调，可以由上层批量传入trackersOfBaseMap
        List<EisObjTerminalTracker> trackersOfBase = trackersOfBaseGroupByBaseReleaseId == null ? objectHelper.getTrackersOfReqPoolBase(baseReleaseId) : trackersOfBaseGroupByBaseReleaseId.get(baseReleaseId);
        if (trackersOfBase == null) {
            trackersOfBase = new ArrayList<>(0);
        }
        Map<Long,Long> objIdToHistoryIdMapOfBase = new HashMap<>();
        for (EisObjTerminalTracker trackerOfBase : trackersOfBase) {
            objIdToHistoryIdMapOfBase.put(trackerOfBase.getObjId(),trackerOfBase.getObjHistoryId());
        }
        List<EisObjChangeHistory> changeHistoriesOfReqPool = objChangeHistoryService.getByReqPoolId(reqPoolId);
        for (EisObjChangeHistory objChangeHistory : changeHistoriesOfReqPool) {
            objIdToHistoryIdMapOfBase.put(objChangeHistory.getObjId(),objChangeHistory.getId());
        }
        Set<Long> spmPoolEntityIdsOfType = spmProcessesOfType.stream().map(e -> e.getReqPoolEntityId()).collect(Collectors.toSet());
        List<EisReqPoolSpm> spmEntitiesOfType = reqSpmPoolService.getBatchByIds(spmPoolEntityIdsOfType);
        List<List<Long>> spmsOfObjIdList = new ArrayList<>();
        Set<Long> objIds = new HashSet<>();
        Set<Long> historyIds = new HashSet<>();
        Map<Long, Long> outerSpaceObjIds = new HashMap<>();
        for (EisReqPoolSpm spmEntity : spmEntitiesOfType) {
            String spmByObjId = spmEntity.getSpmByObjId();
            List<Long> spmByObjIdList = Arrays.stream(spmByObjId.split("\\|")).map(e -> Long.valueOf(e)).collect(Collectors.toList());
            spmsOfObjIdList.add(spmByObjIdList);
            objIds.addAll(spmByObjIdList);
            historyIds.add(spmEntity.getObjHistoryId());
            if (spmEntity.getBridgeAppId() != null) {
                boolean isParentObjId = false;
                for (Long objId : spmByObjIdList) {
                    // 桥梁以后都是父空间对象
                    if (spmEntity.getBridgeObjId().equals(objId)) {
                        isParentObjId = true;
                    }
                    if (isParentObjId) {
                        outerSpaceObjIds.put(objId, spmEntity.getBridgeAppId());
                    }
                }
            }
        }
        List<ObjectBasic> objs = objectBasicService.getByIds(objIds);
        Map<Long,ObjectBasic> objMap = new HashMap<>();
        List<TaskSpmTreeVO.ObjInfo> objInfos = new ArrayList<>();
        for (ObjectBasic obj : objs) {
            Long objId = obj.getId();
            objMap.put(objId,obj);
            TaskSpmTreeVO.ObjInfo objInfo = new TaskSpmTreeVO.ObjInfo();
            objInfo.setObjId(objId);
            objInfo.setHistoryId(objIdToHistoryIdMapOfBase.get(objId));
            objInfo.setOid(obj.getOid());
            objInfo.setObjName(obj.getName());
            objInfo.setObjType(obj.getType());
            objInfo.setTerminalId(terminalId);
            objInfo.setReqPoolId(reqPoolId);
            Long outerSpaceObjAppId = outerSpaceObjIds.get(objId);
            if (outerSpaceObjAppId != null) {
                objInfo.setOtherAppId(outerSpaceObjAppId);
            }
            objInfos.add(objInfo);
        }

        List<ImageRelationDTO> imageRelations = imageRelationService.getByEntityId(historyIds);
        Set<Long> objIdContainsPicture = new HashSet<>();
        for (ImageRelationDTO imageRelation : imageRelations) {
            objIdContainsPicture.add(imageRelation.getEntityId());
        }
        LineageForest lineageForest = lineageHelper.buildForestBySpms(spmsOfObjIdList);
        TaskSpmTreeVO taskSpmTreeVO = new TaskSpmTreeVO();
        taskSpmTreeVO.setObjInfos(objInfos);
        taskSpmTreeVO.setRoots(lineageForest.getRoots());

        EisObjTerminalTracker trackerQuery = new EisObjTerminalTracker();
        trackerQuery.setReqPoolId(reqPoolId);
        trackerQuery.setTerminalId(terminalId);
        List<EisObjTerminalTracker> trackers = terminalTrackerService.search(trackerQuery);
        Map<String,Long> uniqueKeyToTrackerIdMap = new HashMap<>();
        Set<Long> trackerIds = new HashSet<>();
        for (EisObjTerminalTracker tracker : trackers) {
            String uniqueKey = tracker.getObjId() + "|" +tracker.getObjHistoryId();
            uniqueKeyToTrackerIdMap.put(uniqueKey,tracker.getId());
            trackerIds.add(tracker.getId());
        }
        List<TaskProcessSpmEntityVO> devEntityVos = new ArrayList<>();
        for (EisReqPoolSpm spmEntity : spmEntitiesOfType) {
            TaskProcessSpmEntityVO spmEntityVo = new TaskProcessSpmEntityVO();
            ObjectBasic obj = objMap.get(spmEntity.getObjId());
            EisTaskProcess process = poolEntityIdToProcessMap.get(spmEntity.getId());
            String reqTypeEnumStr = spmEntity.getReqType();
            List<String> reqTypeNameList = new ArrayList<>();
            if(!StringUtils.isEmpty(reqTypeEnumStr)){
                List<String> reqTypeEnumList = Lists.newArrayList(reqTypeEnumStr.split(","));
                for (String enumCodeStr : reqTypeEnumList) {
                    Integer enumCode = Integer.valueOf(enumCodeStr);
                    RequirementTypeEnum requirementTypeEnum = RequirementTypeEnum.fromReqType(enumCode);
                    reqTypeNameList.add(requirementTypeEnum.getDesc());
                }
            }
            String reqType = "";
            spmEntityVo.setId(process.getId());
            spmEntityVo.setOid(obj.getOid());
            spmEntityVo.setObjName(obj.getName());
            spmEntityVo.setObjType(obj.getType());
            spmEntityVo.setReqType(String.join("，",reqTypeNameList));
            spmEntityVo.setHasPicture(objIdContainsPicture.contains(spmEntity.getObjHistoryId()));
            spmEntityVo.setSpmByObjId(spmEntity.getSpmByObjId());
            spmEntityVo.setOwner(process.getOwnerName());
            spmEntityVo.setVerifier(process.getVerifierName());
            spmEntityVo.setStatus(process.getStatus());
//            String uniqueKey = spmEntity.getObjId() + "|" + spmEntity.getObjHistoryId();
//            Long trackerId = uniqueKeyToTrackerIdMap.get(uniqueKey);
//            List<CheckHistoryNoDetail> histories = getHistories(trackerId, spmEntity.getSpmByObjId(), checkHistories);
//            spmEntityVo.setTestRecordNum(histories.size());
//            spmEntityVo.setFailedTestRecordNum((int) histories.stream()
//                    .filter(o -> !CheckResultEnum.PASS.getResult().equals(o.getCheckResult())).count());
            spmEntityVo.setMergeConflict(MergeConflictHelper.hasMergeConflict(spmEntity.getSpmByObjId(), mergeConflictObjIds));
            devEntityVos.add(spmEntityVo);
        }
        taskSpmTreeVO.setEntities(devEntityVos);
        return taskSpmTreeVO;
    }

    public List<TaskEventVO> getTaskEventVos(List<EisTaskProcess> taskProcesses){
        List<EisTaskProcess> spmProcessesOfEvent = taskProcesses.stream()
                .filter(e -> e.getReqPoolType().equals(ReqPoolTypeEnum.EVENT.getReqPoolType()))
                .collect(Collectors.toList());
        if(CollectionUtils.isEmpty(spmProcessesOfEvent)){
            return new ArrayList<>();
        }
        Set<Long> eventPoolEntityIds = spmProcessesOfEvent.stream().map(EisTaskProcess::getReqPoolEntityId).collect(Collectors.toSet());
        List<EisReqPoolEvent> reqPoolEvents = reqEventPoolService.getBatchByIds(eventPoolEntityIds);
        Set<Long> eventBuryPointIds = new HashSet<>();
        Map<Long,Long> poolEntityIdToBuryPointIdMap = new HashMap<>();
        for (EisReqPoolEvent reqPoolEvent : reqPoolEvents) {
            eventBuryPointIds.add(reqPoolEvent.getEventBuryPointId());
            poolEntityIdToBuryPointIdMap.put(reqPoolEvent.getId(),reqPoolEvent.getEventBuryPointId());
        }

        Map<Long,Long> buryPointIdToEventIdMap = new HashMap<>();
        Set<Long> eventIds = new HashSet<>();
        List<EisEventBuryPoint> eisEventBuryPoints = eventBuryPointService.getByIds(eventBuryPointIds);
        for (EisEventBuryPoint eisEventBuryPoint : eisEventBuryPoints) {
            buryPointIdToEventIdMap.put(eisEventBuryPoint.getId(),eisEventBuryPoint.getEventId());
            eventIds.add(eisEventBuryPoint.getEventId());
        }
        List<EventSimpleDTO> events = eventService.getEventByIds(eventIds);
        Map<Long,EventSimpleDTO> eventMap = new HashMap<>();
        for (EventSimpleDTO event : events) {
            eventMap.put(event.getId(),event);
        }
        Map<Long, List<CheckHistoryNoDetail>> allByBuryPointIds = CollectionUtils.isEmpty(eisEventBuryPoints) ? new HashMap<>()
                : eventCheckHistoryService.getAllByBuryPointIds(eisEventBuryPoints.stream().map(EisEventBuryPoint::getId).collect(Collectors.toSet()));

        List<TaskEventVO> voList = new ArrayList<>();
        for (EisTaskProcess taskProcess : spmProcessesOfEvent) {
            Long reqEventPoolEntityId = taskProcess.getReqPoolEntityId();
            Long eventBuryPointId = poolEntityIdToBuryPointIdMap.get(reqEventPoolEntityId);
            Long eventId = buryPointIdToEventIdMap.get(eventBuryPointId);
            EventSimpleDTO event = eventMap.get(eventId);
            if (event == null) {
                log.error("eventId={} 不存在", eventId);
                continue;
            }
            TaskEventVO eventVo = new TaskEventVO();
            eventVo.setId(taskProcess.getId());
            eventVo.setEventCode(event.getCode());
            eventVo.setEventName(event.getName());
            eventVo.setStatus(taskProcess.getStatus());
            eventVo.setOwner(taskProcess.getOwnerName());
            eventVo.setVerifier(taskProcess.getVerifierName());
            eventVo.setEventBuryPointId(eventBuryPointId);
            List<CheckHistoryNoDetail> histories = allByBuryPointIds.get(eventBuryPointId);
            if (histories == null) {
                histories = new ArrayList<>(0);
            }
            eventVo.setTestRecordNum(histories.size());
            eventVo.setFailedTestRecordNum((int) histories.stream()
                    .filter(o -> !CheckResultEnum.PASS.getResult().equals(o.getCheckResult())).count());
            voList.add(eventVo);
        }
        return voList;
    }


}
