package com.netease.hz.bdms.easyinsight.service.helper;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.netease.hz.bdms.easyinsight.common.bo.diff.EventDiff;
import com.netease.hz.bdms.easyinsight.common.bo.diff.ParamDiff;
import com.netease.hz.bdms.easyinsight.common.bo.diff.RelationDiff;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.obj.ObjectBasicDTO;
import com.netease.hz.bdms.easyinsight.common.dto.obj.param.ParamWithValueItemDTO;
import com.netease.hz.bdms.easyinsight.common.dto.obj.tracker.event.ObjTrackerEventSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.parambind.ParamBindSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.parambind.ParamBindValueSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.enums.ChangeTypeEnum;
import com.netease.hz.bdms.easyinsight.common.enums.EntityTypeEnum;
import com.netease.hz.bdms.easyinsight.common.util.CollectionUtil;
import com.netease.hz.bdms.easyinsight.dao.EisObjAllRelationReleaseMapper;
import com.netease.hz.bdms.easyinsight.dao.EisObjTerminalTrackerMapper;
import com.netease.hz.bdms.easyinsight.dao.EisReqObjRelationMapper;
import com.netease.hz.bdms.easyinsight.dao.model.*;

import java.util.*;

import com.netease.hz.bdms.easyinsight.common.dto.diff.TrackerDiffDTO;
import com.netease.hz.bdms.easyinsight.service.service.ObjTrackerEventService;
import com.netease.hz.bdms.easyinsight.service.service.ParamBindService;
import com.netease.hz.bdms.easyinsight.service.service.ParamBindValueService;
import com.netease.hz.bdms.easyinsight.service.service.obj.ObjTerminalTrackerService;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ?????????????????????
 */
@Component
@Slf4j
public class DiffHelper {

    @Resource
    private ObjectHelper objectHelper;

    @Resource
    private ObjTrackerEventService objTrackerEventService;

    @Resource
    private ParamBindService paramBindService;

    @Resource
    private ParamBindValueService paramBindValueService;

    @Resource
    private ObjTerminalTrackerService objTerminalTrackerService;

    @Resource
    private EisReqObjRelationMapper reqObjRelationMapper;

    @Resource
    private EisObjAllRelationReleaseMapper allRelationReleaseMapper;

    @Resource
    private EisObjTerminalTrackerMapper objTerminalTrackerMapper;

    public Pair<Boolean, Long> getPubParamPackageDiff(Long oldTrackerId, Long newTrackerId) {
        List<EisObjTerminalTracker> trackers = objTerminalTrackerService.getByIds(Sets.newHashSet(oldTrackerId, newTrackerId));
        EisObjTerminalTracker oldTracker = null;
        EisObjTerminalTracker newTracker = null;
        for (EisObjTerminalTracker tracker : trackers) {
            if (tracker.getId().equals(oldTrackerId)) {
                oldTracker = tracker;
            }
            if (tracker.getId().equals(newTrackerId)) {
                newTracker = tracker;
            }
        }
        if (oldTracker == null && newTracker == null) {
            return new Pair<>(false, null);
        }
        if (oldTracker != null && newTracker != null) {
            return new Pair<>(!Objects.equals(oldTracker.getPubParamPackageId(), newTracker.getPubParamPackageId()), newTracker.getPubParamPackageId());
        }
        return new Pair<>(true, newTracker == null ? null : newTracker.getPubParamPackageId());
    }

    public boolean isPubParamPackageChanged(Long oldTrackerId, Long newTrackerId){
        List<EisObjTerminalTracker> trackers = objTerminalTrackerService.getByIds(Sets.newHashSet(oldTrackerId,newTrackerId));
        Set<Long> pubPackageIds = new HashSet<>();
        for (EisObjTerminalTracker tracker : trackers) {
            pubPackageIds.add(tracker.getPubParamPackageId());
        }
        if (pubPackageIds.size() > 1) {
            return true;
        }
        return false;
    }

    public List<EventDiff> getEventDiffs(Long oldTrackerId,Long newTrackerId){
        if(oldTrackerId == null && newTrackerId == null) {
            return Lists.newArrayList();
        }

        // ???????????????????????????
        List<Long> trackerIds = toList(oldTrackerId, newTrackerId);

        List<ObjTrackerEventSimpleDTO> trackerEvents = objTrackerEventService.getByTrackerId(
                trackerIds);
        Set<Long> eventIds = Sets.newHashSet();
        Map<Long, ObjTrackerEventSimpleDTO> oldEventId2TrackerEventMap = Maps.newHashMap();// key??????????????????ID???value????????????????????????
        Map<Long, ObjTrackerEventSimpleDTO> newEventId2TrackerEventMap = Maps.newHashMap();// key??????????????????ID???value????????????????????????
        if(CollectionUtils.isNotEmpty(trackerEvents)) {
            for(ObjTrackerEventSimpleDTO trackerEvent : trackerEvents) {
                Long eventId = trackerEvent.getEventId();
                Long trackerId = trackerEvent.getTrackerId();
                if(trackerId.equals(oldTrackerId)) {
                    oldEventId2TrackerEventMap.put(eventId, trackerEvent);
                }
                if(trackerId.equals(newTrackerId)) {
                    newEventId2TrackerEventMap.put(eventId, trackerEvent);
                }
                eventIds.add(eventId);
            }
        }

        // ??????oldEventId2TrackerEventMap???newEventId2TrackerEventMap
        List<EventDiff> result = Lists.newArrayList();
        if(CollectionUtils.isNotEmpty(eventIds)) {
            for(Long eventId : eventIds) {
                ObjTrackerEventSimpleDTO oldTrackerEvent = oldEventId2TrackerEventMap.get(eventId);
                ObjTrackerEventSimpleDTO newTrackerEvent = newEventId2TrackerEventMap.get(eventId);

                if(oldTrackerEvent != null && newTrackerEvent != null) {
                    // ????????????????????????
                    if(oldTrackerEvent.getEventParamVersionId().equals(newTrackerEvent.getEventParamVersionId())) {
                        // ??????
//                        EventDiff eventDiff = new EventDiff();
//                        eventDiff.setOldTrackerId(oldTrackerId)
//                            .setNewTrackerId(newTrackerId)
//                            .setChangeType(ChangeTypeEnum.SAME.getChangeType())
//                            .setEventId(eventId)
//                            .setOldEventParamVersionId(oldTrackerEvent.getEventParamVersionId())
//                            .setNewEventParamVersionId(newTrackerEvent.getEventParamVersionId());
//                        result.add(eventDiff);
                    }else {
                        // ??????,??????????????????
                        EventDiff oldEventDiff = new EventDiff();
                        oldEventDiff.setOldTrackerId(oldTrackerId)
                                .setNewTrackerId(newTrackerId)
                                .setChangeType(ChangeTypeEnum.DELETE.getChangeType())
                                .setEventId(eventId)
                                .setOldEventParamVersionId(oldTrackerEvent.getEventParamVersionId())
                                .setNewEventParamVersionId(newTrackerEvent.getEventParamVersionId());
                        result.add(oldEventDiff);
                        EventDiff newEventDiff = new EventDiff();
                        newEventDiff.setOldTrackerId(oldTrackerId)
                                .setNewTrackerId(newTrackerId)
                                .setChangeType(ChangeTypeEnum.CREATE.getChangeType())
                                .setEventId(eventId)
                                .setOldEventParamVersionId(oldTrackerEvent.getEventParamVersionId())
                                .setNewEventParamVersionId(newTrackerEvent.getEventParamVersionId());
                        result.add(newEventDiff);
                    }
                }else if(oldTrackerEvent != null) {
                    // ??????
                    EventDiff oldEventDiff = new EventDiff();
                    oldEventDiff.setOldTrackerId(oldTrackerId)
                            .setNewTrackerId(newTrackerId)
                            .setChangeType(ChangeTypeEnum.DELETE.getChangeType())
                            .setEventId(eventId)
                            .setOldEventParamVersionId(oldTrackerEvent.getEventParamVersionId())
                            .setNewEventParamVersionId(0L);
                    result.add(oldEventDiff);
                }else {
                    // ??????
                    EventDiff newEventDiff = new EventDiff();
                    newEventDiff.setOldTrackerId(oldTrackerId)
                            .setNewTrackerId(newTrackerId)
                            .setChangeType(ChangeTypeEnum.CREATE.getChangeType())
                            .setEventId(eventId)
                            .setOldEventParamVersionId(0L)
                            .setNewEventParamVersionId(newTrackerEvent.getEventParamVersionId());
                    result.add(newEventDiff);
                }
            }
        }

        return result;
    }

    private List<Long> toList(Long oldTrackerId, Long newTrackerId) {
        List<Long> trackerIds = Lists.newArrayList();
        if(oldTrackerId != null) {
            trackerIds.add(oldTrackerId);
        }
        if(newTrackerId != null) {
            trackerIds.add(newTrackerId);
        }
        return trackerIds;
    }

    public List<ParamDiff> getParamDiffs(Long oldTrackerId,Long newTrackerId){
        if(oldTrackerId == null && newTrackerId == null) {
            return Lists.newArrayList();
        }

        // ??????????????????????????????????????????oldTrackerId?????????????????????????????????newTrackerId???????????????????????????
        Map<Long, Set<Long>> oldParamId2ParamValueIdMap = Maps.newHashMap();
        Map<Long, Set<Long>> newParamId2ParamValueIdMap = Maps.newHashMap();
        ParamBindSimpleDTO newData = null;
        List<ParamDiff> paramDiffs = Lists.newArrayList();

        List<Long> trackerIds = toList(oldTrackerId, newTrackerId);
        Long appId = EtContext.get(ContextConstant.APP_ID);
        List<ParamBindSimpleDTO> paramBinds = paramBindService.getByEntityIds(trackerIds, Collections.singleton(EntityTypeEnum.OBJTRACKER
                .getType()), null, appId);
        if(CollectionUtils.isNotEmpty(paramBinds)) {
            Set<Long> paramIds = Sets.newHashSet();
            Set<Long> paramBindIds = paramBinds.stream()
                    .map(ParamBindSimpleDTO::getId)
                    .collect(Collectors.toSet());

            List<ParamBindValueSimpleDTO> paramBindValues = paramBindValueService.getByBindIds(paramBindIds);

            Map<Long, List<ParamBindValueSimpleDTO>> paramBindId2ValueMap = Maps.newHashMap();
            if(CollectionUtils.isNotEmpty(paramBindValues)) {
                for(ParamBindValueSimpleDTO paramBindValue : paramBindValues) {
                    List<ParamBindValueSimpleDTO> tmpBindValues = paramBindId2ValueMap.computeIfAbsent(paramBindValue.getBindId(), k->Lists.newArrayList());
                    tmpBindValues.add(paramBindValue);
                }
            }

            for(ParamBindSimpleDTO paramBind : paramBinds) {
                Long trackerId = paramBind.getEntityId();
                Long paramId = paramBind.getParamId();

                List<ParamBindValueSimpleDTO> paramBindValuesOfParam = paramBindId2ValueMap.get(paramBind.getId());
                Set<Long> tmpParamValueIds = new HashSet<>(0);

                if(trackerId.equals(oldTrackerId)) {
                    tmpParamValueIds = oldParamId2ParamValueIdMap.computeIfAbsent(paramId, k->Sets.newHashSet());
                }else if(trackerId.equals(newTrackerId)) {
                    tmpParamValueIds = newParamId2ParamValueIdMap.computeIfAbsent(paramId, k->Sets.newHashSet());
                    newData = paramBind;
                }

                if(CollectionUtils.isNotEmpty(paramBindValuesOfParam)) {
                    for (ParamBindValueSimpleDTO paramBindValue : paramBindValuesOfParam) {
                        tmpParamValueIds.add(paramBindValue.getParamValueId());
                    }
                }

                paramIds.add(paramId);
            }

            for(Long paramId : paramIds) {
                Set<Long> oldParamValueIds = oldParamId2ParamValueIdMap.get(paramId);
                Set<Long> newParamValueIds = newParamId2ParamValueIdMap.get(paramId);
                boolean oldHasParamValueId = (oldParamValueIds != null);
                boolean newHasParamValueId = (newParamValueIds != null);
                if(oldHasParamValueId && newHasParamValueId) {
                    // ??????oldParamValueIds???newParamValueIds????????????
                    if(CollectionUtil.same(oldParamValueIds, newParamValueIds)) {
//                        ParamDiff paramDiff = new ParamDiff();
//                        paramDiff.setOldTrackerId(oldTrackerId)
//                            .setNewTrackerId(newTrackerId)
//                            .setChangeType(ChangeTypeEnum.SAME.getChangeType())
//                            .setParamId(paramId)
//                            .setOldParamValueIds(oldParamValueIds)
//                            .setNewParamValueIds(newParamValueIds);
//                        paramDiffs.add(paramDiff);
                    }else {
                        ParamDiff oldParamDiff = new ParamDiff();
                        oldParamDiff.setOldTrackerId(oldTrackerId)
                                .setNewTrackerId(newTrackerId)
                                .setChangeType(ChangeTypeEnum.DELETE.getChangeType())
                                .setParamId(paramId)
                                .setOldParamValueIds(oldParamValueIds)
                                .setNewParamValueIds(newParamValueIds)
                                .setNewData(newData);
                        paramDiffs.add(oldParamDiff);

                        ParamDiff newParamDiff = new ParamDiff();
                        newParamDiff.setOldTrackerId(oldTrackerId)
                                .setNewTrackerId(newTrackerId)
                                .setChangeType(ChangeTypeEnum.CREATE.getChangeType())
                                .setParamId(paramId)
                                .setOldParamValueIds(oldParamValueIds)
                                .setNewParamValueIds(newParamValueIds)
                                .setNewData(newData);
                        paramDiffs.add(newParamDiff);
                    }
                }else if(oldHasParamValueId) {
                    // ??????
                    ParamDiff paramDiff = new ParamDiff();
                    paramDiff.setOldTrackerId(oldTrackerId)
                            .setNewTrackerId(newTrackerId)
                            .setChangeType(ChangeTypeEnum.DELETE.getChangeType())
                            .setParamId(paramId)
                            .setOldParamValueIds(oldParamValueIds)
                            .setNewParamValueIds(Sets.newHashSet())
                            .setNewData(newData);
                    paramDiffs.add(paramDiff);
                }else if(newHasParamValueId) {
                    // ??????
                    ParamDiff paramDiff = new ParamDiff();
                    paramDiff.setOldTrackerId(oldTrackerId)
                            .setNewTrackerId(newTrackerId)
                            .setChangeType(ChangeTypeEnum.CREATE.getChangeType())
                            .setParamId(paramId)
                            .setOldParamValueIds(Sets.newHashSet())
                            .setNewParamValueIds(newParamValueIds)
                            .setNewData(newData);
                    paramDiffs.add(paramDiff);
                }
            }

        }
        return paramDiffs;
    }

    /**
     * ??????????????????diff
     */
    public RelationDiff getRelationDiffsBetweenReleases(Long objId, Long oldTerminalReleaseId, Long newTerminalReleaseId) {
        Set<Long> o = getParentObjIdsByTrackerId(objId, oldTerminalReleaseId);
        Set<Long> n = getParentObjIdsByTrackerId(objId, newTerminalReleaseId);
        Set<Long> newParents = new HashSet<>(Sets.difference(n, o));
        Set<Long> deletedParents = new HashSet<>(Sets.difference(o, n));
        return new RelationDiff(newParents, deletedParents);
    }

    /**
     * ??????????????????????????????diff
     */
    public RelationDiff getRelationDiffsBetweenReleaseAndReqPool(Long objId, Long oldTerminalReleaseId, Long newReqPoolId, Long newTerminalId) {
        List<ObjectBasicDTO> objectParentsByReqPooId = objectHelper.getObjectParentsByReqPooId(objId, newReqPoolId, newTerminalId);
        Set<Long> n = CollectionUtils.isEmpty(objectParentsByReqPooId) ? new HashSet<>() : objectParentsByReqPooId.stream().map(ObjectBasicDTO::getId).collect(Collectors.toSet());
        Set<Long> o = getParentObjIdsByTrackerId(objId, oldTerminalReleaseId);
        Set<Long> newParents = new HashSet<>(Sets.difference(n, o));
        Set<Long> deletedParents = new HashSet<>(Sets.difference(o, n));
        return new RelationDiff(newParents, deletedParents);
    }

    private Set<Long> getParentObjIdsByTrackerId(Long objId, Long terminalReleaseId) {
        EisObjAllRelationRelease query = new EisObjAllRelationRelease();
        query.setTerminalReleaseId(terminalReleaseId);
        query.setObjId(objId);
        Set<Long> parentObjIdsOfPreTracker = new HashSet<>();
        List<EisObjAllRelationRelease> oldRelations = allRelationReleaseMapper.select(query);
        if(!CollectionUtils.isEmpty(oldRelations)){
            for (EisObjAllRelationRelease oldRelation : oldRelations) {
                parentObjIdsOfPreTracker.add(oldRelation.getParentObjId());
            }
        }
        return parentObjIdsOfPreTracker;
    }

    public RelationDiff getRelationDiffs(EisObjTerminalTracker tracker){
        EisReqObjRelation reqObjRelationQuery = new EisReqObjRelation();
        reqObjRelationQuery.setObjId(tracker.getObjId());
        reqObjRelationQuery.setTerminalId(tracker.getTerminalId());
        reqObjRelationQuery.setReqPoolId(tracker.getReqPoolId());
        List<EisReqObjRelation> parentsInReq = reqObjRelationMapper.select(reqObjRelationQuery);
        Set<Long> parentObjIdsOfReq = new HashSet<>();
        for (EisReqObjRelation reqObjRelation : parentsInReq) {
            parentObjIdsOfReq.add(reqObjRelation.getParentObjId());
        }
        EisObjTerminalTracker preTracker = objTerminalTrackerMapper.selectByPrimaryKey(tracker.getPreTrackerId());
        Long releaseIdOfPreTracker = preTracker == null ? tracker.getTerminalReleaseId() : preTracker.getTerminalReleaseId();
        EisObjAllRelationRelease query = new EisObjAllRelationRelease();
        query.setTerminalReleaseId(releaseIdOfPreTracker);
        query.setObjId(preTracker == null ? tracker.getObjId() : preTracker.getObjId());
        Set<Long> parentObjIdsOfPreTracker = new HashSet<>();
        List<EisObjAllRelationRelease> oldRelations = allRelationReleaseMapper.select(query);
        if(!CollectionUtils.isEmpty(oldRelations)){
            for (EisObjAllRelationRelease oldRelation : oldRelations) {
                parentObjIdsOfPreTracker.add(oldRelation.getParentObjId());
            }
        }
        Set<Long> newParents = Sets.difference(parentObjIdsOfReq,parentObjIdsOfPreTracker);
        Set<Long> deletedParents = Sets.difference(parentObjIdsOfPreTracker,parentObjIdsOfReq);
        RelationDiff relationDiff = new RelationDiff(newParents,deletedParents);
        return relationDiff;
    }

    /**
     * ???????????????????????????????????????
     * @param paramsOfCur
     * @param paramsOfPre
     * @return
     */
    public List<ParamWithValueItemDTO> paramBindComparison(List<ParamWithValueItemDTO> paramsOfCur
            ,List<ParamWithValueItemDTO> paramsOfPre){
        if (CollectionUtils.isEmpty(paramsOfCur) && CollectionUtils.isEmpty(paramsOfPre)) {
            return Lists.newArrayList();
        }

        // ??????????????????????????????????????????ID?????????????????????key????????????Id, value????????????????????????
        Map<Long, ParamWithValueItemDTO> paramOfCur2Map =
                CollectionUtils.isEmpty(paramsOfCur) ? Maps.newHashMap() :
                        paramsOfCur.stream().collect(Collectors.toMap(
                                ParamWithValueItemDTO::getId, Function.identity()));
        Map<Long, ParamWithValueItemDTO> paramOfPre2Map =
                CollectionUtils.isEmpty(paramsOfPre) ? Maps.newHashMap() :
                        paramsOfPre.stream().collect(Collectors.toMap(
                                ParamWithValueItemDTO::getId, Function.identity()));

        List<ParamWithValueItemDTO> result = Lists.newArrayList();
        // ?????????????????????
        for (Long paramId : paramOfCur2Map.keySet()) {
            ParamWithValueItemDTO curParamBindItem = paramOfCur2Map.get(paramId);
            if (paramOfPre2Map.containsKey(paramId)) {
                // ??????curParamBindItem???preParamBindItem????????????
                ParamWithValueItemDTO preParamBindItem = paramOfPre2Map.get(paramId);
                if (curParamBindItem.equals(preParamBindItem)) {
                    // ?????????????????????
                    curParamBindItem.setParamChangeType(ChangeTypeEnum.SAME.getChangeType());
                    result.add(curParamBindItem);
                } else {
                    // ??????:????????????+??????
                    // ??????
                    curParamBindItem.setParamChangeType(ChangeTypeEnum.CREATE.getChangeType());
                    result.add(curParamBindItem);

                    // ??????
                    preParamBindItem.setParamChangeType(ChangeTypeEnum.DELETE.getChangeType());
                    result.add(preParamBindItem);
                }
            } else {
                // ??????
                curParamBindItem.setParamChangeType(ChangeTypeEnum.CREATE.getChangeType());
                result.add(curParamBindItem);
            }
        }

        // ????????????
        for (Long paramId : paramOfPre2Map.keySet()) {
            ParamWithValueItemDTO preParamBindItem = paramOfPre2Map.get(paramId);
            if (!paramOfCur2Map.containsKey(paramId)) {
                // ??????
                preParamBindItem.setParamChangeType(ChangeTypeEnum.DELETE.getChangeType());
                result.add(preParamBindItem);
            }
        }

        return result;
    }


    /**
     * diffTracker?????????????????????
     * @param taskId
     * @return
     */
    public Set<Long> getDiffTrackerIdsOfTask(Long taskId){
        return new HashSet<>();
    }

    /**
     * ???????????????????????????diff
     */
    public TrackerDiffDTO getReleaseTrackerDiff(Long objId, Long baseLineReleaseId, Long baseLineTrackerId, Long newReleaseId, Long newTrackerId) {
        boolean isSameTracker = Objects.equals(baseLineTrackerId, newTrackerId);
        Pair<Boolean, Long> pubParamPackageDiff = isSameTracker ? new Pair<>(false, null) : getPubParamPackageDiff(baseLineTrackerId, newTrackerId);
        List<ParamDiff> paramDiffs = isSameTracker ? new ArrayList<>(0) : getParamDiffs(baseLineTrackerId, newTrackerId);
        List<EventDiff> eventDiffs = isSameTracker ? new ArrayList<>(0) : getEventDiffs(baseLineTrackerId, newTrackerId);
        RelationDiff relationDiff = getRelationDiffsBetweenReleases(objId, baseLineReleaseId, newReleaseId);
        return new TrackerDiffDTO()
                .setPubParamPackageId(pubParamPackageDiff.getValue())
                .setPubParamPackageChange(pubParamPackageDiff.getKey())
                .setParamDiffs(paramDiffs)
                .setEventDiffs(eventDiffs)
                .setRelationDiff(relationDiff);
    }

    public static boolean isAnyChange(TrackerDiffDTO trackerDiffDTO) {
        return trackerDiffDTO.isPubParamPackageChange()
                || CollectionUtils.isNotEmpty(trackerDiffDTO.getRelationDiff().getNewParents())
                || CollectionUtils.isNotEmpty(trackerDiffDTO.getRelationDiff().getDeletedParents())
                || CollectionUtils.isNotEmpty(trackerDiffDTO.getEventDiffs())
                || CollectionUtils.isNotEmpty(trackerDiffDTO.getParamDiffs());
    }

    /**
     * ???????????????????????????diff
     */
    public TrackerDiffDTO getReqPoolTrackerDiff(Long objId, Long baseLineReleaseId, Long baseLineTrackerId, Long newTrackerId, Long newReqPoolId, Long newTerminalId) {
        boolean isSameTracker = Objects.equals(baseLineTrackerId, newTrackerId);
        Pair<Boolean, Long> pubParamPackageDiff = isSameTracker ? new Pair<>(false, null) : getPubParamPackageDiff(baseLineTrackerId, newTrackerId);
        List<ParamDiff> paramDiffs =  isSameTracker ? new ArrayList<>(0) : getParamDiffs(baseLineTrackerId, newTrackerId);
        List<EventDiff> eventDiffs =  isSameTracker ? new ArrayList<>(0) : getEventDiffs(baseLineTrackerId, newTrackerId);
        RelationDiff relationDiff = getRelationDiffsBetweenReleaseAndReqPool(objId, baseLineReleaseId, newReqPoolId, newTerminalId);
        return new TrackerDiffDTO()
                .setPubParamPackageId(pubParamPackageDiff.getValue())
                .setPubParamPackageChange(pubParamPackageDiff.getKey())
                .setParamDiffs(paramDiffs)
                .setEventDiffs(eventDiffs)
                .setRelationDiff(relationDiff);
    }
}
