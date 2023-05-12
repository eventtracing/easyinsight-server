package com.netease.hz.bdms.easyinsight.service.helper;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.netease.hz.bdms.easyinsight.common.bo.diff.EventDiff;
import com.netease.hz.bdms.easyinsight.common.bo.diff.ParamDiff;
import com.netease.hz.bdms.easyinsight.common.bo.diff.RelationDiff;
import com.netease.hz.bdms.easyinsight.common.bo.diff.ChangeTuple;
import com.netease.hz.bdms.easyinsight.common.bo.lineage.LinageGraph;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.diff.TrackerDiffDTO;
import com.netease.hz.bdms.easyinsight.common.dto.event.EventSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.obj.ObjectBasicDTO;
import com.netease.hz.bdms.easyinsight.common.dto.obj.ObjectTrackerInfoDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.UpdateSpmPoolParam;
import com.netease.hz.bdms.easyinsight.common.enums.*;
import com.netease.hz.bdms.easyinsight.common.exception.CommonException;
import com.netease.hz.bdms.easyinsight.common.param.obj.ObjectTrackerEditParam;
import com.netease.hz.bdms.easyinsight.common.param.param.paramBind.ParamBindItermParam;
import com.netease.hz.bdms.easyinsight.common.util.CommonUtil;
import com.netease.hz.bdms.easyinsight.common.util.JsonUtils;
import com.netease.hz.bdms.easyinsight.dao.model.*;
import com.netease.hz.bdms.easyinsight.service.service.ObjChangeHistoryService;
import com.netease.hz.bdms.easyinsight.service.service.ObjectBasicService;
import com.netease.hz.bdms.easyinsight.common.dto.obj.MergeObjReqDTO;
import com.netease.hz.bdms.easyinsight.service.service.asynchandle.AsyncHandleService;
import com.netease.hz.bdms.easyinsight.service.service.impl.AppRelationService;
import com.netease.hz.bdms.easyinsight.service.service.obj.AllTrackerReleaseService;
import com.netease.hz.bdms.easyinsight.service.service.obj.ObjTerminalTrackerService;
import com.netease.hz.bdms.easyinsight.service.service.obj.ReqObjRelationService;
import com.netease.hz.bdms.easyinsight.service.service.requirement.*;
import com.netease.hz.bdms.easyinsight.service.service.terminalrelease.TerminalReleaseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class RequirementPoolHelper {

    @Autowired
    ReqPoolBasicService reqPoolBasicService;

    @Autowired
    ReqObjRelationService reqObjRelationService;

    @Autowired
    RequirementInfoService requirementInfoService;

    @Autowired
    ReqSpmPoolService reqSpmPoolService;

    @Autowired
    ReqEventPoolService reqEventPoolService;

    @Autowired
    ReqTaskService reqTaskService;

    @Autowired
    TaskProcessService taskProcessService;

    @Autowired
    ObjTerminalTrackerService objTrackerService;

    @Autowired
    ObjChangeHistoryService objChangeHistoryService;

    @Autowired
    TerminalReleaseService terminalReleaseService;

    @Autowired
    ReqPoolRelBaseService reqPoolRelBaseService;

    @Autowired
    DiffHelper diffHelper;

    @Autowired
    LineageHelper lineageHelper;

    @Resource
    private ObjTerminalTrackerService objTerminalTrackerService;

    @Resource
    private ObjectBasicService objectBasicService;

    @Resource
    private AppRelationService appRelationService;

    @Resource
    private AllTrackerReleaseService allTrackerReleaseService;

    @Resource
    private AsyncHandleService asyncHandleService;

    @Resource
    private ObjectHelper objectHelper;

    @Transactional(rollbackFor = Throwable.class)
    public void updateSpmPool(Long reqPoolId, Set<Long> trackerIds, OperationTypeEnum operationTypeEnum,boolean isEdit){
        List<EisObjTerminalTracker> trackers = objTrackerService.getByIds(trackerIds);
        Set<Long> terminalIdsOfTracker = new HashSet<>();
        for (EisObjTerminalTracker tracker : trackers) {
            terminalIdsOfTracker.add(tracker.getTerminalId());
        }
        EisReqPoolRelBaseRelease query = new EisReqPoolRelBaseRelease();
        query.setReqPoolId(reqPoolId);
        List<EisReqPoolRelBaseRelease> relBaseReleases = reqPoolRelBaseService.search(query);
        Map<Long,Long> terminalIdToBaseReleasedIdMap = new HashMap<>();
        for (EisReqPoolRelBaseRelease relBaseRelease : relBaseReleases) {
            terminalIdToBaseReleasedIdMap.put(relBaseRelease.getTerminalId(),relBaseRelease.getBaseReleaseId());
        }
        if(operationTypeEnum.equals(OperationTypeEnum.CREATE)){
            //先处理删除端的情况，目前只要在新建的时候可以编辑删除对象关联端
            handleDeleteTerminalTracker(reqPoolId, trackers);
            handleCreate(reqPoolId,trackers,terminalIdToBaseReleasedIdMap,isEdit);
        }else if(operationTypeEnum.equals(OperationTypeEnum.CHANGE)){
            handleChange(reqPoolId,trackers,terminalIdToBaseReleasedIdMap,isEdit,false);
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void updateSpmPoolNew(Long reqPoolId, List<UpdateSpmPoolParam> updateSpmPoolParams, boolean update){
        List<EisObjTerminalTracker> trackers = objTrackerService.getByIds(updateSpmPoolParams.stream().map(UpdateSpmPoolParam::getTrackerId).collect(Collectors.toSet()));
        Map<Long, EisObjTerminalTracker> trackersMap = new HashMap<>();
        for (EisObjTerminalTracker tracker : trackers) {
            trackersMap.put(tracker.getId(), tracker);
        }
        EisReqPoolRelBaseRelease query = new EisReqPoolRelBaseRelease();
        query.setReqPoolId(reqPoolId);
        List<EisReqPoolRelBaseRelease> relBaseReleases = reqPoolRelBaseService.search(query);
        Map<Long,Long> terminalIdToBaseReleasedIdMap = new HashMap<>();
        for (EisReqPoolRelBaseRelease relBaseRelease : relBaseReleases) {
            terminalIdToBaseReleasedIdMap.put(relBaseRelease.getTerminalId(),relBaseRelease.getBaseReleaseId());
        }

        updateSpmPoolParams.forEach(updateSpmPoolParam -> {
            EisObjTerminalTracker tracker = trackersMap.get(updateSpmPoolParam.getTrackerId());
            if (tracker == null) {
                return;
            }
            OperationTypeEnum operationTypeEnum = updateSpmPoolParam.getOperationTypeEnum();
            boolean isEdit = updateSpmPoolParam.isEdit();
            if (operationTypeEnum.equals(OperationTypeEnum.CREATE)) {
                //先处理删除端的情况，目前只要在新建的时候可以编辑删除对象关联端
                handleDeleteTerminalTracker(reqPoolId, trackers);
                handleCreate(reqPoolId, Arrays.asList(tracker), terminalIdToBaseReleasedIdMap, isEdit);
            } else if (operationTypeEnum.equals(OperationTypeEnum.CHANGE)) {
                handleChange(reqPoolId, Arrays.asList(tracker), terminalIdToBaseReleasedIdMap, isEdit, update);
            }
        });
    }

    private void handleCreate(Long reqPoolId, List<EisObjTerminalTracker> trackers, Map<Long, Long> terminalIdToBaseReleasedIdMap, boolean isEdit) {
        Long appId = EtContext.get(ContextConstant.APP_ID);
        for (EisObjTerminalTracker tracker : trackers) {
            // 当前端可以挂载的桥梁对象
            Long baseReleasedId = Optional.ofNullable(terminalIdToBaseReleasedIdMap.get(tracker.getTerminalId())).orElse(0L);
            LinageGraph reqLinageGraph = lineageHelper.genReqLinageGraph(baseReleasedId,tracker.getTerminalId(),tracker.getReqPoolId());
            List<List<Long>> spmsOfCurrentObjInReqLineage = lineageHelper.getObjIdSpms(reqLinageGraph,tracker.getObjId());
            // 对于根节点是父空间桥梁的，需要补充父空间血缘图
            List<ObjectBasic> parentBridgesOfEachTerminal = appRelationService.getParentBridgeCandidatesByReqPoolId(appId, tracker.getTerminalId(), reqPoolId);
            Map<Long, List<Long>> bridgeUpTerminalIdMapping = appRelationService.getBridgeUpTerminalIdMapping(parentBridgesOfEachTerminal, appId, tracker.getTerminalId());
            Map<Long, LinageGraph> bridgeTerminalLinageGraphMap = lineageHelper.getBridgeTerminalLinageGraphMap(bridgeUpTerminalIdMapping.values().stream().flatMap(o -> o == null ? Stream.empty() : o.stream()).collect(Collectors.toSet()), terminalIdToBaseReleasedIdMap.get(tracker.getTerminalId()));
            spmsOfCurrentObjInReqLineage = lineageHelper.updateBridgeParent(spmsOfCurrentObjInReqLineage, bridgeUpTerminalIdMapping, bridgeTerminalLinageGraphMap);
            /**
             *新建操作
             */
            List<EisReqPoolSpm> list = new ArrayList<>();
            for (List<Long> spmByObjIdList : spmsOfCurrentObjInReqLineage) {
                EisReqPoolSpm reqPoolSpm = new EisReqPoolSpm();
                reqPoolSpm.setSpmByObjId(CommonUtil.getSpmStringByObjIds(spmByObjIdList));
                reqPoolSpm.setObjId(tracker.getObjId());
                reqPoolSpm.setObjHistoryId(tracker.getObjHistoryId());
                reqPoolSpm.setTerminalId(tracker.getTerminalId());
                reqPoolSpm.setReqPoolId(reqPoolId);
                reqPoolSpm.setReqPoolType(ReqPoolTypeEnum.SPM_DEV.getReqPoolType());
                reqPoolSpm.setReqType(RequirementTypeEnum.CREATE.getReqType().toString());
                reqPoolSpm.setAppId(EtContext.get(ContextConstant.APP_ID));
                setBridge(reqPoolSpm, bridgeUpTerminalIdMapping, spmByObjIdList);
                list.add(reqPoolSpm);
            }
            if(isEdit){
                /**
                 * 新建后编辑
                 */
                compareAndUpdateForSelf(reqPoolId,tracker.getTerminalId(),tracker.getObjId(),tracker.getObjHistoryId(),list, false);
                compareAndUpdateForChildren(reqLinageGraph,reqPoolId,tracker.getObjId(),tracker.getTerminalId(), bridgeUpTerminalIdMapping, bridgeTerminalLinageGraphMap);
            }else {
                /**
                 * 初始
                 */
                reqSpmPoolService.insertBatch(list);
            }
        }
    }

    private void setBridge(EisReqPoolSpm reqPoolSpm, Map<Long, List<Long>> bridgeUpTerminalIdMapping, List<Long> spmByObjIdList) {
        if (reqPoolSpm == null || MapUtils.isEmpty(bridgeUpTerminalIdMapping) || CollectionUtils.isEmpty(spmByObjIdList)) {
            return;
        }
        for (Long oid : spmByObjIdList) {
            List<Long> l = bridgeUpTerminalIdMapping.get(oid);
            // 是桥梁，一条桥梁中SPM只有一个，因此找到即可
            if (CollectionUtils.isNotEmpty(l)) {
                reqPoolSpm.setBridgeObjId(oid);
                ObjectBasic objectBasic = objectBasicService.getById(oid);
                if (objectBasic == null) {
                    throw new CommonException("桥梁对象不存在：oid=" + oid);
                }
                reqPoolSpm.setBridgeAppId(objectBasic.getAppId());
                break;
            }
        }
    }

    private void handleChange(Long reqPoolId, List<EisObjTerminalTracker> trackers,Map<Long,Long> terminalIdToBaseReleasedIdMap,boolean isEdit,boolean update) {
        Long appId = EtContext.get(ContextConstant.APP_ID);
        for (EisObjTerminalTracker tracker : trackers) {

            Long baseReleasedId = Optional.ofNullable(terminalIdToBaseReleasedIdMap.get(tracker.getTerminalId())).orElse(0L);
            LinageGraph reqLinageGraph = lineageHelper.genReqLinageGraph(baseReleasedId,tracker.getTerminalId(),tracker.getReqPoolId());
            List<List<Long>> spmsOfCurrentObjInReqLineage = lineageHelper.getObjIdSpms(reqLinageGraph,tracker.getObjId());
            // 对于根节点是父空间桥梁的，需要补充父空间血缘图
            List<ObjectBasic> parentBridgesOfEachTerminal = appRelationService.getParentBridgeCandidatesByReqPoolId(appId, tracker.getTerminalId(), reqPoolId);
            Map<Long, List<Long>> bridgeUpTerminalIdMapping = appRelationService.getBridgeUpTerminalIdMapping(parentBridgesOfEachTerminal, appId, tracker.getTerminalId());
            Map<Long, LinageGraph> bridgeTerminalLinageGraphMap = lineageHelper.getBridgeTerminalLinageGraphMap(bridgeUpTerminalIdMapping.values().stream().flatMap(o -> o == null ? Stream.empty() : o.stream()).collect(Collectors.toSet()), terminalIdToBaseReleasedIdMap.get(tracker.getTerminalId()));
            spmsOfCurrentObjInReqLineage = lineageHelper.updateBridgeParent(spmsOfCurrentObjInReqLineage, bridgeUpTerminalIdMapping, bridgeTerminalLinageGraphMap);
            ChangeTuple changeTuple = getChangeTuple(tracker, baseReleasedId, bridgeUpTerminalIdMapping, bridgeTerminalLinageGraphMap);
            List<List<Long>> newSpms = changeTuple.getNewSpms() == null ? new ArrayList<>(0) : changeTuple.getNewSpms();
            List<List<Long>> deletedSpms = changeTuple.getDeletedSpms();
            //待开发需求池的需求类型
            Set<String> reqTypesOfDev = new HashSet<>();
            //待开发需求池关联的spm
            Set<List<Long>> spmsOfDevPool = new HashSet<>();
            //待下线需求池关联的spm
            Set<List<Long>> spmsOfDeletePool = new HashSet<>();
            List<EisReqPoolSpm> list = new ArrayList<>();

            if(changeTuple.getIsPubParamPackageChanged()){
                reqTypesOfDev.add(RequirementTypeEnum.PUB_PARAM_CHANGE.getReqType().toString());
                spmsOfDevPool.addAll(spmsOfCurrentObjInReqLineage);
            }
            if(changeTuple.getIsPrvParamChanged()){
                reqTypesOfDev.add(RequirementTypeEnum.PRV_PARAM_CHANGE.getReqType().toString());
                spmsOfDevPool.addAll(spmsOfCurrentObjInReqLineage);
            }
            if(changeTuple.getIsEventChanged()) {
                reqTypesOfDev.add(RequirementTypeEnum.EVT_CHANGE.getReqType().toString());
                spmsOfDevPool.addAll(spmsOfCurrentObjInReqLineage);
            }
            if(reqTypesOfDev.isEmpty()){
                reqTypesOfDev.add(RequirementTypeEnum.REUSE_CHANGE.getReqType().toString());
                spmsOfDevPool.addAll(spmsOfCurrentObjInReqLineage);
            }

            if(!CollectionUtils.isEmpty(newSpms)){
//                reqTypesOfDev.add(RequirementType.NEW_PARENT.getReqType().toString());
                spmsOfDevPool.addAll(newSpms);
            }
            for (List<Long> spmOfDevPool : spmsOfDevPool) {
                EisReqPoolSpm reqPoolSpm = new EisReqPoolSpm();
                if(newSpms.contains(spmOfDevPool)){
                    log.info("新血缘spmByObjId:" + CommonUtil.getSpmStringByObjIds(spmOfDevPool));
                    /**
                     * 若当前spm属于新增的spm，则需求类型加上"血缘新增"
                     */
                    reqTypesOfDev.add(RequirementTypeEnum.NEW_PARENT.getReqType().toString());
                }
                reqPoolSpm.setSpmByObjId(CommonUtil.getSpmStringByObjIds(spmOfDevPool));
                reqPoolSpm.setObjId(tracker.getObjId());
                reqPoolSpm.setObjHistoryId(tracker.getObjHistoryId());
                reqPoolSpm.setTerminalId(tracker.getTerminalId());
                reqPoolSpm.setReqPoolId(reqPoolId);
                reqPoolSpm.setReqPoolType(ReqPoolTypeEnum.SPM_DEV.getReqPoolType());
                setBridge(reqPoolSpm, bridgeUpTerminalIdMapping, spmOfDevPool);
                String reqTypesDevStr = String.join(",", reqTypesOfDev);
                reqPoolSpm.setReqType(reqTypesDevStr);
                list.add(reqPoolSpm);
            }
            if(!CollectionUtils.isEmpty(deletedSpms)){
                spmsOfDeletePool.addAll(deletedSpms);
                for (List<Long> spmOfDeletePool : spmsOfDeletePool) {
                    EisReqPoolSpm reqPoolSpm = new EisReqPoolSpm();
                    reqPoolSpm.setSpmByObjId(CommonUtil.getSpmStringByObjIds(spmOfDeletePool));
                    reqPoolSpm.setObjId(tracker.getObjId());
                    reqPoolSpm.setObjHistoryId(tracker.getObjHistoryId());
                    reqPoolSpm.setTerminalId(tracker.getTerminalId());
                    reqPoolSpm.setReqPoolId(reqPoolId);
                    reqPoolSpm.setReqPoolType(ReqPoolTypeEnum.SPM_DELETE.getReqPoolType());
                    reqPoolSpm.setReqType(RequirementTypeEnum.DELETE_PARENT.getReqType().toString());
                    reqPoolSpm.setAppId(EtContext.get(ContextConstant.APP_ID));
                    setBridge(reqPoolSpm, bridgeUpTerminalIdMapping, spmOfDeletePool);
                    list.add(reqPoolSpm);
                }
            }
            if(isEdit){
                /**
                 * 变更后编辑
                 */
                compareAndUpdateForSelf(reqPoolId,tracker.getTerminalId(),tracker.getObjId(),tracker.getObjHistoryId(),list, update);
                compareAndUpdateForChildren(reqLinageGraph,reqPoolId,tracker.getObjId(),tracker.getTerminalId(), bridgeUpTerminalIdMapping, bridgeTerminalLinageGraphMap);
            }else {
                /**
                 * 初始
                 */
                reqSpmPoolService.insertBatch(list);
                compareAndUpdateForChildren(reqLinageGraph,reqPoolId,tracker.getObjId(),tracker.getTerminalId(), bridgeUpTerminalIdMapping, bridgeTerminalLinageGraphMap);
            }
        }

    }

    public void handleDeleteTerminalTracker(Long reqPoolId, List<EisObjTerminalTracker> trackers){
        if(CollectionUtils.isEmpty(trackers)){
            return;
        }
        Long objId = trackers.get(0).getObjId();
        Set<Long> terminalsOfCurrentTracker = new HashSet<>();
        for (EisObjTerminalTracker tracker : trackers) {
            terminalsOfCurrentTracker.add(tracker.getTerminalId());
        }
        EisReqPoolSpm reqPoolSpmQuery = new EisReqPoolSpm();
        reqPoolSpmQuery.setReqPoolId(reqPoolId);
        reqPoolSpmQuery.setObjId(objId);
        List<EisReqPoolSpm> reqPoolSpms = reqSpmPoolService.search(reqPoolSpmQuery);
        //判断对象有没有在编辑时删除端
        Set<Long> reqPoolSpmIdsNeedTobeDelete = new HashSet<>();
        Set<Long> terminalIdsNeedTobedelete = new HashSet<>();
        for (EisReqPoolSpm reqPoolSpm : reqPoolSpms) {
            if(!terminalsOfCurrentTracker.contains(reqPoolSpm.getTerminalId())){
                reqPoolSpmIdsNeedTobeDelete.add(reqPoolSpm.getId());
                terminalIdsNeedTobedelete.add(reqPoolSpm.getTerminalId());
            }
        }
        if(!CollectionUtils.isEmpty(reqPoolSpmIdsNeedTobeDelete)){
            /**
             * 若做了删除端的操作，则需要校验:
             * 1. 当前对象是否被引作父对象
             * 2. 是否已被指派任务
             */
            EisReqObjRelation reqObjRelationQuery = new EisReqObjRelation();
            reqObjRelationQuery.setParentObjId(objId);
            List<EisReqObjRelation> reqObjRelations = reqObjRelationService.search(reqObjRelationQuery);
            if(!CollectionUtils.isEmpty(reqObjRelations)){
                for (EisReqObjRelation reqObjRelation : reqObjRelations) {
                    if(terminalIdsNeedTobedelete.contains(reqObjRelation.getTerminalId())){
                        throw new CommonException("当前对象已被引用作父对象，无法删除关联端");
                    }
                }
            }
            List<EisTaskProcess> processes = taskProcessService.getByReqPoolEntityIds(ReqPoolTypeEnum.SPM_DEV, reqPoolSpmIdsNeedTobeDelete);
            if(!CollectionUtils.isEmpty(processes)){
                throw new CommonException("当前对象相关spm待办项已指派任务，无法删除端");
            }
            reqSpmPoolService.deleteByIds(reqPoolSpmIdsNeedTobeDelete);
        }
    }

    /**
     * 在做编辑操作时，开发池、下线池和编辑之前做比对，增量更新或者删除，不做全删全增
     * @param reqPoolId
     * @param terminalId
     * @param objId
     * @param historyId
     * @param newList
     */
    public void compareAndUpdateForSelf(Long reqPoolId, Long terminalId, Long objId, Long historyId, List<EisReqPoolSpm> newList, boolean update){
        EisReqPoolSpm reqPoolSpm = new EisReqPoolSpm();
        reqPoolSpm.setReqPoolId(reqPoolId);
        reqPoolSpm.setObjId(objId);
        reqPoolSpm.setTerminalId(terminalId);
        reqPoolSpm.setObjHistoryId(historyId);
        List<EisReqPoolSpm> oldList = reqSpmPoolService.search(reqPoolSpm);
        List<EisReqPoolSpm> oldListOfDevPool = new ArrayList<>();
        List<EisReqPoolSpm> oldListOfDeletePool = new ArrayList<>();
        List<EisReqPoolSpm> newListOfDevPool = new ArrayList<>();
        List<EisReqPoolSpm> newListOfDeletePool = new ArrayList<>();
        for (EisReqPoolSpm eisReqPoolSpm : oldList) {
            if(eisReqPoolSpm.getReqPoolType().equals(ReqPoolTypeEnum.SPM_DEV.getReqPoolType())){
                oldListOfDevPool.add(eisReqPoolSpm);
            }else if(eisReqPoolSpm.getReqPoolType().equals(ReqPoolTypeEnum.SPM_DELETE.getReqPoolType())){
                oldListOfDeletePool.add(eisReqPoolSpm);
            }
        }
        for (EisReqPoolSpm eisReqPoolSpm : newList) {
            if(eisReqPoolSpm.getReqPoolType().equals(ReqPoolTypeEnum.SPM_DEV.getReqPoolType())){
                newListOfDevPool.add(eisReqPoolSpm);
            }else if(eisReqPoolSpm.getReqPoolType().equals(ReqPoolTypeEnum.SPM_DELETE.getReqPoolType())){
                newListOfDeletePool.add(eisReqPoolSpm);
            }
        }
        List<String> oldSpms = new ArrayList<>();
        List<String> newSpms = new ArrayList<>();
        for (EisReqPoolSpm eisReqPoolSpm : newListOfDevPool) {
            newSpms.add(eisReqPoolSpm.getSpmByObjId());
        }
        for (EisReqPoolSpm eisReqPoolSpm : oldListOfDevPool) {
            oldSpms.add(eisReqPoolSpm.getSpmByObjId());
        }
        log.info("terminalId = {},新spm待办记录：{}",terminalId,newSpms);
        log.info("terminalId = {},旧spm待办记录：{}",terminalId,oldSpms);
        compareAndUpdateCore(newListOfDevPool,oldListOfDevPool,ReqPoolTypeEnum.SPM_DEV, update);
        compareAndUpdateCore(newListOfDeletePool,oldListOfDeletePool,ReqPoolTypeEnum.SPM_DELETE, false);
//        compareAndUpdateCore()

    }

    public void compareAndUpdateCore(List<EisReqPoolSpm> newList,List<EisReqPoolSpm> oldList,ReqPoolTypeEnum reqPoolTypeEnum, boolean update){
        Map<String,EisReqPoolSpm> combineKeyMapOfOldList = new HashMap<>();
        Map<String,EisReqPoolSpm> combineKeyMapOfNewList = new HashMap<>();
        for (EisReqPoolSpm reqPoolSpm : oldList) {
            /**
             * 组合唯一键：需求池id + spm + 终端id + 需求类型
             */
            String combineKey = reqPoolSpm.getReqPoolId() + "_" + reqPoolSpm.getSpmByObjId()
                    + "_" + reqPoolSpm.getTerminalId() + "_" + reqPoolSpm.getReqType();
            combineKeyMapOfOldList.put(combineKey,reqPoolSpm);
        }
        for (EisReqPoolSpm reqPoolSpm : newList) {
            /**
             * 组合唯一键：需求池id + spm + 终端id + 需求类型
             */
            String combineKey = reqPoolSpm.getReqPoolId() + "_" + reqPoolSpm.getSpmByObjId()
                    + "_" + reqPoolSpm.getTerminalId() + "_" + reqPoolSpm.getReqType();
            combineKeyMapOfNewList.put(combineKey,reqPoolSpm);
        }
        Set<String> keysOfInsert = Sets.difference(combineKeyMapOfNewList.keySet(),combineKeyMapOfOldList.keySet());
        Set<String> keysOfDelete = Sets.difference(combineKeyMapOfOldList.keySet(),combineKeyMapOfNewList.keySet());
        List<EisReqPoolSpm> insertList = new ArrayList<>();
        List<EisReqPoolSpm> deleteList = new ArrayList<>();
        for (String key : keysOfInsert) {
            insertList.add(combineKeyMapOfNewList.get(key));
        }
        for(String key:keysOfDelete){
            deleteList.add(combineKeyMapOfOldList.get(key));
        }

        if(update || !CollectionUtils.isEmpty(insertList) || !CollectionUtils.isEmpty(deleteList)) {
            //有编辑相关参数
            if(update){
                insertList = newList;
                deleteList = oldList;
            }

            if (!CollectionUtils.isEmpty(insertList)) {
                reqSpmPoolService.insertBatch(insertList);
                Set<Long> ids = new HashSet<>();
                if (!CollectionUtils.isEmpty(deleteList)) {
                    ids = deleteList.stream().map(EisReqPoolSpm::getId).collect(Collectors.toSet());
                }
                if (update) {
                    Set<Long> affectedTaskIds = taskProcessService.updateUnReleasedProcessesByReqPoolEntityIds(reqPoolTypeEnum, insertList, ids);
                    affectedTaskIds.forEach(taskId -> asyncHandleService.onTaskAndProcessUpdate(taskId));
                }
            }
            if (!CollectionUtils.isEmpty(deleteList)) {
                Set<Long> ids = deleteList.stream().map(e -> e.getId()).collect(Collectors.toSet());
                reqSpmPoolService.deleteByIds(ids);
                //删除未发布的已指派的任务
                taskProcessService.deleteUnReleasedProcessesByReqPoolEntityIds(reqPoolTypeEnum, ids);
            }
        }
    }

    /**
     * 当对象有子孙对象在需求池中时，考虑当前对象的血缘变更是否给子孙对象的spm带来变化，如果发生变化，则需调整子孙对象待分配项的spm
     * @param linageGraph
     * @param reqPoolId
     * @param objId
     * @param terminalId
     */
    public void compareAndUpdateForChildren(LinageGraph linageGraph, Long reqPoolId, Long objId, Long terminalId, Map<Long, List<Long>> bridgeUpTerminalIdMapping, Map<Long, LinageGraph> parentTerminalLinageGraphMap) {
        EisReqPoolSpm query = new EisReqPoolSpm();
        query.setReqPoolId(reqPoolId);
        query.setTerminalId(terminalId);
        query.setReqPoolType(ReqPoolTypeEnum.SPM_DEV.getReqPoolType());
        List<EisReqPoolSpm> allListInCurrentPool = reqSpmPoolService.search(query);
        //objId的子孙对应的spm待办项
        List<EisReqPoolSpm> reqPoolSpmsOfchildren = new ArrayList<>();
        //需求池spm表中包含目标objId的spm
        for (EisReqPoolSpm reqPoolSpm : allListInCurrentPool) {
            String spmByObjId = reqPoolSpm.getSpmByObjId();
            List<Long> spmAsObjIdList = CommonUtil.transSpmToObjIdList(spmByObjId);
            if(spmAsObjIdList.contains(objId) && !spmAsObjIdList.get(0).equals(objId)){
                reqPoolSpmsOfchildren.add(reqPoolSpm);
            }
        }
        compareAndUpdateByNewLineageForTarget(linageGraph, reqPoolSpmsOfchildren, objId, bridgeUpTerminalIdMapping, parentTerminalLinageGraphMap);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void updateAutoRebaseOnly(Long reqPoolId, Long terminalId, boolean autoRebase) {
        reqPoolRelBaseService.updateAutoRebase(reqPoolId, terminalId, autoRebase);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void rebaseForReqSpms(Long reqPoolId, Long terminalId, Long newReleaseId, boolean autoRebase){
        Set<Long> mergeFailedObjs = mergeFailedObjs(reqPoolId, terminalId, newReleaseId);
        if (CollectionUtils.isNotEmpty(mergeFailedObjs)) {
            // 有冲突对象，更新冲突状态，基线不调整
            objChangeHistoryService.updateConflictStatus(reqPoolId, mergeFailedObjs, ConflictStatusEnum.MERGE_CONFLICT.getStatus());
        } else {
            // 无冲突才调整基线
            reqPoolRelBaseService.changeCurrentUse(reqPoolId, terminalId, newReleaseId, autoRebase);
        }

    }

    public Set<Long> mergeFailedObjs(Long reqPoolId, Long terminalId, Long newReleaseId){
        EisTerminalReleaseHistory releaseHistory = terminalReleaseService.getById(newReleaseId);
        if (releaseHistory == null) {
            throw new CommonException("newReleaseId 不存在 " + newReleaseId);
        }
        if (!terminalId.equals(releaseHistory.getTerminalId())) {
            throw new CommonException("newReleaseId与terminalId不匹配 " + newReleaseId + " " + terminalId);
        }
        LinageGraph linageGraph = lineageHelper.genReleasedLinageGraph(newReleaseId);
        EisReqObjRelation query = new EisReqObjRelation();
        query.setReqPoolId(reqPoolId);
        List<EisReqObjRelation> relationsOfReq = reqObjRelationService.search(query);
        // 只需检查当前terminal下的变更即可
        relationsOfReq = relationsOfReq.stream().filter(relation -> terminalId.equals(relation.getTerminalId())).collect(Collectors.toList());

        //校验：
        // 1.若当前需求存在变更对象，且该对象在新选择的基线中不存在，则无法变基。
        // 2.若需求池中某个对象的父对象不在需求池中（来自基线），并且新选择的基线中不包括这个父对象，则无法变基。
        Set<Long> parentObjIdsOfReq = relationsOfReq.stream().filter(e -> e.getParentObjId() != null).map(e -> e.getParentObjId()).collect(Collectors.toSet());
        Set<Long> objIdsOfReq = relationsOfReq.stream().map(e -> e.getObjId()).collect(Collectors.toSet());
        List<EisObjChangeHistory> objChangeHistoriesOfReq = objChangeHistoryService.getByReqPoolId(reqPoolId);
        Map<Long, Long> historyIdMap = new HashMap<>();
        objChangeHistoriesOfReq.forEach(o -> historyIdMap.put(o.getObjId(), o.getId()));
        objIdsOfReq.addAll(historyIdMap.keySet());  // objIdsOfReq从relationsOfReq中拿存在历史数据问题，根页面可能没有，因此要换个地方拿

        Set<Long> parentObjIdsNotInReq = Sets.difference(parentObjIdsOfReq,objIdsOfReq);
        Set<Long> allObjIdsOfNewBase = linageGraph.getAllObjIds();
        Set<Long> parentObjIdNotInNewBase = Sets.difference(parentObjIdsNotInReq,allObjIdsOfNewBase);
        if(!CollectionUtils.isEmpty(parentObjIdNotInNewBase)){
            List<ObjectBasic> objbasics = objectBasicService.getByIds(parentObjIdNotInNewBase);
            List<String> oids = CollectionUtils.isEmpty(objbasics) ? new ArrayList<>(0) : objbasics.stream()
                    .filter(o -> !ObjSpecialTypeEnum.BRIDGE.getName().equals(o.getSpecialType()))   // 桥梁不在新基线里，但是可以使用
                    .map(ObjectBasic::getOid)
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(oids)) {
                throw new CommonException("选择的基线不包括" + JsonUtils.toJson(oids) + "这些对象，无法变基");
            }
        }
        List<EisObjChangeHistory> changeHistoriesOfReqPool = objChangeHistoryService.getByReqPoolId(reqPoolId);

        // 计算跟本端有关的EisObjChangeHistory
        Set<Long> changeHistoryIds = changeHistoriesOfReqPool.stream().map(EisObjChangeHistory::getId).collect(Collectors.toSet());
        List<EisObjTerminalTracker> trackers = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(changeHistoryIds)) {
            trackers = objTerminalTrackerService.getBatchByChangeHistoryIds(changeHistoryIds);
        }
        if (trackers == null) {
            trackers = new ArrayList<>(0);
        }
        Set<Long> changedObjIdsOfThisTerminal = trackers.stream().filter(tracker -> terminalId.equals(tracker.getTerminalId())).map(EisObjTerminalTracker::getObjHistoryId).collect(Collectors.toSet());

        //获得变更对象id集合
        Set<Long> changedObjIds = changeHistoriesOfReqPool.stream()
                .filter(e -> changedObjIdsOfThisTerminal.contains(e.getId()))   // 只关心本端的
                .filter(e -> e.getType().equals(OperationTypeEnum.CHANGE.getOperationType()))
                .map(e -> e.getObjId())
                .collect(Collectors.toSet());
        //校验变更对象objId在新基线中是否存在
        Set<Long> changedObjIdsNotInNewBase = Sets.difference(changedObjIds,allObjIdsOfNewBase);
        if(!CollectionUtils.isEmpty(changedObjIdsNotInNewBase)){
            List<ObjectBasic> objbasics = objectBasicService.getByIds(changedObjIdsNotInNewBase);
            List<String> oids = CollectionUtils.isEmpty(objbasics) ? new ArrayList<>(0) : objbasics.stream()
                    .filter(o -> !ObjSpecialTypeEnum.BRIDGE.getName().equals(o.getSpecialType()))   // 桥梁不在新基线里，但是可以使用
                    .map(ObjectBasic::getOid)
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(oids)) {
                throw new CommonException("选择的基线不包括" + JsonUtils.toJson(oids) + "这些对象，无法变基");
            }
        }

        // 对需求组中对象，检查其与新基线的historyId相比是否有变化，若有变化则需要合并
        List<EisAllTrackerRelease> trackersOfNewRelease = allTrackerReleaseService.searchByReleaseIdAndObjIds(newReleaseId, new ArrayList<>(historyIdMap.keySet()));
        List<EisObjTerminalTracker> objTerminalTrackersInNewRelease = objTerminalTrackerService.getByIds(trackersOfNewRelease.stream().map(EisAllTrackerRelease::getTrackerId).collect(Collectors.toSet()));
        Map<Long, Long> newReleaseHistoryIdMap = new HashMap<>();   // 新基线对象的historyId
        objTerminalTrackersInNewRelease.forEach(o -> newReleaseHistoryIdMap.put(o.getObjId(), o.getObjHistoryId()));
        Set<Long> needMergeObjIds = new HashSet<>();
        historyIdMap.forEach((objId, currentHistoryId) -> {
            Long newReleaseHistoryId = newReleaseHistoryIdMap.get(objId);
            if (newReleaseHistoryId != null && !newReleaseHistoryId.equals(currentHistoryId)) {
                needMergeObjIds.add(objId);
            }
        });

        return mergeObjs(needMergeObjIds.stream()
                .map(o ->
                        new MergeObjReqDTO().setObjId(o)
                                .setReqPoolId(reqPoolId)
                                .setTargetReleaseId(newReleaseId)
                                .setObjHistoryIdOfReqPool(historyIdMap.get(o)))
                .collect(Collectors.toList()));
    }

    /**
     * 合并对象
     * @return 合并失败的对象，后续需要手动解决冲突
     */
    private Set<Long> mergeObjs(List<MergeObjReqDTO> mergeRequests) {
        Set<Long> mergeFailedObjs = new HashSet<>();
        mergeRequests.forEach(mergeRequest -> {
            boolean success;
            try {
                success = mergeObj(mergeRequest);
            } catch (Exception e) {
                log.warn("自动合并对象失败, mergeRequset={}", JsonUtils.toJson(mergeRequest), e);
                success = false;
            }
            if (!success) {
                mergeFailedObjs.add(mergeRequest.getObjId());
            }
        });
        return mergeFailedObjs;
    }

    /**
     * 在新的血缘图中比对旧的spm待办列表，并更新，使用场景：
     * 1. 对象池中的新建、变更、编辑操作
     * 2. rebase
     * @param linageGraph
     */
    private void compareAndUpdateByNewLineageForTarget(LinageGraph linageGraph, List<EisReqPoolSpm> reqPoolSpms,Long currentChangedObjId, Map<Long, List<Long>> bridgeUpTerminalIdMapping, Map<Long, LinageGraph> parentTerminalLinageGraphMap){
        Set<Long> objIdsOfReq = reqPoolSpms.stream().map(e -> e.getObjId()).collect(Collectors.toSet());
        Map<Long,List<EisReqPoolSpm>> objIdToReqPoolSpmsMap = new HashMap<>();
        for (EisReqPoolSpm reqPoolSpm : reqPoolSpms) {
            List<EisReqPoolSpm> spms = objIdToReqPoolSpmsMap.computeIfAbsent(reqPoolSpm.getObjId(),k -> new ArrayList<>());
            spms.add(reqPoolSpm);
        }
        Map<Long,List<List<Long>>> objIdToSpmsOfLineage = new HashMap<>();
        //根据血缘图，获得到各个对象在这个血缘图下的所有到根节点的路径
        for (Long objId : objIdsOfReq) {
            List<List<Long>> spmsAsListOfChild = lineageHelper.getObjIdSpms(linageGraph,objId);
            spmsAsListOfChild = lineageHelper.updateBridgeParent(spmsAsListOfChild, bridgeUpTerminalIdMapping, parentTerminalLinageGraphMap);
            objIdToSpmsOfLineage.put(objId,spmsAsListOfChild);
        }
        //遍历各个需求对象
        List<EisReqPoolSpm> insertList = new ArrayList<>();
        Set<Long> reqPoolSpmIdsToDelete = new HashSet<>();
        for (Long objId : objIdsOfReq){
            List<EisReqPoolSpm> reqPoolSpmsOfObj = objIdToReqPoolSpmsMap.get(objId);
            List<List<Long>> spmsAsListInLineageOfObjId = objIdToSpmsOfLineage.get(objId);
            Set<String> spmByObjIdsOfReq = reqPoolSpmsOfObj.stream().map(e -> e.getSpmByObjId()).collect(Collectors.toSet());
            //遍历当前需求对象在新血缘图下的所有spm，获取需要新增的spm待办项

            for (List<Long> spmAsListInLineage : spmsAsListInLineageOfObjId) {
                if(spmAsListInLineage.size() > 1){
                    Long parentObjId = spmAsListInLineage.get(1);
                    String spmStringInLineage = CommonUtil.getSpmStringByObjIds(spmAsListInLineage);
                    //如果具有相同的parentId，则判断是否要纳入新增的待办项集合
                    for (EisReqPoolSpm reqPoolSpm : reqPoolSpmsOfObj) {
                        String spmByObjId = reqPoolSpm.getSpmByObjId();
                        List<String> objIdStringList = Lists.newArrayList(spmByObjId.split("\\|"));
                        //正常情况下在这个分支下list大小一定大于1
                        Long parentObjIdOfReq = Long.valueOf(objIdStringList.get(1));
                        if(parentObjId.equals(parentObjIdOfReq)&& spmAsListInLineage.contains(currentChangedObjId)){
                            if(!spmByObjIdsOfReq.contains(spmStringInLineage)){
                                //若在需求池中没有该spm，则新增
                                EisReqPoolSpm newReqPoolSpm = new EisReqPoolSpm();
                                newReqPoolSpm.setSpmByObjId(spmStringInLineage);
                                newReqPoolSpm.setReqPoolId(reqPoolSpm.getReqPoolId());
                                newReqPoolSpm.setObjId(reqPoolSpm.getObjId());
                                newReqPoolSpm.setTerminalId(reqPoolSpm.getTerminalId());
                                newReqPoolSpm.setObjHistoryId(reqPoolSpm.getObjHistoryId());
                                newReqPoolSpm.setReqType(reqPoolSpm.getReqType());
                                newReqPoolSpm.setReqPoolType(reqPoolSpm.getReqPoolType());
                                newReqPoolSpm.setAppId(reqPoolSpm.getAppId());
                                setBridge(reqPoolSpm, bridgeUpTerminalIdMapping, spmAsListInLineage);
                                insertList.add(newReqPoolSpm);
                            }
                        }
                    }
                }else {
                    //当前对象为根节点，则reqPoolSpmsOfObj集合大小只能为1，因为它只有一条spm，这种情况下什么都不用做
                }
            }

            //比对，获得待删除的记录
            Set<String> spmsAsStringInLineage = new HashSet<>();
            for (List<Long> spmAsList : spmsAsListInLineageOfObjId) {
                String spmAsString = String.join("|",spmAsList.stream().map(e -> e.toString()).collect(Collectors.toList()));
                spmsAsStringInLineage.add(spmAsString);
            }
            for (EisReqPoolSpm reqPoolSpm : reqPoolSpmsOfObj){
                String spmByObjId = reqPoolSpm.getSpmByObjId();
                if(!spmsAsStringInLineage.contains(spmByObjId)){
                    reqPoolSpmIdsToDelete.add(reqPoolSpm.getId());
                }
            }

        }
        Set<Long> taskIdsNeedToUpdateStatus = new HashSet<>();
        if(!CollectionUtils.isEmpty(insertList)){
            reqSpmPoolService.insertBatch(insertList);
        }
        if(!CollectionUtils.isEmpty(reqPoolSpmIdsToDelete)){
            reqSpmPoolService.deleteByIds(reqPoolSpmIdsToDelete);
            List<EisTaskProcess> processes = taskProcessService.getByReqPoolEntityIds(ReqPoolTypeEnum.SPM_DEV, reqPoolSpmIdsToDelete);
            if(!CollectionUtils.isEmpty(processes)){
                for (EisTaskProcess process : processes) {
                    taskIdsNeedToUpdateStatus.add(process.getTaskId());
                }
                taskProcessService.deleteUnReleasedProcessesByReqPoolEntityIds(ReqPoolTypeEnum.SPM_DEV, reqPoolSpmIdsToDelete);
            }
        }
        for (Long taskId : taskIdsNeedToUpdateStatus) {
            Integer status = taskProcessService.getTaskNewStatusByProcesses(taskId);
            EisReqTask taskToBeUpdate = new EisReqTask();
            taskToBeUpdate.setId(taskId);
            taskToBeUpdate.setStatus(status);
            reqTaskService.updateById(taskToBeUpdate);
            // 同步状态到三方
            asyncHandleService.onTaskAndProcessUpdate(taskId);
        }
    }

    /**
     * 根据发布记录id获取本次发布的相关对象changeHistory
     * @param releaseId
     * @return
     */
    public List<EisObjChangeHistory> getTaskRelObjIdsByReleaseId(Long releaseId){
        EisReqTask taskQuery = new EisReqTask();
        taskQuery.setTerminalReleaseId(releaseId);
        List<EisReqTask> tasks = reqTaskService.search(taskQuery);
        Set<Long> taskIds = tasks.stream().map(e -> e.getId()).collect(Collectors.toSet());
        List<EisTaskProcess> taskProcesses = taskProcessService.getBatchByTaskIds(taskIds);
        Set<Long> reqPoolEntityIds = taskProcesses.stream().map(e -> e.getReqPoolEntityId()).collect(Collectors.toSet());
        List<EisReqPoolSpm> reqPoolSpms = reqSpmPoolService.getBatchByIds(reqPoolEntityIds);
        if(CollectionUtils.isEmpty(reqPoolSpms)){
            return new ArrayList<>();
        }
        Set<Long> reqPoolIds = reqPoolSpms.stream().map(e -> e.getReqPoolId()).collect(Collectors.toSet());
        Map<Long,Map<Long,EisObjChangeHistory>> reqPoolIdToChangeHistoriesMap = new HashMap<>();
        for (Long reqPoolId : reqPoolIds) {
            List<EisObjChangeHistory> changeHistories = objChangeHistoryService.getByReqPoolId(reqPoolId);
            Map<Long,EisObjChangeHistory> changeHistoryMap = new HashMap<>();
            for (EisObjChangeHistory changeHistory : changeHistories) {
                changeHistoryMap.put(changeHistory.getObjId(),changeHistory);
            }
            reqPoolIdToChangeHistoriesMap.put(reqPoolId,changeHistoryMap);
        }
        List<EisObjChangeHistory> objChangeHistoriesOfReleaseId = new ArrayList<>();
        for (EisReqPoolSpm reqPoolSpm : reqPoolSpms) {
            Long objId = reqPoolSpm.getObjId();
            Long reqPoolId = reqPoolSpm.getReqPoolId();
            EisObjChangeHistory objChangeHistory = reqPoolIdToChangeHistoriesMap.get(reqPoolId).get(objId);
            objChangeHistoriesOfReleaseId.add(objChangeHistory);
        }

        return objChangeHistoriesOfReleaseId;
    }

    /**
     * 需求组删除
     * @param id
     */
    @Transactional(rollbackFor = Throwable.class)
    public void deleteReqPool(Long id){
        EisReqPoolSpm spmPoolQuery = new EisReqPoolSpm();
        spmPoolQuery.setReqPoolId(id);
        List<EisReqPoolSpm> reqPoolSpms = reqSpmPoolService.search(spmPoolQuery);
        if(!CollectionUtils.isEmpty(reqPoolSpms)){
            throw new CommonException("需求组对象池不为空，无法删除");
        }
        EisReqPoolEvent eventPoolQuery = new EisReqPoolEvent();
        eventPoolQuery.setReqPoolId(id);
        List<EisReqPoolEvent> reqPoolEvents = reqEventPoolService.search(eventPoolQuery);
        if(!CollectionUtils.isEmpty(reqPoolEvents)){
            throw new CommonException("需求组事件池不为空，无法删除");
        }
        EisRequirementInfo reqInfoQuery = new EisRequirementInfo();
        reqInfoQuery.setReqPoolId(id);
        List<EisRequirementInfo> requirementInfos = requirementInfoService.search(reqInfoQuery);
        if(!CollectionUtils.isEmpty(requirementInfos)){
            Set<Long> reqIds = new HashSet<>();
            for (EisRequirementInfo requirementInfo : requirementInfos) {
                reqIds.add(requirementInfo.getId());
            }
            requirementInfoService.deleteByIds(reqIds);
            List<EisReqTask> reqTasks = reqTaskService.getByReqIds(reqIds);
            Set<Long> taskIds = reqTasks.stream().map(e -> e.getId()).collect(Collectors.toSet());
            reqTaskService.deleteByIds(taskIds);
        }
        reqPoolBasicService.deleteById(id);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void deleteRequirement(Long id){
        EisRequirementInfo reqInfoQuery = new EisRequirementInfo();
        reqInfoQuery.setId(id);
        Set<Long> reqIds = Sets.newHashSet(id);
        requirementInfoService.deleteByIds(reqIds);
        List<EisReqTask> reqTasks = reqTaskService.getByReqIds(reqIds);
        Set<Long> taskIds = reqTasks.stream().map(e -> e.getId()).collect(Collectors.toSet());
        if(!CollectionUtils.isEmpty(taskProcessService.getBatchByTaskIds(taskIds))){
            throw new CommonException("存在任务已被指派，无法删除当前需求");
        }
        reqTaskService.deleteByIds(taskIds);
    }

    private ChangeTuple getChangeTuple(EisObjTerminalTracker tracker, Long baseReleasedId, Map<Long, List<Long>> bridgeUpTerminalIdMapping, Map<Long, LinageGraph> parentTerminalLinageGraphMap) {
        ChangeTuple changeTuple = new ChangeTuple();
        LinageGraph baseLineageGraph = lineageHelper.genReleasedLinageGraph(baseReleasedId);
        List<List<Long>> spmsOfCurrentObjInBaseLineage = lineageHelper.getObjIdSpms(baseLineageGraph,tracker.getObjId());
        spmsOfCurrentObjInBaseLineage = lineageHelper.updateBridgeParent(spmsOfCurrentObjInBaseLineage, bridgeUpTerminalIdMapping, parentTerminalLinageGraphMap);
        LinageGraph reqLinageGraph = lineageHelper.genReqLinageGraph(baseReleasedId,tracker.getTerminalId(),tracker.getReqPoolId());
        List<List<Long>> spmsOfCurrentObjInReqLineage = lineageHelper.getObjIdSpms(reqLinageGraph,tracker.getObjId());
        spmsOfCurrentObjInReqLineage = lineageHelper.updateBridgeParent(spmsOfCurrentObjInReqLineage, bridgeUpTerminalIdMapping, parentTerminalLinageGraphMap);
        RelationDiff relationDiff = diffHelper.getRelationDiffs(tracker);
        Boolean isPubParamPackageChanged = diffHelper.isPubParamPackageChanged(tracker.getPreTrackerId(),tracker.getId());
        List<ParamDiff> prvParamsDiff = diffHelper.getParamDiffs(tracker.getObjId(), tracker.getPreTrackerId(),tracker.getId());
        List<EventDiff> eventsDiff = diffHelper.getEventDiffs(tracker.getPreTrackerId(),tracker.getId());
        List<List<Long>> newSpms = getNewSpms(spmsOfCurrentObjInReqLineage,relationDiff);
        List<List<Long>> deletedSpms = getDeletedSpms(spmsOfCurrentObjInBaseLineage,relationDiff);
        changeTuple.setIsPubParamPackageChanged(isPubParamPackageChanged);
        changeTuple.setIsPrvParamChanged(!CollectionUtils.isEmpty(prvParamsDiff));
        changeTuple.setIsEventChanged(!CollectionUtils.isEmpty(eventsDiff));
        changeTuple.setNewSpms(newSpms);
        changeTuple.setDeletedSpms(deletedSpms);
        return changeTuple;
    }

    /**
     * 获取当前变更时创建的新spm
     * @param objSpmsInReqLineage
     * @param relationDiff
     * @return
     */
    private List<List<Long>> getNewSpms(List<List<Long>> objSpmsInReqLineage,RelationDiff relationDiff){
        List<List<Long>> newSpms = new ArrayList<>();
        Set<Long> newParents = relationDiff.getNewParents();
        for (List<Long> spm : objSpmsInReqLineage) {
            if(spm.size() > 1 && newParents.contains(spm.get(1))){
                newSpms.add(spm);
            }
        }
        return newSpms;
    }

    /**
     * 获取当前变更时删除的spm
     * @param objSpmsInBaseLineage
     * @param relationDiff
     * @return
     */
    private List<List<Long>> getDeletedSpms(List<List<Long>> objSpmsInBaseLineage,RelationDiff relationDiff){
        List<List<Long>> deletedSpms = new ArrayList<>();
        Set<Long> deletedParents = relationDiff.getDeletedParents();
        for (List<Long> spm : objSpmsInBaseLineage) {
            if(spm.size() > 1 && deletedParents.contains(spm.get(1))){
                deletedSpms.add(spm);
            }
        }
        return deletedSpms;
    }

    /**
     * @return 合并是否成功，如果返回false，后续需要手动合并冲突
     */
    public boolean mergeObj(MergeObjReqDTO mergeObjReqDTO) {
        Long objId = mergeObjReqDTO.getObjId();
        Long reqPoolObjHistoryId = mergeObjReqDTO.getObjHistoryIdOfReqPool();
        Long reqPoolId = mergeObjReqDTO.getReqPoolId();
        Long targetReleaseId = mergeObjReqDTO.getTargetReleaseId();

        // 若对象已上线，则无需合并
        EisTaskProcess taskProcessQuery = new EisTaskProcess();
        taskProcessQuery.setObjId(objId);
        taskProcessQuery.setReqPoolId(reqPoolId);
        List<EisTaskProcess> taskProcesses = taskProcessService.search(taskProcessQuery);
        for (EisTaskProcess taskProcess : taskProcesses) {
            if (ProcessStatusEnum.ONLINE.getState().equals(taskProcess.getStatus())) {
                return true;
            }
        }

        EisObjChangeHistory currentObjectChangeHistory = objChangeHistoryService.getById(reqPoolObjHistoryId);
        if (currentObjectChangeHistory == null) {
            // 当前需求池没这个对象，不需要合并
            return true;
        }
        if (ConflictStatusEnum.RESOLVED.getStatus().equals(currentObjectChangeHistory.getConflictStatus())) {
            // 当前对象是解决过冲突的，不需要再合并，但是为了下次能合并，需要把其状态删除
            objChangeHistoryService.updateConflictStatus(reqPoolId, Collections.singleton(currentObjectChangeHistory.getObjId()), ConflictStatusEnum.NON.getStatus());
            return true;
        }

        // 若已冲突，则不需要合并，还是冲突态
        boolean isInMergeConflict = ConflictStatusEnum.fromStatus(currentObjectChangeHistory.getConflictStatus()) == ConflictStatusEnum.MERGE_CONFLICT;
        if (isInMergeConflict) {
            return true;
        }

        // 目标tracker
        EisObjTerminalTracker targetTracker = getReleaseTracker(targetReleaseId, objId);
        if (targetTracker == null) {
            // 不应出现的异常情况，若出现，手动解决
            return false;
        }
        Long appId = targetTracker.getAppId();
        Long currentTerminalId = targetTracker.getTerminalId();

        EisReqPoolRelBaseRelease baseLineOfCurrentTerminal = reqPoolRelBaseService.getCurrentUse(reqPoolId, currentTerminalId);
        if (baseLineOfCurrentTerminal == null) {
            // 不应出现的异常情况，若出现，手动解决
            return false;
        }

        // 基线tracker
        EisObjTerminalTracker baseLineTracker = getReleaseTracker(baseLineOfCurrentTerminal.getBaseReleaseId(), objId);
        if (baseLineTracker == null) {
            // 当前需求组基线下没有该对象，说明对象是新建的。目标基线也有该对象，属于在2个需求组同时新建对象，一个先上线的情况。这种情况不可能发生。
            // 如果真出现：手动解决
            return false;
        }

        // 需求池tracker
        List<EisObjTerminalTracker> reqPoolTrackersOfAllTerminals = getReqPoolTracker(reqPoolId, objId, reqPoolObjHistoryId);
        if (CollectionUtils.isEmpty(reqPoolTrackersOfAllTerminals)) {
            // 当前需求组内没有该对象，属于异常情况，手动解决
            return false;
        }

        Long targetHistoryId = targetTracker.getObjHistoryId();
        EisObjChangeHistory targetHistory = objChangeHistoryService.getById(targetHistoryId);
        if (targetHistory == null) {
            // 不应出现的异常情况，若出现，手动解决
            return false;
        }

        Long baseLineHistoryId = baseLineTracker.getObjHistoryId();
        EisObjChangeHistory baseLineHistory = objChangeHistoryService.getById(baseLineHistoryId);
        if (baseLineHistory == null) {
            // 不应出现的异常情况，若出现，手动解决
            return false;
        }

        // 双端一致的话，变更一端时，也要变更其他端
        boolean currentConsistency = Boolean.TRUE.equals(currentObjectChangeHistory.getConsistency());
        boolean mergedConsistency = mergeBoolean(Boolean.TRUE.equals(baseLineHistory.getConsistency()),
                Boolean.TRUE.equals(targetHistory.getConsistency()),
                currentConsistency);

        // 更新双端一致
        if (mergedConsistency != currentConsistency) {
            updateConsistency(mergedConsistency, currentObjectChangeHistory);
        }

        // 需求池tracker
        EisObjTerminalTracker reqPoolTrackerOfCurrentTerminal = reqPoolTrackersOfAllTerminals.stream().filter(o -> currentTerminalId.equals(o.getTerminalId())).findFirst().orElse(null);
        if (reqPoolTrackerOfCurrentTerminal == null) {
            // 当前没有该对象，无需合并
            return true;
        }

        // 当前端下，目标对对比需求池基线变化
        TrackerDiffDTO baseLineDiff = diffHelper.getReleaseTrackerDiff(objId, baseLineOfCurrentTerminal.getBaseReleaseId(), baseLineTracker.getId(), targetReleaseId, targetTracker.getId());
        // 当前端下，需求组内变化
        TrackerDiffDTO reqPoolDiff = diffHelper.getReqPoolTrackerDiff(objId, baseLineOfCurrentTerminal.getBaseReleaseId(), baseLineTracker.getId(), reqPoolTrackerOfCurrentTerminal.getId(), reqPoolId, currentTerminalId);
        TrackerDiffDTO mergedDiff = mergeDiff(baseLineDiff, reqPoolDiff);
        if (mergedDiff == null) {
            // 合并失败需手动解决
            return false;
        }

        // 进行合并
        try {
            List<ObjectTrackerInfoDTO> objectTrackerInfoListOfBaseLine = objectHelper.getObjTrackersInfo(objId, baseLineTracker.getObjHistoryId(), false);
            List<ObjectTrackerInfoDTO> objectTrackerInfoListOfReqPool = objectHelper.getObjTrackersInfo(objId, reqPoolTrackerOfCurrentTerminal.getObjHistoryId(), false);
            List<ObjectTrackerEditParam> objectTrackerEditParams = applyDiff(objectTrackerInfoListOfReqPool, objectTrackerInfoListOfBaseLine, mergedDiff, currentTerminalId, mergedConsistency);
            if (objectTrackerEditParams == null) {
                // 不需要改动的情况，视为改动成功
                return true;
            }
            log.info("合并基线后更新对象 reqPoolId={} objId={} objHistoryId={} baseLineDiff={} reqPoolDiff={} mergedDiff={} originTracker={} newTracker={}",
                    reqPoolId, objId, reqPoolObjHistoryId,
                    JsonUtils.toJson(baseLineDiff), JsonUtils.toJson(reqPoolDiff), JsonUtils.toJson(mergedDiff),
                    JsonUtils.toJson(objectTrackerInfoListOfReqPool), JsonUtils.toJson(objectTrackerEditParams));
            updateObject(appId, objectTrackerEditParams, objId, reqPoolObjHistoryId, reqPoolId);
        } catch (Exception e) {
            log.error("合并对象失败，转为手动合并", e);
            return false;
        }
        return true;
    }

    private boolean mergeBoolean(boolean baseLine, boolean target, boolean reqPool) {
        // 结果一样，那就是一样，不冲突
        if (target == reqPool) {
            return target;
        }
        // 结果不一样，那看谁改了，谁改了就以谁的为准
        boolean isReqPoolChange = reqPool != baseLine;
        return isReqPoolChange ? reqPool : target;
    }

    /**
     * 合并变更
     * @return 若合并失败，返回null；若合并成功，返回合并的结果
     */
    private TrackerDiffDTO mergeDiff(TrackerDiffDTO baseLineDiff, TrackerDiffDTO reqPoolDiff) {
        if (!DiffHelper.isAnyChange(baseLineDiff) && !DiffHelper.isAnyChange(reqPoolDiff)) {
            reqPoolDiff.setAcceptReqPool(true);
            return reqPoolDiff;
        }
        try {
            TrackerDiffDTO result = new TrackerDiffDTO();
            mergePubParamPackageIdDiff(result, baseLineDiff, reqPoolDiff);
            mergeRelationDiff(result, baseLineDiff, reqPoolDiff);
            mergeParamDiff(result, baseLineDiff, reqPoolDiff);
            mergeEventDiff(result, baseLineDiff, reqPoolDiff);
            return result;
        } catch (Exception e) {
            log.info("mergeDiff conflict, baseLineDiff={} reqPoolDiff={} msg={}", JsonUtils.toJson(baseLineDiff), JsonUtils.toJson(reqPoolDiff), e.getMessage());
            log.debug("", e);
            return null;
        }
    }

    private void mergeParamDiff(TrackerDiffDTO result, TrackerDiffDTO diffA, TrackerDiffDTO diffB) {
        // 检查下参数
        List<ParamDiff> paramDiffsA = diffA.getParamDiffs();
        List<ParamDiff> paramDiffsB = diffB.getParamDiffs();
        if (CollectionUtils.isNotEmpty(paramDiffsA)) {
            paramDiffsA.forEach(p -> {
                if (ChangeTypeEnum.CREATE.getChangeType().equals(p.getChangeType()) && p.getNewData() == null) {
                    throw new CommonException("自动合并失败，diff.newData为空. paramDiffsA=" + JsonUtils.toJson(paramDiffsA));
                }
            });
        }
        if (CollectionUtils.isNotEmpty(paramDiffsB)) {
            paramDiffsB.forEach(p -> {
                if (ChangeTypeEnum.CREATE.getChangeType().equals(p.getChangeType()) && p.getNewData() == null) {
                    throw new CommonException("自动合并失败，diff.newData为空. paramDiffsB=" + JsonUtils.toJson(paramDiffsB));
                }
            });
        }
//        // 仅有一者改变，则以另一者为准
//        if (CollectionUtils.isEmpty(paramDiffsA)) {
//            result.setParamDiffs(paramDiffsB);
//            return;
//        }
//        // 仅有一者改变，则以另一者为准
//        if (CollectionUtils.isEmpty(paramDiffsB)) {
//            result.setParamDiffs(paramDiffsA);
//            return;
//        }

        // 两者都变，则需要合并
        Map<Long, List<ParamDiff>> diffsAGroupByObjId = new HashMap<>();
        paramDiffsA.forEach(paramDiff -> {
            List<ParamDiff> paramDiffsOfObjId = diffsAGroupByObjId.computeIfAbsent(paramDiff.getObjId(), k -> new ArrayList<>());
            paramDiffsOfObjId.add(paramDiff);
        });
        Map<Long, List<ParamDiff>> diffsBGroupByObjId = new HashMap<>();
        paramDiffsB.forEach(paramDiff -> {
            List<ParamDiff> paramDiffsOfEventId = diffsBGroupByObjId.computeIfAbsent(paramDiff.getObjId(), k -> new ArrayList<>());
            paramDiffsOfEventId.add(paramDiff);
        });
        for(Long objId : diffsAGroupByObjId.keySet()){
            if(!diffsBGroupByObjId.containsKey(objId)){
                continue;
            }
            // 同一对象,参数变更数量不一致，报冲突
            if(diffsAGroupByObjId.get(objId).size() != diffsBGroupByObjId.get(objId).size()){
                throw new CommonException("自动合并失败，param变更冲突. paramA=" + JsonUtils.toJson(paramDiffsA) + "paramB=" + JsonUtils.toJson(paramDiffsB));
            }
            // 同一对象，参数变更结果不一致，报冲突
            List<ParamDiff> paramADiffs = diffsAGroupByObjId.get(objId);
            List<ParamDiff> paramBDiffs = diffsBGroupByObjId.get(objId);
            Set<Long> diffAIds = paramADiffs.stream().map(ParamDiff::getParamId).collect(Collectors.toSet());
            Set<Long> diffBIds = paramBDiffs.stream().map(ParamDiff::getParamId).collect(Collectors.toSet());
            if(!diffAIds.containsAll(diffBIds)){
                throw new CommonException("自动合并失败，param变更冲突. paramA=" + JsonUtils.toJson(paramDiffsA) + "paramB=" + JsonUtils.toJson(paramDiffsB));
            }
            Map<Long, ParamDiff> paramDiffAMap = paramADiffs.stream().collect(Collectors.toMap(ParamDiff::getParamId, Function.identity()));
            for(ParamDiff paramDiff : paramBDiffs){
                ParamDiff paramDiffA = paramDiffAMap.get(paramDiff.getParamId());
                //参数变更不是同一个参数，报冲突
                if(!paramDiffAMap.containsKey(paramDiff.getParamId())){
                    throw new CommonException("自动合并失败，param变更冲突. paramA=" + JsonUtils.toJson(paramDiffsA) + "paramB=" + JsonUtils.toJson(paramDiffsB));
                }
                //参数变更类型不一样，报冲突
                if(!paramDiffA.getChangeType().equals(paramDiff.getChangeType())){
                    throw new CommonException("自动合并失败，param变更类型冲突. param=" + JsonUtils.toJson(paramDiff));
                }
                //参数变更值不一样，报冲突
                if(paramDiffA.getNewParamValueIds().size() != paramDiff.getNewParamValueIds().size()){
                    throw new CommonException("自动合并失败，param变更内容冲突. param=" + JsonUtils.toJson(paramDiff));
                }
                if(!paramDiffA.getNewParamValueIds().containsAll(paramDiff.getNewParamValueIds())){
                    throw new CommonException("自动合并失败，param变更内容冲突. param=" + JsonUtils.toJson(paramDiff));
                }
            }
        }

//        // 两者都变，则需要合并
//        Map<Long, List<ParamDiff>> diffsGroupById = new HashMap<>();
//        paramDiffsA.forEach(paramDiff -> {
//            List<ParamDiff> paramDiffsOfEventId = diffsGroupById.computeIfAbsent(paramDiff.getParamId(), k -> new ArrayList<>());
//            paramDiffsOfEventId.add(paramDiff);
//        });
//        paramDiffsB.forEach(paramDiff -> {
//            List<ParamDiff> paramDiffsOfEventId = diffsGroupById.computeIfAbsent(paramDiff.getParamId(), k -> new ArrayList<>());
//            paramDiffsOfEventId.add(paramDiff);
//        });
//        // 逐个paramId合并
//        List<ParamDiff> mergedParamDiffs = new ArrayList<>();
//        diffsGroupById.forEach((paramId, paramIdDiffsOfParamId) -> {
//            // 处理删除：删100次和删1次没有区别，所以只需要第一个删除操作
//            List<ParamDiff> deleteActions = paramIdDiffsOfParamId.stream().filter(o -> ChangeTypeEnum.DELETE.getChangeType().equals(o.getChangeType())).collect(Collectors.toList());
//            if (CollectionUtils.isNotEmpty(deleteActions)) {
//                mergedParamDiffs.add(deleteActions.get(0));
//            }
//
//            // 处理新增
//            List<ParamDiff> addActions = paramIdDiffsOfParamId.stream().filter(o -> ChangeTypeEnum.CREATE.getChangeType().equals(o.getChangeType())).collect(Collectors.toList());
//            if (CollectionUtils.isEmpty(addActions)) {
//                return;
//            }
//            // 如果有2个删除，1个新增，这种情况是一个删，一个改，这种属于冲突情况
//            if (deleteActions.size() > 1 && addActions.size() == 1) {
//                throw new CommonException("合并DIFF失败，参数" + paramId + "同时存在删除和修改，需要人工确认");
//            }
//            // 都修改了该参数
//            ParamDiff mergedAddAction = null;
//            for (ParamDiff addAction : addActions) {
//                if (mergedAddAction == null) {
//                    mergedAddAction = addAction;
//                    continue;
//                }
//                // 合并
//                mergeSingleParamAddAction(mergedAddAction, addAction);
//            }
//            mergedParamDiffs.add(mergedAddAction);
//        });
//        result.setParamDiffs(mergedParamDiffs);
    }

    private void mergeSingleParamAddAction(ParamDiff to, ParamDiff from) {
        // 当前已经新增、删除的
        Set<Long> add = new HashSet<>(Sets.difference(to.getNewParamValueIds(), to.getOldParamValueIds()));
        Set<Long> delete = new HashSet<>(Sets.difference(to.getOldParamValueIds(), to.getNewParamValueIds()));

        // 新来的新增、删除
        Set<Long> newAdd = Sets.difference(from.getNewParamValueIds(), from.getOldParamValueIds());
        Set<Long> newDelete = Sets.difference(from.getOldParamValueIds(), from.getNewParamValueIds());

        // 变更全部合并
        add.addAll(newAdd);
        delete.addAll(newDelete);

        // 变更应用到old上
        Set<Long> result = new HashSet<>(to.getOldParamValueIds());
        result.removeIf(delete::contains);
        result.addAll(add);
        to.setNewParamValueIds(result);
    }

    private void mergeEventDiff(TrackerDiffDTO result, TrackerDiffDTO diffA, TrackerDiffDTO diffB) {
        List<EventDiff> eventDiffsA = diffA.getEventDiffs();
        List<EventDiff> eventDiffsB = diffB.getEventDiffs();
        // 仅有一者改变，则以另一者为准
        if (CollectionUtils.isEmpty(eventDiffsA)) {
            result.setEventDiffs(eventDiffsB);
            return;
        }
        // 仅有一者改变，则以另一者为准
        if (CollectionUtils.isEmpty(eventDiffsB)) {
            result.setEventDiffs(eventDiffsA);
            return;
        }
        // 两者都变，则需要合并
        Map<Long, List<EventDiff>> diffsGroupById = new HashMap<>();
        eventDiffsA.forEach(eventDiff -> {
            List<EventDiff> eventDiffsOfEventId = diffsGroupById.computeIfAbsent(eventDiff.getEventId(), k -> new ArrayList<>());
            eventDiffsOfEventId.add(eventDiff);
        });
        eventDiffsB.forEach(eventDiff -> {
            List<EventDiff> eventDiffsOfEventId = diffsGroupById.computeIfAbsent(eventDiff.getEventId(), k -> new ArrayList<>());
            eventDiffsOfEventId.add(eventDiff);
        });
        // 逐个eventId合并
        List<EventDiff> mergedEventDiffs = new ArrayList<>();
        diffsGroupById.forEach((eventId, eventDiffsOfEventId) -> {
            // 处理删除：删100次和删1次没有区别，所以只需要第一个删除操作
            List<EventDiff> deleteActions = eventDiffsOfEventId.stream().filter(o -> ChangeTypeEnum.DELETE.getChangeType().equals(o.getChangeType())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(deleteActions)) {
                mergedEventDiffs.add(deleteActions.get(0));
            }
            // 处理新增
            List<EventDiff> addActions = eventDiffsOfEventId.stream().filter(o -> ChangeTypeEnum.CREATE.getChangeType().equals(o.getChangeType())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(addActions)) {
                return;
            }
            // 如果有2个删除，1个新增，这种情况是一个删，一个改，这种属于冲突情况
            if (deleteActions.size() > 1 && addActions.size() == 1) {
                throw new CommonException("合并DIFF失败，事件" + eventId + "同时存在删除和修改，需要人工确认");
            }
            // 都修改了该事件，如果修改后结果都一样，那就可以合并，否则就属于合并冲突
            Set<Long> newEventParamVersionIdSet = new HashSet<>();
            for (EventDiff eventDiff : addActions) {
                newEventParamVersionIdSet.add(eventDiff.getNewEventParamVersionId() == null ? -1L : eventDiff.getNewEventParamVersionId());
            }
            if (newEventParamVersionIdSet.size() > 1) {
                throw new CommonException("合并DIFF失败，事件" + eventId + "参数版本不一致，需要人工确认");
            }
            // 均为同样的修改，取第一个即可
            mergedEventDiffs.add(addActions.get(0));
        });
        result.setEventDiffs(mergedEventDiffs);
    }

    private void mergeRelationDiff(TrackerDiffDTO result, TrackerDiffDTO diffA, TrackerDiffDTO diffB) {
        RelationDiff mergedRelationDiff = new RelationDiff();
        mergedRelationDiff.setNewParents(new HashSet<>());
        mergedRelationDiff.setDeletedParents(new HashSet<>());
        mergedRelationDiff.getNewParents().addAll(diffA.getRelationDiff().getNewParents());
        mergedRelationDiff.getNewParents().addAll(diffB.getRelationDiff().getNewParents());
        mergedRelationDiff.getDeletedParents().addAll(diffA.getRelationDiff().getDeletedParents());
        mergedRelationDiff.getDeletedParents().addAll(diffB.getRelationDiff().getDeletedParents());
        result.setRelationDiff(mergedRelationDiff);
    }

    private void mergePubParamPackageIdDiff(TrackerDiffDTO result, TrackerDiffDTO diffA, TrackerDiffDTO diffB) {
        // 仅有一者改变，则以另一者为准
        if (!diffA.isPubParamPackageChange()) {
            result.setPubParamPackageChange(diffB.isPubParamPackageChange());
            result.setPubParamPackageId(diffB.getPubParamPackageId());
            return;
        }
        // 仅有一者改变，则以另一者为准
        if (!diffB.isPubParamPackageChange()) {
            result.setPubParamPackageChange(diffA.isPubParamPackageChange());
            result.setPubParamPackageId(diffA.getPubParamPackageId());
            return;
        }
        // 两者都变，则需要校验是否一致
        if (Objects.equals(diffA.getPubParamPackageId(), diffB.getPubParamPackageId())) {
            result.setPubParamPackageChange(diffA.isPubParamPackageChange());
            result.setPubParamPackageId(diffA.getPubParamPackageId());
            return;
        }
        throw new CommonException("合并DIFF失败，公参包参数不一致，需要人工确认");
    }

    private EisObjTerminalTracker getReleaseTracker(Long releaseId, Long objId) {
        EisAllTrackerRelease trackerRelease = allTrackerReleaseService.getByReleaseIdAndObjId(releaseId, objId);
        if (trackerRelease == null) {
            return null;
        }
        return objTerminalTrackerService.getById(trackerRelease.getTrackerId());
    }

    private List<EisObjTerminalTracker> getReqPoolTracker(Long reqPoolId, Long objId, Long objHistoryId) {

        EisObjTerminalTracker queryTracker = new EisObjTerminalTracker();
        queryTracker.setReqPoolId(reqPoolId);
        queryTracker.setObjHistoryId(objHistoryId);
        queryTracker.setObjId(objId);
        return objTerminalTrackerService.search(queryTracker);
    }

    private void updateConsistency(boolean consistency, EisObjChangeHistory objChangeHistory) {
        objChangeHistory.setConsistency(consistency);
        objChangeHistoryService.update(objChangeHistory);
    }

    private List<ObjectTrackerEditParam> applyDiff(List<ObjectTrackerInfoDTO> trackerInfoListOfReqPool, List<ObjectTrackerInfoDTO> trackerInfoListOfBaseLine, TrackerDiffDTO diff, Long currentTerminalId, boolean consistencyOfReqPool) {
        if (diff.isAcceptReqPool()) {
            // 不需要更新
            return null;
        }
        List<ObjectTrackerEditParam> trackerParam = trackerInfoListOfBaseLine.stream().map(this::convertToEditParam).collect(Collectors.toList());
        log.info("applyDiff before={}", JsonUtils.toJson(trackerParam));
        Map<Long, Long> reqPoolTrackerIdMap = new HashMap<>();
        Map<Long, Boolean> isConsistencyTerminalMap = new HashMap<>();
        trackerInfoListOfReqPool.forEach(t -> {
            reqPoolTrackerIdMap.put(t.getTerminal().getId(), t.getId());
            // 有双端同步的端
            isConsistencyTerminalMap.put(t.getTerminal().getId(), TerminalTypeEnum.APP.getType().equals(t.getTerminal().getTerminalType()));
        });

        if (!diff.isAcceptBase()) { // 如果是acceptBase，则不需要更新trackerParam了
            trackerParam.forEach(t -> {
                boolean isConsistencyTerminal = Optional.ofNullable(isConsistencyTerminalMap.get(t.getTerminalId())).orElse(false);
                boolean needApply = needApplyDiff(t, currentTerminalId, consistencyOfReqPool, isConsistencyTerminal);
                if (!needApply) {
                    return;
                }
                // 对trackerParam应用diff的修改
                doApplyDiff(t, diff);
            });
        }


        // 因为是编辑需求组内tracker，因此trackerId要使用需求组内的
        List<ObjectTrackerEditParam> result = new ArrayList<>();
        trackerParam.forEach(t -> {
            Long targetTrackerId = reqPoolTrackerIdMap.get(t.getTerminalId());
            if (targetTrackerId == null) {
                // 说明当前需求组根本就没这个端，不需要改
                return;
            }
            t.setId(targetTrackerId);
            result.add(t);
        });
        log.info("applyDiff after={}", JsonUtils.toJson(result));
        return result;
    }

    public void doApplyDiff(ObjectTrackerEditParam trackerEditParam, TrackerDiffDTO diff) {
        if (trackerEditParam == null) {
            return;
        }
        // 处理父对象
        RelationDiff relationDiff = diff.getRelationDiff();
        if (relationDiff != null && CollectionUtils.isNotEmpty(relationDiff.getDeletedParents())) {
            trackerEditParam.getParentObjs().removeAll(relationDiff.getDeletedParents());
        }
        if (relationDiff != null && CollectionUtils.isNotEmpty(relationDiff.getNewParents())) {
            trackerEditParam.getParentObjs().addAll(relationDiff.getNewParents());
        }

        // 处理公参包变化
        if (diff.isPubParamPackageChange() && diff.getPubParamPackageId() != null) {
            trackerEditParam.setPubParamPackageId(diff.getPubParamPackageId());
        }

        // 处理事件变化，修改是先删后增，因此先应用删除，再应用新增
        List<EventDiff> eventDiffs = diff.getEventDiffs();
        if (CollectionUtils.isNotEmpty(eventDiffs)) {
            List<EventDiff> deleteEventActions = eventDiffs.stream().filter(o -> ChangeTypeEnum.DELETE.getChangeType().equals(o.getChangeType())).collect(Collectors.toList());
            List<EventDiff> addEventActions = eventDiffs.stream().filter(o -> ChangeTypeEnum.CREATE.getChangeType().equals(o.getChangeType())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(deleteEventActions)) {
                deleteEventActions.forEach(deleteEventAction -> {
                    Long eventId = deleteEventAction.getEventId();
                    trackerEditParam.getEventIds().removeIf(o -> o == null || o.equals(eventId));
                    trackerEditParam.getEventParamVersionIdMap().remove(eventId);
                });
            }
            if (CollectionUtils.isNotEmpty(addEventActions)) {
                addEventActions.forEach(addEventAction -> {
                    Long eventId = addEventAction.getEventId();
                    trackerEditParam.getEventIds().add(eventId);
                    trackerEditParam.getEventParamVersionIdMap().put(eventId, addEventAction.getNewEventParamVersionId());
                });
            }
            if (CollectionUtils.isEmpty(trackerEditParam.getEventIds())) {
                throw new CommonException("合并失败，合并后事件为空，需要人工解决冲突");
            }
        }

        // 处理参数变化
        List<ParamDiff> paramDiffs = diff.getParamDiffs();
        if (CollectionUtils.isNotEmpty(paramDiffs)) {
            List<ParamDiff> deleteParamActions = paramDiffs.stream().filter(o -> ChangeTypeEnum.DELETE.getChangeType().equals(o.getChangeType())).collect(Collectors.toList());
            List<ParamDiff> addParamActions = paramDiffs.stream().filter(o -> ChangeTypeEnum.CREATE.getChangeType().equals(o.getChangeType())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(deleteParamActions)) {
                deleteParamActions.forEach(deleteParamAction -> {
                    Long paramId = deleteParamAction.getParamId();
                    trackerEditParam.getParamBinds().removeIf(o -> o == null || o.getParamId().equals(paramId));
                });
            }
            if (CollectionUtils.isNotEmpty(addParamActions)) {
                addParamActions.forEach(addParamAction -> {
                    ParamBindItermParam p = new ParamBindItermParam();
                    p.setValues(new ArrayList<>(addParamAction.getNewParamValueIds()));
                    p.setParamId(addParamAction.getParamId());
                    p.setDescription(addParamAction.getNewData().getDescription());
                    p.setMust(addParamAction.getNewData().getMust());
                    p.setNotEmpty(addParamAction.getNewData().getNotEmpty());
                    p.setNeedTest(addParamAction.getNewData().getNeedTest());
                    p.setIsEncode(addParamAction.getNewData().getIsEncode());
                    trackerEditParam.getParamBinds().add(p);
                });
            }
        }
    }

    private boolean needApplyDiff(ObjectTrackerEditParam t, Long currentTerminalId, boolean consistencyOfReqPool, boolean isConsistencyTerminal) {
        // 是当前端
        if (t.getTerminalId().equals(currentTerminalId)) {
            return true;
        }
        // 不是当前端，不需要双端同步
        if (!consistencyOfReqPool) {
            return false;
        }
        return isConsistencyTerminal;
    }

    private ObjectTrackerEditParam convertToEditParam(ObjectTrackerInfoDTO dto) {
        ObjectTrackerEditParam result = new ObjectTrackerEditParam();
        result.setId(dto.getId());
        result.setTerminalId(dto.getTerminal().getId());
        result.setPubParamPackageId(dto.getPubParamPackageId());
        result.setEventParamVersionIdMap(dto.getEventParamVersionIdMap());
        result.setEventIds(CollectionUtils.isEmpty(dto.getEvents()) ? new ArrayList<>(0)
                : dto.getEvents().stream().map(EventSimpleDTO::getId).collect(Collectors.toList()));
        result.setParentObjs(CollectionUtils.isEmpty(dto.getParentObjects()) ? new ArrayList<>(0)
                : dto.getParentObjects().stream().map(ObjectBasicDTO::getId).collect(Collectors.toList()));
        result.setParamBinds(CollectionUtils.isEmpty(dto.getPrivateParam()) ? new ArrayList<>(0)
                : dto.getPrivateParam().stream().map(paramBindItemDTO -> {
            ParamBindItermParam p = new ParamBindItermParam();
            p.setValues(paramBindItemDTO.getSelectedValues());
            p.setParamId(paramBindItemDTO.getId());
            p.setDescription(paramBindItemDTO.getDescription());
            p.setMust(paramBindItemDTO.getMust());
            p.setNotEmpty(paramBindItemDTO.getNotEmpty());
            p.setNeedTest(paramBindItemDTO.getNeedTest());
            p.setIsEncode(paramBindItemDTO.getIsEncode());
            return p;
        }).collect(Collectors.toList()));
        return result;
    }

    private void updateObject(Long appId, List<ObjectTrackerEditParam> trackerEditParams, Long objId, Long objHistoryId, Long reqPoolId) {
        if (CollectionUtils.isEmpty(trackerEditParams)) {
            return;
        }
        Set<Long> trackerIdsBeforeEdit = new HashSet<>();
        trackerEditParams.forEach(t -> {
            if (t != null && t.getId() != null) {
                trackerIdsBeforeEdit.add(t.getId());
            }
        });
        // 因为存在"双端一致"情况下，安卓上线，iPhone还未上线的情况，此时安卓基线更新，iPhone基线还是老的，双端一致处理后iPhone的父对象可能在iPhone基线不存在
        // 因此这里先不校验了
//        trackerEditParams.forEach(p -> objectHelper.checkParentExist(appId, p.getParentObjs(), p.getTerminalId(), reqPoolId));
        List<Long> newTrackerIds = objectHelper.editObjectTrackerInfo(objId, objHistoryId, reqPoolId, trackerEditParams);

        // 3. 更新对象 spm需求关联池 信息
        List<UpdateSpmPoolParam> updateSpmPoolParams = new ArrayList<>();
        newTrackerIds.forEach(newTrackerId -> {
            UpdateSpmPoolParam p = new UpdateSpmPoolParam();
            p.setTrackerId(newTrackerId);
            if (trackerIdsBeforeEdit.contains(newTrackerId)) {
                p.setOperationTypeEnum(OperationTypeEnum.CHANGE);
                p.setEdit(true);
            } else {
                // 这种是编辑时新增，走新增流程
                p.setOperationTypeEnum(OperationTypeEnum.CREATE);
                p.setEdit(false);
            }
            updateSpmPoolParams.add(p);
        });
        updateSpmPoolNew(reqPoolId, updateSpmPoolParams, true);
    }
}
