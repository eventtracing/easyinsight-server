package com.netease.hz.bdms.easyinsight.service.service.impl;

import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.netease.hz.bdms.easyinsight.common.bo.lineage.LinageGraph;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.terminal.TerminalSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.enums.*;
import com.netease.hz.bdms.easyinsight.common.query.TaskPageQuery;
import com.netease.hz.bdms.easyinsight.common.util.JsonUtils;
import com.netease.hz.bdms.easyinsight.common.vo.task.*;
import com.netease.hz.bdms.easyinsight.dao.model.*;
import com.netease.hz.bdms.easyinsight.service.helper.LineageHelper;
import com.netease.hz.bdms.easyinsight.service.service.TerminalService;
import com.netease.hz.bdms.easyinsight.service.service.TerminalVersionInfoService;
import com.netease.hz.bdms.easyinsight.service.service.requirement.*;
import com.netease.hz.bdms.easyinsight.service.service.terminalrelease.TerminalReleaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReqTaskProcessService {

    @Autowired
    private TaskProcessService taskProcessService;

    @Autowired
    private ReqPoolBasicService reqPoolBasicService;

    @Autowired
    private RequirementInfoService requirementInfoService;

    @Autowired
    private ReqTaskService reqTaskService;

    @Autowired
    private TerminalService terminalService;

    @Autowired
    private TerminalVersionInfoService terminalVersionInfoService;

    @Autowired
    private TerminalReleaseService terminalReleaseService;

    @Autowired
    private LineageHelper lineageHelper;


    public List<ReqTaskVO> queryTaskInfoList(String reqIssueKey){

        List<ReqTaskVO> result = new ArrayList();
        Long appId = 4L;

        EisRequirementInfo reqQuery = new EisRequirementInfo();
        reqQuery.setReqIssueKey(reqIssueKey);
        List<EisRequirementInfo> requirementInfos = requirementInfoService.search(reqQuery);

        if(CollectionUtils.isEmpty(requirementInfos)){
            return result;
        }

        Set<Long> reqIdsForFilter = requirementInfos.stream().map(e -> e.getId()).collect(Collectors.toSet());
        Set<Long> taskIdsWithoutProcecsses = getTaskIdsWithOutProcesses();
        TaskPageQuery pageQuery = new TaskPageQuery();

        pageQuery.setAppId(appId);
//        pageQuery.setTerminalId(queryVo.getTerminalId());
//        pageQuery.setStatus(queryVo.getStatus());
        pageQuery.setReqIds(reqIdsForFilter);
        pageQuery.setExcludeIds(taskIdsWithoutProcecsses);
        pageQuery.setCurrentPage(1);
        pageQuery.setPageSize(100);
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
        List<ReqTaskVO> taskVos = new ArrayList<>();
        for (EisReqTask task : tasks) {
            ReqTaskVO vo = new ReqTaskVO();
            EisRequirementInfo requirementInfo = reqMap.get(task.getRequirementId());
            EisReqPool reqPool = reqPoolMap.get(requirementInfo.getReqPoolId());
            vo.setId(task.getId());
            vo.setTerminalId(task.getTerminalId());
            vo.setTerminal(terminalMap.get(task.getTerminalId()));
            vo.setTerminalVersion(task.getTerminalVersion());
            vo.setReqPoolId(reqIdToPoolIdMap.get(task.getRequirementId()));
            if(reqPool != null){
                String dataOwnersString = reqPool.getDataOwners();
                if(!StringUtils.isEmpty(dataOwnersString)){
                    String ownerNames = "";
                    List<UserSimpleDTO> dataOwners = JsonUtils.parseList(reqPool.getDataOwners(),UserSimpleDTO.class);
                    if(dataOwners != null){
                        List<String> ownerNameList = dataOwners.stream().map(e -> e.getUserName()).collect(Collectors.toList());
                        ownerNames = String.join(",",ownerNameList);
                        vo.setDataOnwers(ownerNames);
                    }
                }
            }
            vo.setOwner(task.getOwnerName());
            vo.setVerifier(task.getVerifierName());
            vo.setReqIssueKey(requirementInfo.getReqIssueKey());
            vo.setReqName(requirementInfo.getReqName());
            vo.setTaskName(task.getTaskName());
            vo.setStatus(task.getStatus());
            vo.setSprint(task.getIteration());
            taskVos.add(vo);
        }
        return taskVos;
    }

    public List<EisTaskProcess> queryProcessByTerminalVersion(Long terminalId, Long terminalVersionId, Set<Long> taskIds){

        //??????????????????????????????????????????
        List<EisReqTask> tasks = reqTaskService.getByIds(taskIds);
        Set<Long> taskIdsOfUnRelease = tasks.stream().map(EisReqTask::getId).collect(Collectors.toSet());
        List<EisTaskProcess> processes =  taskProcessService.getBatchByTaskIds(taskIdsOfUnRelease);

        //????????????????????????????????????????????????
        Set<Long> reqPoolIds = new HashSet<>();
        for (EisTaskProcess process : processes) {
            reqPoolIds.add(process.getReqPoolId());
        }
        /**
         * ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
         */
//        if(reqPoolIds.size() > 1){
//            throw new CommonException("??????????????????????????????????????????");
//        }

//        //????????????????????????????????????????????????????????????
//        Long newTerminalReleaseId = terminalReleaseService.releaseAndUpdate(terminalId,terminalVersionId);
//        Long reqPoolId = reqPoolIds.iterator().next();
        Map<Long,Set<Long>> objRelations = new HashMap<>();
        //?????????????????????????????????
        Long baseReleaseId = 0L;
        EisTerminalReleaseHistory latestRelease = terminalReleaseService.getLatestRelease(terminalId);
        if(latestRelease != null){
            baseReleaseId = latestRelease.getId();
            objRelations = getObjRelationBase(baseReleaseId);
        }
        Map<Integer,List<EisTaskProcess>> processesGroupByType = processes.stream().collect(Collectors.groupingBy(EisTaskProcess::getReqPoolType));
        //??????spm????????????
        List<EisTaskProcess> spmDevProcesses = Optional.ofNullable(processesGroupByType.get(ReqPoolTypeEnum.SPM_DEV.getReqPoolType()))
                .orElse(new ArrayList<>());

        return spmDevProcesses;
//        if(!CollectionUtils.isEmpty(spmDevProcesses)){
//            objRelations = getObjRelationByDevSpmAndBase(objRelations, spmDevProcesses);
//        }

//        if(!MapUtils.isEmpty(objRelations)){
//            //?????????????????????
//            releaseForNewLineage(newTerminalReleaseId,terminalId,objRelations);
//            //???????????????tracker??????
//            releaseForTrackerOfLineage(baseReleaseId,newTerminalReleaseId,terminalId,reqPoolIds,spmDevProcesses);
//        }
    }

    public Map<Long,Set<Long>> queryObjRelationsFromProcess(List<EisTaskProcess> processes){
        Map<Long,Set<Long>> objToParentsOfProcess = new HashMap<>();
        for (EisTaskProcess process : processes) {
            String spmByObjId = process.getSpmByObjId();
            Long objId = process.getObjId();
            List<Long> spmByObjIdAsList = Lists.newArrayList(spmByObjId.split("\\|")).stream()
                    .map(e -> Long.valueOf(e)).collect(Collectors.toList());
            Set<Long> parentObjIds = objToParentsOfProcess.computeIfAbsent(objId,k->new HashSet<>());
            if(spmByObjIdAsList.size() > 1){
                parentObjIds.add(spmByObjIdAsList.get(1));
            }
        }
        return objToParentsOfProcess;

    }


    private Set<Long> getTaskIdsWithOutProcesses(){
        EisReqTask taskQuery = new EisReqTask();
        Long appId = 4L;
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

    private Map<Long,Set<Long>> getObjRelationBase(Long baseReleaseId){
        if(baseReleaseId.equals(0l)){
            return new HashMap<>();
        }
        LinageGraph linageGraph = lineageHelper.genReleasedLinageGraph(baseReleaseId);
        Map<Long, Set<Long>> objToParentsOfBase = linageGraph.getParentsMap();
        return objToParentsOfBase;
    }

//    private Map<Long,Set<Long>> getObjRelationByDevSpmAndBase(Map<Long, Set<Long>> objToParentsOfBase, List<EisTaskProcess> devProcesses){
//        //????????????????????????????????????????????????????????????????????????????????????????????????????????????
//        Map<Long,Set<Long>> objToParentsOfDevProcess = getObjIdToParentsMapByProcesses(devProcesses);
//        Map<Long,Set<Long>> objToParentsCombine = new HashMap<>();
//        for (Long objId : objToParentsOfDevProcess.keySet()) {
//            Set<Long> parentsOfCombine = objToParentsCombine.computeIfAbsent(objId,k->new HashSet<>());
//            if(objToParentsOfBase.containsKey(objId)){
//                //????????????????????????objId??????????????????????????????????????????????????????????????????
//                Set<Long> parentsOfProcess = objToParentsOfDevProcess.get(objId);
//                Set<Long> parentsOfBase = objToParentsOfBase.get(objId);
//                parentsOfCombine.addAll(parentsOfProcess);
//                parentsOfCombine.addAll(parentsOfBase);
//            }else {
//                //?????????????????????objId,????????????????????????????????????????????????????????????????????????
//                Set<Long> parentsOfProcess = objToParentsOfDevProcess.get(objId);
//                parentsOfCombine.addAll(parentsOfProcess);
//            }
//        }
//        //???????????????????????????????????????????????????????????????????????????
//        for (Long objId : objToParentsOfBase.keySet()) {
//            Set<Long> parentsOfCombine = objToParentsCombine.computeIfAbsent(objId,k->new HashSet<>());
//            if(!objToParentsOfDevProcess.keySet().contains(objId)){
//                parentsOfCombine.addAll(objToParentsOfBase.get(objId));
//            }
//        }
//        return objToParentsCombine;
//    }
//
//    private Map<Long,Set<Long>> getObjRelationByDeleteSpmAndBase(Map<Long,Set<Long>> objToParentsMap,List<EisTaskProcess> spmDeleteProcesses){
//        Map<Long,Set<Long>> objToParentsOfSpmDeleteProcess = getObjIdToParentsMapByProcesses(spmDeleteProcesses);
//        Map<Long,Set<Long>> objToParentsCombine = new HashMap<>();
//        for (Long objId : objToParentsMap.keySet()) {
//            Set<Long> baseParents = objToParentsMap.get(objId);
//            Set<Long> toBeDeleteParents = objToParentsOfSpmDeleteProcess.get(objId);
//            Set<Long> parentsOfCombine = objToParentsCombine.computeIfAbsent(objId,k->new HashSet<>());
//            if(CollectionUtils.isEmpty(baseParents)){
//                continue;
//            }
//            if(!CollectionUtils.isEmpty(toBeDeleteParents)){
//                Set<Long> newParents = new HashSet<>(Sets.difference(baseParents,toBeDeleteParents));
//                parentsOfCombine.addAll(newParents);
//            }else {
//                parentsOfCombine.addAll(baseParents);
//            }
//        }
//        return objToParentsCombine;
//    }
//
//    private Map<Long,Set<Long>> getObjIdToParentsMapByProcesses(List<EisTaskProcess> processes){
//        Map<Long,Set<Long>> objToParentsOfProcess = new HashMap<>();
//        for (EisTaskProcess process : processes) {
//            String spmByObjId = process.getSpmByObjId();
//            Long objId = process.getObjId();
//            List<Long> spmByObjIdAsList = Lists.newArrayList(spmByObjId.split("\\|")).stream()
//                    .map(e -> Long.valueOf(e)).collect(Collectors.toList());
//            Set<Long> parentObjIds = objToParentsOfProcess.computeIfAbsent(objId,k->new HashSet<>());
//            if(spmByObjIdAsList.size() > 1){
//                parentObjIds.add(spmByObjIdAsList.get(1));
//            }
//        }
//        return objToParentsOfProcess;
//    }

}
