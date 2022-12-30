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
            throw new CommonException("没有相关任务需要发布");
        }

        Set<Long> taskIdsOfUnRelease = tasks.stream().map(e -> e.getId()).collect(Collectors.toSet());
        List<EisTaskProcess> processes =  taskProcessService.getBatchByTaskIds(taskIdsOfUnRelease);

        // 检查是否有冲突
        //校验任务流程是否属于同一个需求组
        Set<Long> reqPoolIds = new HashSet<>();
        for (EisTaskProcess process : processes) {
            reqPoolIds.add(process.getReqPoolId());
        }

        Set<Long> conflictReqPoolIds = mergeConflictHelper.filterConflictReqPoolIds(reqPoolIds);
        if (CollectionUtils.isNotEmpty(conflictReqPoolIds)) {
            throw new CommonException("以下需求池基线合并冲突未处理完毕：" + JsonUtils.toJson(conflictReqPoolIds));
        }

        EisTerminalReleaseHistory latestRelease = terminalReleaseService.getLatestRelease(terminalId);
        if (latestRelease != null) {
            // 检查是否为最新基线
            List<EisReqPoolRelBaseRelease> eisReqPoolRelBaseReleases = reqPoolRelBaseService.batchGetCurrentUse(reqPoolIds);
            Set<Long> needMergeReqPooIds = eisReqPoolRelBaseReleases.stream()
                    .filter(o -> o.getTerminalId().equals(terminalId))
                    .filter(o -> !latestRelease.getId().equals(o.getBaseReleaseId()))
                    .map(EisReqPoolRelBaseRelease::getReqPoolId).collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(needMergeReqPooIds)) {
                throw new CommonException("以下需求池基线需要先变基到最新，以合并最新基线内相关对象改动：" + JsonUtils.toJson(needMergeReqPooIds));
            }
        }

        Map<Long,List<EisTaskProcess>> processesGroupByTaskId = processes.stream().collect(Collectors.groupingBy(EisTaskProcess::getTaskId));

        //校验是否有任务未绑定任何流程
        for (Long taskId : taskIdsOfUnRelease) {
            if(!processesGroupByTaskId.containsKey(taskId)){
                throw new CommonException("任务" + taskId + "没有被指派任何待办流程");
            }
        }
        //校验流程是否都已测试完成
        for (EisTaskProcess process : processes) {
            if(!process.getStatus().equals(ProcessStatusEnum.TEST_FINISHED.getState())){
                // TODO：提示优化
                throw new CommonException("id为" + process.getId() + "的待办流程未测试通过，无法上线");
            }
        }

        /**
         * 这里要注意一下，之前是不允许发布时跨需求组发布的，但是现在由于业务要求放开了，发布时同一个端版本可能会关联多个需求组
         */
//        if(reqPoolIds.size() > 1){
//            throw new CommonException("待发布任务不在同一个需求组下");
//        }

        //插入最新发布记录，并更新上一次发布的记录
        Long newTerminalReleaseId = terminalReleaseService.releaseAndUpdate(EtContext.get(ContextConstant.APP_ID), terminalId,terminalVersionId);

        Map<Long,Set<Long>> originRelations = new HashMap<>();
        //获取最新发布版本的血缘
        Long baseReleaseId = 0L;
        LinageGraph oldLinageGraph = null;
        if(latestRelease != null){
            baseReleaseId = latestRelease.getId();
            oldLinageGraph = getObjRelationBaseNoBridgeUp(baseReleaseId);
            originRelations = oldLinageGraph.getParentsMap();
        }
        Map<Long,Set<Long>> objRelations = originRelations;
        Map<Integer,List<EisTaskProcess>> processesGroupByType = processes.stream().collect(Collectors.groupingBy(EisTaskProcess::getReqPoolType));
        //处理spm开发流程
        List<EisTaskProcess> spmDevProcesses = Optional.ofNullable(processesGroupByType.get(ReqPoolTypeEnum.SPM_DEV.getReqPoolType()))
                .orElse(new ArrayList<>());
        if(!CollectionUtils.isEmpty(spmDevProcesses)){
            objRelations = getObjRelationByDevSpmAndBase(objRelations, spmDevProcesses);
            // 检查父对象是不是都存在
            checkParentsExist(oldLinageGraph, spmDevProcesses);
        }
        //处理spm下线流程
        List<EisTaskProcess> spmDeleteProcesses = Optional.ofNullable(processesGroupByType.get(ReqPoolTypeEnum.SPM_DELETE.getReqPoolType()))
                .orElse(new ArrayList<>());
        if(!CollectionUtils.isEmpty(spmDeleteProcesses)){
            objRelations = getObjRelationByDeleteSpmAndBase(objRelations,spmDeleteProcesses);
        }
        if(!MapUtils.isEmpty(objRelations)){
            //新血缘发布上线
            releaseForNewLineage(EtContext.get(ContextConstant.APP_ID), newTerminalReleaseId,terminalId,objRelations);
            //新血缘关联tracker新增
            List<EisTaskProcess> processesToReleaseTracker = new ArrayList<>(spmDevProcesses);
            if (CollectionUtils.isNotEmpty(spmDeleteProcesses)) {
                processesToReleaseTracker.addAll(spmDeleteProcesses);
            }
            releaseForTrackerOfLineage(baseReleaseId,newTerminalReleaseId,terminalId,reqPoolIds,processesToReleaseTracker);
        }
        //处理事件埋点流程
        List<EisTaskProcess> eventProcesses = Optional.ofNullable(processesGroupByType.get(ReqPoolTypeEnum.EVENT.getReqPoolType()))
                .orElse(new ArrayList<>());
        //发布事件埋点
        releaseForEventProcesses(newTerminalReleaseId,eventProcesses);
        //更新任务和流程的状态
        updateStatus(newTerminalReleaseId,tasks,processes);
        // 信息同步到三方
        if (CollectionUtils.isNotEmpty(tasks)) {
            tasks.forEach(t -> asyncHandleService.onTaskAndProcessUpdate(t.getId()));
        }

        // 如果涉及桥梁改动，需要对子空间进行发布，并且与子空间新版本关联，使子空间新版本可使用到最新桥梁
        releaseChildren4BridgeChange(newTerminalReleaseId, terminalId, spmDevProcesses, spmDeleteProcesses);

        // 若本空间作为子空间，发布时需要复制原本所有的映射关系
        if (latestRelease != null) { // 到这里，latestRelease已是上个版本
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
     * 若桥梁有改动，子空间都要发布一个版本
     */
    private void releaseChildren4BridgeChange(Long releaseId, Long terminalId, List<EisTaskProcess> spmDevProcesses, List<EisTaskProcess> spmDeleteProcesses) {
        // 父空间发布桥梁相关改动时，所有子空间需要更新一个版本
        Long appId = EtContext.get(ContextConstant.APP_ID);
        AppSimpleDTO appSimpleDTO = appService.getAppById(appId);
        if (appSimpleDTO == null) {
            throw new CommonException("appId不存在：" + appId);
        }

        TerminalSimpleDTO terminalSimpleDTO = terminalService.getById(terminalId);
        if (terminalSimpleDTO == null) {
            throw new CommonException("terminalId不存在：" + terminalId);
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
                // 本空间不需要发2次
                Long subTerminalId = p.getValue();
                if (subTerminalId.equals(terminalId)) {
                    continue;
                }
                subAppAndTerminalsToRelease.computeIfAbsent(p.getKey(), k -> new HashSet<>()).add(p.getValue());
            }
        }
        // 需要发布子空间
        subAppAndTerminalsToRelease.forEach((subAppId, subTerminalIds) -> {
            subTerminalIds.forEach(subTerminalId -> {
                String versionName = "父空间桥梁更新（" + appSimpleDTO.getName() + "-" + terminalSimpleDTO.getName() + "）" + releaseId + "-" + subTerminalId;
                Long subReleaseId = emptyRelease(versionName, subAppId, subTerminalId);
                // 获取子空间发布后的releaseId映射
                Set<Long> parentReleaseIds = new HashSet<>(appRelationService.getParentReleaseIds(subReleaseId));
                // 每个端只保留最新一个
                Set<Long> updatedParentReleaseIds = new HashSet<>(parentReleaseIds);
                updatedParentReleaseIds.add(releaseId);    // 此次需要新增releaseId作为父releaseId
                updatedParentReleaseIds = terminalReleaseService.getByIds(updatedParentReleaseIds).stream()
                        .collect(Collectors.toMap(EisTerminalReleaseHistory::getTerminalId, o -> o, (o1, o2) -> o1.getCreateTime().after(o2.getCreateTime()) ? o1 : o2))    // 同一个端保留较新那个
                        .values().stream().map(EisTerminalReleaseHistory::getId).collect(Collectors.toSet());
                // 差异更新到DB，先删后增
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
     * 空发布，血缘、对象信息都保持不变，仅增加一个版本号而已
     * @return 发布的releaseId
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
        //获取最新发布版本的血缘
        Long baseReleaseId = 0L;
        LinageGraph oldLinageGraph = null;
        if(latestRelease != null){
            baseReleaseId = latestRelease.getId();
            oldLinageGraph = getObjRelationBaseNoBridgeUp(baseReleaseId);
            originRelations = oldLinageGraph.getParentsMap();
        }
        Map<Long, Set<Long>> objRelations = originRelations;
        // 拷贝对象关系
        if (!MapUtils.isEmpty(objRelations)) {
            releaseForNewLineage(appId, newTerminalReleaseId, terminalId, objRelations);
        }
        // 拷贝tracker
        copyTrackerToNewReleaseId(baseReleaseId, newTerminalReleaseId, terminalId);

        // 若本空间作为子空间，发布时需要复制原本所有的映射关系
        if (latestRelease != null) { // 到这里，latestRelease已是上个版本
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
     * 新增或修改SPM时，需要校验父对象都存在
     */
    private void checkParentsExist(LinageGraph oldLinageGraph, List<EisTaskProcess> spmDevProcesses) {
        if (CollectionUtils.isEmpty(spmDevProcesses)) {
            return;
        }
        // 原版本已有oid
        Set<Long> originExistingOids = oldLinageGraph == null ? new HashSet<>() : oldLinageGraph.getAllObjIds();

        Set<Long> oidsToAdd = new HashSet<>();  // 待新增节点
        Set<Long> parentOidsToRelate = new HashSet<>(); // 待挂载父节点
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

        // 检查父节点是否都存在
        Set<Long> notExistParents = new HashSet<>();
        parentOidsToRelate.forEach(parentOid -> {
            // 原来血缘树里就有，合法
            if (originExistingOids.contains(parentOid)) {
                return;
            }
            // 新增的对象里有，合法
            if (oidsToAdd.contains(parentOid)) {
                return;
            }
            notExistParents.add(parentOid);
        });

        if (CollectionUtils.isNotEmpty(notExistParents)) {
            // 桥梁挂载属于合法
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
            // 桥梁不卡点
            if (CollectionUtils.isNotEmpty(outerSpaceObjIds)) {
                notExistParents.removeIf(outerSpaceObjIds::contains);
                notExistObjects.removeIf(o -> outerSpaceObjIds.contains(o.getId()));
            }
            if (CollectionUtils.isNotEmpty(notExistParents)) {
                throw new CommonException("以下父对象未上线，无法继续发布：" + JsonUtils.toJson(notExistObjects.stream().map(ObjectBasic::getOid).collect(Collectors.toList())));
            }
        }
    }

    /**
     * 新血缘
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
     * 空发布，拷贝tracker
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
     * 将tracker设为发布上线，写入eis_all_tracker_release
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
        //objIdSet校验冲突情况：若不同需求组都对同一个objId发生变更，则视为冲突
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
                throw new CommonException("oid为" + oid + "的对象在需求组" + JsonUtils.toJson(reqPoolIdSetOfObjId) + "内都有变更，存在冲突，请拆分上线端版本");
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
     * 获取对象关系，不包括桥梁以上血缘
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
        //同一个端版本号关联的任务只能在一个需求组下，因此这些流程的需求组是同一个
        Map<Long,Set<Long>> objToParentsOfDevProcess = getObjIdToParentsMapByProcesses(devProcesses);
        Map<Long,Set<Long>> objToParentsCombine = new HashMap<>();
        for (Long objId : objToParentsOfDevProcess.keySet()) {
            Set<Long> parentsOfCombine = objToParentsCombine.computeIfAbsent(objId,k->new HashSet<>());
            if(objToParentsOfBase.containsKey(objId)){
                //基线里也存在当前objId，则说明当前对象是变更操作，所以取父对象并集
                Set<Long> parentsOfProcess = objToParentsOfDevProcess.get(objId);
                Set<Long> parentsOfBase = objToParentsOfBase.get(objId);
                parentsOfCombine.addAll(parentsOfProcess);
                parentsOfCombine.addAll(parentsOfBase);
            }else {
                //基线不存在当前objId,则说明是新增操作，直接取待上线流程中的父对象集合
                Set<Long> parentsOfProcess = objToParentsOfDevProcess.get(objId);
                parentsOfCombine.addAll(parentsOfProcess);
            }
        }
        //获取存在基线内、但不在需求下的对象，直接复制父血缘
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
