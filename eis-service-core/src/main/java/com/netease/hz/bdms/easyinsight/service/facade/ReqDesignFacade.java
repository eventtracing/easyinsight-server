package com.netease.hz.bdms.easyinsight.service.facade;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.netease.hz.bdms.easyinsight.common.aop.MethodLog;
import com.netease.hz.bdms.easyinsight.common.bo.lineage.LinageGraph;
import com.netease.hz.bdms.easyinsight.common.bo.lineage.LineageForest;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.dto.image.ImageRelationDTO;
import com.netease.hz.bdms.easyinsight.common.dto.terminal.TerminalSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.enums.*;
import com.netease.hz.bdms.easyinsight.common.exception.CommonException;
import com.netease.hz.bdms.easyinsight.common.query.Search;
import com.netease.hz.bdms.easyinsight.common.util.CommonUtil;
import com.netease.hz.bdms.easyinsight.common.util.JsonUtils;
import com.netease.hz.bdms.easyinsight.common.vo.requirement.*;
import com.netease.hz.bdms.easyinsight.common.vo.task.*;
import com.netease.hz.bdms.easyinsight.dao.model.*;
import com.netease.hz.bdms.easyinsight.service.helper.LineageHelper;
import com.netease.hz.bdms.easyinsight.service.helper.MergeConflictHelper;
import com.netease.hz.bdms.easyinsight.service.helper.ObjectHelper;
import com.netease.hz.bdms.easyinsight.service.helper.RequirementPoolHelper;
import com.netease.hz.bdms.easyinsight.common.dto.rebase.RebaseDTO;
import com.netease.hz.bdms.easyinsight.common.dto.rebase.TerminalBaseDTO;
import com.netease.hz.bdms.easyinsight.service.service.*;
import com.netease.hz.bdms.easyinsight.service.service.impl.AppRelationService;
import com.netease.hz.bdms.easyinsight.service.service.asynchandle.AsyncHandleService;
import com.netease.hz.bdms.easyinsight.service.service.impl.LockService;
import com.netease.hz.bdms.easyinsight.service.service.obj.AllTrackerReleaseService;
import com.netease.hz.bdms.easyinsight.service.service.obj.EventBuryPointService;
import com.netease.hz.bdms.easyinsight.service.service.obj.ObjTerminalTrackerService;
import com.netease.hz.bdms.easyinsight.service.service.requirement.*;
import com.netease.hz.bdms.easyinsight.service.service.terminalrelease.TerminalReleaseService;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class ReqDesignFacade {

    @Autowired
    ReqPoolBasicService reqPoolBasicService;

    @Autowired
    ReqSpmPoolService reqSpmPoolService;

    @Autowired
    ReqEventPoolService reqEventPoolService;

    @Autowired
    RequirementInfoService requirementInfoService;

    @Autowired
    ReqTaskService reqTaskService;

    @Autowired
    TaskProcessService taskProcessService;

    @Autowired
    ObjectBasicService objectBasicService;

    @Autowired
    ObjChangeHistoryService objChangeHistoryService;

    @Autowired
    ReqPoolRelBaseService reqPoolRelBaseService;

    @Autowired
    EventBuryPointService eventBuryPointService;

    @Autowired
    TerminalService terminalService;

    @Autowired
    TerminalVersionInfoService terminalVersionInfoService;

    @Autowired
    ObjTerminalTrackerService objTerminalTrackerService;

    @Autowired
    LineageHelper lineageHelper;

    @Autowired
    ImageRelationService imageRelationService;

    @Autowired
    ObjectHelper objectHelper;

    @Autowired
    RequirementPoolHelper requirementPoolHelper;

    @Autowired
    TerminalReleaseService terminalReleaseService;

    @Autowired
    AllTrackerReleaseService allTrackerReleaseService;

    @Autowired
    ReqTaskFacade reqTaskFacade;

    @Resource
    private AppRelationService appRelationService;

    @Resource
    private MergeConflictHelper mergeConflictHelper;

    @Resource
    private AsyncHandleService asyncHandleService;

    @Resource
    private LockService lockService;

    private static final String NOTIFY_KEY_REBASE_ALL = "rebaseAll";

    public void deleteProcess(long processId) {
        taskProcessService.deleteById(processId);
    }

    private ExecutorService rebaseExecutor = new ThreadPoolExecutor(1, 1, 10L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(500),
            new ThreadPoolExecutor.DiscardPolicy() {
            });

    /**
     * 根据spm需求池各记录构建出血缘图层级结构返回给前端，按reqPoolType区分是开发血缘、下线血缘
     * @param reqPoolId 需求组Id
     * @param reqPoolType 需求组血缘视图类型(开发血缘、下线血缘，命名不太规范)
     * @param showCompleteTree 是否展示完整血缘
     * @return
     */
    public List<ReqSpmTreeVO> getSpmTrees(Long reqPoolId, Integer reqPoolType,boolean showCompleteTree, String searchStr, boolean showUnAssign){
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null !=  appId, "未指定产品信息");
        ReqPoolTypeEnum reqPoolTypeEnum = ReqPoolTypeEnum.valueOfType(reqPoolType);
        if (reqPoolTypeEnum == null) {
            throw new CommonException("reqPoolType无效");
        }

        List<ReqSpmTreeVO> resultList = new ArrayList<>();
        EisReqPoolSpm query = new EisReqPoolSpm();
        query.setReqPoolId(reqPoolId);
        query.setReqPoolType(reqPoolType);
        //查询待指派项
        List<EisReqPoolSpm> reqPoolSpms = reqSpmPoolService.search(query);

        Set<Long> allRelatedObjIds = reqPoolSpms.stream().flatMap(reqPoolSpm -> {
            String spmByObjId = reqPoolSpm.getSpmByObjId();
            if (StringUtils.isEmpty(spmByObjId)) {
                return Stream.empty();
            }
            return CommonUtil.transSpmToOidList(spmByObjId).stream().filter(StringUtils::isNumeric).map(Long::parseLong);
        }).collect(Collectors.toSet());
        List<ObjectBasic> allRelatedObjs = objectBasicService.getByIds(allRelatedObjIds);
        Map<String, ObjectBasic> allRelatedOids = allRelatedObjs.stream().collect(Collectors.toMap(ObjectBasic::getOid, o -> o, (oldV, newV) -> oldV));

        Set<Long> objIds = new HashSet<>();
        // 通过搜索条件过滤
        boolean isMatchObjectOnly = false;
        if (StringUtils.isNotBlank(searchStr) && searchStr.startsWith("*")) {
            isMatchObjectOnly = true;
            searchStr = searchStr.substring(1);
        }
        Pair<List<String>, Set<Long>> p = filterReqPoolEntityIds(appId, searchStr, reqPoolSpms, allRelatedOids, isMatchObjectOnly);
        List<String> expandSpms = p.getKey();
        Set<Long> reqPoolEntityIds = p.getValue();

                //查询已指派的流程
        List<EisTaskProcess> taskProcesses = taskProcessService.getByReqPoolEntityIds(reqPoolTypeEnum, reqPoolEntityIds);
        //需求池实体id -> 任务流程 map
        Map<Long,EisTaskProcess> relPoolEntityIdToProcessMap = new HashMap<>();
        Set<Long> taskIds = new HashSet<>();
        for (EisTaskProcess taskProcess : taskProcesses) {
            relPoolEntityIdToProcessMap.put(taskProcess.getReqPoolEntityId(),taskProcess);
            taskIds.add(taskProcess.getTaskId());
        }
        //查询任务基本信息
        List<EisReqTask> tasks = reqTaskService.getByIds(taskIds);
        Map<Long,EisReqTask> taskMap = new HashMap<>();
        for (EisReqTask task : tasks) {
            taskMap.put(task.getId(),task);
        }
        for (EisReqPoolSpm reqPoolSpm : reqPoolSpms) {
            objIds.add(reqPoolSpm.getObjId());
        }
        //查询当前产品下的所有对象基本信息
        ObjectBasic objQuery = new ObjectBasic();
        Set<Long> parentAppIds = appRelationService.getParentAppIds(EtContext.get(ContextConstant.APP_ID));
        if (CollectionUtils.isEmpty(parentAppIds)) {
            objQuery.setAppId(EtContext.get(ContextConstant.APP_ID));
        }
        List<ObjectBasic> objectBasics = objectBasicService.search(objQuery);
        Map<Long,ObjectBasic> objMap = new HashMap<>();
        for (ObjectBasic objectBasic : objectBasics) {
            objMap.put(objectBasic.getId(),objectBasic);
        }
        //查询对象关联图片信息，用来展示对象是否有图片
        List<ImageRelationDTO> imageRelations = imageRelationService.getByEntityId(objIds);
        Set<Long> objIdContainsPicture = new HashSet<>();
        for (ImageRelationDTO imageRelation : imageRelations) {
            objIdContainsPicture.add(imageRelation.getEntityId());
        }
        //查询端信息
        Map<Long,List<EisReqPoolSpm>> groupByTerminal = reqPoolSpms.stream().collect(Collectors.groupingBy(EisReqPoolSpm::getTerminalId));
        List<TerminalSimpleDTO> terminals = terminalService.getByIds(groupByTerminal.keySet());
        Map<Long,String> terminalIdToNameMap = new HashMap<>();
        for (TerminalSimpleDTO terminal : terminals) {
            terminalIdToNameMap.put(terminal.getId(),terminal.getName());
        }
        //查询需求基本信息
        EisRequirementInfo reqQuery = new EisRequirementInfo();
        reqQuery.setReqPoolId(reqPoolId);
        List<EisRequirementInfo> requirementInfos = requirementInfoService.search(reqQuery);
        Map<Long,String> reqIdToNameMap = new HashMap<>();
        for (EisRequirementInfo requirementInfo : requirementInfos) {
            reqIdToNameMap.put(requirementInfo.getId(),requirementInfo.getReqName());
        }
        EisObjTerminalTracker trackerQuery = new EisObjTerminalTracker();
        trackerQuery.setReqPoolId(reqPoolId);

        // 存在合并基线冲突的objId
        Set<Long> mergeConflictObjIds = mergeConflictHelper.getMergeConflictObjIdsOfReqPool(reqPoolId);

        //查询对象参数实体
        List<EisObjTerminalTracker> trackers = objTerminalTrackerService.search(trackerQuery);
        for (Long terminalId : groupByTerminal.keySet()) {
            ReqSpmTreeVO reqDevSpmTreeVO = new ReqSpmTreeVO();
            List<EisReqPoolSpm> reqPoolSpmsOfCurrentTerminal = groupByTerminal.get(terminalId);
            //所有的spm，每个spm以对象id列表的形式组织
            List<List<Long>> spmsOfObjIdList = new ArrayList<>();
            List<ReqSpmEntityVO> spmEntityVos = new ArrayList<>();
            Set<Long> objIdsOfReq = new HashSet<>();
            for (EisReqPoolSpm reqPoolSpm : reqPoolSpmsOfCurrentTerminal) {
                String spmByObjId = reqPoolSpm.getSpmByObjId();
                EisTaskProcess assignedTaskProcess = relPoolEntityIdToProcessMap.get(reqPoolSpm.getId());
                if(showUnAssign && assignedTaskProcess != null){
                    continue;
                }
                // reqPoolEntityIds是满足搜索条件的，过滤未满足搜索条件的
                if (!reqPoolEntityIds.contains(reqPoolSpm.getId())) {
                    continue;
                }
                List<Long> spmByObjIdList = Arrays.stream(spmByObjId.split("\\|")).map(e -> Long.valueOf(e)).collect(Collectors.toList());
                spmsOfObjIdList.add(spmByObjIdList);
                ReqSpmEntityVO devSpmEntityVo = new ReqSpmEntityVO();
                ObjectBasic relObj = objMap.get(reqPoolSpm.getObjId());
                objIdsOfReq.add(reqPoolSpm.getObjId());
                Long trackerId = null;
                //获取关联的trackerId，tracker是对象在某个端某个版本下的参数实体，通过对象Id、终端id、对象变更历史id唯一确定
                for (EisObjTerminalTracker tracker : trackers) {
                    if(reqPoolSpm.getObjId().equals(tracker.getObjId())
                            && reqPoolSpm.getTerminalId().equals(tracker.getTerminalId())
                            && reqPoolSpm.getObjHistoryId().equals(tracker.getObjHistoryId())){
                        trackerId = tracker.getId();
                    }
                }
                devSpmEntityVo.setId(reqPoolSpm.getId());
//                devSpmEntityVo.setObjId(relObj.getId());
                devSpmEntityVo.setTrackerId(trackerId);
                devSpmEntityVo.setHistoryId(reqPoolSpm.getObjHistoryId());
                devSpmEntityVo.setObjType(relObj.getType());
                devSpmEntityVo.setOid(relObj.getOid());
                devSpmEntityVo.setObjName(relObj.getName());
                devSpmEntityVo.setReqPoolId(reqPoolId);
                devSpmEntityVo.setSpmByObjId(reqPoolSpm.getSpmByObjId());
                devSpmEntityVo.setMergeConflict(MergeConflictHelper.hasMergeConflict(reqPoolSpm.getSpmByObjId(), mergeConflictObjIds));
                String reqTypeDesc = CommonUtil.transReqTypeToDescription(reqPoolSpm.getReqType());
                devSpmEntityVo.setReqType(reqTypeDesc);
                //判断spm待指派项是否已指派给任务，若已指派，则返回任务名称、状态、需求名称
                if(assignedTaskProcess != null){
                    EisReqTask task = taskMap.get(assignedTaskProcess.getTaskId());
                    Long reqId = task.getRequirementId();
                    devSpmEntityVo.setTaskId(task.getId());
                    devSpmEntityVo.setTaskName(task.getTaskName());
                    devSpmEntityVo.setStatus(assignedTaskProcess.getStatus());
                    devSpmEntityVo.setReqName(reqIdToNameMap.get(reqId));
                }
                devSpmEntityVo.setHasPicture(objIdContainsPicture.contains(reqPoolSpm.getObjId()));
                spmEntityVos.add(devSpmEntityVo);
            }
            LineageForest lineageForest = new LineageForest();
            //获得需求组某个端的基线
            EisReqPoolRelBaseRelease reqPoolRelBaseRelease = reqPoolRelBaseService.getCurrentUse(reqPoolId,terminalId);
            Long baseReleaseId = 0L;
            if(reqPoolRelBaseRelease != null){
                baseReleaseId = reqPoolRelBaseRelease.getBaseReleaseId();
            }
            //获取基线中所有tracer(对象有参数绑定的实体，定义为tracker，存储在eis_obj_terminal_tracker中，通过objId（对象id） + terminalId（终端id） + historyId（变更历史id）唯一确定)
            List<EisObjTerminalTracker> trackersOfBase = objectHelper.getTrackersOfReqPoolBase(baseReleaseId);
            Map<Long,Long> objIdToHistoryIdMapOfBase = new HashMap<>();
            for (EisObjTerminalTracker trackerOfBase : trackersOfBase) {
                objIdToHistoryIdMapOfBase.put(trackerOfBase.getObjId(),trackerOfBase.getObjHistoryId());
            }
            List<BaseObjEntityVO> baseObjEntityVos = new ArrayList<>();
            if(showCompleteTree){
                /**
                 * 查看需求涉及的完整血缘树（需求下的对象的每条spm都展示）
                 */

                List<List<Long>> totalSpmsOfObjIdList = new ArrayList<>();
                //构建血缘图
                LinageGraph graph = lineageHelper.genReqLinageGraph(baseReleaseId,terminalId,reqPoolId);
                for (Long objId : objIdsOfReq) {
                    //获得对象在血缘图下的所有spm，以objId的列表形式组织
                    List<List<Long>> spmsListOfCurrentobj =  lineageHelper.getObjIdSpms(graph,objId);
                    totalSpmsOfObjIdList.addAll(spmsListOfCurrentobj);
                }
                Set<Long> allObjIds = totalSpmsOfObjIdList.stream().flatMap(l -> l.stream()).collect(Collectors.toSet());
                Set<Long> outerSpaceObjIds = AppRelationService.getOuterSpaceObjIds(spmsOfObjIdList, objMap);
                for (Long objId : allObjIds) {
//                    if(ObjIdsOfReqPool.contains(objId)){
//                        continue;
//                    }
                    ObjectBasic relObj = objMap.get(objId);
                    BaseObjEntityVO baseObjEntityVo = new BaseObjEntityVO();
                    baseObjEntityVo.setObjId(relObj.getId());
                    baseObjEntityVo.setObjName(relObj.getName());
                    baseObjEntityVo.setObjType(relObj.getType());
                    baseObjEntityVo.setOid(relObj.getOid());
                    baseObjEntityVo.setHistoryId(objIdToHistoryIdMapOfBase.get(relObj.getId()));
                    if (outerSpaceObjIds.contains(relObj.getId())) {
                        baseObjEntityVo.setOtherAppId(relObj.getAppId());
                    }
                    baseObjEntityVo.setMergeConflict(mergeConflictObjIds.contains(relObj.getId()));
                    baseObjEntityVos.add(baseObjEntityVo);
                }
                lineageForest = lineageHelper.buildForestBySpms(totalSpmsOfObjIdList);
            }else {
                /**
                 * 查看需求涉及的局部血缘树（只包括待办项涉及的spm）
                 */
                //
                lineageForest = lineageHelper.buildForestBySpms(spmsOfObjIdList);
                Set<Long> allObjIds = spmsOfObjIdList.stream().flatMap(l -> l.stream()).collect(Collectors.toSet());
                Set<Long> outerSpaceObjIds = AppRelationService.getOuterSpaceObjIds(spmsOfObjIdList, objMap);
                for (Long objId : allObjIds) {
//                    if(ObjIdsOfReqPool.contains(objId)){
//                        continue;
//                    }
                    ObjectBasic relObj = objMap.get(objId);
                    if (relObj == null) {
                        throw new CommonException("objId= " + objId + " 对象已被删除导致以下spm无法渲染，请重新编辑子对象" + JsonUtils.toJson(getErrorSpm(spmsOfObjIdList, objId, objMap)));
                    }
                    BaseObjEntityVO baseObjEntityVo = new BaseObjEntityVO();
                    baseObjEntityVo.setObjId(relObj.getId());
                    baseObjEntityVo.setObjName(relObj.getName());
                    baseObjEntityVo.setObjType(relObj.getType());
                    baseObjEntityVo.setOid(relObj.getOid());
                    baseObjEntityVo.setHistoryId(objIdToHistoryIdMapOfBase.get(relObj.getId()));
                    if (outerSpaceObjIds.contains(relObj.getId())) {
                        baseObjEntityVo.setOtherAppId(relObj.getAppId());
                    }
                    baseObjEntityVo.setMergeConflict(mergeConflictObjIds.contains(relObj.getId()));
                    baseObjEntityVos.add(baseObjEntityVo);
                }
            }

            reqDevSpmTreeVO.setTerminalId(terminalId);
            reqDevSpmTreeVO.setTerminalName(terminalIdToNameMap.get(terminalId));
            reqDevSpmTreeVO.setReqDevSpmEntities(spmEntityVos);
            reqDevSpmTreeVO.setBaseObjEntities(baseObjEntityVos);
            reqDevSpmTreeVO.setRoots(lineageForest.getRoots());
            if (CollectionUtils.isNotEmpty(expandSpms)) {
                reqDevSpmTreeVO.setSpmsToExpand(expandSpms);
            } else {
                reqDevSpmTreeVO.setSpmsToExpand(new ArrayList<>(0));
            }
            resultList.add(reqDevSpmTreeVO);
        }
        return resultList;
    }

    private List<String> getErrorSpm(List<List<Long>> spmsOfObjIdList, Long objId, Map<Long,ObjectBasic> objMap) {
        return spmsOfObjIdList.stream()
                .filter(o -> o.contains(objId))
                .map(o -> getSpm(o, objMap))
                .collect(Collectors.toList());
    }

    private String getSpm(List<Long> oids, Map<Long,ObjectBasic> objMap) {
        StringBuilder sb = new StringBuilder();
        oids.forEach(o -> {
            ObjectBasic obj = objMap.get(o);
            sb.append(obj == null ? null : obj.getOid()).append("|");
        });
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    private Pair<List<String>, Set<Long>> filterReqPoolEntityIds(Long appId, String searchStr, List<EisReqPoolSpm> reqPoolSpms, Map<String, ObjectBasic> allRelatedOids, boolean isMatchOidOnly) {
        if (CollectionUtils.isEmpty(reqPoolSpms)) {
            return new Pair<>(null, new HashSet<>());
        }
        // 不搜索，直接返回所有的
        if (StringUtils.isEmpty(searchStr)) {
            return new Pair<>(null, reqPoolSpms.stream().map(EisReqPoolSpm::getId).collect(Collectors.toSet()));
        }

        // searchStr如果是oid，这里就能拿到
        ObjectBasic objectBasicBySearchStr = allRelatedOids.get(searchStr);
        boolean isSearchStrSpm = searchStr.contains("|");
        boolean isSearchStrOid = objectBasicBySearchStr != null;

        // 按SPM匹配，返回前缀为该SPM的
        if (isSearchStrSpm) {
            return new Pair<>(Arrays.asList(searchStr), filterAsSpm(searchStr, reqPoolSpms, allRelatedOids));
        }
        // 如果是OID，需要区分是 按对象皮皮额 还是按SPM匹配
        if (isSearchStrOid) {
            if (isMatchOidOnly) {
                return new Pair<>(null, filterAsObjectMatch(appId, searchStr, reqPoolSpms));
            }
            Long searchObjId = objectBasicBySearchStr.getId();
            Set<Long> rootObjIds = reqPoolSpms.stream().map(reqPoolSpm -> {
                String spmByObjId = reqPoolSpm.getSpmByObjId();
                List<Long> spmObjIds = CommonUtil.transSpmToObjIdList(spmByObjId);
                if (CollectionUtils.isEmpty(spmObjIds)) {
                    return null;
                }
                return spmObjIds.get(spmObjIds.size() -1);
            }).filter(Objects::nonNull).collect(Collectors.toSet());
            boolean isSearchObjRoot = rootObjIds.contains(searchObjId);
            if (isSearchObjRoot) {
                // 搜根节点，按SPM搜
                return new Pair<>(Arrays.asList(searchStr), filterAsSpm(searchStr, reqPoolSpms, allRelatedOids));
            } else {
                // 非根节点，按模糊匹配
                return new Pair<>(null, filterAsObjectMatch(appId, searchStr, reqPoolSpms));
            }
        }
        // 既不是SPM，也不是oid，走模糊匹配
        return new Pair<>(null, filterAsObjectMatch(appId, searchStr, reqPoolSpms));
    }

    private Set<Long> filterAsSpm(String searchStr, List<EisReqPoolSpm> reqPoolSpms, Map<String, ObjectBasic> allRelatedOids) {
        List<String> spmOids = CommonUtil.transSpmToOidList(searchStr);
        List<Long> spmObjIds = new ArrayList<>();
        spmOids.forEach(spmOid -> {
            ObjectBasic objectBasic = allRelatedOids.get(spmOid);
            if (objectBasic == null) {
                throw new CommonException("匹配结果为空，搜索SPM中" + spmOid + "不在本需求组中");
            }
            spmObjIds.add(objectBasic.getId());
        });
        String searchSpmByObjId = CommonUtil.getSpmStringByObjIds(spmObjIds);
        return reqPoolSpms.stream()
                .filter(reqPoolSpm -> reqPoolSpm.getSpmByObjId().endsWith(searchSpmByObjId))
                .map(EisReqPoolSpm::getId)
                .collect(Collectors.toSet());
    }

    private Set<Long> filterAsObjectMatch(Long appId, String searchStr, List<EisReqPoolSpm> reqPoolSpms) {
        // 按对象oid/名字匹配，过滤出SPM中名字匹配的
        Search search = new Search();
        search.setSearch(searchStr);
        search.setAppId(appId);
        List<ObjectBasic> objs = StringUtils.isEmpty(searchStr) ? new ArrayList<>() : new ArrayList<>(objectBasicService.searchLike(search));
        Set<Long> searchObjIds = objs.stream().map(ObjectBasic::getId).collect(Collectors.toSet());
        return reqPoolSpms.stream()
                .filter(reqPoolSpm -> searchObjIds.contains(reqPoolSpm.getObjId()))
                .map(EisReqPoolSpm::getId)
                .collect(Collectors.toSet());
    }


    /**
     * 获取对象池对象列表返回给前端
     * @param reqPoolId 需求组id
     * @return
     */
    public List<ReqObjVO> getReqObjs(Long reqPoolId, String searchStr, String order, Boolean rule){
        //通过需求组id查询需求空间内的diff对象
        List<EisObjChangeHistory> changeHistories = objChangeHistoryService.getByReqPoolId(reqPoolId);
        Set<Long> objIds = new HashSet<>();
        Set<Long> changeHistoryIds = new HashSet<>();
        Map<Long,Long> objIdToChangeHistoryIdMap = new HashMap<>();
        Map<Long,Date> objIdToCreateTimeMap = new HashMap<>();
        Map<Long,Date> objIdToUpdateTimeMap = new HashMap<>();
        Map<Long,String> objIdToUpdateUserMap = new HashMap<>();
        Map<Long,String> objIdToCreateUserMap = new HashMap<>();
        Map<Long,Integer> objIdToChangeTypeMap = new HashMap<>();
        Map<Long,ConflictStatusEnum> mergeConflictsMap = new HashMap<>();
        for (EisObjChangeHistory changeHistory : changeHistories) {
            objIdToChangeTypeMap.put(changeHistory.getObjId(),changeHistory.getType());
            objIdToCreateTimeMap.put(changeHistory.getObjId(),changeHistory.getCreateTime());
            objIdToUpdateTimeMap.put(changeHistory.getObjId(),changeHistory.getUpdateTime());
            objIdToUpdateUserMap.put(changeHistory.getObjId(),changeHistory.getUpdateName());
            objIdToCreateUserMap.put(changeHistory.getObjId(),changeHistory.getCreateName());
            mergeConflictsMap.put(changeHistory.getObjId(), ConflictStatusEnum.fromStatus(changeHistory.getConflictStatus()));
        }
        for (EisObjChangeHistory changeHistory : changeHistories) {
            objIds.add(changeHistory.getObjId());
            changeHistoryIds.add(changeHistory.getId());
            objIdToChangeHistoryIdMap.put(changeHistory.getObjId(),changeHistory.getId());
        }
        List<EisObjTerminalTracker> trackers = objTerminalTrackerService.getBatchByChangeHistoryIds(changeHistoryIds);
        List<ObjectBasic> objectBasics = objectBasicService.getByIds(objIds);
        Map<Long,ObjectBasic> objMap = new HashMap<>();
        Map<Long,Set<Long>> objIdToTerminalIdsMap = new HashMap<>();
        Set<Long> allTerminalIds = new HashSet<>();
        for (ObjectBasic objectBasic : objectBasics) {
            objMap.put(objectBasic.getId(),objectBasic);
        }
        //获得map: 对象id -> 关联端id集合
        for (EisObjTerminalTracker tracker : trackers) {
            allTerminalIds.add(tracker.getTerminalId());
            Set<Long> terminalIds = objIdToTerminalIdsMap.computeIfAbsent(tracker.getObjId(),k->new HashSet<>());
            terminalIds.add(tracker.getTerminalId());
        }
        List<TerminalSimpleDTO> allTerminals = terminalService.getByIds(allTerminalIds);
        Map<Long,String> idToNameMap = new HashMap<>();
        for (TerminalSimpleDTO terminal : allTerminals) {
            idToNameMap.put(terminal.getId(),terminal.getName());
        }
        Map<Long,String> objIdToTerminalNameMap = new HashMap<>();
        for (Long objId : objIdToTerminalIdsMap.keySet()) {
            Set<Long> termianlIds = objIdToTerminalIdsMap.get(objId);
            Set<String> terminalNames = new HashSet<>();
            String terminalNamesCombine = "";
            for (Long termianlId : termianlIds) {
                terminalNames.add(idToNameMap.get(termianlId));
            }
            terminalNamesCombine = String.join(",",terminalNames);
            objIdToTerminalNameMap.put(objId,terminalNamesCombine);
        }
        List<ImageRelationDTO> imageRelations = imageRelationService.getByEntityId(changeHistoryIds);
        Set<Long> objIdContainsPicture = new HashSet<>();
        for (ImageRelationDTO imageRelation : imageRelations) {
            objIdContainsPicture.add(imageRelation.getEntityId());
        }
        //获得当前需求组下已发布上线的对象，这些对象不可编辑
        Set<Long> objIdsOfRelease = taskProcessService.getObjIdsOfRelease(reqPoolId);

        // 获取匹配方法，这里会根据是否是SPM，走SPM匹配，或按名字oid匹配
        Set<String> oids = objectBasics.stream().map(ObjectBasic::getOid).collect(Collectors.toSet());
        if (searchStr != null && searchStr.startsWith("*")) {
            searchStr = searchStr.substring(1);
        }
        boolean isSpm = searchStr != null && (searchStr.contains("|") || oids.contains(searchStr));
        BiFunction<String, ObjectBasic, Boolean> searchMather = getSearchMather(searchStr, isSpm);

        List<ReqObjVO> resultList = new ArrayList<>();
        for (Long objId : objIds) {
            ReqObjVO vo = new ReqObjVO();
            ObjectBasic objectBasic = objMap.get(objId);
            boolean matchSearch = searchMather.apply(searchStr, objectBasic);
            if (!matchSearch) {
                continue;
            }
            Long changeHistoryId = objIdToChangeHistoryIdMap.get(objId);
            vo.setObjId(objId);
            vo.setObjType(objectBasic.getType());
            vo.setSpecialType(objectBasic.getSpecialType());
            // 冲突状态展示
            ConflictStatusEnum conflictStatusEnum = mergeConflictsMap.get(objId);
            vo.setMergeConflict(ConflictStatusEnum.MERGE_CONFLICT == conflictStatusEnum);
            vo.setOid(objectBasic.getOid());
            vo.setObjName(objectBasic.getName());
            vo.setOperatorType(objIdToChangeTypeMap.get(objId));
            vo.setHasPicture(objIdContainsPicture.contains(changeHistoryId));
            vo.setTerminals(objIdToTerminalNameMap.get(objId));
            vo.setCreateTime(objIdToCreateTimeMap.get(objId));
            vo.setUpdateTime(objIdToUpdateTimeMap.get(objId));
            vo.setUpdateName(objIdToUpdateUserMap.get(objId));
            vo.setCreateName(objIdToCreateUserMap.get(objId));
            vo.setEditable(!objIdsOfRelease.contains(objId));
            resultList.add(vo);
        }

        //排序
        if(!StringUtils.isEmpty(order) && rule != null) {
            switch (order) {
                case "oid":
                    resultList.sort(Comparator.comparing(ReqObjVO::getOid));
                    break;
                case "objType":
                    resultList.sort(Comparator.comparing(ReqObjVO::getObjType));
                    break;
                case "createTime":
                    resultList.sort(Comparator.comparing(ReqObjVO::getCreateTime));
                    break;
                case "updateTime":
                    resultList.sort(Comparator.comparing(ReqObjVO::getUpdateTime));
                    break;
            }
            if(rule) {
                Collections.reverse(resultList);
            }
        }
        return resultList;
    }

    private BiFunction<String, ObjectBasic, Boolean> getSearchMather(String searchStr, boolean isSpm) {
        // 不过滤，所有对象都匹配
        if (StringUtils.isEmpty(searchStr)) {
            return (s, objectBasic) -> true;
        }
        // 按SPM匹配，路径上每个oid都可以匹配
        if (isSpm) {
            HashSet<String> oidSet = new HashSet<>(CommonUtil.transSpmToOidList(searchStr));
            return (s, objectBasic) -> oidSet.contains(objectBasic.getOid());
        }
        // 按名字、oid匹配
        return (s, objectBasic) -> objectBasic.getOid().contains(s) || objectBasic.getName().contains(s);
    }

    /**
     * 指派任务
     *
     * @param assignEntities 待指派项ID
     * @param taskIds 任务ID列表
     */
    @MethodLog
    @Transactional(rollbackFor = Throwable.class)
    public void assign(List<AssignEntityVO> assignEntities, Set<Long> taskIds){
        if(CollectionUtils.isEmpty(assignEntities) || CollectionUtils.isEmpty(taskIds)){
            throw new CommonException("请勾选至少一个可指派的任务（开始、待审核）");
        }
        // 防止NPE
        taskIds = taskIds.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        // poolType 校验
        Integer poolType = assignEntities.get(0).getPoolType();
        ReqPoolTypeEnum reqPoolTypeEnum = ReqPoolTypeEnum.valueOfType(poolType);
        if (reqPoolTypeEnum == null) {
            throw new CommonException("poolType 不合法");
        }
        for (AssignEntityVO assignEntity : assignEntities) {
            if (!reqPoolTypeEnum.getReqPoolType().equals(assignEntity.getPoolType())) {
                throw new CommonException("每次变更的列表poolType需要一致");
            }
        }

        Set<Long> reqPoolEntityIds = assignEntities.stream().map(e -> e.getId()).collect(Collectors.toSet());
        List<EisTaskProcess> newList = new ArrayList<>();
        Map<Integer,List<AssignEntityVO>> entitiesGroupbyType = assignEntities.stream().collect(Collectors.groupingBy(AssignEntityVO::getPoolType));
        List<EisReqTask> tasks = reqTaskService.getByIds(taskIds);
        //校验：任务若已上线不能指派
        for (EisReqTask task : tasks) {
            if(task.getStatus().equals(ProcessStatusEnum.ONLINE.getState())){
                throw new CommonException("任务已上线，不能指派");
            }
        }
        //校验：若指派项已经上线，不能指派
        List<EisTaskProcess> oldList = taskProcessService.getByReqPoolEntityIds(reqPoolTypeEnum, reqPoolEntityIds);
        Set<Long> taskIdsNeedToUpdateStatus = oldList.stream().map(e -> e.getTaskId()).collect(Collectors.toSet());
        taskIdsNeedToUpdateStatus.addAll(taskIds);
        for (EisTaskProcess process : oldList) {
            if(process.getStatus().equals(ProcessStatusEnum.ONLINE.getState())){
                throw new CommonException("指派项已上线，不能重新指派");
            }
            if(process.getStatus().equals(ProcessStatusEnum.VERIFY_FINISHED.getState())){
                throw new CommonException("指派项已审核，不能重新指派");
            }
            if(process.getStatus().equals(ProcessStatusEnum.DEV_FINISHED.getState())){
                throw new CommonException("指派项已完成，不能重新指派");
            }
            if(process.getStatus().equals(ProcessStatusEnum.TEST_FINISHED.getState())){
                throw new CommonException("指派项已测试完成，不能重新指派");
            }
        }

        Map<Long,EisReqTask> taskGroupbyTerminal = new HashMap<>();
        for (EisReqTask task : tasks) {
            taskGroupbyTerminal.put(task.getTerminalId(),task);
        }
        Set<Long> reqPoolIds = new HashSet<>();
        for (Integer reqPoolType : entitiesGroupbyType.keySet()) {
            Set<Long> entityIds = entitiesGroupbyType.get(reqPoolType).stream().map(e -> e.getId()).collect(Collectors.toSet());

            if(ReqPoolTypeEnum.SPM_DEV.getReqPoolType().equals(reqPoolType)
                ||ReqPoolTypeEnum.SPM_DELETE.getReqPoolType().equals(reqPoolType)){
                //待指派项属于spm待开发项或待下线项
                List<EisReqPoolSpm> reqPoolSpms = reqSpmPoolService.getBatchByIds(entityIds);
                for (EisReqPoolSpm reqPoolSpm : reqPoolSpms) {
                    Long terminalId = reqPoolSpm.getTerminalId();
                    EisReqTask targetTask = taskGroupbyTerminal.get(terminalId);
                    if (targetTask == null) {
                        throw new CommonException("terminalId无对应targetTask,terminalId=" + terminalId);
                    }
                    EisTaskProcess process = new EisTaskProcess();
                    process.setTaskId(targetTask.getId());
                    process.setObjId(reqPoolSpm.getObjId());
                    process.setSpmByObjId(reqPoolSpm.getSpmByObjId());
                    process.setStatus(ReqTaskStatusEnum.START.getState());
                    reqPoolIds.add(reqPoolSpm.getReqPoolId());
                    process.setReqPoolId(reqPoolSpm.getReqPoolId());
                    process.setReqPoolEntityId(reqPoolSpm.getId());
                    process.setReqPoolType(reqPoolType);
                    process.setOwnerEmail(targetTask.getOwnerEmail());
                    process.setOwnerName(targetTask.getOwnerName());
                    process.setVerifierEmail(targetTask.getVerifierEmail());
                    process.setVerifierName(targetTask.getVerifierName());
                    UserDTO currentUserDTO = EtContext.get(ContextConstant.USER);
                    process.setCreateEmail(currentUserDTO.getEmail());
                    process.setCreateName(currentUserDTO.getUserName());
                    newList.add(process);
                }
            }else {
                //待指派项属于事件埋点
                List<EisReqPoolEvent> reqPoolEvents = reqEventPoolService.getBatchByIds(entityIds);
                Set<Long> eventBuryPointIds = reqPoolEvents.stream().map(e -> e.getEventBuryPointId()).collect(Collectors.toSet());
                List<EisEventBuryPoint> eisEventBuryPoints = eventBuryPointService.getByIds(eventBuryPointIds);
                Map<Long,Long> eventBuryPointIdToEventIdMap = new HashMap<>();
                for (EisEventBuryPoint eisEventBuryPoint : eisEventBuryPoints) {
                    eventBuryPointIdToEventIdMap.put(eisEventBuryPoint.getId(),eisEventBuryPoint.getEventId());
                }
                for (EisReqPoolEvent reqPoolEvent : reqPoolEvents) {
                    Long terminalId = reqPoolEvent.getTerminalId();
                    EisReqTask targetTask = taskGroupbyTerminal.get(terminalId);
                    EisTaskProcess process = new EisTaskProcess();
                    process.setEventId(eventBuryPointIdToEventIdMap.get(reqPoolEvent.getEventBuryPointId()));
                    process.setTaskId(targetTask.getId());
                    process.setStatus(ReqTaskStatusEnum.START.getState());
                    reqPoolIds.add(reqPoolEvent.getReqPoolId());
                    process.setReqPoolId(reqPoolEvent.getReqPoolId());
                    process.setReqPoolEntityId(reqPoolEvent.getId());
                    process.setReqPoolType(reqPoolType);
                    process.setOwnerEmail(targetTask.getOwnerEmail());
                    process.setOwnerName(targetTask.getOwnerName());
                    process.setVerifierEmail(targetTask.getVerifierEmail());
                    process.setVerifierName(targetTask.getVerifierName());
                    newList.add(process);
                }
            }
        }

        // 涉及需求池如果有冲突没解决，不允许指派
        Set<Long> conflictReqPoolIds = mergeConflictHelper.filterConflictReqPoolIds(reqPoolIds);
        if (CollectionUtils.isNotEmpty(conflictReqPoolIds)) {
            throw new CommonException("当前需求组下存在合并冲突，请先解决冲突");
        }

        //删除旧的指派流程，插入新的指派流程
        Set<Long> oldProcessIds = oldList.stream().map(e -> e.getId()).collect(Collectors.toSet());
        taskProcessService.deleteByIds(oldProcessIds);
        taskProcessService.insertBatch(newList);
        for (Long taskId : taskIdsNeedToUpdateStatus) {
            Integer status = taskProcessService.getTaskNewStatusByProcesses(taskId);
            EisReqTask taskToBeUpdate = new EisReqTask();
            taskToBeUpdate.setId(taskId);
            taskToBeUpdate.setStatus(status);
            reqTaskService.updateById(taskToBeUpdate);
            // 异步同步到三方
            asyncHandleService.onTaskAndProcessUpdate(taskId);
        }
    }

    /**
     * 取消任务指派
     *
     * @param ids 指派项Id列表
     */
    public void cancelAssign(Set<Long> ids){
        if(CollectionUtils.isEmpty(ids)){
            return;
        }
        List<EisTaskProcess> taskProcesses = taskProcessService.getBatchByIds(ids);
        if (CollectionUtils.isEmpty(taskProcesses)) {
            return;
        }
        Set<Long> relatedTaskIds = taskProcesses.stream().map(EisTaskProcess::getTaskId).filter(Objects::nonNull).collect(Collectors.toSet());
        taskProcessService.deleteByIds(ids);
        // 同步更新相关taskId的状态
        relatedTaskIds.forEach(relatedTaskId -> {
            asyncHandleService.onTaskAndProcessUpdate(relatedTaskId);
        });
    }

    /**
     * 取消任务指派
     *
     * @param cancleAssignVOs 指派项信息
     */
    public void cancelSpmAssign(List<CancleSpmAssignVO> cancleAssignVOs){
        if(CollectionUtils.isEmpty(cancleAssignVOs)){
            return;
        }

        for(CancleSpmAssignVO cancleAssignVO : cancleAssignVOs) {
            taskProcessService.deleteByInfos(cancleAssignVO);
        }
    }

    /**
     * 取消任务指派
     *
     * @param cancelAssignBatchVO
     */
    public void cancelAssignBatch(CancelAssignBatchVO cancelAssignBatchVO){

        List<OidAssignVO> oidAssignVOS = cancelAssignBatchVO.getOidAssignVOS();
        Set<Long> ids = new HashSet<>();
        for(OidAssignVO oidAssignVO : oidAssignVOS) {

            EisReqTask task = reqTaskService.getById(oidAssignVO.getTaskId());
            if (task == null) {
                log.error("taskId " + oidAssignVO.getTaskId() + " 不存在");
                continue;
            }
            //已指派信息
            TaskProcessViewQueryVO vo = new TaskProcessViewQueryVO();
            vo.setTaskId(oidAssignVO.getTaskId());
            TaskProcessVO taskProcessVO = reqTaskFacade.getProcessVo(vo);

            if(oidAssignVO.getType().equals(EntityTypeEnum.OBJTRACKER.getType())){
                TaskSpmTreeVO taskSpmTreeVO = taskProcessVO.getDevSpmTree();
                TaskSpmTreeVO taskDelSpmTreeVO = taskProcessVO.getDeleteSpmTree();
                if (CollectionUtils.isEmpty(taskSpmTreeVO.getEntities()) && CollectionUtils.isEmpty(taskDelSpmTreeVO.getEntities())) {
                    continue;
                }
                List<TaskProcessSpmEntityVO> entities = new ArrayList<>();
                if(CollectionUtils.isNotEmpty(taskSpmTreeVO.getEntities())) {
                    entities.addAll(taskSpmTreeVO.getEntities());
                }
                if(CollectionUtils.isNotEmpty(taskDelSpmTreeVO.getEntities())) {
                    entities.addAll(taskDelSpmTreeVO.getEntities());
                }
                List<TaskProcessSpmEntityVO> taskProcessSpmEntityVOS = entities.stream().filter(entityVO -> entityVO.getSpmByObjId().equals(oidAssignVO.getSpmByObjId())).collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(taskProcessSpmEntityVOS)){
                    ids.add(taskProcessSpmEntityVOS.get(0).getId());
                }
            }else if(oidAssignVO.getType().equals(EntityTypeEnum.EVENT.getType())){
                List<TaskEventVO> taskEventVOS = taskProcessVO.getEvents().stream().filter(taskEventVO -> taskEventVO.getEventBuryPointId().equals(oidAssignVO.getEventBuryPointId())).collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(taskEventVOS)){
                    ids.add(taskEventVOS.get(0).getId());
                }
            }

        }

        if(CollectionUtils.isEmpty(ids)){
            return;
        }
        List<EisTaskProcess> taskProcesses = taskProcessService.getBatchByIds(ids);
        Set<Long> accessIds = new HashSet<>();
        for(EisTaskProcess process : taskProcesses){
            if(process.getStatus().equals(ProcessStatusEnum.VERIFY_FINISHED.getState()) || process.getStatus().equals(ProcessStatusEnum.START.getState()) || process.getStatus().equals(ProcessStatusEnum.WAIT_VERIFY.getState())){
                accessIds.add(process.getId());
            }
        }
        if(CollectionUtils.isEmpty(accessIds)){
            throw new CommonException("只有【开始】、【待审核】、【已审核】的任务可以取消指派，请至少勾选一个");
        }
        taskProcessService.deleteByIds(accessIds);
    }

    /**
     * 指派页面下拉框选项
     * @param assignEntities
     * @param reqPoolId
     * @return
     */
    public List<AssignAggreVO> getAssignAggre(List<AssignEntityVO> assignEntities,Long reqPoolId, boolean syncAllTerminal){
        Long appId = EtContext.get(ContextConstant.APP_ID);
        EisRequirementInfo reqQuery = new EisRequirementInfo();
        reqQuery.setReqPoolId(reqPoolId);
        List<EisRequirementInfo> requirementInfos = requirementInfoService.search(reqQuery);
        if(CollectionUtils.isEmpty(requirementInfos)){
            return new ArrayList<>();
        }
        Set<Long> reqIds = requirementInfos.stream().map(e -> e.getId()).collect(Collectors.toSet());
        List<TerminalSimpleDTO> terminals = terminalService.getByAppId(appId);
        List<EisReqTask> tasks = reqTaskService.getByReqIds(reqIds);
        if(CollectionUtils.isEmpty(tasks)){
            return new ArrayList<>();
        }
        Map<Long,List<EisReqTask>> taskGroupByTerminal = tasks.stream()
                .filter(e -> e.getTerminalId() != null)
                .collect(Collectors.groupingBy(EisReqTask::getTerminalId));
        Map<Integer,List<AssignEntityVO>> entitiesGroupbyType = assignEntities.stream().collect(Collectors.groupingBy(AssignEntityVO::getPoolType));
        Set<Long> terminalIdsOfAssignEntity = new HashSet<>();
        for (Integer reqPoolType : entitiesGroupbyType.keySet()){
            Set<Long> entityIds = entitiesGroupbyType.get(reqPoolType).stream().map(e -> e.getId()).collect(Collectors.toSet());
            if(ReqPoolTypeEnum.SPM_DEV.getReqPoolType().equals(reqPoolType)
                    ||ReqPoolTypeEnum.SPM_DELETE.getReqPoolType().equals(reqPoolType)){
                List<EisReqPoolSpm> reqPoolSpms = reqSpmPoolService.getBatchByIds(entityIds);
                for (EisReqPoolSpm reqPoolSpm : reqPoolSpms) {
                    terminalIdsOfAssignEntity.add(reqPoolSpm.getTerminalId());
                }
            }else {
                List<EisReqPoolEvent> reqPoolEvents = reqEventPoolService.getBatchByIds(entityIds);
                Set<Long> eventBuryPointIds = reqPoolEvents.stream().map(e -> e.getEventBuryPointId()).collect(Collectors.toSet());
                List<EisEventBuryPoint> eisEventBuryPoints = eventBuryPointService.getByIds(eventBuryPointIds);
                for (EisEventBuryPoint eisEventBuryPoint : eisEventBuryPoints) {
                    terminalIdsOfAssignEntity.add(eisEventBuryPoint.getTerminalId());
                }
            }
        }

        List<AssignAggreVO> list = new ArrayList<>();
        for (TerminalSimpleDTO terminal : terminals) {
            Long terminalId = terminal.getId();
            // 如果开启多端同步，则返回所有端的任务；如果未开启，则对于勾选不含的端，不返回
            if(!syncAllTerminal && !terminalIdsOfAssignEntity.contains(terminalId)){
                continue;
            }
            String terminalName = terminal.getName();
            AssignAggreVO assignAggreVo = new AssignAggreVO();
            assignAggreVo.setTerminalId(terminalId);
            assignAggreVo.setTerminalName(terminalName);
            List<EisReqTask> tasksOfTerminal = taskGroupByTerminal.get(terminalId);
            if(!CollectionUtils.isEmpty(tasksOfTerminal)){
                List<AssignAggreVO.AssignTargetTask> targetTasks = new ArrayList<>();
                for (EisReqTask task : tasksOfTerminal) {
                    AssignAggreVO.AssignTargetTask assignTargetTask = new AssignAggreVO.AssignTargetTask();
                    assignTargetTask.setId(task.getId());
                    assignTargetTask.setTaskName(task.getTaskName());
                    targetTasks.add(assignTargetTask);
                }
                assignAggreVo.setTargetTasks(targetTasks);
            }
            list.add(assignAggreVo);
        }
        return list;
    }

    /**
     * 删除对象池中的对象
     * @param objId
     * @param reqPoolId
     */
    @MethodLog
    @Transactional(rollbackFor = Throwable.class)
    public void deleteObjPoolEntity(Long objId, Long reqPoolId){
        /**
         * 若该对象未指派spm给任务：
         * 1.删除tracker
         * 2.删除tracker关联event
         * 3.删除需求血缘表的tracker关联血缘
         * 4.删除obj_history
         * 以上步骤已在objectHelper.deleteObjectDuplication中实现
         * 5.删除需求关联spm待指派项
         */
        EisReqPoolSpm query = new EisReqPoolSpm();
        query.setReqPoolId(reqPoolId);
        query.setObjId(objId);
        List<EisReqPoolSpm> list = reqSpmPoolService.search(query);
        Set<Long> ids = new HashSet<>();
        for (EisReqPoolSpm reqPoolSpm : list) {
            if (!ReqPoolTypeEnum.SPM_DEV.getReqPoolType().equals(reqPoolSpm.getReqPoolType())) {
                throw new CommonException("暂时不支持非ReqPoolTypeEnum.SPM_DEV的删除");
            }
            ids.add(reqPoolSpm.getId());
        }
        List<EisTaskProcess> taskProcesses = taskProcessService.getByReqPoolEntityIds(ReqPoolTypeEnum.SPM_DEV, ids);
        if(!CollectionUtils.isEmpty(taskProcesses)){
            throw new CommonException("需求池删除项已指派给任务，需在任务中解除指派");
        }

        reqSpmPoolService.deleteByIds(ids);
        objectHelper.deleteObjectDuplication(objId,reqPoolId);
    }

    /**
     * 删除事件埋点需求池中的待办项
     *
     * @param eventPoolEntityId
     * @return
     */
    public void deleteEventPoolEntity(Long eventPoolEntityId){
        EisTaskProcess processQuery = new EisTaskProcess();
        processQuery.setReqPoolEntityId(eventPoolEntityId);
        List<EisTaskProcess> taskProcesses = taskProcessService.search(processQuery);
        if(!CollectionUtils.isEmpty(taskProcesses)){
            throw new CommonException("需求池删除项已指派给任务，需在任务中解除指派");
        }
        reqEventPoolService.deleteById(eventPoolEntityId);
        //todo:调用事件埋点删除
    }


    /**
     * 获取需求组关联任务列表
     * @param reqPoolId
     * @return
     */
    public List<ReqTaskVO> getTasks(Long reqPoolId){
        Long appId = EtContext.get(ContextConstant.APP_ID);
        List<ReqTaskVO> taskVos = new ArrayList<>();
        List<TerminalSimpleDTO> terminals = terminalService.getByAppId(appId);
        Map<Long,String> terminalMap = new HashMap<>();
        for (TerminalSimpleDTO terminal : terminals) {
            terminalMap.put(terminal.getId(),terminal.getName());
        }
        EisRequirementInfo reqQuery = new EisRequirementInfo();
        reqQuery.setReqPoolId(reqPoolId);
        List<EisRequirementInfo> requirementInfos = requirementInfoService.search(reqQuery);
        Map<Long,EisRequirementInfo> reqMap = new HashMap<>();
        for (EisRequirementInfo requirementInfo : requirementInfos) {
            reqMap.put(requirementInfo.getId(),requirementInfo);
        }

        // 存在合并基线冲突的objId
        boolean hasMergeConflicts = mergeConflictHelper.hasMergeConflict(reqPoolId);
        Set<Long> reqIds = requirementInfos.stream().map(e -> e.getId()).collect(Collectors.toSet());
        List<EisReqTask> tasks = reqTaskService.getByReqIds(reqIds);
        for (EisReqTask task : tasks) {
            ReqTaskVO vo = new ReqTaskVO();
            EisRequirementInfo requirementInfo = Optional.ofNullable(reqMap.get(task.getRequirementId()))
                    .orElse(new EisRequirementInfo());
            vo.setId(task.getId());
            vo.setTerminalId(task.getTerminalId());
            vo.setTerminal(terminalMap.get(task.getTerminalId()));
            vo.setTerminalVersion(task.getTerminalVersion());
            vo.setReqPoolId(reqPoolId);
            vo.setOwner(task.getOwnerName());
            vo.setVerifier(task.getVerifierName());
            vo.setReqIssueKey(requirementInfo.getReqIssueKey());
            vo.setReqName(requirementInfo.getReqName());
            vo.setTaskName(task.getTaskName());
            vo.setStatus(task.getStatus());
            vo.setSprint(task.getIteration());
            if (hasMergeConflicts) {
                vo.setMergeConflict(true);
            }
            taskVos.add(vo);
        }
        return taskVos;
    }
    /**
     * 待办项统计（左上角那些统计）
     * @param reqPoolId
     * @return
     */
    public ReqPoolStatisticVO getStatistic(Long reqPoolId){
        ReqPoolStatisticVO reqPoolStatisticVO = new ReqPoolStatisticVO();
        Integer allSpmsSum = 0;
        Integer assignedSpmSum = 0;
        Integer unAssignedSpmSum = 0;
        Integer releasedSpmProcessesSum = 0;
        EisReqPoolSpm spmQuery = new EisReqPoolSpm();
        spmQuery.setReqPoolId(reqPoolId);
        List<EisReqPoolSpm> reqPoolSpms = reqSpmPoolService.search(spmQuery);
        Set<Long> entityIds = reqPoolSpms.stream().map(e -> e.getId()).collect(Collectors.toSet());
        List<EisTaskProcess> taskProcesses = taskProcessService.getByReqPoolEntityIds(ReqPoolTypeEnum.SPM_DEV, entityIds);

        // 这段逻辑是为了兼容数据库中大量重复指派任务导致算出来数字是负的
        Map<String, EisTaskProcess> toMap = taskProcesses.stream().collect(Collectors.toMap(o -> o.getSpmByObjId() + "_" + o.getEventId() + "_" + o.getTaskId(), o -> o, (oldV, newV) -> oldV.getStatus() > newV.getStatus() ? oldV : newV));
        taskProcesses = new ArrayList<>(toMap.values());

        List<EisTaskProcess> spmTaskProcesses = taskProcesses.stream()
                .filter(e -> e.getReqPoolType().equals(ReqPoolTypeEnum.SPM_DEV.getReqPoolType()))
                .collect(Collectors.toList());
        List<EisTaskProcess> eventTaskProcesses = taskProcesses.stream()
                .filter(e -> e.getReqPoolType().equals(ReqPoolTypeEnum.EVENT.getReqPoolType()))
                .collect(Collectors.toList());
        for (EisTaskProcess taskProcess : spmTaskProcesses) {
            if(taskProcess.getStatus().equals(ReqTaskStatusEnum.ONLINE.getState())){
                releasedSpmProcessesSum++;
            }
        }
        allSpmsSum = reqPoolSpms.size();
        if(!CollectionUtils.isEmpty(reqPoolSpms)){
            Set<Long> reqPoolSpmEntityIds = reqPoolSpms.stream().map(e -> e.getId()).collect(Collectors.toSet());
            List<EisTaskProcess> assignedList = taskProcessService.getByReqPoolEntityIds(ReqPoolTypeEnum.SPM_DEV, reqPoolSpmEntityIds);
            // 这段逻辑是为了兼容数据库中大量重复指派任务导致算出来数字是负的
            Map<String, EisTaskProcess> assignedListToMap = assignedList.stream().collect(Collectors.toMap(o -> o.getSpmByObjId() + "_" + o.getEventId() + "_" + o.getTaskId(), o -> o, (oldV, newV) -> oldV.getStatus() > newV.getStatus() ? oldV : newV));
            assignedList = new ArrayList<>(assignedListToMap.values());
            assignedSpmSum = assignedList.size();
            unAssignedSpmSum = allSpmsSum - assignedSpmSum;
        }
        reqPoolStatisticVO.setAllSpms(allSpmsSum);
        reqPoolStatisticVO.setAssignedSpms(allSpmsSum - releasedSpmProcessesSum);
        reqPoolStatisticVO.setUnAssignedSpms(unAssignedSpmSum);

        Integer eventsSum = 0;
        Integer assignedEventsSum = 0;
        Integer unAssignedEventsSum = 0;
        Integer releasedEventProcessesSum = 0;
        EisReqPoolEvent reqEventQuery = new EisReqPoolEvent();
        reqEventQuery.setReqPoolId(reqPoolId);
        List<EisReqPoolEvent> reqPoolEvents = reqEventPoolService.search(reqEventQuery);
        if(!CollectionUtils.isEmpty(reqPoolEvents)){
            eventsSum = reqPoolEvents.size();
            Set<Long> reqEventPoolEntityIds = reqPoolEvents.stream().map(e -> e.getId()).collect(Collectors.toSet());
            List<EisTaskProcess> assignedList = taskProcessService.getByReqPoolEntityIds(ReqPoolTypeEnum.EVENT, reqEventPoolEntityIds);
            assignedEventsSum = assignedList.size();
            unAssignedEventsSum = eventsSum - assignedEventsSum;
        }
        for (EisTaskProcess taskProcess : eventTaskProcesses) {
            if(taskProcess.getStatus().equals(ReqTaskStatusEnum.ONLINE.getState())){
                releasedEventProcessesSum++;
            }
        }
        reqPoolStatisticVO.setAllEvents(eventsSum);
        reqPoolStatisticVO.setAssignedEvents(eventsSum - releasedEventProcessesSum);
        reqPoolStatisticVO.setUnAssignedEvents(unAssignedEventsSum);

        Integer taskSum = 0;
        EisRequirementInfo reqQuery = new EisRequirementInfo();
        reqQuery.setReqPoolId(reqPoolId);
        List<EisRequirementInfo> requirementInfos = requirementInfoService.search(reqQuery);
        if(!CollectionUtils.isEmpty(requirementInfos)){
            Set<Long> reqIds = requirementInfos.stream().map(e -> e.getId()).collect(Collectors.toSet());
            taskSum = reqTaskService.getByReqIds(reqIds).size();
        }
        reqPoolStatisticVO.setTasks(taskSum);
        List<EisObjChangeHistory> byReqPoolIds = objChangeHistoryService.getByReqPoolId(reqPoolId);
        reqPoolStatisticVO.setObjCount(byReqPoolIds.size());
        return reqPoolStatisticVO;
    }

    /**
     * 基线变更编辑页面
     * @param reqPoolId
     * @return
     */
    public RebaseEditVO getRebaseEditVo(Long reqPoolId) {
        Long appId = EtContext.get(ContextConstant.APP_ID);

        // 查询需求组关联基线
        EisReqPoolRelBaseRelease query = new EisReqPoolRelBaseRelease();
        query.setReqPoolId(reqPoolId);
        List<EisReqPoolRelBaseRelease> reqPoolRelBaseReleases = reqPoolRelBaseService.search(query);

        // 端版本名字映射
        EisTerminalVersionInfo terminalVersionInfoQuery = new EisTerminalVersionInfo();
        terminalVersionInfoQuery.setAppId(appId);
        List<EisTerminalVersionInfo> terminalVersionInfos = terminalVersionInfoService.search(terminalVersionInfoQuery);
        Map<Long,String> terminalVersionIdtoNameMap = new HashMap<>();
        for (EisTerminalVersionInfo terminalVersionInfo : terminalVersionInfos) {
            terminalVersionIdtoNameMap.put(terminalVersionInfo.getId(),terminalVersionInfo.getName());
        }

        // 所有releaseHistoryId的映射
        Map<Long, EisTerminalReleaseHistory> releaseHistoryMap = new HashMap<>();
        EisTerminalReleaseHistory releaseHistoryQuery = new EisTerminalReleaseHistory();
        releaseHistoryQuery.setAppId(appId);
        List<EisTerminalReleaseHistory> releaseHistories = terminalReleaseService.search(releaseHistoryQuery);
        for (EisTerminalReleaseHistory releaseHistory : releaseHistories) {
            releaseHistoryMap.put(releaseHistory.getId(), releaseHistory);
        }

        boolean hasMergeConflict = mergeConflictHelper.hasMergeConflict(reqPoolId);

        List<TerminalSimpleDTO> terminals = terminalService.getByAppId(appId);
        Map<Long, List<EisReqPoolRelBaseRelease>> relBaseGroupByTerminalId = reqPoolRelBaseReleases.stream().collect(Collectors.groupingBy(EisReqPoolRelBaseRelease::getTerminalId));
        List<RebaseEditVO.TerminalBaseVO> resultList = new ArrayList<>();
        for (TerminalSimpleDTO terminal : terminals) {
            RebaseEditVO.TerminalBaseVO terminalBaseVo = getTerminalBaseVO(reqPoolId, terminal, relBaseGroupByTerminalId.get(terminal.getId()), releaseHistoryMap, terminalVersionIdtoNameMap, hasMergeConflict);
            if (terminalBaseVo != null) {
                resultList.add(terminalBaseVo);
            }
        }
        return new RebaseEditVO().setTerminalBases(resultList);
    }

    private RebaseEditVO.TerminalBaseVO getTerminalBaseVO(Long reqPoolId, TerminalSimpleDTO terminal, List<EisReqPoolRelBaseRelease> reqPoolRelBaseReleases,
                                                         Map<Long, EisTerminalReleaseHistory> releaseHistoryMap,
                                                         Map<Long,String> terminalVersionIdtoNameMap,
                                                         boolean hasMergeConflict) {
        if (CollectionUtils.isEmpty(reqPoolRelBaseReleases)) {
            return null;
        }
        Long terminalId = terminal.getId();
        String terminalName = terminal.getName();
        EisReqPoolRelBaseRelease firstRelease = reqPoolRelBaseService.getFirstUse(reqPoolId, terminalId);
        EisReqPoolRelBaseRelease currentUseRelease = reqPoolRelBaseReleases.stream().filter(EisReqPoolRelBaseRelease::getCurrentUse).findFirst().orElse(null);
        Long firstBaseReleaseId = firstRelease == null ? 0L : firstRelease.getBaseReleaseId();
        long currentBaseReleaseId = currentUseRelease == null ? 0L : currentUseRelease.getBaseReleaseId();
        boolean autoRebase = currentUseRelease == null ? true : Boolean.TRUE.equals(currentUseRelease.getAutoRebase());

        RebaseEditVO.TerminalBaseVO result = new RebaseEditVO.TerminalBaseVO();
        EisTerminalReleaseHistory newestRelease = terminalReleaseService.getLatestRelease(terminalId);
        if (newestRelease == null) {
            return null;
        }
        result.setTerminalId(terminalId);
        result.setTerminalName(terminalName);
        EisTerminalReleaseHistory first = releaseHistoryMap.get(firstBaseReleaseId);
        Long firstBaseTerminalVersionId = first == null ? null : first.getTerminalVersionId();
        result.setFirstTerminalVersion(firstBaseTerminalVersionId == null ? "无" : terminalVersionIdtoNameMap.get(firstBaseTerminalVersionId));
        EisTerminalReleaseHistory current = releaseHistoryMap.get(currentBaseReleaseId);
        Long currentBaseTerminalVersionId = current == null ? null : current.getTerminalVersionId();
        result.setCurrentTerminalVersion(currentBaseTerminalVersionId == null ? "无" : terminalVersionIdtoNameMap.get(currentBaseTerminalVersionId));
        List<RebaseEditVO.TerminalSelectionVO> selections = getTerminalSelections(newestRelease, terminalVersionIdtoNameMap, releaseHistoryMap, currentUseRelease, hasMergeConflict);
        Set<Long> selectionIds = selections.stream().map(o -> o.getBaseReleaseId()).collect(Collectors.toSet());
        result.setSelections(selections);
        long currentSelection = autoRebase ? -currentBaseReleaseId : currentBaseReleaseId;
        // 这是为了兼容历史数据，处理没触发自动变基的情况
        if (currentSelection < 0L && !selectionIds.contains(currentSelection)) {
            currentSelection = Math.abs(currentSelection);
        }
        result.setCurrentBaseReleaseId(currentSelection);
        return result;
    }

    private List<RebaseEditVO.TerminalSelectionVO> getTerminalSelections(EisTerminalReleaseHistory newestRelease,
                                                                         Map<Long,String> terminalVersionIdtoNameMap,
                                                                         Map<Long, EisTerminalReleaseHistory> releaseHistoryMap,
                                                                         EisReqPoolRelBaseRelease currentUseRelease,
                                                                         boolean hasMergeConflict) {
        if (currentUseRelease == null) {
            throw new CommonException("未找到当前在使用的版本");
        }
        Long currentBaseReleaseId = currentUseRelease.getBaseReleaseId();
        boolean autoRebase = Boolean.TRUE.equals(currentUseRelease.getAutoRebase());

        // 合并冲突时，只展示当前选择
        if (hasMergeConflict) {
            EisTerminalReleaseHistory current = releaseHistoryMap.get(currentBaseReleaseId);

            Long currentBaseTerminalVersionId = current == null ? null : current.getTerminalVersionId();

            RebaseEditVO.TerminalSelectionVO currentTerminalSelectionVo = new RebaseEditVO.TerminalSelectionVO();
            currentTerminalSelectionVo.setBaseReleaseId(currentBaseReleaseId);
            currentTerminalSelectionVo.setTerminalVersionName(autoRebase ? "冲突处理中(" +
                    (currentBaseTerminalVersionId == null ? "无" : terminalVersionIdtoNameMap.get(currentBaseTerminalVersionId)) +
                    ")" : terminalVersionIdtoNameMap.get(currentBaseTerminalVersionId));
            List<RebaseEditVO.TerminalSelectionVO> terminalSelections = new ArrayList<>();
            terminalSelections.add(currentTerminalSelectionVo);
            return terminalSelections;
        }

        // 最新基线（非自动变基）
        Long newestReleaseId = newestRelease.getId();
        Long terminalVersionId = newestRelease.getTerminalVersionId();
        String versionName = terminalVersionIdtoNameMap.get(terminalVersionId);
        RebaseEditVO.TerminalSelectionVO newestTerminalSelectionVo = new RebaseEditVO.TerminalSelectionVO();
        newestTerminalSelectionVo.setBaseReleaseId(newestReleaseId);
        newestTerminalSelectionVo.setTerminalVersionName(versionName);

        List<RebaseEditVO.TerminalSelectionVO> terminalSelections = new ArrayList<>();


        // 最新基线（自动变基）
        RebaseEditVO.TerminalSelectionVO autoRebaseVO = JsonUtils.parseObject(JsonUtils.toJson(newestTerminalSelectionVo), RebaseEditVO.TerminalSelectionVO.class);
        if (autoRebaseVO == null) {
            throw new CommonException("autoRebaseVO is null"); // 这个应该不会抛
        }
        autoRebaseVO.setTerminalVersionName("最新(" + autoRebaseVO.getTerminalVersionName() + ")");
        autoRebaseVO.setBaseReleaseId(-newestReleaseId);    // 负数表示自动变基


        // 当前基线的选项
        releaseHistoryMap.put(newestRelease.getId(), newestRelease);
        EisTerminalReleaseHistory current = releaseHistoryMap.get(currentBaseReleaseId);
        Long currentBaseTerminalVersionId = current == null ? null : current.getTerminalVersionId();
        RebaseEditVO.TerminalSelectionVO currentTerminalSelectionVo = null;
        if(!currentBaseReleaseId.equals(newestReleaseId)){
            currentTerminalSelectionVo = new RebaseEditVO.TerminalSelectionVO();
            currentTerminalSelectionVo.setBaseReleaseId(currentBaseReleaseId);
            currentTerminalSelectionVo.setTerminalVersionName(currentBaseTerminalVersionId == null ? "无" : terminalVersionIdtoNameMap.get(currentBaseTerminalVersionId));
        }

        // 按顺序插入结果
        terminalSelections.add(autoRebaseVO);
        terminalSelections.add(newestTerminalSelectionVo);
        if (currentTerminalSelectionVo != null) {
            terminalSelections.add(currentTerminalSelectionVo);
        }
        return terminalSelections;
    }

    public void rebaseAllMessageAsync(Long appId, Long terminalId) {
        if (appId == null || terminalId == null) {
            return;
        }
        rebaseExecutor.submit(() -> rebaseAllToLatest(appId, terminalId));
    }

    public void rebaseAllToLatest(Long appId, Long terminalId) {
        log.info("自动变基开始, appId={} terminalId={}", appId, terminalId);
        EtContext.put(ContextConstant.APP_ID, appId);
        UserDTO currUser = new UserDTO();
        currUser.setId(0L);
        currUser.setEmail("自动变基");
        currUser.setUserName("自动变基");
        EtContext.put(ContextConstant.USER, currUser);
        EisReqPool q = new EisReqPool();
        q.setAppId(appId);
        List<EisReqPool> allReqPools = reqPoolBasicService.search(q);
        if (CollectionUtils.isEmpty(allReqPools)) {
            return;
        }
        allReqPools.forEach(o -> {
            // 跳过3个月没有动作的需求池
            if (o.getUpdateTime().getTime() < System.currentTimeMillis() - 3 * 30 * 86400000L) {
                return;
            }
            String lockKey = "rebaseAllToLatest_" + appId + "_" + o.getId() + "_" + terminalId;
            boolean lockSuccess = lockService.tryLock(lockKey, 30000L);
            if (!lockSuccess) {
                log.error("需求池自动变基失败, 上次变基仍在进行中，appId={} reqPoolId={} terminalId={}", appId, o.getId(), terminalId);
                return;
            }
            try {
                rebaseReqPoolToLatest(o.getId(), terminalId);
            } catch (Exception e) {
                log.error("需求池自动变基失败, appId={} reqPoolId={} terminalId={}", appId, o.getId(), terminalId, e);
            } finally {
                lockService.releaseLock(lockKey);
            }
        });
    }

    /**
     * 变到最新基线
     */
    public void rebaseReqPoolToLatest(Long reqPoolId, Long paramTerminalId) {
        if (mergeConflictHelper.hasMergeConflict(reqPoolId)) {
            log.info("自动变基失败，冲突未解决，reqPoolId={}", reqPoolId);
            return;
        }
        EisReqPoolRelBaseRelease q = new EisReqPoolRelBaseRelease();
        q.setReqPoolId(reqPoolId);
        q.setCurrentUse(true);
        List<EisReqPoolRelBaseRelease> rebaseSettings = reqPoolRelBaseService.search(q);
        if (CollectionUtils.isEmpty(rebaseSettings)) {
            return;
        }
        rebaseSettings.forEach(rebaseSetting -> {
            if (!Boolean.TRUE.equals(rebaseSetting.getAutoRebase())) {
                return;
            }
            // 检查是否需要变基，只变基指定的端
            Long terminalId = rebaseSetting.getTerminalId();
            if (terminalId == null) {
                return;
            }
            // 参数指定了只rebase某个端
            if (paramTerminalId != null && !paramTerminalId.equals(terminalId)) {
                return;
            }
            EisTerminalReleaseHistory latestRelease = terminalReleaseService.getLatestRelease(terminalId);
            if (latestRelease == null) {
                return;
            }
            Long targetReleaseId = latestRelease.getId();
            Long currentReleaseId = rebaseSetting.getBaseReleaseId();
            if (targetReleaseId == null || currentReleaseId == null) {
                return;
            }
            if (targetReleaseId <= currentReleaseId) {
                log.info("自动变基跳过, 无需变基，reqPoolId={} terminalId={} targetReleaseId={}", reqPoolId, terminalId, targetReleaseId);
                return;
            }
            // 需要变基到最新基线
            try {
                requirementPoolHelper.rebaseForReqSpms(reqPoolId, terminalId, targetReleaseId, true);
                log.info("自动变基成功，reqPoolId={} terminalId={} targetReleaseId={}", reqPoolId, terminalId, targetReleaseId);
            } catch (Exception e) {
                log.warn("自动变基失败，reqPoolId={} terminalId={} targetReleaseId={}", reqPoolId, terminalId, targetReleaseId, e);
            }
        });
    }

    /**
     * 需求组基线变更
     * @param rebaseVo
     */
    @MethodLog
    @Transactional(rollbackFor = Throwable.class)
    public void reBase(RebaseVO rebaseVo){
        if (mergeConflictHelper.hasMergeConflict(rebaseVo.getReqPoolId())) {
            throw new CommonException("当前需求池存在冲突未解决，无法手动变基");
        }
        RebaseDTO rebaseDTO = convertToRebaseDTO(rebaseVo);
        Map<Long, TerminalBaseDTO> currentMap = new HashMap<>();
        RebaseEditVO rebaseEditVo = getRebaseEditVo(rebaseVo.getReqPoolId());
        if (rebaseEditVo == null) {
            throw new CommonException("查询当前基线失败");
        }
        List<RebaseEditVO.TerminalBaseVO> terminalBases = rebaseEditVo.getTerminalBases();
        if (CollectionUtils.isNotEmpty(terminalBases)) {
            for (RebaseEditVO.TerminalBaseVO terminalBase : terminalBases) {
                currentMap.put(terminalBase.getTerminalId(), convertToTerminalRebaseDTO(terminalBase));
            }
        }

        List<TerminalBaseDTO> details = rebaseDTO.getDetails();
        for (TerminalBaseDTO detail : details) {
            // 如果基线未改变，无需执行变更
            TerminalBaseDTO currentBaseDTO = currentMap.get(detail.getTerminalId());
            if (currentBaseDTO != null) {
                Long currentReleaseId = currentBaseDTO.getReleaseId();
                if (currentReleaseId != null && currentReleaseId.equals(detail.getReleaseId())) {
                    requirementPoolHelper.updateAutoRebaseOnly(rebaseVo.getReqPoolId(), detail.getTerminalId(), detail.isAutoRebase());
                    continue;
                }
            }
            // 更新autoRebase
            log.info("rebaseForReqSpms() reqPoolId={} terminalId={} newReleaseId={} autoRebase={}", rebaseVo.getReqPoolId(),detail.getTerminalId(),detail.getReleaseId(), detail.isAutoRebase());
            requirementPoolHelper.rebaseForReqSpms(rebaseVo.getReqPoolId(), detail.getTerminalId(), detail.getReleaseId(), detail.isAutoRebase());
        }
    }

    /**
     * 基线变更历史
     * @param reqPoolId
     * @param terminalId
     * @return
     */
    public List<RelBaseReleaseHistoryVO> getBaseChangeHitories(Long reqPoolId,Long terminalId){
        Long appId = EtContext.get(ContextConstant.APP_ID);
        EisReqPoolRelBaseRelease query = new EisReqPoolRelBaseRelease();
        query.setReqPoolId(reqPoolId);
        query.setTerminalId(terminalId);
        List<EisReqPoolRelBaseRelease> reqPoolRelBaseReleases = reqPoolRelBaseService.search(query);
        if(CollectionUtils.isEmpty(reqPoolRelBaseReleases)){
            return new ArrayList<>();
        }
        EisTerminalVersionInfo terminalVersionInfoQuery = new EisTerminalVersionInfo();
        terminalVersionInfoQuery.setAppId(appId);
        List<EisTerminalVersionInfo> terminalVersionInfos = terminalVersionInfoService.search(terminalVersionInfoQuery);
        Map<Long,String> terminalIdtoNameMap = new HashMap<>();
        for (EisTerminalVersionInfo terminalVersionInfo : terminalVersionInfos) {
            terminalIdtoNameMap.put(terminalVersionInfo.getId(),terminalVersionInfo.getName());
        }
        List<EisTerminalReleaseHistory> releaseHistories = terminalReleaseService.getByIds(reqPoolRelBaseReleases.stream().map(e -> e.getBaseReleaseId()).collect(Collectors.toSet()));
        Map<Long,EisTerminalReleaseHistory> releaseHistoryMap = new HashMap<>();
        for (EisTerminalReleaseHistory releaseHistory : releaseHistories) {
            releaseHistoryMap.put(releaseHistory.getId(),releaseHistory);
        }
        reqPoolRelBaseReleases = reqPoolRelBaseReleases.stream().sorted((a,b) -> b.getCreateTime().compareTo(a.getCreateTime())).collect(Collectors.toList());
        List<RelBaseReleaseHistoryVO> resultList = new ArrayList<>();
        for (EisReqPoolRelBaseRelease reqPoolRelBaseRelease : reqPoolRelBaseReleases) {
            Long releaseId = reqPoolRelBaseRelease.getBaseReleaseId();
            EisTerminalReleaseHistory releaseHistory = releaseHistoryMap.get(releaseId);
            RelBaseReleaseHistoryVO vo = new RelBaseReleaseHistoryVO();
            vo.setCreateTime(reqPoolRelBaseRelease.getCreateTime().getTime());
            vo.setCreateUser(reqPoolRelBaseRelease.getCreateName());
            if(releaseHistory != null){
                vo.setTerminalVersion(terminalIdtoNameMap.get(releaseHistory.getTerminalVersionId()));
            }
            resultList.add(vo);
        }
        return resultList;
    }

    /**
     * 若当前需求组某个端的基线不是最新的，则返回true，前端提示
     * @param reqPoolId
     * @return
     */
    public boolean newBaseReleaseNotice(Long reqPoolId){
        EisReqPoolRelBaseRelease query = new EisReqPoolRelBaseRelease();
        query.setReqPoolId(reqPoolId);
        query.setCurrentUse(true);
        List<EisReqPoolRelBaseRelease> reqPoolRelBaseReleases = reqPoolRelBaseService.search(query);
        Map<Long,Long> terminalIdToBaseReleaseId = new HashMap<>();
        for (EisReqPoolRelBaseRelease reqPoolRelBaseRelease : reqPoolRelBaseReleases) {
            terminalIdToBaseReleaseId.put(reqPoolRelBaseRelease.getTerminalId(),reqPoolRelBaseRelease.getBaseReleaseId());
        }
        for (Long terminalId : terminalIdToBaseReleaseId.keySet()) {
            EisTerminalReleaseHistory terminalReleaseHistory = terminalReleaseService.getLatestRelease(terminalId);
            if(terminalReleaseHistory == null){
                continue;
            }
            if(!terminalIdToBaseReleaseId.get(terminalId).equals(terminalReleaseHistory.getId())){
                return true;
            }
        }
        return false;
    }

    public boolean isEditable(Long reqPoolId){
        EisReqPool eisReqPool = reqPoolBasicService.getById(reqPoolId);
        return eisReqPool.getEditable();
    }


    public String getSpm(JSONObject jsonObject){
        String spmByObjId = (String)jsonObject.get("spmByObjId");
        List<Long> objIdsList = Lists.newArrayList(spmByObjId.split("\\|"))
                .stream().map(e -> Long.valueOf(e)).collect(Collectors.toList());
        List<ObjectBasic> objectBasics = objectBasicService.getByIds(objIdsList);
        List<String> oidList = new ArrayList<>();
        Map<Long,String> objIdToOidMap = new HashMap<>();
        for (ObjectBasic objectBasic : objectBasics) {
            objIdToOidMap.put(objectBasic.getId(),objectBasic.getOid());
        }
        for (Long objId : objIdsList) {
            oidList.add(objIdToOidMap.get(objId));
        }
        String spm = String.join("|",oidList);
        return spm;
    }

    private static RebaseDTO convertToRebaseDTO(RebaseVO rebaseVO) {
        RebaseDTO result = new RebaseDTO();
        result.setReqPoolId(rebaseVO.getReqPoolId());
        if (rebaseVO.getDetails() != null) {
            result.setDetails(rebaseVO.getDetails().stream().map(o -> {
                TerminalBaseDTO terminalBaseDTO = new TerminalBaseDTO();
                terminalBaseDTO.setTerminalId(o.getTerminalId());
                terminalBaseDTO.setReleaseId(Math.abs(o.getNewReleaseId()));
                terminalBaseDTO.setAutoRebase(o.getNewReleaseId() < 0L);
                return terminalBaseDTO;
            }).collect(Collectors.toList()));
        }
        return result;
    }

    private static TerminalBaseDTO convertToTerminalRebaseDTO(RebaseEditVO.TerminalBaseVO terminalBaseVO) {
        TerminalBaseDTO result = new TerminalBaseDTO();
        result.setTerminalId(terminalBaseVO.getTerminalId());
        result.setReleaseId(terminalBaseVO.getCurrentBaseReleaseId() == null ? null : Math.abs(terminalBaseVO.getCurrentBaseReleaseId()));
        result.setAutoRebase(terminalBaseVO.getCurrentBaseReleaseId() == null ? true : terminalBaseVO.getCurrentBaseReleaseId() < 0L);
        return result;
    }
}
