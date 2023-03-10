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
import com.netease.hz.bdms.easyinsight.common.exception.CommonException;
import com.netease.hz.bdms.easyinsight.common.param.event.EventBuryPointCreateParam;
import com.netease.hz.bdms.easyinsight.common.param.event.EventBuryPointEditParam;
import com.netease.hz.bdms.easyinsight.common.vo.event.*;
import com.netease.hz.bdms.easyinsight.common.vo.requirement.UnDevelopedEventVO;
import com.netease.hz.bdms.easyinsight.dao.model.*;
import com.netease.hz.bdms.easyinsight.service.service.EventService;
import com.netease.hz.bdms.easyinsight.service.service.TerminalService;
import com.netease.hz.bdms.easyinsight.service.service.TerminalVersionInfoService;
import com.netease.hz.bdms.easyinsight.service.service.obj.EventBuryPointService;
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

    /**
     * ????????????????????????????????????????????????????????????
     *
     * @param reqPoolId ?????????ID
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
                log.error("eventSimpleDTO?????????, eventId={}", eventId);
                continue;
            }
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
     * ??????????????????????????????????????????????????????????????????????????????
     *
     * @param reqPoolEventId ???????????????????????????ID
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void delete(Long reqPoolEventId){
        Set<Long> querySet = new HashSet<>();
        querySet.add(reqPoolEventId);
        List<EisTaskProcess> taskProcesses = taskProcessService.getByReqPoolEntityIds(ReqPoolTypeEnum.EVENT, querySet);
        if(CollectionUtils.isNotEmpty(taskProcesses)){
            throw new CommonException("??????????????????????????????????????????????????????????????????");
        }
        EisReqPoolEvent reqPoolEvent = reqEventPoolService.getById(reqPoolEventId);
        Long eventBuryPointId = reqPoolEvent.getEventBuryPointId();
        reqEventPoolService.deleteById(reqPoolEventId);
        eventBuryPointService.deleteById(eventBuryPointId);
    }

    /**
     * ????????????????????????????????????????????????????????????
     *
     * @param param
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void create(EventBuryPointCreateParam param){
        Preconditions.checkArgument(null != param, "???????????????????????????????????????");

        Long reqPoolId = param.getReqPoolId();
        Long eventId = param.getEventId();
        Long eventParamPackageId = param.getEventParamPackageId();
        Long terminalId = param.getTerminalId();
        Long pubParamPackageId = param.getPubParamPackageId();

        // 1. ????????????????????????
        EisEventBuryPoint eventBuryPoint = new EisEventBuryPoint();
        eventBuryPoint.setEventId(eventId);
        eventBuryPoint.setEventParamPackageId(eventParamPackageId);
        eventBuryPoint.setReqPoolId(reqPoolId);
        eventBuryPoint.setTerminalId(terminalId);
        eventBuryPoint.setTerminalReleaseId(pubParamPackageId);
        eventBuryPointService.insert(eventBuryPoint);

        // 2. ???????????????????????????
        EisReqPoolEvent reqPoolEvent = new EisReqPoolEvent();
        reqPoolEvent.setReqPoolId(eventBuryPoint.getReqPoolId());
        reqPoolEvent.setEventId(eventBuryPoint.getEventId());
        reqPoolEvent.setTerminalId(eventBuryPoint.getTerminalId());
        reqPoolEvent.setEventBuryPointId(eventBuryPoint.getId());
        reqEventPoolService.insert(reqPoolEvent);
    }

    /**
     * ?????????????????????????????????????????????????????????????????????
     *
     * @param param
     */
    public void edit(EventBuryPointEditParam param){
        Preconditions.checkArgument(null != param, "???????????????????????????????????????");
        Long eventBuryId = param.getEventBuryPointId();
        EisEventBuryPoint eventBuryPoint = eventBuryPointService.getById(eventBuryId);
        if(null == eventBuryPoint){
            log.warn("????????????eventBuryPointId={}??????????????????", eventBuryId);
            throw new CommonException("????????????????????????????????????");
        }

        EisEventBuryPoint updateEntity = new EisEventBuryPoint();
        updateEntity.setId(eventBuryId);
        updateEntity.setReqPoolId(eventBuryPoint.getReqPoolId());
        updateEntity.setEventParamPackageId(param.getEventParamPackageId());
        updateEntity.setTerminalParamPackageId(param.getPubParamPackageId());
        eventBuryPointService.update(updateEntity);
    }


    /**
     * ??????????????????????????????????????????????????????????????????
     *
     * @param releaseId ????????????ID
     * @param search ?????????????????????code???name???
     */
    public List<EventBuryPointSimpleVO> list(Long releaseId, String search){

        // 1. ??????????????????????????????
        EisEventBuryPoint eventQuery = new EisEventBuryPoint();
        eventQuery.setTerminalReleaseId(releaseId);
        List<EisEventBuryPoint> eventBuryPointList = eventBuryPointService.search(eventQuery);

        // 3. ????????????????????????
        Set<Long> eventIds = eventBuryPointList.stream()
                .map(EisEventBuryPoint::getEventId)
                .collect(Collectors.toSet());
        List<EventSimpleDTO> eventSimpleDTOList = eventService.getEventByIds(eventIds);
        Map<Long, EventSimpleDTO> eventSimpleMap = eventSimpleDTOList.stream()
                .collect(Collectors.toMap(EventSimpleDTO::getId, Function.identity()));

        // 4. ????????????????????????
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

        // 5. ??????????????????????????????
        List<EventBuryPointSimpleVO> eventBuryPointSimpleVOList = Lists.newArrayList();
        for (EisEventBuryPoint eventBuryPoint : eventBuryPointList) {
            EventBuryPointSimpleVO eventBuryPointSimpleVO = new EventBuryPointSimpleVO();
            // ????????????
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

            // ????????????
            eventBuryPointSimpleVOList.add(eventBuryPointSimpleVO);
        }

        return eventBuryPointSimpleVOList;
    }

    /**
     * ???????????????????????????
     *
     * @param eventBuryPointId ????????????ID
     */
    public EventBuryPointVO getEventBuryPoint(Long eventBuryPointId){
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "?????????????????????");

        // 1. ????????????????????????
        EisEventBuryPoint eventBuryPoint = eventBuryPointService.getById(eventBuryPointId);

        // 2. ??????????????????
        // ?????? ?????? ????????????
        Long terminalId = eventBuryPoint.getTerminalId();
        TerminalSimpleDTO terminalSimpleDTO = terminalService.getById(terminalId);
        if(null == terminalSimpleDTO){
            String errorMsg = String.format("????????????????????????eventBuryPointId={%d}???????????????terminal={%d}??????",
                    eventBuryPointId, terminalId);
            log.warn(errorMsg);
            throw new CommonException(errorMsg);
        }
        Long pubParmaPackageId = eventBuryPoint.getTerminalParamPackageId();

        // ?????? ?????? ??????????????????
        Long eventId = eventBuryPoint.getEventId();
        EventSimpleDTO eventSimpleDTO = eventService.getEventById(eventId);
        if(null == eventSimpleDTO){
            String errorMsg = String.format("????????????????????????eventBuryPointId={%d}???????????????eventId={%d}??????",
                    eventBuryPointId, eventId);
            log.warn(errorMsg);
            throw new CommonException(errorMsg);
        }
        Long eventParamPackageId = eventBuryPoint.getEventParamPackageId();

        // 3. ????????????
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
     * ????????????????????????????????????????????????
     *
     * @param eventBuryPointId ????????????ID
     */
    public List<ReleasedEventBuryPointVO> getReleaseHistory(Long eventBuryPointId){
        // 1. ???????????????????????????????????????
        EisEventBuryPoint eventBuryPoint = eventBuryPointService.getById(eventBuryPointId);

        // 2. ??????????????????????????????
        Long preId = null == eventBuryPoint ? 0L : eventBuryPoint.getPreId();
        List<EisEventBuryPoint> eventBuryPointList = Lists.newArrayList();
        while(preId != 0L){
            eventBuryPoint = eventBuryPointService.getById(preId);
            if(null != eventBuryPoint){
                eventBuryPointList.add(eventBuryPoint);
                preId = eventBuryPoint.getPreId();
            }
        }

        // 3. ????????????????????????
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

        // 4. ????????????????????????????????????
        List<ReleasedEventBuryPointVO> eventBuryPointVOList = Lists.newArrayList();
        for (EisEventBuryPoint eventHistory : eventBuryPointList) {
            // ????????????
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
            // ????????????
            eventBuryPointVOList.add(releasedEventBuryPointVO);
        }

        return eventBuryPointVOList;
    }

    /**
     * ???????????????????????????????????????????????????
     */
    public ReleasedEventAggregateVO getReleasedAggregateInfo(){
        // 1. ????????????????????????
        EisEventBuryPoint eventBuryPointQuery = new EisEventBuryPoint();
        List<EisEventBuryPoint> eventBuryPointList = eventBuryPointService.search(eventBuryPointQuery);

        // 2. ???????????????????????????????????????Id
        Set<Long> preIds = eventBuryPointList.stream()
                .map(EisEventBuryPoint::getPreId)
                .collect(Collectors.toSet());

        // 3. ?????????????????????????????????
        eventBuryPointList = eventBuryPointList.stream()
                .filter(k -> !k.getTerminalReleaseId().equals(0L) && !preIds.contains(k.getId()))
                .collect(Collectors.toList());

        // 4. ????????????????????????
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

        // 5. ??????????????????
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
     * ???????????????????????????????????????????????????
     */
    public ReleasedEventAggregationVO getReleasedAggregationInfo(){
        Long appId = EtContext.get(ContextConstant.APP_ID);
        if (appId == null) {
            throw new CommonException("appId?????????");
        }
        List<TerminalSimpleDTO> appTerminalVersions = terminalService.getByAppId(appId);
        Set<Long> appTerminalIds = appTerminalVersions == null ? new HashSet<>() : appTerminalVersions.stream().map(TerminalSimpleDTO::getId).collect(Collectors.toSet());

        // 1. ????????????????????????
        EisEventBuryPoint eventBuryPointQuery = new EisEventBuryPoint();
        List<EisEventBuryPoint> eventBuryPointList = eventBuryPointService.search(eventBuryPointQuery);

        // ????????????app???terminalVersion
        eventBuryPointList = eventBuryPointList.stream().filter(eisEventBuryPoint -> appTerminalIds.contains(eisEventBuryPoint.getTerminalId())).collect(Collectors.toList());

        // 2. ???????????????????????????????????????Id
        Set<Long> preIds = eventBuryPointList.stream()
                .map(EisEventBuryPoint::getPreId)
                .collect(Collectors.toSet());

        // 3. ?????????????????????????????????
        eventBuryPointList = eventBuryPointList.stream()
                .filter(k -> !k.getTerminalReleaseId().equals(0L) && !preIds.contains(k.getId()))
                .collect(Collectors.toList());

        // 4. ????????????????????????
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

        // 5. ??????????????????


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
        result.setList(new ArrayList<>(resultMap.values()));
        return result;
    }

    /**
     * ??????????????????????????????????????????????????????????????????????????? ??????????????????
     *
     * @return
     */
    public EventAggregateInfoVO getAggregateInfo(){
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "????????????????????????");

        // ????????????
        List<TerminalSimpleDTO> terminalSimpleDTOList = terminalService.getByAppId(appId);
        // ??????
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

    /**
     * ?????????????????????????????????
     *
     * @param eventBuryPointId
     * @return
     */
    public String getExampleData(Long eventBuryPointId){
        return "";
    }

}
