package com.netease.hz.bdms.easyinsight.service.helper;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.netease.eis.adapters.RealtimeConfigAdapter;
import com.netease.hz.bdms.easyinsight.common.aop.MethodLog;
import com.netease.hz.bdms.easyinsight.common.bo.lineage.LinageGraph;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.app.AppSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.terminal.TerminalSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.enums.ObjSpecialTypeEnum;
import com.netease.hz.bdms.easyinsight.common.enums.ProcessStatusEnum;
import com.netease.hz.bdms.easyinsight.common.enums.ReqPoolTypeEnum;
import com.netease.hz.bdms.easyinsight.common.enums.ReqTaskStatusEnum;
import com.netease.hz.bdms.easyinsight.common.exception.CommonException;
import com.netease.hz.bdms.easyinsight.common.util.CommonUtil;
import com.netease.hz.bdms.easyinsight.common.util.JsonUtils;
import com.netease.hz.bdms.easyinsight.dao.model.*;
import com.netease.hz.bdms.easyinsight.service.service.AppService;
import com.netease.hz.bdms.easyinsight.service.service.ObjectBasicService;
import com.netease.hz.bdms.easyinsight.service.service.TerminalService;
import com.netease.hz.bdms.easyinsight.service.service.TerminalVersionInfoService;
import com.netease.hz.bdms.easyinsight.service.service.asynchandle.AsyncHandleService;
import com.netease.hz.bdms.easyinsight.service.service.impl.AppRelationService;
import com.netease.hz.bdms.easyinsight.service.service.obj.AllTrackerReleaseService;
import com.netease.hz.bdms.easyinsight.service.service.obj.EventBuryPointService;
import com.netease.hz.bdms.easyinsight.service.service.obj.ObjRelationReleaseService;
import com.netease.hz.bdms.easyinsight.service.service.obj.ObjTerminalTrackerService;
import com.netease.hz.bdms.easyinsight.service.service.requirement.ReqEventPoolService;
import com.netease.hz.bdms.easyinsight.service.service.requirement.ReqPoolRelBaseService;
import com.netease.hz.bdms.easyinsight.service.service.requirement.ReqTaskService;
import com.netease.hz.bdms.easyinsight.service.service.requirement.TaskProcessService;
import com.netease.hz.bdms.easyinsight.service.service.terminalrelease.TerminalReleaseService;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReleaseHelper {

    @Resource
    private RealtimeConfigAdapter realtimeConfigAdapter;

    @Resource
    private ReqTaskService reqTaskService;

    @Resource
    private ObjectBasicService objectBasicService;

    @Resource
    private TerminalReleaseService terminalReleaseService;

    @Resource
    private ReqEventPoolService reqEventPoolService;

    @Resource
    private EventBuryPointService eventBuryPointService;

    @Resource
    private ObjRelationReleaseService objRelationReleaseService;

    @Resource
    private ObjTerminalTrackerService objTerminalTrackerService;

    @Resource
    private AllTrackerReleaseService allTrackerReleaseService;

    @Resource
    private ReqTaskHelper taskHelper;

    @Resource
    private TerminalVersionInfoService terminalVersionInfoService;

    @Resource
    private TaskProcessService taskProcessService;

    @Resource
    private LineageHelper lineageHelper;

    @Resource
    private AppRelationService appRelationService;

    @Resource
    private MergeConflictHelper mergeConflictHelper;

    @Resource
    private ReqPoolRelBaseService reqPoolRelBaseService;

    @Resource
    private AppService appService;

    @Resource
    private TerminalService terminalService;

    @Resource
    private AsyncHandleService asyncHandleService;

    @MethodLog
    @Transactional(rollbackFor = Throwable.class)
    public void releaseMain(Set<Long> taskIds, Long terminalId, Long terminalVersionId){

        List<EisReqTask> tasks = reqTaskService.getByIds(taskIds);
        tasks = tasks.stream().filter(e -> e.getTerminalReleaseId() == null).collect(Collectors.toList());

        Set<Long> unReleasedTaskIds = tasks.stream().map(e -> e.getId()).collect(Collectors.toSet());
        if(CollectionUtils.isEmpty(unReleasedTaskIds)){
            throw new CommonException("??????????????????????????????");
        }

        Set<Long> taskIdsOfUnRelease = tasks.stream().map(e -> e.getId()).collect(Collectors.toSet());
        List<EisTaskProcess> processes =  taskProcessService.getBatchByTaskIds(taskIdsOfUnRelease);

        // ?????????????????????
        //????????????????????????????????????????????????
        Set<Long> reqPoolIds = new HashSet<>();
        for (EisTaskProcess process : processes) {
            reqPoolIds.add(process.getReqPoolId());
        }

        Set<Long> conflictReqPoolIds = mergeConflictHelper.filterConflictReqPoolIds(reqPoolIds);
        if (CollectionUtils.isNotEmpty(conflictReqPoolIds)) {
            throw new CommonException("???????????????????????????????????????????????????" + JsonUtils.toJson(conflictReqPoolIds));
        }

        EisTerminalReleaseHistory latestRelease = terminalReleaseService.getLatestRelease(terminalId);
        if (latestRelease != null) {
            // ???????????????????????????
            List<EisReqPoolRelBaseRelease> eisReqPoolRelBaseReleases = reqPoolRelBaseService.batchGetCurrentUse(reqPoolIds);
            Set<Long> needMergeReqPooIds = eisReqPoolRelBaseReleases.stream()
                    .filter(o -> o.getTerminalId().equals(terminalId))
                    .filter(o -> !latestRelease.getId().equals(o.getBaseReleaseId()))
                    .map(EisReqPoolRelBaseRelease::getReqPoolId).collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(needMergeReqPooIds)) {
                throw new CommonException("?????????????????????????????????????????????????????????????????????????????????????????????" + JsonUtils.toJson(needMergeReqPooIds));
            }
        }

        Map<Long,List<EisTaskProcess>> processesGroupByTaskId = processes.stream().collect(Collectors.groupingBy(EisTaskProcess::getTaskId));

        //??????????????????????????????????????????
        for (Long taskId : taskIdsOfUnRelease) {
            if(!processesGroupByTaskId.containsKey(taskId)){
                throw new CommonException("??????" + taskId + "?????????????????????????????????");
            }
        }
        //????????????????????????????????????
        for (EisTaskProcess process : processes) {
            if(!process.getStatus().equals(ProcessStatusEnum.TEST_FINISHED.getState())){
                // TODO???????????????
                throw new CommonException("id???" + process.getId() + "?????????????????????????????????????????????");
            }
        }

        /**
         * ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
         */
//        if(reqPoolIds.size() > 1){
//            throw new CommonException("??????????????????????????????????????????");
//        }

        //????????????????????????????????????????????????????????????
        Long newTerminalReleaseId = terminalReleaseService.releaseAndUpdate(EtContext.get(ContextConstant.APP_ID), terminalId,terminalVersionId);

        Map<Long,Set<Long>> originRelations = new HashMap<>();
        //?????????????????????????????????
        Long baseReleaseId = 0L;
        LinageGraph oldLinageGraph = null;
        if(latestRelease != null){
            baseReleaseId = latestRelease.getId();
            oldLinageGraph = getObjRelationBaseNoBridgeUp(baseReleaseId);
            originRelations = oldLinageGraph.getParentsMap();
        }
        Map<Long,Set<Long>> objRelations = originRelations;
        Map<Integer,List<EisTaskProcess>> processesGroupByType = processes.stream().collect(Collectors.groupingBy(EisTaskProcess::getReqPoolType));
        //??????spm????????????
        List<EisTaskProcess> spmDevProcesses = Optional.ofNullable(processesGroupByType.get(ReqPoolTypeEnum.SPM_DEV.getReqPoolType()))
                .orElse(new ArrayList<>());
        if(!CollectionUtils.isEmpty(spmDevProcesses)){
            objRelations = getObjRelationByDevSpmAndBase(objRelations, spmDevProcesses);
            // ?????????????????????????????????
            checkParentsExist(oldLinageGraph, spmDevProcesses);
        }
        //??????spm????????????
        List<EisTaskProcess> spmDeleteProcesses = Optional.ofNullable(processesGroupByType.get(ReqPoolTypeEnum.SPM_DELETE.getReqPoolType()))
                .orElse(new ArrayList<>());
        if(!CollectionUtils.isEmpty(spmDeleteProcesses)){
            objRelations = getObjRelationByDeleteSpmAndBase(objRelations,spmDeleteProcesses);
        }
        if(!MapUtils.isEmpty(objRelations)){
            //?????????????????????
            releaseForNewLineage(EtContext.get(ContextConstant.APP_ID), newTerminalReleaseId,terminalId,objRelations);
            //???????????????tracker??????
            List<EisTaskProcess> processesToReleaseTracker = new ArrayList<>(spmDevProcesses);
            if (CollectionUtils.isNotEmpty(spmDeleteProcesses)) {
                processesToReleaseTracker.addAll(spmDeleteProcesses);
            }
            releaseForTrackerOfLineage(baseReleaseId,newTerminalReleaseId,terminalId,reqPoolIds,processesToReleaseTracker);
        }
        //????????????????????????
        List<EisTaskProcess> eventProcesses = Optional.ofNullable(processesGroupByType.get(ReqPoolTypeEnum.EVENT.getReqPoolType()))
                .orElse(new ArrayList<>());
        //??????????????????
        releaseForEventProcesses(newTerminalReleaseId,eventProcesses);
        //??????????????????????????????
        updateStatus(newTerminalReleaseId,tasks,processes);
        // ?????????????????????
        if (CollectionUtils.isNotEmpty(tasks)) {
            tasks.forEach(t -> asyncHandleService.onTaskAndProcessUpdate(t.getId()));
        }

        // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        releaseChildren4BridgeChange(newTerminalReleaseId, terminalId, spmDevProcesses, spmDeleteProcesses);

        // ??????????????????????????????????????????????????????????????????????????????
        if (latestRelease != null) { // ????????????latestRelease??????????????????
            List<Long> parentReleaseIds = appRelationService.getParentReleaseIds(latestRelease.getId());
            if (CollectionUtils.isNotEmpty(parentReleaseIds)) {
                parentReleaseIds.forEach(parentReleaseId -> {
                    appRelationService.addReleaseRelation(newTerminalReleaseId, parentReleaseId);
                });
            }
        }

        asyncHandleService.onVersionReleaseSuccess(terminalId, terminalVersionId);
    }

    /**
     * ??????????????????????????????????????????????????????
     */
    private void releaseChildren4BridgeChange(Long releaseId, Long terminalId, List<EisTaskProcess> spmDevProcesses, List<EisTaskProcess> spmDeleteProcesses) {
        // ??????????????????????????????????????????????????????????????????????????????
        Long appId = EtContext.get(ContextConstant.APP_ID);
        AppSimpleDTO appSimpleDTO = appService.getAppById(appId);
        if (appSimpleDTO == null) {
            throw new CommonException("appId????????????" + appId);
        }

        TerminalSimpleDTO terminalSimpleDTO = terminalService.getById(terminalId);
        if (terminalSimpleDTO == null) {
            throw new CommonException("terminalId????????????" + terminalId);
        }

        Set<Long> allChangedObjIds = new HashSet<>();
        spmDevProcesses.forEach(o -> {
            String spmByObjId = o.getSpmByObjId();
            List<Long> spmByObjIdList = Arrays.stream(spmByObjId.split("\\|")).map(e -> Long.valueOf(e)).collect(Collectors.toList());
            allChangedObjIds.addAll(spmByObjIdList);
        });
        spmDeleteProcesses.forEach(o -> {
            String spmByObjId = o.getSpmByObjId();
            List<Long> spmByObjIdList = Arrays.stream(spmByObjId.split("\\|")).map(e -> Long.valueOf(e)).collect(Collectors.toList());
            allChangedObjIds.addAll(spmByObjIdList);
        });
        if (CollectionUtils.isEmpty(allChangedObjIds)) {
            return;
        }
        List<ObjectBasic> allChangedObjs = objectBasicService.getByIds(allChangedObjIds);
        if (CollectionUtils.isEmpty(allChangedObjs)) {
            return;
        }
        Map<Long, Set<Long>> subAppAndTerminalsToRelease = new HashMap<>();
        for (ObjectBasic o : allChangedObjs) {
            if (!ObjSpecialTypeEnum.BRIDGE.getName().equals(o.getSpecialType())) {
                continue;
            }
            Pair<Long, Long> p = appRelationService.getBridgeChildrenTerminalId(terminalId, o);
            if (p != null) {
                // ?????????????????????2???
                Long subTerminalId = p.getValue();
                if (subTerminalId.equals(terminalId)) {
                    continue;
                }
                subAppAndTerminalsToRelease.computeIfAbsent(p.getKey(), k -> new HashSet<>()).add(p.getValue());
            }
        }
        // ?????????????????????
        subAppAndTerminalsToRelease.forEach((subAppId, subTerminalIds) -> {
            subTerminalIds.forEach(subTerminalId -> {
                String versionName = "????????????????????????" + appSimpleDTO.getName() + "-" + terminalSimpleDTO.getName() + "???" + releaseId + "-" + subTerminalId;
                Long subReleaseId = emptyRelease(versionName, subAppId, subTerminalId);
                // ???????????????????????????releaseId??????
                Set<Long> parentReleaseIds = new HashSet<>(appRelationService.getParentReleaseIds(subReleaseId));
                // ??????????????????????????????
                Set<Long> updatedParentReleaseIds = new HashSet<>(parentReleaseIds);
                updatedParentReleaseIds.add(releaseId);    // ??????????????????releaseId?????????releaseId
                updatedParentReleaseIds = terminalReleaseService.getByIds(updatedParentReleaseIds).stream()
                        .collect(Collectors.toMap(EisTerminalReleaseHistory::getTerminalId, o -> o, (o1, o2) -> o1.getCreateTime().after(o2.getCreateTime()) ? o1 : o2))    // ??????????????????????????????
                        .values().stream().map(EisTerminalReleaseHistory::getId).collect(Collectors.toSet());
                // ???????????????DB???????????????
                Sets.SetView<Long> toRemoveSet = Sets.difference(parentReleaseIds, updatedParentReleaseIds);
                if (CollectionUtils.isNotEmpty(toRemoveSet)) {
                    toRemoveSet.forEach(toRemove -> {
                        appRelationService.removeReleaseRelation(subReleaseId, toRemove);
                    });
                }
                Sets.SetView<Long> toAddSet = Sets.difference(updatedParentReleaseIds, parentReleaseIds);
                if (CollectionUtils.isNotEmpty(toAddSet)) {
                    toAddSet.forEach(toAdd -> {
                        appRelationService.addReleaseRelation(subReleaseId, toAdd);
                    });
                }
            });
        });
    }

    /**
     * ?????????????????????????????????????????????????????????????????????????????????
     * @return ?????????releaseId
     */
    private Long emptyRelease(String versionName, Long appId, Long terminalId) {
        EisTerminalVersionInfo newEisTerminalVersionInfo = new EisTerminalVersionInfo();
        newEisTerminalVersionInfo.setName(versionName);
        newEisTerminalVersionInfo.setNum("");
        newEisTerminalVersionInfo.setAppId(appId);
        newEisTerminalVersionInfo.setCreateEmail("SYSTEM");
        newEisTerminalVersionInfo.setCreateName("SYSTEM");
        newEisTerminalVersionInfo.setUpdateEmail("SYSTEM");
        newEisTerminalVersionInfo.setUpdateName("SYSTEM");
        newEisTerminalVersionInfo.setCreateTime(new Date());
        Long terminalVersionId = terminalVersionInfoService.create(newEisTerminalVersionInfo);
        EisTerminalReleaseHistory latestRelease = terminalReleaseService.getLatestRelease(terminalId);
        Long newTerminalReleaseId = terminalReleaseService.releaseAndUpdate(appId, terminalId, terminalVersionId);
        Map<Long,Set<Long>> originRelations = new HashMap<>();
        //?????????????????????????????????
        Long baseReleaseId = 0L;
        LinageGraph oldLinageGraph = null;
        if(latestRelease != null){
            baseReleaseId = latestRelease.getId();
            oldLinageGraph = getObjRelationBaseNoBridgeUp(baseReleaseId);
            originRelations = oldLinageGraph.getParentsMap();
        }
        Map<Long, Set<Long>> objRelations = originRelations;
        // ??????????????????
        if (!MapUtils.isEmpty(objRelations)) {
            releaseForNewLineage(appId, newTerminalReleaseId, terminalId, objRelations);
        }
        // ??????tracker
        copyTrackerToNewReleaseId(baseReleaseId, newTerminalReleaseId, terminalId);

        // ??????????????????????????????????????????????????????????????????????????????
        if (latestRelease != null) { // ????????????latestRelease??????????????????
            List<Long> parentReleaseIds = appRelationService.getParentReleaseIds(latestRelease.getId());
            if (CollectionUtils.isNotEmpty(parentReleaseIds)) {
                parentReleaseIds.forEach(parentReleaseId -> {
                    appRelationService.addReleaseRelation(newTerminalReleaseId, parentReleaseId);
                });
            }
        }
        return newTerminalReleaseId;
    }

    /**
     * ???????????????SPM????????????????????????????????????
     */
    private void checkParentsExist(LinageGraph oldLinageGraph, List<EisTaskProcess> spmDevProcesses) {
        if (CollectionUtils.isEmpty(spmDevProcesses)) {
            return;
        }
        // ???????????????oid
        Set<Long> originExistingOids = oldLinageGraph == null ? new HashSet<>() : oldLinageGraph.getAllObjIds();

        Set<Long> oidsToAdd = new HashSet<>();  // ???????????????
        Set<Long> parentOidsToRelate = new HashSet<>(); // ??????????????????
        List<String> spmsToDev = spmDevProcesses.stream().map(EisTaskProcess::getSpmByObjId).collect(Collectors.toList());
        spmsToDev.forEach(spmByObjId -> {
            List<Long> oidsOfSpm = CommonUtil.transSpmToObjIdList(spmByObjId);
            if (CollectionUtils.isEmpty(oidsOfSpm)) {
                return;
            }
            oidsToAdd.add(oidsOfSpm.get(0));
            oidsOfSpm.remove(0);
            if (CollectionUtils.isNotEmpty(oidsOfSpm)) {
                parentOidsToRelate.addAll(oidsOfSpm);
            }
        });

        // ??????????????????????????????
        Set<Long> notExistParents = new HashSet<>();
        parentOidsToRelate.forEach(parentOid -> {
            // ?????????????????????????????????
            if (originExistingOids.contains(parentOid)) {
                return;
            }
            // ??????????????????????????????
            if (oidsToAdd.contains(parentOid)) {
                return;
            }
            notExistParents.add(parentOid);
        });

        if (CollectionUtils.isNotEmpty(notExistParents)) {
            // ????????????????????????
            List<ObjectBasic> notExistObjects = objectBasicService.getByIds(notExistParents);
            List<List<Long>> spmsOfObjIdList = new ArrayList<>();
            spmDevProcesses.forEach(o -> {
                String spmByObjId = o.getSpmByObjId();
                List<Long> spmByObjIdList = Arrays.stream(spmByObjId.split("\\|")).map(e -> Long.valueOf(e)).collect(Collectors.toList());
                spmsOfObjIdList.add(spmByObjIdList);
            });
            Set<Long> allObjIds = spmsOfObjIdList.stream().flatMap(l -> l.stream()).collect(Collectors.toSet());
            List<ObjectBasic> allObjBasics = objectBasicService.getByIds(allObjIds);
            Set<Long> outerSpaceObjIds = AppRelationService.getOuterSpaceObjIds(spmsOfObjIdList, allObjBasics.stream().collect(Collectors.toMap(ObjectBasic::getId, o -> o, (o1, o2) -> o1)));
            // ???????????????
            if (CollectionUtils.isNotEmpty(outerSpaceObjIds)) {
                notExistParents.removeIf(outerSpaceObjIds::contains);
                notExistObjects.removeIf(o -> outerSpaceObjIds.contains(o.getId()));
            }
            if (CollectionUtils.isNotEmpty(notExistParents)) {
                throw new CommonException("????????????????????????????????????????????????" + JsonUtils.toJson(notExistObjects.stream().map(ObjectBasic::getOid).collect(Collectors.toList())));
            }
        }
    }

    /**
     * ?????????
     * @param newTerminalReleaseId
     * @param terminalId
     * @param objToParentsCombine
     */
    @Transactional(rollbackFor = Throwable.class)
    public void releaseForNewLineage(
            Long appId, Long newTerminalReleaseId, Long terminalId, Map<Long,Set<Long>> objToParentsCombine){
        List<EisObjAllRelationRelease> newReleaseRelations = new ArrayList<>();
        for (Long objId : objToParentsCombine.keySet()) {
            if(!CollectionUtils.isEmpty(objToParentsCombine.get(objId))){
                Set<Long> parentObjIds = objToParentsCombine.get(objId);
                for (Long parentObjId : parentObjIds) {
                    EisObjAllRelationRelease relation = new EisObjAllRelationRelease();
                    relation.setObjId(objId);
                    relation.setParentObjId(parentObjId);
                    relation.setTerminalId(terminalId);
                    relation.setTerminalReleaseId(newTerminalReleaseId);
                    relation.setAppId(appId);
                    newReleaseRelations.add(relation);
                }
            }else {
                EisObjAllRelationRelease relation = new EisObjAllRelationRelease();
                relation.setObjId(objId);
                relation.setTerminalId(terminalId);
                relation.setTerminalReleaseId(newTerminalReleaseId);
                relation.setAppId(appId);
                newReleaseRelations.add(relation);
            }
        }
        objRelationReleaseService.insertBatch(newReleaseRelations);
    }

    /**
     * ??????????????????tracker
     */
    @Transactional(rollbackFor = Throwable.class)
    public void copyTrackerToNewReleaseId(Long baseReleaseId, Long newReleaseId, Long terminalId) {
        Long appId = EtContext.get(ContextConstant.APP_ID);
        EisAllTrackerRelease query = new EisAllTrackerRelease();
        query.setTerminalReleaseId(baseReleaseId);
        List<EisAllTrackerRelease> trackerOfBase = allTrackerReleaseService.search(query);
        if (CollectionUtils.isEmpty(trackerOfBase)) {
            return;
        }
        Map<Long, Long> objIdToTrackerIdOfBase = new HashMap<>();
        for (EisAllTrackerRelease eisAllTrackerRelease : trackerOfBase) {
            objIdToTrackerIdOfBase.put(eisAllTrackerRelease.getObjId(), eisAllTrackerRelease.getTrackerId());
        }
        List<EisAllTrackerRelease> insertList = new ArrayList<>();
        for (Long objId : objIdToTrackerIdOfBase.keySet()) {
            EisAllTrackerRelease trackerRelease = new EisAllTrackerRelease();
            trackerRelease.setTerminalId(terminalId);
            trackerRelease.setTerminalReleaseId(newReleaseId);
            trackerRelease.setObjId(objId);
            trackerRelease.setTrackerId(objIdToTrackerIdOfBase.get(objId));
            trackerRelease.setAppId(appId);
            insertList.add(trackerRelease);
        }
        allTrackerReleaseService.insertBatch(insertList);
    }

    /**
     * ???tracker???????????????????????????eis_all_tracker_release
     * @param baseReleaseId
     * @param newReleaseId
     * @param terminalId
     * @param reqPoolIds
     * @param devProcesses
     */
    @Transactional(rollbackFor = Throwable.class)
    public void releaseForTrackerOfLineage(Long baseReleaseId,Long newReleaseId, Long terminalId,Set<Long> reqPoolIds,List<EisTaskProcess> devProcesses){
        Long appId = EtContext.get(ContextConstant.APP_ID);
        EisAllTrackerRelease query = new EisAllTrackerRelease();
        query.setTerminalReleaseId(baseReleaseId);
        List<EisAllTrackerRelease> trackerOfBase = allTrackerReleaseService.search(query);
        Map<Long,Long> objIdToTrackerIdOfBase = new HashMap<>();
        for (EisAllTrackerRelease eisAllTrackerRelease : trackerOfBase) {
            objIdToTrackerIdOfBase.put(eisAllTrackerRelease.getObjId(),eisAllTrackerRelease.getTrackerId());
        }
        Set<Long> objIdsOfProcesses = devProcesses.stream().map(e -> e.getObjId()).collect(Collectors.toSet());
        List<EisObjTerminalTracker> allTrackers = new ArrayList<>();
        //objIdSet??????????????????????????????????????????????????????objId??????????????????????????????
        Map<Long,List<Long>> objIdCountMap = new HashMap<>();
        for (Long reqPoolId : reqPoolIds) {
            EisObjTerminalTracker terminalTrackerQuery = new EisObjTerminalTracker();
            terminalTrackerQuery.setReqPoolId(reqPoolId);
            terminalTrackerQuery.setTerminalId(terminalId);
            List<EisObjTerminalTracker> trackers = objTerminalTrackerService.search(terminalTrackerQuery);
            Set<Long> objIds = trackers.stream().map(e -> e.getObjId()).collect(Collectors.toSet());
            for (Long objId : objIds) {
                List<Long> reqPoolIdSetOfObjId = objIdCountMap.computeIfAbsent(objId, k -> new ArrayList<>());
                reqPoolIdSetOfObjId.add(reqPoolId);
            }
            trackers = trackers.stream().filter(e -> objIdsOfProcesses.contains(e.getObjId())).collect(Collectors.toList());
            allTrackers.addAll(trackers);
        }
        ObjectBasic objSearch = new ObjectBasic();
        objSearch.setAppId(appId);
        List<ObjectBasic> objs = objectBasicService.search(objSearch);
        Map<Long,String> objIdToOidMap = new HashMap<>();
        for (ObjectBasic obj : objs) {
            objIdToOidMap.put(obj.getId(),obj.getOid());
        }
        for (Long objId : objIdCountMap.keySet()) {
            List<Long> reqPoolIdSetOfObjId = objIdCountMap.get(objId);
            if(reqPoolIdSetOfObjId != null && reqPoolIdSetOfObjId.size() > 1){
                String oid = objIdToOidMap.get(objId);
                throw new CommonException("oid???" + oid + "?????????????????????" + JsonUtils.toJson(reqPoolIdSetOfObjId) + "?????????????????????????????????????????????????????????");
            }
        }

        Map<Long,Long> objIdToTrackerIdOfProcesses = new HashMap<>();
        for (EisObjTerminalTracker tracker : allTrackers) {
            objIdToTrackerIdOfProcesses.put(tracker.getObjId(),tracker.getId());
        }

        Map<Long,Long> objIdToTrackerIdCombine = new HashMap<>();
        objIdToTrackerIdCombine.putAll(objIdToTrackerIdOfBase);
        objIdToTrackerIdCombine.putAll(objIdToTrackerIdOfProcesses);
        List<EisAllTrackerRelease> insertList = new ArrayList<>();
        for (Long objId : objIdToTrackerIdCombine.keySet()) {
            EisAllTrackerRelease trackerRelease = new EisAllTrackerRelease();
            trackerRelease.setTerminalId(terminalId);
            trackerRelease.setTerminalReleaseId(newReleaseId);
            trackerRelease.setObjId(objId);
            trackerRelease.setTrackerId(objIdToTrackerIdCombine.get(objId));
            trackerRelease.setAppId(appId);
            insertList.add(trackerRelease);
        }
        allTrackerReleaseService.insertBatch(insertList);
        for (EisObjTerminalTracker tracker : allTrackers) {
            tracker.setTerminalReleaseId(newReleaseId);
        }
        objTerminalTrackerService.updateBatch(allTrackers);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void updateStatus(Long newTerminalReleaseId, List<EisReqTask> tasks, List<EisTaskProcess> processes){
        for (EisReqTask task : tasks) {
            task.setStatus(ReqTaskStatusEnum.ONLINE.getState());
            task.setTerminalReleaseId(newTerminalReleaseId);
        }
        for (EisTaskProcess process : processes) {
            process.setStatus(ReqTaskStatusEnum.ONLINE.getState());
        }
        if(!CollectionUtils.isEmpty(tasks)){
            reqTaskService.updateBatch(tasks);
        }
        if(!CollectionUtils.isEmpty(processes)){
            taskProcessService.updateBatch(processes);
        }
    }

    private void releaseForEventProcesses(Long newReleaseId,List<EisTaskProcess> eventProcesses){
        Set<Long> eventReqEntityIds = new HashSet<>();
        for (EisTaskProcess eventProcess : eventProcesses) {
            eventReqEntityIds.add(eventProcess.getReqPoolEntityId());
        }
        List<EisReqPoolEvent> reqPoolEvents = reqEventPoolService.getBatchByIds(eventReqEntityIds);
        Set<Long> eventBuryPointIds = reqPoolEvents.stream().map(e -> e.getEventBuryPointId()).collect(Collectors.toSet());
        List<EisEventBuryPoint> eisEventBuryPoints = eventBuryPointService.getByIds(eventBuryPointIds);
        for (EisEventBuryPoint eisEventBuryPoint : eisEventBuryPoints) {
            eisEventBuryPoint.setTerminalReleaseId(newReleaseId);
        }
        for (EisEventBuryPoint eisEventBuryPoint : eisEventBuryPoints) {
            eventBuryPointService.update(eisEventBuryPoint);
        }

    }

    /**
     * ????????????????????????????????????????????????
     */
    private LinageGraph getObjRelationBaseNoBridgeUp(Long baseReleaseId){
        if(baseReleaseId.equals(0L)){
            LinageGraph result = new LinageGraph();
            result.setAllObjIds(Sets.newHashSet());
            result.setParentsMap(Maps.newHashMap());
            result.setChildrenMap(Maps.newHashMap());
            return result;
        }
        return lineageHelper.genReleasedLinageGraphNoBridge(baseReleaseId);
    }

    private Map<Long,Set<Long>> getObjRelationByDevSpmAndBase(Map<Long, Set<Long>> objToParentsOfBase, List<EisTaskProcess> devProcesses){
        //????????????????????????????????????????????????????????????????????????????????????????????????????????????
        Map<Long,Set<Long>> objToParentsOfDevProcess = getObjIdToParentsMapByProcesses(devProcesses);
        Map<Long,Set<Long>> objToParentsCombine = new HashMap<>();
        for (Long objId : objToParentsOfDevProcess.keySet()) {
            Set<Long> parentsOfCombine = objToParentsCombine.computeIfAbsent(objId,k->new HashSet<>());
            if(objToParentsOfBase.containsKey(objId)){
                //????????????????????????objId??????????????????????????????????????????????????????????????????
                Set<Long> parentsOfProcess = objToParentsOfDevProcess.get(objId);
                Set<Long> parentsOfBase = objToParentsOfBase.get(objId);
                parentsOfCombine.addAll(parentsOfProcess);
                parentsOfCombine.addAll(parentsOfBase);
            }else {
                //?????????????????????objId,????????????????????????????????????????????????????????????????????????
                Set<Long> parentsOfProcess = objToParentsOfDevProcess.get(objId);
                parentsOfCombine.addAll(parentsOfProcess);
            }
        }
        //???????????????????????????????????????????????????????????????????????????
        for (Long objId : objToParentsOfBase.keySet()) {
            Set<Long> parentsOfCombine = objToParentsCombine.computeIfAbsent(objId,k->new HashSet<>());
            if(!objToParentsOfDevProcess.containsKey(objId)){
                parentsOfCombine.addAll(objToParentsOfBase.get(objId));
            }
        }
        return objToParentsCombine;
    }

    public Map<Long,Set<Long>> getObjRelationByDeleteSpmAndBase(Map<Long,Set<Long>> objToParentsMap,List<EisTaskProcess> spmDeleteProcesses){
        Map<Long,Set<Long>> objToParentsOfSpmDeleteProcess = getObjIdToParentsMapByProcesses(spmDeleteProcesses);
        Map<Long,Set<Long>> objToParentsCombine = new HashMap<>();
        for (Long objId : objToParentsMap.keySet()) {
            Set<Long> baseParents = objToParentsMap.get(objId);
            Set<Long> toBeDeleteParents = objToParentsOfSpmDeleteProcess.get(objId);
            Set<Long> parentsOfCombine = objToParentsCombine.computeIfAbsent(objId,k->new HashSet<>());
            if(CollectionUtils.isEmpty(baseParents)){
                continue;
            }
            if(!CollectionUtils.isEmpty(toBeDeleteParents)){
                Set<Long> newParents = new HashSet<>(Sets.difference(baseParents,toBeDeleteParents));
                parentsOfCombine.addAll(newParents);
            }else {
                parentsOfCombine.addAll(baseParents);
            }
        }
        return objToParentsCombine;
    }

    private Map<Long,Set<Long>> getObjIdToParentsMapByProcesses(List<EisTaskProcess> processes){
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
}
