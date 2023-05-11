package com.netease.hz.bdms.easyinsight.service.facade;

import com.google.common.collect.*;
import com.netease.hz.bdms.easyinsight.service.helper.LineageHelper;
import com.netease.hz.bdms.easyinsight.service.helper.ParamBindHelper;
import com.netease.hz.bdms.easyinsight.service.service.*;
import com.netease.hz.bdms.easyinsight.common.bo.lineage.LinageGraph;
import com.netease.hz.bdms.easyinsight.common.bo.lineage.NodeOfTestTree;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.checkhistory.CheckHistoryAggreDTO;
import com.netease.hz.bdms.easyinsight.common.dto.checkhistory.CheckHistorySimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.PagingResultDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.PagingSortDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.dto.event.EventSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.obj.param.ParamWithValueItemDTO;
import com.netease.hz.bdms.easyinsight.common.dto.obj.tracker.event.ObjTrackerEventSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.ParamValueSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.parambind.ParamBindItemDTO;
import com.netease.hz.bdms.easyinsight.common.dto.realtimetest.RealTimeTestResourceDTO;
import com.netease.hz.bdms.easyinsight.common.dto.realtimetest.TestHistoryRecordDTO;
import com.netease.hz.bdms.easyinsight.common.dto.realtimetest.TestTreeVO;
import com.netease.hz.bdms.easyinsight.common.dto.terminal.TerminalSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.version.VersionSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.enums.EntityTypeEnum;
import com.netease.hz.bdms.easyinsight.common.enums.ParamTypeEnum;
import com.netease.hz.bdms.easyinsight.common.enums.ParamValueTypeEnum;
import com.netease.hz.bdms.easyinsight.common.enums.ReqPoolTypeEnum;
import com.netease.hz.bdms.easyinsight.common.exception.CommonException;
import com.netease.hz.bdms.easyinsight.common.util.CommonUtil;
import com.netease.hz.bdms.easyinsight.common.vo.realtimetest.ReqNode;
import com.netease.hz.bdms.easyinsight.common.vo.task.TaskEventVO;
import com.netease.hz.bdms.easyinsight.dao.model.*;
import com.netease.hz.bdms.easyinsight.service.service.impl.ReqTaskProcessService;
import com.netease.hz.bdms.easyinsight.dao.model.ObjMappings;
import com.netease.hz.bdms.easyinsight.service.service.impl.EventCheckHistoryService;
import com.netease.hz.bdms.easyinsight.service.service.obj.AllTrackerReleaseService;
import com.netease.hz.bdms.easyinsight.service.service.obj.EventBuryPointService;
import com.netease.hz.bdms.easyinsight.service.service.obj.ObjTerminalTrackerService;
import com.netease.hz.bdms.easyinsight.service.service.requirement.*;
import com.netease.hz.bdms.easyinsight.service.service.terminalrelease.TerminalReleaseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class BuryPointTestFacade {

    @Autowired
    ObjectBasicService objectBasicService;

    @Autowired
    ReqSpmPoolService reqSpmPoolService;

    @Autowired
    RequirementInfoService requirementInfoService;

    @Autowired
    ReqPoolRelBaseService reqPoolRelBaseService;

    @Autowired
    TerminalReleaseService terminalReleaseService;

    @Autowired
    ObjTerminalTrackerService objTerminalTrackerService;

    @Autowired
    AllTrackerReleaseService allTrackerReleaseService;

    @Autowired
    ObjTrackerEventService objTrackerEventService;

    @Autowired
    EventService eventService;

    @Autowired
    TerminalService terminalService;

    @Autowired
    ReqTaskService reqTaskService;

    @Autowired
    TaskProcessService taskProcessService;

    @Autowired
    ParamBindHelper paramBindHelper;

    @Autowired
    LineageHelper lineageHelper;

    @Autowired
    SpmCheckHistoryService spmCheckHistoryService;
    @Autowired
    EventCheckHistoryService eventCheckHistoryService;

    @Autowired
    TerminalVersionInfoService terminalVersionInfoService;

    @Autowired
    RealTimeTestRecordService realTimeTestRecordService;

    @Autowired
    private VersionService versionService;

    @Autowired
    EventBuryPointService eventBuryPointService;

    @Autowired
    ReqTaskFacade reqTaskFacade;

    @Autowired
    ReqTaskProcessService reqTaskProcessService;

    /**
     * 实时测试获取源数据
     * @author yufangzheng
     * @param
     * @return
     */
    public RealTimeTestResourceDTO getResourceDto(Long taskId,Long terminalId){
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Long baseReleaseId = null;
        Long reqPoolId= 0L;
        LinageGraph linageGraph = null;
        Long terminalVersionId = null;
        List<EisObjTerminalTracker> trackers = new ArrayList<>();
        List<List<Long>> spmsAsObjIdList = new ArrayList<>();
        if(taskId != null){
            EisReqTask task = reqTaskService.getById(taskId);
            if (task == null) {
                throw new CommonException("taskId " + taskId + " 不存在");
            }
            Long requirementId = task.getRequirementId();
            EisRequirementInfo requirementInfo = requirementInfoService.getById(requirementId);
            if (requirementInfo == null) {
                throw new CommonException("requirementId " + requirementId + " 不存在");
            }
            reqPoolId = requirementInfo.getReqPoolId();
            EisReqPoolRelBaseRelease reqPoolRelBaseRelease = reqPoolRelBaseService.getCurrentUse(reqPoolId,terminalId);

            if(reqPoolRelBaseRelease != null){
                baseReleaseId = reqPoolRelBaseRelease.getBaseReleaseId();
                // 查找基线版本
                if (baseReleaseId != null) {
                    EisTerminalReleaseHistory baseRelease = terminalReleaseService.getById(baseReleaseId);
                    if (baseRelease != null) {
                        terminalVersionId = baseRelease.getTerminalVersionId();
                    }
                }else {
                    log.error("该任务无基线，无法测试，taslId={}, terminalId={}", taskId, terminalId);
                    return null;
                }
            }

            List<EisTaskProcess> processes = reqTaskProcessService.queryProcessByTerminalVersion(terminalId, terminalVersionId, Collections.singleton(taskId));
            Set<Long> spmPoolEntityIdsOfType = processes.stream().map(EisTaskProcess::getReqPoolEntityId).collect(Collectors.toSet());
            List<EisReqPoolSpm> spmEntitiesOfType = reqSpmPoolService.getBatchByIds(spmPoolEntityIdsOfType);
            for (EisReqPoolSpm spmEntity : spmEntitiesOfType) {
                String spmByObjId = spmEntity.getSpmByObjId();
                List<Long> spmByObjIdList = Arrays.stream(spmByObjId.split("\\|")).map(Long::valueOf).collect(Collectors.toList());
                spmsAsObjIdList.add(spmByObjIdList);
            }
//            Map<Long,Set<Long>> reqRelation = reqTaskProcessService.queryObjRelationsFromProcess(processes);
//            allObjRelation = lineageHelper.combineReqRelation(reqRelation, allObjRelation);
            linageGraph = lineageHelper.genReqLinageGraph(baseReleaseId,terminalId,reqPoolId);
            trackers = getTrackersOfReqPoolAndBase(reqPoolId,terminalId);
        } else {
            EisTerminalReleaseHistory latestRelease = terminalReleaseService.getLatestRelease(terminalId);
            baseReleaseId = latestRelease.getId();
            terminalVersionId = latestRelease.getTerminalVersionId();
            linageGraph = lineageHelper.genReleasedLinageGraph(baseReleaseId);
            trackers = getTrackerOfBase(baseReleaseId);
        }


        List<EventSimpleDTO> eventsOfCurrentApp = eventService.searchEvent(null,appId,null,null,null,null);
        Map<String,String> allEventNameMap = new HashMap<>();
        Map<Long,String> allEventCodeMap = new HashMap<>();
        Map<Long,Long> allEventVersionMap = new HashMap<>();
        for (EventSimpleDTO eventSimpleDTO : eventsOfCurrentApp) {
            allEventNameMap.put(eventSimpleDTO.getCode(),eventSimpleDTO.getName());
            allEventCodeMap.put(eventSimpleDTO.getId(), eventSimpleDTO.getCode());

            //获取事件对应的当前参数版本
            List<VersionSimpleDTO> versions = versionService
                    .searchVersion(eventSimpleDTO.getId(), 2, null, appId, null, null, null, null);
            for(VersionSimpleDTO versionSimpleDTO : versions){
                if(versionSimpleDTO.getCurrentUsing().equals(true)){
                    allEventVersionMap.put(eventSimpleDTO.getId(), versionSimpleDTO.getId());
                }
            }
        }


        RealTimeTestResourceDTO realTimeTestResourceDTO = new RealTimeTestResourceDTO();

        Set<Long> eventIds = new HashSet<>();
        Map<String,Long> eventBuryPointMap = new HashMap<>();
        if(taskId != null){

            EisTaskProcess processQuery = new EisTaskProcess();
            processQuery.setTaskId(taskId);
            List<EisTaskProcess> taskProcesses = taskProcessService.search(processQuery);
            List<TaskEventVO> taskEventVos = reqTaskFacade.getTaskEventVos(taskProcesses);

//            EisReqPoolEvent query = new EisReqPoolEvent();
//            query.setReqPoolId(reqPoolId);
//            List<EisReqPoolEvent> reqPoolEvents = reqEventPoolService.search(query);

            for(TaskEventVO taskEventVO : taskEventVos){
                EisEventBuryPoint eventBuryPoint = eventBuryPointService.getById(taskEventVO.getEventBuryPointId());
                allEventVersionMap.put(eventBuryPoint.getEventId(), eventBuryPoint.getEventParamPackageId());
                eventIds.add(eventBuryPoint.getEventId());
                eventBuryPointMap.put(taskEventVO.getEventCode(), taskEventVO.getEventBuryPointId());
            }
//            eventIds.addAll(reqPoolEvents.stream().map(EisReqPoolEvent::getEventId).collect(Collectors.toList()));
//            List<EventSimpleDTO> events = eventService.getEventByIds(eventIds);
//            Map<Long,String> eventIdToCodeMap = events.stream().collect(Collectors.toMap(EventSimpleDTO::getId, EventSimpleDTO::getCode));
//            for(EisReqPoolEvent eisReqPoolEvent : reqPoolEvents){
//                String eventCode = eventIdToCodeMap.get(eisReqPoolEvent.getEventId());
//                if(StringUtils.isNotBlank(eventCode)){
//                    eventBuryPointMap.put(eventCode, eisReqPoolEvent.getEventBuryPointId());
//                }
//            }
        }
        //
        eventIds.addAll(eventsOfCurrentApp.stream().map(EventSimpleDTO::getId).collect(Collectors.toList()));

        Set<Long> allNeedObjIds = spmsAsObjIdList.stream().flatMap(Collection::stream).collect(Collectors.toSet());
        ObjMappings objMappings = objectBasicService.getMapping(appId, allNeedObjIds);
        Map<String,RealTimeTestResourceDTO.Linage> SpmAndLineageMapOfAll = getSpmToLineageMapForRealTimeTest(linageGraph,trackers, objMappings);
        Set<Long> trackerIds = trackers.stream().map(e -> e.getId()).collect(Collectors.toSet());
        List<RealTimeTestResourceDTO.ObjMeta> allTrackersOfLineage = getObjMeTa(new ArrayList<>(trackers));
        List<ParamWithValueItemDTO> eventPublicParams = getEventPublicParams(new HashSet<>(trackerIds), eventIds);
        List<ParamWithValueItemDTO> globalPublicParams = getGlobalPublicParams(terminalId);
        TerminalSimpleDTO terminalSimpleDTO = terminalService.getById(terminalId);

        Set<String> spmByOids = new HashSet<>();
        for(List<Long> spmByObjIds : spmsAsObjIdList) {
            String spmByObjId = CommonUtil.getSpmStringByObjIds(spmByObjIds);
            String spmByOid = CommonUtil.transSpmByObjIdToSpmByOid(objMappings.getObjIdToOidMap(), spmByObjId);
            spmByOids.add(spmByOid);
        }

//        AuditSpmInfoDto auditSpmInfoDto = getSpmFromLineageMap(linageGraph, allObjRelation.keySet());

        realTimeTestResourceDTO.setAppId(appId);
        realTimeTestResourceDTO.setObjMetas(allTrackersOfLineage);
        realTimeTestResourceDTO.setLinageMap(SpmAndLineageMapOfAll);
        realTimeTestResourceDTO.setTerminal(terminalSimpleDTO.getName());
        realTimeTestResourceDTO.setGlobalPublicParams(globalPublicParams);
        realTimeTestResourceDTO.setEventPublicParams(eventPublicParams);
        realTimeTestResourceDTO.setAllEventNameMap(allEventNameMap);
        realTimeTestResourceDTO.setAllObjNameMap(objMappings.getAllObjNameMap());
        realTimeTestResourceDTO.setEventBuryPointMap(eventBuryPointMap);
        realTimeTestResourceDTO.setAllEventCodeMap(allEventCodeMap);
        realTimeTestResourceDTO.setAllEventVersionMap(allEventVersionMap);
        realTimeTestResourceDTO.setUpdateSpmInfo(spmByOids);

        // 组装基线名称
        if (terminalVersionId != null) {
            EisTerminalVersionInfo terminalVersionInfo = terminalVersionInfoService.getById(terminalVersionId);
            if (terminalVersionInfo != null) {
                realTimeTestResourceDTO.setBaseLineName(terminalVersionInfo.getName());
            }
        }

//        realTimeTestResourceDTO.setRoutePath2OidMap(routePath2OidMap);
        return realTimeTestResourceDTO;
    }

    public CheckHistoryAggreDTO getCheckHistoryAggreDTO(Long processId) {
        return spmCheckHistoryService.aggregateCheckHistory(processId);
    }

    public PagingResultDTO<TestHistoryRecordDTO> getTestHistoryRecords(Long code, Long taskId, Integer result, Long userId, String reqName, String terminal, String baseVer, Long startTime, Long endTime, Long appId, PagingSortDTO pagingSortDTO) {

        Integer totalNum = 0;

        List<TestHistoryRecordDTO> testHistoryRecordDTOS = realTimeTestRecordService.getTestHistory(code, taskId, result, userId, reqName, terminal, baseVer, startTime, endTime, appId,
                pagingSortDTO.getOrderBy(), pagingSortDTO.getOrderRule(), pagingSortDTO.getOffset(), pagingSortDTO.getPageSize());

        if(code == null) {
            totalNum = realTimeTestRecordService.getTestHistorySize(userId, taskId, result, reqName, terminal, baseVer, startTime, endTime, appId);
        }else {
            totalNum = testHistoryRecordDTOS.size();
        }

        Integer pageNum = pagingSortDTO.getCurrentPage();
        PagingResultDTO<TestHistoryRecordDTO> pagingResultDTO = new PagingResultDTO<TestHistoryRecordDTO>();
        pagingResultDTO.setTotalNum(totalNum).setList(testHistoryRecordDTOS).setPageNum(pageNum);
        return pagingResultDTO;

    }

    public List<TestHistoryRecordDTO> getTestHistoryRecordsByTaskId(Long taskId) {


        return realTimeTestRecordService.getTestHistoryByTaskId(taskId);

    }

    public Long saveTestHistoryRecord(TestHistoryRecordDTO testHistoryRecordDTO) {

        return realTimeTestRecordService.saveTestHistory(testHistoryRecordDTO);

    }

    public PagingResultDTO<CheckHistorySimpleDTO> listPagingResult(String objSpm, Long buryPointId, Long processId, String eventCode,
                                                                   Integer result, PagingSortDTO pagingSortDTO) {
        EisObjTerminalTracker tracker = getTracker(processId);
        EisTaskProcess process = taskProcessService.getById(processId);
        Long trackerId = tracker.getId();
        if(StringUtils.isBlank(objSpm) && buryPointId != null){
            trackerId = buryPointId;
        }

        Long spmPoolEntityId = process.getReqPoolEntityId();
        EisReqPoolSpm spmEntitieOfType = reqSpmPoolService.getById(spmPoolEntityId);
        String spmByObjId = spmEntitieOfType.getSpmByObjId();
        List<Long> objIdsInSpm = CommonUtil.transSpmToObjIdList(spmByObjId);
        ObjMappings objMappings = objectBasicService.getMapping(EtContext.get(ContextConstant.APP_ID), objIdsInSpm);
        Map<Long,String> objIdToOidMap = objMappings.getObjIdToOidMap();
        String spm = CommonUtil.transSpmByObjIdToSpmByOid(objIdToOidMap,spmByObjId);
        Integer totalNum = spmCheckHistoryService.getCheckHistorySize(trackerId, spm, eventCode, result);
        List<CheckHistorySimpleDTO> checkHistorySimpleDTOList = new ArrayList<>();
        if(StringUtils.isNotBlank(spm)) {
            checkHistorySimpleDTOList = spmCheckHistoryService.getCheckHistory(trackerId, spm,
                    eventCode, result, pagingSortDTO.getOrderBy(), pagingSortDTO.getOrderRule(),
                    pagingSortDTO.getOffset(), pagingSortDTO.getPageSize());
        }else {
            checkHistorySimpleDTOList = eventCheckHistoryService.getCheckHistory(trackerId, spm,
                    eventCode, result, pagingSortDTO.getOrderBy(), pagingSortDTO.getOrderRule(),
                    pagingSortDTO.getOffset(), pagingSortDTO.getPageSize());
        }
        Integer pageNum = pagingSortDTO.getCurrentPage();
        PagingResultDTO<CheckHistorySimpleDTO> pagingResultDTO = new PagingResultDTO<CheckHistorySimpleDTO>();
        pagingResultDTO
                .setTotalNum(totalNum)
                .setList(checkHistorySimpleDTOList)
                .setPageNum(pageNum);
        return pagingResultDTO;
    }


    public PagingResultDTO<CheckHistorySimpleDTO> queryPagingResult(String objSpm, Long buryPointId, Long trackerId, String eventCode,
                                                                   Integer result, PagingSortDTO pagingSortDTO) {

        if(StringUtils.isBlank(objSpm) && buryPointId != null){
            trackerId = buryPointId;
        }
//        String spmByObjId = process.getSpmByObjId();
//        ObjectBasic objQuery = new ObjectBasic();
//        objQuery.setAppId(EtContext.get(ContextConstant.APP_ID));
//        List<ObjectBasic>  objs = objectBasicService.search(objQuery);
//        Map<Long,String> objIdToOidMap = new HashMap<>();
//        for (ObjectBasic obj : objs) {
//            objIdToOidMap.put(obj.getId(),obj.getOid());
//        }
//        String spm = CommonUtil.transSpmByObjIdToSpmByOid(objIdToOidMap,spmByObjId);
        Integer totalNum = spmCheckHistoryService.getCheckHistorySize(trackerId, objSpm, eventCode, result);
        List<CheckHistorySimpleDTO> checkHistorySimpleDTOList = new ArrayList<>();
        if(StringUtils.isNotBlank(objSpm)) {
            checkHistorySimpleDTOList = spmCheckHistoryService.getCheckHistory(trackerId, objSpm,
                    eventCode, result, pagingSortDTO.getOrderBy(), pagingSortDTO.getOrderRule(),
                    pagingSortDTO.getOffset(), pagingSortDTO.getPageSize());
        }else {
            checkHistorySimpleDTOList = eventCheckHistoryService.getCheckHistory(trackerId, objSpm,
                    eventCode, result, pagingSortDTO.getOrderBy(), pagingSortDTO.getOrderRule(),
                    pagingSortDTO.getOffset(), pagingSortDTO.getPageSize());
        }
        Integer pageNum = pagingSortDTO.getCurrentPage();
        PagingResultDTO<CheckHistorySimpleDTO> pagingResultDTO = new PagingResultDTO<CheckHistorySimpleDTO>();
        pagingResultDTO
                .setTotalNum(totalNum)
                .setList(checkHistorySimpleDTOList)
                .setPageNum(pageNum);
        return pagingResultDTO;
    }

    public void deleteValidateRecord(Long id) {
        spmCheckHistoryService.deleteCheckHistory(id);
    }

    private EisObjTerminalTracker getTracker(Long processId){
        EisTaskProcess process = taskProcessService.getById(processId);
        if (process == null) {
            throw new CommonException("EisTaskProcess processId " + processId + " 不存在");
        }
        Long taskId = process.getTaskId();
        EisReqTask task = reqTaskService.getById(taskId);
        Long terminalId = task.getTerminalId();
        EisObjTerminalTracker trackerQuery = new EisObjTerminalTracker();
        trackerQuery.setReqPoolId(process.getReqPoolId());
        trackerQuery.setObjId(process.getObjId());
        trackerQuery.setTerminalId(terminalId);
        //正常情况下reqPooId + objId + terminalId 会查出唯一一个tracker
        List<EisObjTerminalTracker> trackers = objTerminalTrackerService.search(trackerQuery);

        if(CollectionUtils.isEmpty(trackers)){
            return new EisObjTerminalTracker();
        }
        return trackers.get(0);
    }

    private List<EisObjTerminalTracker> getTrackersOfReqPoolAndBase(Long reqPoolId,Long terminalId){
//        EisReqPoolRelBaseRelease reqPoolRelBaseRelease = reqPoolRelBaseService.getCurrentUse(reqPoolId,terminalId);
        EisTerminalReleaseHistory latestRelease = terminalReleaseService.getLatestRelease(terminalId);
//        Long baseReleaseId = reqPoolRelBaseRelease.getBaseReleaseId();
        Long baseReleaseId = 0L;
        if(latestRelease != null){
            baseReleaseId = latestRelease.getId();
        }
        EisObjTerminalTracker trackerSearch = new EisObjTerminalTracker();
        trackerSearch.setReqPoolId(reqPoolId);
        trackerSearch.setTerminalId(terminalId);
        List<EisObjTerminalTracker> trackersOfReq = objTerminalTrackerService.search(trackerSearch);
        Set<Long> objIdsOfReq = trackersOfReq.stream().map(e -> e.getObjId()).collect(Collectors.toSet());
        EisAllTrackerRelease trackerRelease = new EisAllTrackerRelease();
        trackerRelease.setTerminalReleaseId(baseReleaseId);
        List<EisAllTrackerRelease> allTrackerReleases = allTrackerReleaseService.search(trackerRelease);
        Set<Long> trackerIdsOfBase = allTrackerReleases.stream()
                .filter(e -> !objIdsOfReq.contains(e.getObjId()))
                .map(e -> e.getTrackerId())
                .collect(Collectors.toSet());
        List<EisObjTerminalTracker> trackersOfBase = objTerminalTrackerService.getByIds(trackerIdsOfBase);
        List<EisObjTerminalTracker> allTrackers = new ArrayList<>();
        allTrackers.addAll(trackersOfReq);
        allTrackers.addAll(trackersOfBase);
        return allTrackers;
    }

    private List<EisObjTerminalTracker> getTrackerOfBase(Long releaseId){
        EisAllTrackerRelease trackerRelease = new EisAllTrackerRelease();
        trackerRelease.setTerminalReleaseId(releaseId);
        List<EisAllTrackerRelease> allTrackerReleases = allTrackerReleaseService.search(trackerRelease);
        Set<Long> trackerIdsOfBase = allTrackerReleases.stream()
                .map(e -> e.getTrackerId())
                .collect(Collectors.toSet());
        List<EisObjTerminalTracker> trackersOfBase = objTerminalTrackerService.getByIds(trackerIdsOfBase);
        return trackersOfBase;
    }

    private List<RealTimeTestResourceDTO.ObjMeta> getObjMeTa(List<EisObjTerminalTracker> trackers){
        List<RealTimeTestResourceDTO.ObjMeta> objTrackersOfDto = new ArrayList<>();
        Set<Long> trackerIds = new HashSet<>();
        Set<Long> objIds = new HashSet<>();
        for (EisObjTerminalTracker tracker : trackers) {
            trackerIds.add(tracker.getId());
            objIds.add(tracker.getObjId());
        }
        Long appId = EtContext.get(ContextConstant.APP_ID);
        //搜索对象基本信息
        List<ObjectBasic> objs = objectBasicService.getByIds(objIds);
        Map<Long,ObjectBasic> objIdMap = new HashMap<>();
        for (ObjectBasic obj : objs) {
            objIdMap.put(obj.getId(),obj);
        }
        Map<Long,ObjectBasic> trackerId2ObjMap = new HashMap<>();
        for (EisObjTerminalTracker tracker : trackers) {
            Long objId = tracker.getObjId();
            Long trackerId = tracker.getId();
            trackerId2ObjMap.put(trackerId,objIdMap.get(objId));
        }
        // 搜索最新的事件信息
        List<ObjTrackerEventSimpleDTO> objTrackerEvents = objTrackerEventService
                .getByTrackerId(trackerIds);
        Map<Long, List<Long>> trackerId2EventIdMap = Maps.newHashMap();
        List<Long> eventIds = Lists.newArrayList();
        Map<Long,Map<Long,Long>> trackerIdToEventVersionMap = new HashMap<>();
        for (ObjTrackerEventSimpleDTO objTrackerEvent : objTrackerEvents) {
            Long trackerId = objTrackerEvent.getTrackerId();
            List<Long> tmpEventMap = trackerId2EventIdMap
                    .computeIfAbsent(trackerId, k -> Lists.newArrayList());
            tmpEventMap.add(objTrackerEvent.getEventId());
            eventIds.add(objTrackerEvent.getEventId());
            Map<Long,Long> eventAndVerisonMap = trackerIdToEventVersionMap.computeIfAbsent(trackerId,k -> new HashMap<>());
            eventAndVerisonMap.put(objTrackerEvent.getEventId(),objTrackerEvent.getEventParamVersionId());
        }
        List<EventSimpleDTO> events = eventService.getEventByIds(eventIds);
        Map<Long, EventSimpleDTO> eventId2EventMap = events.stream()
                .collect(Collectors.toMap(EventSimpleDTO::getId, Function.identity(), (k1, k2) -> k1));
        // 搜索最新的参数配置
        Map<Long, List<ParamBindItemDTO>> trackerId2ParamBindMap = paramBindHelper
                .getParamBinds(appId, trackerIds, EntityTypeEnum.OBJTRACKER.getType(), null);
        for (Long trackerId : trackerIds) {
            RealTimeTestResourceDTO.ObjMeta objMeta = new RealTimeTestResourceDTO.ObjMeta();
            /**
             * 事件处理
             */
            List<RealTimeTestResourceDTO.Event> eventsOfDto = new ArrayList<>();
            List<Long> eventSimpleDTOS = Optional.ofNullable(trackerId2EventIdMap.get(trackerId)).orElse(new ArrayList<>());
            Map<Long,Long> eventToParamVersionMap = trackerIdToEventVersionMap.get(trackerId);
            for (Long eventId : eventSimpleDTOS) {
                EventSimpleDTO tmpEvent = eventId2EventMap.get(eventId);
                if (tmpEvent == null) {
                    log.error("eventId={} 不存在", eventId);
                    continue;
                }
                RealTimeTestResourceDTO.Event eventOfDto = new RealTimeTestResourceDTO.Event();
                Long paramVersionId = eventToParamVersionMap.get(eventId);
                eventOfDto.setId(eventId);
                eventOfDto.setCode(tmpEvent.getCode());
                eventOfDto.setName(tmpEvent.getName());
                eventOfDto.setParamVersion(paramVersionId);
                eventsOfDto.add(eventOfDto);
            }
            objMeta.setEvents(eventsOfDto);
            /**
             * 对象私参处理
             */
            ObjectBasic obj = trackerId2ObjMap.get(trackerId);
            List<ParamBindItemDTO> privateParams = Optional.ofNullable(trackerId2ParamBindMap.get(trackerId)).orElse(new ArrayList<>());
            ParamBindItemDTO oidParam = mockOidParam(obj.getOid());
            privateParams.add(oidParam);
            objMeta.setPrivateParams(privateParams);
            /**
             * 对象基本信息处理
             */

            objMeta.setOid(obj.getOid());
            objMeta.setObjName(obj.getName());
            objMeta.setObjType(obj.getType());
            objTrackersOfDto.add(objMeta);
        }
        return objTrackersOfDto;

    }

    /**
     * 获取需求树
     * @param taskId
     * @param terminalId
     * @return
     */
    public TestTreeVO getReqTree(Long terminalId,Long taskId){
        EisReqTask task = reqTaskService.getById(taskId);
        Long reqId = task.getRequirementId();
        EisRequirementInfo requirementInfo = requirementInfoService.getById(reqId);
        EisTaskProcess taskProcessQuery = new EisTaskProcess();
        taskProcessQuery.setTaskId(taskId);
        List<EisTaskProcess> processes = taskProcessService.search(taskProcessQuery);
        processes = processes.stream()
                .filter(e -> e.getReqPoolType().equals(ReqPoolTypeEnum.SPM_DEV.getReqPoolType()))
                .collect(Collectors.toList());
        if(CollectionUtils.isEmpty(processes)){
            return null;
        }
        Set<Long> reqPoolSpmEntityIds = new HashSet<>();
        for (EisTaskProcess process : processes) {
            reqPoolSpmEntityIds.add(process.getReqPoolEntityId());
        }
        List<List<Long>> spmsAsObjIdList = new ArrayList<>();

        Set<Long> spmPoolEntityIdsOfType = processes.stream().map(EisTaskProcess::getReqPoolEntityId).collect(Collectors.toSet());
        List<EisReqPoolSpm> spmEntitiesOfType = reqSpmPoolService.getBatchByIds(spmPoolEntityIdsOfType);
        for (EisReqPoolSpm spmEntity : spmEntitiesOfType) {
            String spmByObjId = spmEntity.getSpmByObjId();
            List<Long> spmByObjIdList = Arrays.stream(spmByObjId.split("\\|")).map(Long::valueOf).collect(Collectors.toList());
            spmsAsObjIdList.add(spmByObjIdList);
        }
//        for (EisTaskProcess process : processes) {
//            spmsAsObjIdList.add(CommonUtil.transSpmToObjIdList(process.getSpmByObjId()));
//        }
        List<EisReqPoolSpm> reqPoolSpms = reqSpmPoolService.getBatchByIds(reqPoolSpmEntityIds);
        Set<Long> allObjIds = reqPoolSpms.stream().map(EisReqPoolSpm::getSpmByObjId).flatMap(s -> CommonUtil.transSpmToObjIdList(s).stream()).collect(Collectors.toSet());
        List<ObjectBasic>  objs = objectBasicService.getByIds(allObjIds);
        Map<Long,ObjectBasic> objMap = new HashMap<>();
        Map<Long,String> objIdToOidMap = new HashMap<>();
        for (ObjectBasic obj : objs) {
            objMap.put(obj.getId(),obj);
            objIdToOidMap.put(obj.getId(),obj.getOid());
        }
        List<ReqNode> reqNodes = new ArrayList<>();
        Set<String> spmByObjIdSet = new HashSet<>();
        Long reqPoolId = null;
        for (EisReqPoolSpm reqPoolSpm : reqPoolSpms) {
            ReqNode reqNode = new ReqNode();
            Long objId = reqPoolSpm.getObjId();
            ObjectBasic targetObj = objMap.get(objId);
            reqNode.setObjId(objId);
            String reqTypeDesc = CommonUtil.transReqTypeToDescription(reqPoolSpm.getReqType());
            reqNode.setReqType(reqTypeDesc);
            String spm = CommonUtil.transSpmByObjIdToSpmByOid(objIdToOidMap,reqPoolSpm.getSpmByObjId());
            reqNode.setSpm(spm);
            reqNodes.add(reqNode);
            spmByObjIdSet.add(reqPoolSpm.getSpmByObjId());
            reqPoolId = reqPoolSpm.getReqPoolId();
        }
        for (List<Long> spmAsObjIdList : spmsAsObjIdList) {
            for(int i=0;i<spmAsObjIdList.size();i++){
                List<Long> subList = spmAsObjIdList.subList(i,spmAsObjIdList.size());
                Long objId = spmAsObjIdList.get(i);
                ObjectBasic objectBasic = objMap.get(objId);
                String spmByObjId = CommonUtil.getSpmStringByObjIds(subList);
                if(spmByObjIdSet.contains(spmByObjId)){
                    continue;
                }
                spmByObjIdSet.add(spmByObjId);
                ReqNode reqNode = new ReqNode();
                String spmByOid = CommonUtil.transSpmByObjIdToSpmByOid(objIdToOidMap,spmByObjId);
                reqNode.setSpm(spmByOid);
                reqNode.setObjId(objectBasic.getId());
                reqNodes.add(reqNode);
            }
        }

        List<NodeOfTestTree> trees = lineageHelper.buildTreeForTest(spmsAsObjIdList,objMap);
        TestTreeVO testTreeVo = new TestTreeVO();
        testTreeVo.setTaskName(task.getTaskName());
        testTreeVo.setReqName(requirementInfo.getReqName());
        testTreeVo.setRoots(trees);
        testTreeVo.setNodesOfReq(reqNodes);
        testTreeVo.setReqPoolId(reqPoolId);
        return testTreeVo;
    }

    /**
     * 获取基线树
     * @param
     * @param terminalId
     * @return
     */
    public TestTreeVO getBaseTree(Long terminalId){
        EisTerminalReleaseHistory releaseHistory = terminalReleaseService.getLatestRelease(terminalId);
        Long latestReleaseId = null;
        if(releaseHistory != null) {
             latestReleaseId = releaseHistory.getId();
        }
        LinageGraph linageGraph = lineageHelper.genReleasedLinageGraph(latestReleaseId);
        Set<Long> objIds = linageGraph.getAllObjIds();
        List<ObjectBasic>  objs = objectBasicService.getByIds(objIds);
        Map<Long,ObjectBasic> objMap = new HashMap<>();
        for (ObjectBasic obj : objs) {
            objMap.put(obj.getId(),obj);
        }
//        List<ReqNode> reqNodes = new ArrayList<>();
//        for (Long objId : objIds) {
//            List<List<Long>> spmsByObjIdAsList = lineageHelper2.getObjIdSpms(linageGraph,objId);
//            ObjectBasic targetObj = objMap.get(objId);
//            for (List<Long> spmByObjIdAsList : spmsByObjIdAsList) {
//                String spmByObjId = CommonUtil.getSpmStringByObjIds(spmByObjIdAsList);
//                String spmByOid = CommonUtil.transSpmByObjIdToSpmByOid(objIdToOidMap,spmByObjId);
//                ReqNode reqNode = new ReqNode();
//                reqNode.setObjId(targetObj.getId());
//                reqNode.setOid(targetObj.getOid());
//                reqNode.setObjName(targetObj.getName());
//                reqNode.setSpm(spmByOid);
//                reqNode.setSpmByObjId(spmByObjId);
//                reqNodes.add(reqNode);
//            }
//        }
        List<NodeOfTestTree> trees = lineageHelper.buildForestByGraphForTest(linageGraph,objMap);
        TestTreeVO testTreeVo = new TestTreeVO();
        testTreeVo.setRoots(trees);
//        testTreeVo.setNodesOfReq(reqNodes);
        return testTreeVo;
    }

    private List<ParamWithValueItemDTO> getEventPublicParams(Set<Long> trackerIds, Set<Long> pointEventIds){
        Long appId = EtContext.get(ContextConstant.APP_ID);
        List<Integer> entityTypes = new ArrayList<>();
        entityTypes.add(EntityTypeEnum.EVENT.getType());
        List<ObjTrackerEventSimpleDTO> objTrackerEvents = objTrackerEventService
                .getByTrackerId(trackerIds);
        Set<Long> eventIds = new HashSet<>();
        for (ObjTrackerEventSimpleDTO objTrackerEvent : objTrackerEvents) {
            eventIds.add(objTrackerEvent.getEventId());
        }
        eventIds.addAll(pointEventIds);
        if(CollectionUtils.isEmpty(eventIds)){
            return new ArrayList<>();
        }
        // 从参数绑定系列表中获取绑定的各种类型的参数
        List<ParamWithValueItemDTO> objParamBindWithValues = paramBindHelper
                .getParamBindWithValue(appId, eventIds, entityTypes, null);
        return objParamBindWithValues;
    }

    private List<ParamWithValueItemDTO> getGlobalPublicParams(Long terminalId){
        Long appId = EtContext.get(ContextConstant.APP_ID);
        List<Integer> entityTypes = new ArrayList<>();
        entityTypes.add(EntityTypeEnum.TERMINAL.getType());
        // 从参数绑定系列表中获取绑定的各种类型的参数
        List<ParamWithValueItemDTO> objParamBindWithValues = paramBindHelper
                .getParamBindWithValue(appId, Lists.newArrayList(terminalId), entityTypes, null);
        objParamBindWithValues = objParamBindWithValues.stream().filter(e -> e.getNeedTest()).collect(Collectors.toList());
        return objParamBindWithValues;
    }


    public Map<String,RealTimeTestResourceDTO.Linage> getSpmToLineageMapForRealTimeTest(LinageGraph linageGraph, List<EisObjTerminalTracker> objTrackersOfGraph, ObjMappings objMappings){
        Map<Long,Long> objIdToTrackerIdMap = new HashMap<>();
        for (EisObjTerminalTracker terminalTracker : objTrackersOfGraph) {
            objIdToTrackerIdMap.put(terminalTracker.getObjId(),terminalTracker.getId());
        }
        Map<String,RealTimeTestResourceDTO.Linage> totalLinageMap = new HashMap<>();
        Set<Long> objIdsOfGraph = linageGraph.getAllObjIds();
        List<List<String>> allSpmsAsOidList = new ArrayList<>();
        Map<Long, String> objIdToOidMap = objMappings.getObjIdToOidMap();
        Map<String, Long> oidToObjIdMap = objMappings.getOidToObjIdMap();
        for (Long objIdOfGraph : objIdsOfGraph) {
            List<List<Long>> spmsByObjIdAsList = lineageHelper.getObjIdSpms(linageGraph,objIdOfGraph);
            for (List<Long> spmByObjIdAsList : spmsByObjIdAsList) {
                List<String> spmAsOidList = new ArrayList<>();
                for (Long objId : spmByObjIdAsList) {
                    String oid = objIdToOidMap.get(objId);
                    spmAsOidList.add(oid);
                }
                allSpmsAsOidList.add(spmAsOidList);
            }
        }
        for (List<String> spmAsOidList : allSpmsAsOidList) {
            String targetOid = spmAsOidList.get(0);
            Long objId = oidToObjIdMap.get(targetOid);
            Long trackerId = objIdToTrackerIdMap.get(objId);
            String spm = String.join("|",spmAsOidList);
            RealTimeTestResourceDTO.Linage linage = new RealTimeTestResourceDTO.Linage();
            linage.setLinageNodes(spmAsOidList);
            linage.setSpm(spm);
            linage.setOid(targetOid);
            linage.setTrackerId(trackerId);
            totalLinageMap.put(spm,linage);
        }
        return totalLinageMap;
    }

    @Transactional(rollbackFor = Throwable.class)  // 批量插入，需要事务
    public List<Long> saveCheckHistory(List<CheckHistorySimpleDTO> checkHistorySimpleDTOS) {
        if(CollectionUtils.isEmpty(checkHistorySimpleDTOS)){
            return new ArrayList<>();
        }
//        UserDTO currentUserDTO = EtContext.get(ContextConstant.USER);
        log.info("开始保存测试记录，用户:{},trackerId:{},spm:{}",checkHistorySimpleDTOS.get(0).getSaver()
                ,checkHistorySimpleDTOS.get(0).getTrackerId(),checkHistorySimpleDTOS.get(0).getSpm());
        List<Long> result = new ArrayList<>();
        // 2. 填充 saver, saveTime 字段
//        UserSimpleDTO currentUser = BeanConvertUtils.convert(currentUserDTO, UserSimpleDTO.class);
//        for(CheckHistorySimpleDTO checkHistorySimpleDTO: checkHistorySimpleDTOS) {
//            checkHistorySimpleDTO.setSaver(currentUser);
//            checkHistorySimpleDTO.setSaveTime(new Date().getTime());
//        }
        // 3. 过滤trackerId或spm为空的记录
        checkHistorySimpleDTOS = checkHistorySimpleDTOS.stream()
                .filter(checkHistorySimpleDTO -> null != checkHistorySimpleDTO.getTrackerId())
                .filter(checkHistorySimpleDTO -> null != checkHistorySimpleDTO.getSpm())
                .collect(Collectors.toList());
        List<CheckHistorySimpleDTO> spmHistorys = checkHistorySimpleDTOS.stream()
                .filter(checkHistorySimpleDTO -> !checkHistorySimpleDTO.getSpm().equals(""))
                .collect(Collectors.toList());
        List<CheckHistorySimpleDTO> eventHistorys = checkHistorySimpleDTOS.stream()
                .filter(checkHistorySimpleDTO -> checkHistorySimpleDTO.getSpm().equals(""))
                .collect(Collectors.toList());
        // 4. 调用 service层 批量插入
        if (!CollectionUtils.isEmpty(spmHistorys)){
            result.addAll(spmCheckHistoryService.createCheckHistory(checkHistorySimpleDTOS));
        }
        if (!CollectionUtils.isEmpty(eventHistorys)){
            result.addAll(eventCheckHistoryService.createCheckHistory(checkHistorySimpleDTOS));
        }
        return result;
    }

    public Integer deleteCheckHistory(Long checkHistoryId) {
        UserDTO currentUserDTO = EtContext.get(ContextConstant.USER);
        log.info("开始删除测试记录，用户:{},checkHistoryId:{}",currentUserDTO.getUserName(),checkHistoryId);
        return spmCheckHistoryService.deleteCheckHistory(checkHistoryId);
    }


    private ParamBindItemDTO mockOidParam(String oid){
        ParamBindItemDTO paramBindItemDTO = new ParamBindItemDTO();
        ParamValueSimpleDTO paramValueSimpleDTO = new ParamValueSimpleDTO();
        paramValueSimpleDTO.setCode(oid);
        paramValueSimpleDTO.setId(-1L);
        paramBindItemDTO.setCode("_oid")
                .setName("oid")
                .setMust(true)
                .setNotEmpty(true)
                .setParamType(ParamTypeEnum.OBJ_NORMAL_PARAM.getType())
                .setValueType(ParamValueTypeEnum.CONSTANT.getType())
                .setValues(Lists.newArrayList(paramValueSimpleDTO))
                .setSelectedValues(Lists.newArrayList(-1L));
        return paramBindItemDTO;
    }


}
