package com.netease.hz.bdms.easyinsight.service.helper;

import com.netease.hz.bdms.easyinsight.common.bo.diff.EventDiff;
import com.netease.hz.bdms.easyinsight.common.bo.diff.ParamDiff;
import com.netease.hz.bdms.easyinsight.common.bo.diff.RelationDiff;
import com.netease.hz.bdms.easyinsight.common.dto.event.EventSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.obj.ObjectBasicDTO;
import com.netease.hz.bdms.easyinsight.common.dto.obj.ObjectTrackerInfoDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.parambind.ParamBindItemDTO;
import com.netease.hz.bdms.easyinsight.common.enums.ChangeTypeEnum;
import com.netease.hz.bdms.easyinsight.common.enums.DiffTypeEnum;
import com.netease.hz.bdms.easyinsight.dao.model.EisAllTrackerRelease;
import com.netease.hz.bdms.easyinsight.service.service.obj.AllTrackerReleaseService;
import com.netease.hz.bdms.easyinsight.service.service.obj.ObjTerminalTrackerService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TrackerDiffHelper {

    @Resource
    private ObjectHelper objectHelper;

    @Resource
    private DiffHelper diffHelper;

    @Resource
    private ObjTerminalTrackerService objTerminalTrackerService;

    @Resource
    private AllTrackerReleaseService allTrackerReleaseService;

    /**
     * 查询已发布版本之间的对比
     * @return currentReleaseId对应的版本数据
     */
    public ObjectTrackerInfoDTO getReleaseTrackerDiff(Long objId, Long currentReleaseId, Long preReleaseId) {
        // 当前版本为空，无需对比
        Long currentTrackerId = getTrackerIdOfObj(objId, currentReleaseId);
        if (currentTrackerId == null) {
            return null;
        }
        ObjectTrackerInfoDTO currentTrackerInfo = objectHelper.getObjTrackersInfo(objId, currentTrackerId);
        if (currentTrackerInfo == null) {
            return null;
        }
        // 没有上个版本，无需对比
        Long preTrackerId = getTrackerIdOfObj(objId, preReleaseId);
        if (preTrackerId == null || preReleaseId.equals(currentReleaseId)) {
            return currentTrackerInfo;
        }
        composeTrackerDiff(objId, currentTrackerInfo, preTrackerId);
        return currentTrackerInfo;
    }

    public void composeTrackerDiff(Long objId, ObjectTrackerInfoDTO currentTracker) {
        composeTrackerDiff(objId, currentTracker, null);
    }

    public void composeTrackerDiff(Long objId, ObjectTrackerInfoDTO currentTracker, Long preTrackerId) {
        Long trackerId = currentTracker.getId();
        if (preTrackerId == null) {
            preTrackerId = currentTracker.getPreTrackerId();
        }
        if (currentTracker.getParentObjects() == null) {
            currentTracker.setParentObjects(new ArrayList<>());
        }

        // 属于新增的对象，没有preTracker
        if (preTrackerId == null || preTrackerId.equals(0L)) {
            setAllNew(currentTracker);
            return;
        }

        ObjectTrackerInfoDTO preTracker = objectHelper.getObjTrackersInfo(objId, preTrackerId);
        if (preTracker == null) {
            // 应该不会走到这个逻辑，tracker表不会做删除
            setAllNew(currentTracker);
            return;
        }

        // 组装公参数diff展示
        boolean pubParamPackageChanged = diffHelper.isPubParamPackageChanged(preTrackerId, trackerId);
        currentTracker.setPubParamPackageIdDiff(pubParamPackageChanged ? DiffTypeEnum.MOD.getFieldName() : DiffTypeEnum.NON.getFieldName());

        // 组装事件公参包diff
        currentTracker.setEventParamVersionDiff(getEventParamVersionDiff(currentTracker.getEventParamVersionIdMap(), preTracker.getEventParamVersionIdMap()));
        // 补充展示删除的EventParamVersionId
        currentTracker.getEventParamVersionDiff().forEach((eventCode, diffType) -> {
            if (DiffTypeEnum.DEL.getFieldName().equals(diffType)) {
                Long deletedVersionId = preTracker.getEventParamVersionIdMap().get(eventCode);
                if (deletedVersionId != null) {
                    currentTracker.getEventParamVersionIdMap().put(eventCode, deletedVersionId);
                }
            }
        });

        // 组装事件diff展示
        List<EventDiff> eventDiffs = diffHelper.getEventDiffs(preTrackerId, trackerId);
        if (CollectionUtils.isNotEmpty(eventDiffs)) {
            List<EventSimpleDTO> deletedEvent = new ArrayList<>();
            Map<Long, List<EventDiff>> eventIdDiffMap = eventDiffs.stream().collect(Collectors.groupingBy(EventDiff::getEventId));
            eventIdDiffMap.forEach((eventId, eventDiffsOfEventId) -> {
                if (CollectionUtils.isEmpty(eventDiffsOfEventId)) {
                    return;
                }
                // 有新增、又有删除，其实是修改、或者没变
                if (eventDiffsOfEventId.size() > 1) {
                    // 事件本身在编辑对象时是没有可以修改的东西的
                    EventSimpleDTO currentEvent = findEvent(currentTracker, eventId);
                    if (currentEvent != null) {
                        currentEvent.setDiffType(DiffTypeEnum.NON.getFieldName());
                    }
                    return;
                }
                // 仅新增或仅删除
                EventSimpleDTO event = findEvent(currentTracker, preTracker, eventId);
                if (event != null) {
                    DiffTypeEnum diffTypeEnum = toDiffTpeEnum(eventDiffsOfEventId.get(0).getChangeType());
                    event.setDiffType(diffTypeEnum.getFieldName());
                    if (diffTypeEnum == DiffTypeEnum.DEL) {
                        deletedEvent.add(event);
                    }
                }
            });
            // 删除的在currentTracker里没有，要补充进去可查看
            currentTracker.getEvents().addAll(deletedEvent);
        }

        // 组装血缘diff展示
        RelationDiff relationDiffs = diffHelper.getRelationDiffs(objTerminalTrackerService.getById(currentTracker.getId()));
        if (relationDiffs != null) {
            if (CollectionUtils.isNotEmpty(relationDiffs.getNewParents())) {
                for (Long newParent : relationDiffs.getNewParents()) {
                    ObjectBasicDTO parent = findParent(currentTracker, preTracker, newParent);
                    if (parent != null) {
                        parent.setDiffType(DiffTypeEnum.NEW.getFieldName());
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(relationDiffs.getDeletedParents())) {
                for (Long deletedParent : relationDiffs.getDeletedParents()) {
                    ObjectBasicDTO parent = findParent(currentTracker, preTracker, deletedParent);
                    if (parent != null) {
                        parent.setDiffType(DiffTypeEnum.DEL.getFieldName());
                        // 删除的在currentTracker里没有，要补充进去可查看
                        currentTracker.getParentObjects().add(parent);
                    }
                }
            }
        }

        // 参数变化情况
        List<ParamBindItemDTO> deleteParamBindItems = new ArrayList<>();
        List<ParamDiff> paramDiffs = diffHelper.getParamDiffs(objId, preTrackerId, trackerId);
        if (CollectionUtils.isNotEmpty(paramDiffs)) {
            Map<Long, List<ParamDiff>> paramIdDiffMap = paramDiffs.stream().collect(Collectors.groupingBy(ParamDiff::getParamId));
            paramIdDiffMap.forEach((paramId, paramDiffsOfParamId) -> {
                // 有新增、又有删除，其实是修改、或者不变
                if (paramDiffsOfParamId.size() > 1) {
                    ParamBindItemDTO current = findPrivateParam(currentTracker, paramId);
                    ParamBindItemDTO pre = findPrivateParam(preTracker, paramId);
                    if (current == null || pre == null) {
                        return;
                    }
                    boolean isSameSelectedValues = CollectionUtils.isEqualCollection(current.getSelectedValues(), pre.getSelectedValues());
                    boolean isSameNull = isSameBoolean(current.getNotEmpty(), pre.getNotEmpty());
                    boolean isSameMust = isSameBoolean(current.getMust(), pre.getMust());
                    boolean isSameEncoded = isSameBoolean(current.getIsEncode(), pre.getIsEncode());
                    boolean isAllSame = isSameSelectedValues && isSameNull && isSameEncoded && isSameMust;
                    current.setDiffType(isAllSame ? DiffTypeEnum.NON.getFieldName() : DiffTypeEnum.MOD.getFieldName());
                    current.setSelectedValuesDiff(isSameSelectedValues ? DiffTypeEnum.NON.getFieldName() : DiffTypeEnum.MOD.getFieldName());
                    return;
                }
                // 仅新增，或仅删除
                ParamBindItemDTO privateParam = findPrivateParam(currentTracker, preTracker, paramId);
                if (privateParam != null) {
                    DiffTypeEnum diffTypeEnum = toDiffTpeEnum(paramDiffsOfParamId.get(0).getChangeType());
                    if (diffTypeEnum == DiffTypeEnum.DEL) {
                        deleteParamBindItems.add(privateParam);
                    }
                    privateParam.setDiffType(diffTypeEnum.getFieldName());
                    privateParam.setSelectedValuesDiff(diffTypeEnum.getFieldName());
                }
            });
        }
        if (CollectionUtils.isNotEmpty(deleteParamBindItems)) {
            currentTracker.getPrivateParam().addAll(deleteParamBindItems);
        }
    }

    private boolean isSameBoolean(Boolean a, Boolean b) {
        if (a == null && b == null) {
            return true;
        }
        if (a != null && b != null) {
            return a.equals(b);
        }
        return false;
    }

    private Map<Long, String> getEventParamVersionDiff(Map<Long, Long> current, Map<Long, Long> pre) {
        if (current == null) {
            current = new HashMap<>();
        }
        if (pre == null) {
            pre = new HashMap<>();
        }
        Set<Long> allEventIds = new HashSet<>();
        allEventIds.addAll(current.keySet());
        allEventIds.addAll(pre.keySet());

        Map<Long, String> result = new HashMap<>();
        for (Long eventId : allEventIds) {
            Long currentV = current.get(eventId);
            Long preV = pre.get(eventId);
            if (currentV == null && preV == null) {
                result.put(eventId, DiffTypeEnum.NON.getFieldName());
                continue;
            }
            if (currentV != null && preV == null) {
                result.put(eventId, DiffTypeEnum.NEW.getFieldName());
                continue;
            }
            if (currentV == null && pre != null) {
                result.put(eventId, DiffTypeEnum.DEL.getFieldName());
                continue;
            }
            if (currentV != null && preV != null) {
                boolean same = currentV.equals(preV);
                result.put(eventId, same ? DiffTypeEnum.NON.getFieldName() : DiffTypeEnum.MOD.getFieldName() );
            }
        }
        return result;
    }

    private ParamBindItemDTO findPrivateParam(ObjectTrackerInfoDTO tracker, ObjectTrackerInfoDTO preTracker, Long paramId) {
        return Optional.ofNullable(findPrivateParam(tracker, paramId)).orElse(findPrivateParam(preTracker, paramId));

    }

    private ParamBindItemDTO findPrivateParam(ObjectTrackerInfoDTO tracker, Long paramId) {
        if (CollectionUtils.isNotEmpty(tracker.getPrivateParam())) {
            for (ParamBindItemDTO privateParam : tracker.getPrivateParam()) {
                if (privateParam.getId().equals(paramId)) {
                    return privateParam;
                }
            }
        }
        return null;
    }

    private ObjectBasicDTO findParent(ObjectTrackerInfoDTO tracker, ObjectTrackerInfoDTO preTracker, Long parentObjId) {
        return Optional.ofNullable(findParent(tracker, parentObjId)).orElse(findParent(preTracker, parentObjId));
    }

    private ObjectBasicDTO findParent(ObjectTrackerInfoDTO tracker, Long parentObjId) {
        if (CollectionUtils.isNotEmpty(tracker.getParentObjects())) {
            for (ObjectBasicDTO parentObject : tracker.getParentObjects()) {
                if (parentObject.getId().equals(parentObjId)) {
                    return parentObject;
                }
            }
        }
        return null;
    }

    private EventSimpleDTO findEvent(ObjectTrackerInfoDTO tracker, ObjectTrackerInfoDTO preTracker, Long eventId) {
        return Optional.ofNullable(findEvent(tracker, eventId)).orElse(findEvent(preTracker, eventId));
    }

    private EventSimpleDTO findEvent(ObjectTrackerInfoDTO tracker, Long eventId) {
        if (CollectionUtils.isNotEmpty(tracker.getEvents())) {
            for (EventSimpleDTO event : tracker.getEvents()) {
                if (event.getId().equals(eventId)) {
                    return event;
                }
            }
        }
        return null;
    }

    private DiffTypeEnum toDiffTpeEnum(Integer changeType) {
        if (ChangeTypeEnum.CREATE.getChangeType().equals(changeType)) {
            return DiffTypeEnum.NEW;
        }
        if (ChangeTypeEnum.DELETE.getChangeType().equals(changeType)) {
            return DiffTypeEnum.DEL;
        }
        return DiffTypeEnum.NON;
    }

    private void setAllNew(ObjectTrackerInfoDTO currentTracker) {
        // 公参
        currentTracker.setPubParamPackageIdDiff(DiffTypeEnum.NEW.getFieldName());

        // 参数绑定
        currentTracker.setEventParamVersionDiff(new HashMap<>());
        if (MapUtils.isNotEmpty(currentTracker.getEventParamVersionIdMap())) {
            currentTracker.getEventParamVersionIdMap().keySet().forEach(eventId -> currentTracker.getEventParamVersionDiff().put(eventId, DiffTypeEnum.NEW.getFieldName()));
        }
        // 血缘
        if (CollectionUtils.isNotEmpty(currentTracker.getParentObjects())) {
            currentTracker.getParentObjects().forEach(parentObj -> parentObj.setDiffType(DiffTypeEnum.NEW.getFieldName()));
        }
        // 事件
        if (CollectionUtils.isNotEmpty(currentTracker.getEvents())) {
            currentTracker.getEvents().forEach(eventSimpleDTO -> eventSimpleDTO.setDiffType(DiffTypeEnum.NEW.getFieldName()));
        }
        // 私参
        if (CollectionUtils.isNotEmpty(currentTracker.getPrivateParam())) {
            currentTracker.getPrivateParam().forEach(privateParam -> {
                privateParam.setDiffType(DiffTypeEnum.NEW.getFieldName());
                privateParam.setSelectedValuesDiff(DiffTypeEnum.NEW.getFieldName());
            });
        }
    }

    /**
     * 获取某个已发布版本下的对象trackerId
     */
    private Long getTrackerIdOfObj(Long objId, Long releaseId) {
        EisAllTrackerRelease allTrackerRelease = allTrackerReleaseService.getByReleaseIdAndObjId(releaseId, objId);
        if (allTrackerRelease == null) {
            return null;
        }
        return allTrackerRelease.getTrackerId();
    }
}
