package com.netease.hz.bdms.easyinsight.service.facade;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.common.CommonAggregateDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.CommonRelationAggregateDTO;
import com.netease.hz.bdms.easyinsight.common.dto.event.EventSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.terminal.TerminalSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.enums.ProcessStatusEnum;
import com.netease.hz.bdms.easyinsight.common.enums.ReqPoolTypeEnum;
import com.netease.hz.bdms.easyinsight.common.enums.TrackerContentTypeEnum;
import com.netease.hz.bdms.easyinsight.common.exception.CommonException;
import com.netease.hz.bdms.easyinsight.common.param.event.EventBuryPointCreateParam;
import com.netease.hz.bdms.easyinsight.common.param.event.EventBuryPointEditParam;
import com.netease.hz.bdms.easyinsight.common.param.event.EventObjRelation;
import com.netease.hz.bdms.easyinsight.common.param.obj.server.ServerApiInfo;
import com.netease.hz.bdms.easyinsight.common.util.JsonUtils;
import com.netease.hz.bdms.easyinsight.common.vo.event.*;
import com.netease.hz.bdms.easyinsight.common.vo.requirement.UnDevelopedEventVO;
import com.netease.hz.bdms.easyinsight.dao.model.*;
import com.netease.hz.bdms.easyinsight.service.service.EventService;
import com.netease.hz.bdms.easyinsight.service.service.ObjectBasicService;
import com.netease.hz.bdms.easyinsight.service.service.TerminalService;
import com.netease.hz.bdms.easyinsight.service.service.TerminalVersionInfoService;
import com.netease.hz.bdms.easyinsight.service.service.obj.EventBuryPointService;
import com.netease.hz.bdms.easyinsight.service.service.requirement.ReqEventObjRelationService;
import com.netease.hz.bdms.easyinsight.service.service.requirement.ReqEventPoolService;
import com.netease.hz.bdms.easyinsight.service.service.requirement.ReqTaskService;
import com.netease.hz.bdms.easyinsight.service.service.requirement.TaskProcessService;
import com.netease.hz.bdms.easyinsight.service.service.terminalrelease.TerminalReleaseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EventPoolFacade {

    @Autowired
    ReqEventPoolService reqEventPoolService;

    @Autowired
    TaskProcessService taskProcessService;

    @Autowired
    EventBuryPointService eventBuryPointService;

    @Autowired
    TerminalService terminalService;

    @Autowired
    ReqTaskService taskService;

    @Autowired
    EventService eventService;

    @Autowired
    TerminalReleaseService terminalReleaseService;

    @Autowired
    TerminalVersionInfoService terminalVersionInfoService;

    @Autowired
    ReqEventObjRelationService reqEventObjRelationService;

    @Autowired
    ObjectBasicService objectBasicService;

    /**
     * 需求管理模块——获取需求组下的事件埋点池
     *
     * @param reqPoolId 需求组ID
     * @return
     */
    public List<UnDevelopedEventVO> getReqPoolEvents(Long reqPoolId){
        EisReqPoolEvent query = new EisReqPoolEvent();
        query.setReqPoolId(reqPoolId);
        List<EisReqPoolEvent> reqPoolEvents = reqEventPoolService.search(query);
        if(CollectionUtils.isEmpty(reqPoolEvents)){
            return new ArrayList<>();
        }
        Set<Long> poolEntityIds = new HashSet<>();
        Set<Long> eventBuryPointIds = new HashSet<>();
        for (EisReqPoolEvent reqPoolEvent : reqPoolEvents) {
            poolEntityIds.add(reqPoolEvent.getId());
            eventBuryPointIds.add(reqPoolEvent.getEventBuryPointId());
        }
        Set<Long> eventIds = new HashSet<>();
        List<EisEventBuryPoint> eventBuryPoints = eventBuryPointService.getByIds(eventBuryPointIds);
        Map<Long,Long> eventBuryPointIdToEventIdMap = new HashMap<>();
        for (EisEventBuryPoint buryPoint : eventBuryPoints) {
            eventIds.add(buryPoint.getEventId());
            eventBuryPointIdToEventIdMap.put(buryPoint.getId(),buryPoint.getEventId());
        }
        List<EventSimpleDTO> events = eventService.getEventByIds(eventIds);
        Map<Long,EventSimpleDTO> eventIdMap = new HashMap<>();
        for (EventSimpleDTO event : events) {
            eventIdMap.put(event.getId(),event);
        }
        List<EisTaskProcess> taskProcesses = taskProcessService.getByReqPoolEntityIds(ReqPoolTypeEnum.EVENT, poolEntityIds);
        Map<Long,EisTaskProcess> taskProcessByEventPointId = new HashMap<>();
        Set<Long> taskIds = new HashSet<>();
        for (EisTaskProcess taskProcess : taskProcesses) {
            taskProcessByEventPointId.put(taskProcess.getReqPoolEntityId(),taskProcess);
            taskIds.add(taskProcess.getTaskId());
        }
        List<EisReqTask> tasks = taskService.getByIds(taskIds);
        Map<Long,EisReqTask> taskMap = new HashMap<>();
        for (EisReqTask task : tasks) {
            taskMap.put(task.getId(),task);
        }
        Long appId = EtContext.get(ContextConstant.APP_ID);
        List<TerminalSimpleDTO> terminals = terminalService.getByAppId(appId);
        Map<Long,String> terminalNameMap = new HashMap<>();
        //
        List<EisEventObjRelation> relations = reqEventObjRelationService.getByEventEntityIds(poolEntityIds);
        List<Long> objIds = relations.stream().map(EisEventObjRelation::getObjId).collect(Collectors.toList());
        List<ObjectBasic> objectBasics = objectBasicService.getByIds(objIds);
        Map<Long, String> objInfoMap = objectBasics.stream().collect(Collectors.toMap(ObjectBasic::getId, ObjectBasic::getOid));
        Map<Long, List<EventObjRelation>> entityRelationMap = new HashMap<>();
        for(EisEventObjRelation relation : relations){
            if(entityRelationMap.containsKey(relation.getEventPoolEntityId())){
                List<EventObjRelation> eventObjRelations = entityRelationMap.get(relation.getEventPoolEntityId());
                Set<String> keySet = eventObjRelations.stream().map(vo -> vo.getTerminalId() + "|" + vo.getObjId()).collect(Collectors.toSet());
                if(!keySet.contains(relation.getTerminalId() + "|" + relation.getObjId())){
                    EventObjRelation eventObjRelation = new EventObjRelation();
                    eventObjRelation.setObjId(relation.getObjId());
                    eventObjRelation.setOid(objInfoMap.get(relation.getObjId()));
                    eventObjRelation.setTerminalId(relation.getTerminalId());
                    eventObjRelations.add(eventObjRelation);
                    entityRelationMap.put(relation.getEventPoolEntityId(), eventObjRelations);
                }
            }else {
                List<EventObjRelation> eventObjRelations = new ArrayList<>();
                EventObjRelation eventObjRelation = new EventObjRelation();
                eventObjRelation.setObjId(relation.getObjId());
                eventObjRelation.setTerminalId(relation.getTerminalId());
                eventObjRelation.setOid(objInfoMap.get(relation.getObjId()));
                eventObjRelations.add(eventObjRelation);
                entityRelationMap.put(relation.getEventPoolEntityId(), eventObjRelations);
            }
        }
        for (TerminalSimpleDTO terminal : terminals) {
            terminalNameMap.put(terminal.getId(),terminal.getName());
        }
        List<UnDevelopedEventVO> result = new ArrayList<>();
        for (EisReqPoolEvent reqPoolEvent : reqPoolEvents) {
            UnDevelopedEventVO vo = new UnDevelopedEventVO();
            Long eventId = eventBuryPointIdToEventIdMap.get(reqPoolEvent.getEventBuryPointId());
            Long terminalId = reqPoolEvent.getTerminalId();
            vo.setReqPoolEventId(reqPoolEvent.getId());
            vo.setEventBuryPointId(reqPoolEvent.getEventBuryPointId());
            EventSimpleDTO eventSimpleDTO = eventIdMap.get(eventId);
            if (eventSimpleDTO == null) {
                log.error("eventSimpleDTO不存在, eventId={}", eventId);
                continue;
            }
            vo.setApplicableObjTypes(eventSimpleDTO.getApplicableObjTypes());
            vo.setEventName(eventSimpleDTO.getName());
            vo.setEventCode(eventSimpleDTO.getCode());
            EisTaskProcess taskProcess = taskProcessByEventPointId.get(reqPoolEvent.getEventBuryPointId());
            if(taskProcess != null){
                EisReqTask task = taskMap.get(taskProcess.getTaskId());
                vo.setTaskName(task.getTaskName());
                vo.setTaskId(task.getId());
                vo.setStatus(ProcessStatusEnum.fromState(taskProcess.getStatus()).getDesc());
                vo.setReqName(task.getReqIssueKey());
            }else{
                vo.setStatus(ProcessStatusEnum.UNASSIGN.getDesc());
            }
            vo.setTerminalName(terminalNameMap.get(terminalId));
            List<EventObjRelation> eventObjRelations = entityRelationMap.get(reqPoolEvent.getId());
            vo.setRelations(eventObjRelations);
            result.add(vo);
        }
        return result;
    }


    public List<UnDevelopedEventVO> getReqPoolEvents(Set<Long> entityIds){
        List<EisReqPoolEvent> reqPoolEvents = reqEventPoolService.getBatchByIds(entityIds);
        if(CollectionUtils.isEmpty(reqPoolEvents)){
            return new ArrayList<>();
        }
        Set<Long> poolEntityIds = new HashSet<>();
        Set<Long> eventBuryPointIds = new HashSet<>();
        for (EisReqPoolEvent reqPoolEvent : reqPoolEvents) {
            poolEntityIds.add(reqPoolEvent.getId());
            eventBuryPointIds.add(reqPoolEvent.getEventBuryPointId());
        }
        Set<Long> eventIds = new HashSet<>();
        List<EisEventBuryPoint> eventBuryPoints = eventBuryPointService.getByIds(eventBuryPointIds);
        Map<Long,Long> eventBuryPointIdToEventIdMap = new HashMap<>();
        for (EisEventBuryPoint buryPoint : eventBuryPoints) {
            eventIds.add(buryPoint.getEventId());
            eventBuryPointIdToEventIdMap.put(buryPoint.getId(),buryPoint.getEventId());
        }
        List<EventSimpleDTO> events = eventService.getEventByIds(eventIds);
        Map<Long,EventSimpleDTO> eventIdMap = new HashMap<>();
        for (EventSimpleDTO event : events) {
            eventIdMap.put(event.getId(),event);
        }
        List<EisTaskProcess> taskProcesses = taskProcessService.getByReqPoolEntityIds(ReqPoolTypeEnum.EVENT, poolEntityIds);
        Map<Long,EisTaskProcess> taskProcessByEventPointId = new HashMap<>();
        Set<Long> taskIds = new HashSet<>();
        for (EisTaskProcess taskProcess : taskProcesses) {
            taskProcessByEventPointId.put(taskProcess.getReqPoolEntityId(),taskProcess);
            taskIds.add(taskProcess.getTaskId());
        }
        List<EisReqTask> tasks = taskService.getByIds(taskIds);
        Map<Long,EisReqTask> taskMap = new HashMap<>();
        for (EisReqTask task : tasks) {
            taskMap.put(task.getId(),task);
        }
        Long appId = EtContext.get(ContextConstant.APP_ID);
        List<TerminalSimpleDTO> terminals = terminalService.getByAppId(appId);
        Map<Long,String> terminalNameMap = new HashMap<>();
        for (TerminalSimpleDTO terminal : terminals) {
            terminalNameMap.put(terminal.getId(),terminal.getName());
        }
        List<UnDevelopedEventVO> result = new ArrayList<>();
        for (EisReqPoolEvent reqPoolEvent : reqPoolEvents) {
            UnDevelopedEventVO vo = new UnDevelopedEventVO();
            Long eventId = eventBuryPointIdToEventIdMap.get(reqPoolEvent.getEventBuryPointId());
            Long terminalId = reqPoolEvent.getTerminalId();
            vo.setReqPoolEventId(reqPoolEvent.getId());
            vo.setEventBuryPointId(reqPoolEvent.getEventBuryPointId());
            EventSimpleDTO eventSimpleDTO = eventIdMap.get(eventId);
            if (eventSimpleDTO == null) {
                log.error("eventSimpleDTO不存在, eventId={}", eventId);
                continue;
            }
            vo.setApplicableObjTypes(eventSimpleDTO.getApplicableObjTypes());
            vo.setEventName(eventSimpleDTO.getName());
            vo.setEventCode(eventSimpleDTO.getCode());
            EisTaskProcess taskProcess = taskProcessByEventPointId.get(reqPoolEvent.getEventBuryPointId());
            if(taskProcess != null){
                EisReqTask task = taskMap.get(taskProcess.getTaskId());
                vo.setTaskName(task.getTaskName());
                vo.setTaskId(task.getId());
                vo.setStatus(ProcessStatusEnum.fromState(taskProcess.getStatus()).getDesc());
                vo.setReqName(task.getReqIssueKey());
            }else{
                vo.setStatus(ProcessStatusEnum.UNASSIGN.getDesc());
            }
            vo.setTerminalName(terminalNameMap.get(terminalId));
            result.add(vo);
        }
        return result;
    }

    /**
     * 需求管理模块——删除需求组下事件埋点池中的某事件埋点
     *
     * @param reqPoolEventId 需求池事件埋点关联ID
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void delete(Long reqPoolEventId){
        Set<Long> querySet = new HashSet<>();
        querySet.add(reqPoolEventId);
        List<EisTaskProcess> taskProcesses = taskProcessService.getByReqPoolEntityIds(ReqPoolTypeEnum.EVENT, querySet);
        if(CollectionUtils.isNotEmpty(taskProcesses)){
            throw new CommonException("需求池删除项已指派给任务，需在任务中解除指派");
        }
        EisReqPoolEvent reqPoolEvent = reqEventPoolService.getById(reqPoolEventId);
        Long eventBuryPointId = reqPoolEvent.getEventBuryPointId();
        reqEventPoolService.deleteById(reqPoolEventId);
        eventBuryPointService.deleteById(eventBuryPointId);
        reqEventObjRelationService.deleteByEntityId(reqPoolEventId);
    }

    /**
     * 需求管理模块——新建需求池关联的事件埋点
     *
     * @param param
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void create(EventBuryPointCreateParam param){
        Preconditions.checkArgument(null != param, "事件埋点创建信息不能为空！");

        Long reqPoolId = param.getReqPoolId();
        Long eventId = param.getEventId();
        Long eventParamPackageId = param.getEventParamPackageId();
        Long terminalId = param.getTerminalId();
        Long pubParamPackageId = param.getPubParamPackageId();

        // 1. 插入事件埋点信息
        EisEventBuryPoint eventBuryPoint = new EisEventBuryPoint();
        eventBuryPoint.setEventId(eventId);
        eventBuryPoint.setEventParamPackageId(eventParamPackageId);
        eventBuryPoint.setReqPoolId(reqPoolId);
        eventBuryPoint.setTerminalId(terminalId);
//        eventBuryPoint.setTerminalReleaseId(pubParamPackageId);
        eventBuryPointService.insert(eventBuryPoint);

        // 2. 插入事件需求池信息
        EisReqPoolEvent reqPoolEvent = new EisReqPoolEvent();
        reqPoolEvent.setReqPoolId(eventBuryPoint.getReqPoolId());
        reqPoolEvent.setEventId(eventBuryPoint.getEventId());
        reqPoolEvent.setTerminalId(eventBuryPoint.getTerminalId());
        reqPoolEvent.setEventBuryPointId(eventBuryPoint.getId());
        reqEventPoolService.insert(reqPoolEvent);

        // 3. 插入事件埋点对象关联关系
        List<EventObjRelation> eventObjRelations = param.getRelationList();
        if(CollectionUtils.isEmpty(eventObjRelations)){
            return;
        }
        List<EisEventObjRelation> relations = new ArrayList<>();
        for(EventObjRelation entry : eventObjRelations){
            if(entry.getTerminalId() == null || entry.getObjId() == null){
                continue;
            }
            EisEventObjRelation eisEventObjRelation = new EisEventObjRelation();
            eisEventObjRelation.setEventPoolEntityId(reqPoolEvent.getId());
            eisEventObjRelation.setTerminalId(entry.getTerminalId());
            eisEventObjRelation.setObjId(entry.getObjId());
            relations.add(eisEventObjRelation);
        }
        reqEventObjRelationService.insertBatch(relations);
    }

    /**
     * 需求管理模块——事件埋点池——更新事件埋点信息
     *
     * @param param
     */
    public void edit(EventBuryPointEditParam param){
        Preconditions.checkArgument(null != param, "事件埋点编辑信息不能为空！");
        Long eventBuryId = param.getEventBuryPointId();
        EisEventBuryPoint eventBuryPoint = eventBuryPointService.getById(eventBuryId);
        if(null == eventBuryPoint){
            log.warn("未查询到eventBuryPointId={}的埋点事件！", eventBuryId);
            throw new CommonException("未查询到对应的埋点事件！");
        }

        EisEventBuryPoint updateEntity = new EisEventBuryPoint();
        updateEntity.setId(eventBuryId);
        updateEntity.setReqPoolId(eventBuryPoint.getReqPoolId());
        updateEntity.setEventParamPackageId(param.getEventParamPackageId());
        updateEntity.setTerminalParamPackageId(param.getPubParamPackageId());
        if(param.getApiInfos() != null) {
            updateEntity.setExtInfo(toTrackerContent(param.getApiInfos()));
        }
        eventBuryPointService.update(updateEntity);

        //更新事件埋点的映射对象
        List<EventObjRelation> eventObjRelations = param.getRelationList();
        if(CollectionUtils.isEmpty(eventObjRelations) || param.getReqPoolEventId() == null){
            return;
        }
        reqEventObjRelationService.deleteByEntityId(param.getReqPoolEventId());
        List<EisEventObjRelation> relations = new ArrayList<>();
        for(EventObjRelation entry : eventObjRelations){
            if(entry.getTerminalId() == null || entry.getObjId() == null){
                continue;
            }
            EisEventObjRelation eisEventObjRelation = new EisEventObjRelation();
            eisEventObjRelation.setEventPoolEntityId(param.getReqPoolEventId());
            eisEventObjRelation.setTerminalId(entry.getTerminalId());
            eisEventObjRelation.setObjId(entry.getObjId());
            relations.add(eisEventObjRelation);
        }
        reqEventObjRelationService.insertBatch(relations);

        //更新事件埋点的api信息

    }


    /**
     * 已上线事件模块——获取已上线的埋点事件的列表
     *
     * @param releaseId 发布版本ID
     * @param search 搜索条件（事件code或name）
     */
    public List<EventBuryPointSimpleVO> list(Long releaseId, String search){

        // 1. 获取全部埋点事件信息
        EisEventBuryPoint eventQuery = new EisEventBuryPoint();
        eventQuery.setTerminalReleaseId(releaseId);
        List<EisEventBuryPoint> eventBuryPointList = eventBuryPointService.search(eventQuery);

        // 3. 相关信息批量查询
        Set<Long> eventIds = eventBuryPointList.stream()
                .map(EisEventBuryPoint::getEventId)
                .collect(Collectors.toSet());
        List<EventSimpleDTO> eventSimpleDTOList = eventService.getEventByIds(eventIds);
        Map<Long, EventSimpleDTO> eventSimpleMap = eventSimpleDTOList.stream()
                .collect(Collectors.toMap(EventSimpleDTO::getId, Function.identity()));

        // 4. 根据筛选条件过滤
        if(StringUtils.isNotBlank(search)) {
            eventBuryPointList = eventBuryPointList.stream()
                    .filter(k -> eventSimpleMap.containsKey(k.getEventId()))
                    .filter(k -> {
                        EventSimpleDTO eventSimple = eventSimpleMap.get(k.getEventId());
                        if (eventSimple == null) {
                            return false;
                        }
                        return search.equals(eventSimple.getCode())
                                || search.equals(eventSimple.getName());
                    })
                    .collect(Collectors.toList());
        }

        // 查询对象关联关系

        // 5. 埋点事件简略信息汇总
        List<EventBuryPointSimpleVO> eventBuryPointSimpleVOList = Lists.newArrayList();
        for (EisEventBuryPoint eventBuryPoint : eventBuryPointList) {
            EventBuryPointSimpleVO eventBuryPointSimpleVO = new EventBuryPointSimpleVO();
            // 信息填充
            eventBuryPointSimpleVO.setEventBuryPointId(eventBuryPoint.getId());
            eventBuryPointSimpleVO.setCreateName(eventBuryPoint.getCreateName());
            eventBuryPointSimpleVO.setCreateTime(eventBuryPoint.getCreateTime());
            eventBuryPointSimpleVO.setUpdateName(eventBuryPoint.getUpdateName());
            eventBuryPointSimpleVO.setUpdateTime(eventBuryPoint.getUpdateTime());

            EventSimpleDTO eventSimpleDTO = eventSimpleMap.get(eventBuryPoint.getEventId());
            if (eventSimpleDTO == null) {
                continue;
            }
            eventBuryPointSimpleVO.setEventCode(eventSimpleDTO.getCode());
            eventBuryPointSimpleVO.setEventName(eventSimpleDTO.getName());

            // 加入列表
            eventBuryPointSimpleVOList.add(eventBuryPointSimpleVO);
        }

        return eventBuryPointSimpleVOList;
    }

    /**
     * 获取某事件埋点信息
     *
     * @param eventBuryPointId 事件埋点ID
     */
    public EventBuryPointVO getEventBuryPoint(Long eventBuryPointId){
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "未指定产品信息");

        // 1. 获取埋点事件信息
        EisEventBuryPoint eventBuryPoint = eventBuryPointService.getById(eventBuryPointId);

        // 2. 关联信息查询
        // 终端 及其 公参信息
        Long terminalId = eventBuryPoint.getTerminalId();
        TerminalSimpleDTO terminalSimpleDTO = terminalService.getById(terminalId);
        if(null == terminalSimpleDTO){
            String errorMsg = String.format("未查询到事件埋点eventBuryPointId={%d}对应的终端terminal={%d}信息",
                    eventBuryPointId, terminalId);
            log.warn(errorMsg);
            throw new CommonException(errorMsg);
        }
        Long pubParmaPackageId = eventBuryPoint.getTerminalParamPackageId();

        // 事件 及其 绑定参数信息
        Long eventId = eventBuryPoint.getEventId();
        EventSimpleDTO eventSimpleDTO = eventService.getEventById(eventId);
        if(null == eventSimpleDTO){
            String errorMsg = String.format("未查询到事件埋点eventBuryPointId={%d}对应的事件eventId={%d}信息",
                    eventBuryPointId, eventId);
            log.warn(errorMsg);
            throw new CommonException(errorMsg);
        }
        Long eventParamPackageId = eventBuryPoint.getEventParamPackageId();

        // 3. 信息填充
        EventBuryPointVO eventBuryPointVO = new EventBuryPointVO();
        eventBuryPointVO.setTerminalId(terminalId);
        eventBuryPointVO.setPubParamPackageId(pubParmaPackageId);
        eventBuryPointVO.setTerminalName(terminalSimpleDTO.getName());

        eventBuryPointVO.setEventId(eventId);
        eventBuryPointVO.setEventName(eventSimpleDTO.getName());
        eventBuryPointVO.setEventCode(eventBuryPointVO.getEventCode());
        eventBuryPointVO.setEventParamPackageId(eventParamPackageId);

        eventBuryPointVO.setCreateName(eventBuryPoint.getCreateName());
        eventBuryPointVO.setCreateTime(eventBuryPoint.getCreateTime());
        eventBuryPointVO.setUpdateName(eventBuryPoint.getUpdateName());
        eventBuryPointVO.setUpdateTime(eventBuryPoint.getUpdateTime());

        return eventBuryPointVO;
    }


    /**
     * 获取已上线事件埋点的历史发布版本
     *
     * @param eventBuryPointId 事件埋点ID
     */
    public List<ReleasedEventBuryPointVO> getReleaseHistory(Long eventBuryPointId){
        // 1. 获取当前事件埋点的基本信息
        EisEventBuryPoint eventBuryPoint = eventBuryPointService.getById(eventBuryPointId);

        // 2. 依次查询历史发布版本
        Long preId = null == eventBuryPoint ? 0L : eventBuryPoint.getPreId();
        List<EisEventBuryPoint> eventBuryPointList = Lists.newArrayList();
        while(preId != 0L){
            eventBuryPoint = eventBuryPointService.getById(preId);
            if(null != eventBuryPoint){
                eventBuryPointList.add(eventBuryPoint);
                preId = eventBuryPoint.getPreId();
            }
        }

        // 3. 相关信息批量查询
        Set<Long> terminalIds = eventBuryPointList.stream()
                .map(EisEventBuryPoint::getTerminalId)
                .collect(Collectors.toSet());
        List<TerminalSimpleDTO> terminalSimpleDTOList = terminalService.getByIds(terminalIds);
        Map<Long, TerminalSimpleDTO> terminalSimpleMap = terminalSimpleDTOList.stream()
                .collect(Collectors.toMap(TerminalSimpleDTO::getId, Function.identity()));

        Set<Long> terminalReleaseIds = eventBuryPointList.stream()
                .map(EisEventBuryPoint::getTerminalReleaseId)
                .collect(Collectors.toSet());
        List<EisTerminalReleaseHistory> terminalReleaseHistoryList = terminalReleaseService
                .getByIds(terminalReleaseIds);
        Map<Long, EisTerminalReleaseHistory> releaseHistoryMap = terminalReleaseHistoryList.stream()
                .collect(Collectors.toMap(EisTerminalReleaseHistory::getId, Function.identity()));

        Set<Long> terminalVersionIds = terminalReleaseHistoryList.stream()
                .map(EisTerminalReleaseHistory::getTerminalVersionId)
                .collect(Collectors.toSet());
        List<EisTerminalVersionInfo> terminalVersionInfoList = terminalVersionInfoService
                .getByIds(terminalVersionIds);
        Map<Long, EisTerminalVersionInfo> terminalVersionInfoMap = terminalVersionInfoList.stream()
                .collect(Collectors.toMap(EisTerminalVersionInfo::getId, Function.identity()));

        // 4. 事件埋点发布历史信息整理
        List<ReleasedEventBuryPointVO> eventBuryPointVOList = Lists.newArrayList();
        for (EisEventBuryPoint eventHistory : eventBuryPointList) {
            // 信息填充
            ReleasedEventBuryPointVO releasedEventBuryPointVO = new ReleasedEventBuryPointVO();
            releasedEventBuryPointVO.setEventBuryPointId(eventBuryPointId);
            
            Long terminalId = eventHistory.getTerminalId();
            TerminalSimpleDTO currTerminal = terminalSimpleMap.get(terminalId);
            if(null != currTerminal){
                releasedEventBuryPointVO.setTerminalId(terminalId);
                releasedEventBuryPointVO.setTerminalName(currTerminal.getName());
            }

            Long terminalReleaseId = eventHistory.getTerminalReleaseId();
            Long terminalVersionId = 0L;
            EisTerminalReleaseHistory currTerminalRelease = releaseHistoryMap.get(terminalReleaseId);
            if(null != currTerminalRelease){
                releasedEventBuryPointVO.setTerminalReleaseId(terminalReleaseId);
                releasedEventBuryPointVO.setReleaser(currTerminalRelease.getCreateName());
                releasedEventBuryPointVO.setReleaseTime(currTerminalRelease.getCreateTime());
                terminalVersionId = releasedEventBuryPointVO.getTerminalVersionId();
            }

            EisTerminalVersionInfo terminalVersionInfo = terminalVersionInfoMap.get(terminalVersionId);
            if(null != terminalVersionInfo){
                releasedEventBuryPointVO.setTerminalVersionId(terminalVersionId);
                releasedEventBuryPointVO.setTerminalVersionName(terminalVersionInfo.getName());
            }
            // 加入列表
            eventBuryPointVOList.add(releasedEventBuryPointVO);
        }

        return eventBuryPointVOList;
    }

    /**
     * 已上线事件埋点页面——聚合信息查询
     */
    public ReleasedEventAggregateVO getReleasedAggregateInfo(){
        // 1. 查询全部事件埋点
        EisEventBuryPoint eventBuryPointQuery = new EisEventBuryPoint();
        List<EisEventBuryPoint> eventBuryPointList = eventBuryPointService.search(eventBuryPointQuery);

        // 2. 搜集事件埋点历史版本对应的Id
        Set<Long> preIds = eventBuryPointList.stream()
                .map(EisEventBuryPoint::getPreId)
                .collect(Collectors.toSet());

        // 3. 过滤出已上线的事件埋点
        eventBuryPointList = eventBuryPointList.stream()
                .filter(k -> !k.getTerminalReleaseId().equals(0L) && !preIds.contains(k.getId()))
                .collect(Collectors.toList());

        // 4. 相关信息批量查询
        Set<Long> terminalReleaseIds = eventBuryPointList.stream()
                .map(EisEventBuryPoint::getTerminalReleaseId)
                .collect(Collectors.toSet());
        List<EisTerminalReleaseHistory> terminalReleaseList = terminalReleaseService.getByIds(terminalReleaseIds);

        Set<Long> terminalVersionIds = terminalReleaseList.stream()
                .map(EisTerminalReleaseHistory::getTerminalVersionId)
                .collect(Collectors.toSet());
        List<EisTerminalVersionInfo> terminalVersionList = terminalVersionInfoService.getByIds(terminalVersionIds);
        Map<Long, EisTerminalVersionInfo> terminalVersionMap = terminalVersionList.stream()
                .collect(Collectors.toMap(EisTerminalVersionInfo::getId, Function.identity()));

        Set<Long> terminalIds = eventBuryPointList.stream()
                .map(EisEventBuryPoint::getTerminalId)
                .collect(Collectors.toSet());
        List<TerminalSimpleDTO> terminalSimpleList = terminalService.getByIds(terminalIds);
        Map<Long, TerminalSimpleDTO> terminalSimpleMap = terminalSimpleList.stream()
                .collect(Collectors.toMap(TerminalSimpleDTO::getId, Function.identity()));

        // 5. 聚合信息整理
        ReleasedEventAggregateVO eventAggregateVO = new ReleasedEventAggregateVO();
        List<CommonAggregateDTO> terminals = Lists.newArrayList();
        List<CommonRelationAggregateDTO> releases = Lists.newArrayList();
        for (EisTerminalReleaseHistory terminalReleaseHistory : terminalReleaseList) {
            Long terminalId = terminalReleaseHistory.getTerminalId();
            Long terminalVersionId = terminalReleaseHistory.getTerminalVersionId();
            Long terminalReleaseId = terminalReleaseHistory.getId();

            TerminalSimpleDTO currTerminal = terminalSimpleMap.get(terminalId);
            EisTerminalVersionInfo currTerminalVersionInfo = terminalVersionMap.get(terminalVersionId);
            if(null == currTerminal || null == currTerminalVersionInfo) {
                continue;
            }

            CommonAggregateDTO terminalInfo = new CommonAggregateDTO();
            terminalInfo.setKey(terminalId.toString());
            terminalInfo.setValue(currTerminal.getName());
            terminals.add(terminalInfo);

            CommonRelationAggregateDTO releaseInfo = new CommonRelationAggregateDTO();
            String name = currTerminalVersionInfo.getName() + "-" + terminalReleaseId;
            releaseInfo.setAssociatedKey(terminalId.toString());
            releaseInfo.setKey(terminalReleaseId.toString());
            releaseInfo.setValue(name);
            releases.add(releaseInfo);
        }
        eventAggregateVO.setTerminals(terminals);
        eventAggregateVO.setReleases(releases);
        return eventAggregateVO;
    }

    /**
     * 已上线事件埋点页面——聚合信息查询
     */
    public ReleasedEventAggregationVO getReleasedAggregationInfo(){
        Long appId = EtContext.get(ContextConstant.APP_ID);
        if (appId == null) {
            throw new CommonException("appId未指定");
        }
        List<TerminalSimpleDTO> appTerminalVersions = terminalService.getByAppId(appId);
        List<Long> severTerminalId = appTerminalVersions == null ? new ArrayList<>() :appTerminalVersions.stream().filter(dto -> dto.getName().equals("Server")).map(TerminalSimpleDTO::getId).collect(Collectors.toList());

        Set<Long> appTerminalIds = appTerminalVersions == null ? new HashSet<>() : appTerminalVersions.stream().map(TerminalSimpleDTO::getId).collect(Collectors.toSet());

        // 1. 查询全部事件埋点
        EisEventBuryPoint eventBuryPointQuery = new EisEventBuryPoint();
        List<EisEventBuryPoint> eventBuryPointList = eventBuryPointService.search(eventBuryPointQuery);

        // 过滤出本app的terminalVersion
        eventBuryPointList = eventBuryPointList.stream().filter(eisEventBuryPoint -> appTerminalIds.contains(eisEventBuryPoint.getTerminalId())).collect(Collectors.toList());

        // 2. 搜集事件埋点历史版本对应的Id
        Set<Long> preIds = eventBuryPointList.stream()
                .map(EisEventBuryPoint::getPreId)
                .collect(Collectors.toSet());

        // 3. 过滤出已上线的事件埋点
        eventBuryPointList = eventBuryPointList.stream()
                .filter(k -> !k.getTerminalReleaseId().equals(0L) && !preIds.contains(k.getId()))
                .collect(Collectors.toList());

        // 4. 相关信息批量查询
        Set<Long> terminalReleaseIds = eventBuryPointList.stream()
                .map(EisEventBuryPoint::getTerminalReleaseId)
                .collect(Collectors.toSet());
        List<EisTerminalReleaseHistory> terminalReleaseList = terminalReleaseService.getByIds(terminalReleaseIds);

        Set<Long> terminalVersionIds = terminalReleaseList.stream()
                .map(EisTerminalReleaseHistory::getTerminalVersionId)
                .collect(Collectors.toSet());
        List<EisTerminalVersionInfo> terminalVersionList = terminalVersionInfoService.getByIds(terminalVersionIds);
        Map<Long, EisTerminalVersionInfo> terminalVersionMap = terminalVersionList.stream()
                .collect(Collectors.toMap(EisTerminalVersionInfo::getId, Function.identity()));

        Set<Long> terminalIds = eventBuryPointList.stream()
                .map(EisEventBuryPoint::getTerminalId)
                .collect(Collectors.toSet());
        List<TerminalSimpleDTO> terminalSimpleList = terminalService.getByIds(terminalIds);
        Map<Long, TerminalSimpleDTO> terminalSimpleMap = terminalSimpleList.stream()
                .collect(Collectors.toMap(TerminalSimpleDTO::getId, Function.identity()));

        // 5. 聚合信息整理


        Map<String, ReleasedEventAggregationSimpleVO> resultMap = new HashMap<>();

        for (EisTerminalReleaseHistory terminalReleaseHistory : terminalReleaseList) {
            Long terminalId = terminalReleaseHistory.getTerminalId();
            Long terminalVersionId = terminalReleaseHistory.getTerminalVersionId();
            Long terminalReleaseId = terminalReleaseHistory.getId();

            TerminalSimpleDTO currTerminal = terminalSimpleMap.get(terminalId);
            EisTerminalVersionInfo currTerminalVersionInfo = terminalVersionMap.get(terminalVersionId);
            if(null == currTerminal || null == currTerminalVersionInfo) {
                continue;
            }

            CommonAggregateDTO terminalInfo = new CommonAggregateDTO();
            terminalInfo.setKey(terminalId.toString());
            terminalInfo.setValue(currTerminal.getName());
            ReleasedEventAggregationSimpleVO simple = resultMap.computeIfAbsent(terminalId.toString(), k -> {
                ReleasedEventAggregationSimpleVO s = new ReleasedEventAggregationSimpleVO();
                s.setTerminal(terminalInfo);
                s.setReleases(new ArrayList<>());
                return s;
            });
            CommonRelationAggregateDTO releaseInfo = new CommonRelationAggregateDTO();
            String name = currTerminalVersionInfo.getName();
            releaseInfo.setAssociatedKey(terminalId.toString());
            releaseInfo.setKey(terminalReleaseId.toString());
            releaseInfo.setValue(name);
            simple.getReleases().add(releaseInfo);
        }
        ReleasedEventAggregationVO result = new ReleasedEventAggregationVO();
        //插入服务端埋点
        ReleasedEventAggregationSimpleVO serverSimpleVO = new ReleasedEventAggregationSimpleVO();
        CommonAggregateDTO commonAggregateDTO = new CommonAggregateDTO();
        if(CollectionUtils.isNotEmpty(severTerminalId)){
            commonAggregateDTO.setKey(String.valueOf(severTerminalId.get(0)));
        }
        commonAggregateDTO.setValue("Server");
        serverSimpleVO.setTerminal(commonAggregateDTO);

        List<ReleasedEventAggregationSimpleVO> thisList = new ArrayList<>(resultMap.values());
        thisList.add(serverSimpleVO);
        result.setList(thisList);
        return result;
    }

    /**
     * 需求管理——埋点设计——事件埋点池——新建事件埋点 获取终端信息
     *
     * @return
     */
    public EventAggregateInfoVO getAggregateInfo(){
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "未指定产品信息！");

        // 信息查询
        List<TerminalSimpleDTO> terminalSimpleDTOList = terminalService.getByAppId(appId);
        // 聚合
        EventAggregateInfoVO eventAggregateInfoVO = new EventAggregateInfoVO();
        List<CommonAggregateDTO> terminals = Lists.newArrayList();
        for (TerminalSimpleDTO terminalSimpleDTO : terminalSimpleDTOList) {
            CommonAggregateDTO terminalInfo = new CommonAggregateDTO();
            terminalInfo.setKey(terminalSimpleDTO.getId().toString());
            terminalInfo.setValue(terminalSimpleDTO.getName());
            terminals.add(terminalInfo);
        }
        eventAggregateInfoVO.setTerminals(terminals);
        return eventAggregateInfoVO;
    }

    private static String toTrackerContent(List<ServerApiInfo> serverAPIInfoDatas) {
        Map<String, String> result = new HashMap<>();
        result.put(TrackerContentTypeEnum.SERVER_API_INFO.getType(), JsonUtils.toJson(serverAPIInfoDatas));
        return JsonUtils.toJson(result);
    }

    /**
     * 获取事件埋点的样例数据
     *
     * @param eventBuryPointId
     * @return
     */
    public String getExampleData(Long eventBuryPointId){
        return "";
    }

}
