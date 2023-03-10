package com.netease.hz.bdms.easyinsight.service.facade;
import com.netease.eis.adapters.CacheAdapter;
import com.netease.eis.adapters.RealtimeConfigAdapter;
import com.netease.hz.bdms.easyinsight.service.service.*;
import com.netease.hz.bdms.easyinsight.common.dto.obj.*;
import com.netease.hz.bdms.easyinsight.common.dto.param.parambind.ParamBindItemDTO;
import com.netease.hz.bdms.easyinsight.common.dto.require.ReqPoolObjDTO;
import com.netease.hz.bdms.easyinsight.common.dto.spm.SpmInfoDTO;
import com.netease.hz.bdms.easyinsight.common.dto.tag.SpmTagSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.enums.OperationTypeEnum;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.netease.hz.bdms.easyinsight.common.aop.MethodLog;
import com.netease.hz.bdms.easyinsight.common.bo.lineage.LinageGraph;
import com.netease.hz.bdms.easyinsight.common.bo.lineage.Node;
import com.netease.hz.bdms.easyinsight.common.bo.lineage.TotalLineageGraph;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.checkhistory.CheckHistorySimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.CommonAggregateDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.CommonRelationAggregateDTO;
import com.netease.hz.bdms.easyinsight.common.dto.event.EventSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.image.ImageRelationDTO;
import com.netease.hz.bdms.easyinsight.common.dto.obj.tracker.event.ObjTrackerEventSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.UpdateSpmPoolParam;
import com.netease.hz.bdms.easyinsight.common.dto.tag.ObjTagSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.tag.TagSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.terminal.TerminalSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.enums.*;
import com.netease.hz.bdms.easyinsight.common.exception.CommonException;
import com.netease.hz.bdms.easyinsight.common.exception.ObjException;
import com.netease.hz.bdms.easyinsight.common.obj.TwoTuple;
import com.netease.hz.bdms.easyinsight.common.param.obj.*;
import com.netease.hz.bdms.easyinsight.common.param.param.paramBind.ParamBindItermParam;
import com.netease.hz.bdms.easyinsight.common.param.tag.ObjBasicTagDTO;
import com.netease.hz.bdms.easyinsight.common.util.*;
import com.netease.hz.bdms.easyinsight.common.vo.obj.*;
import com.netease.hz.bdms.easyinsight.common.vo.release.BaseReleaseVO;
import com.netease.hz.bdms.easyinsight.dao.model.*;
import com.netease.hz.bdms.easyinsight.service.helper.LineageHelper;
import com.netease.hz.bdms.easyinsight.service.helper.ObjectHelper;
import com.netease.hz.bdms.easyinsight.service.helper.RequirementPoolHelper;
import com.netease.hz.bdms.easyinsight.service.helper.TrackerDiffHelper;
import com.netease.hz.bdms.easyinsight.service.service.obj.AllTrackerReleaseService;
import com.netease.hz.bdms.easyinsight.service.service.obj.ObjCidInfoService;
import com.netease.hz.bdms.easyinsight.service.service.obj.ObjTerminalTrackerService;
import com.netease.hz.bdms.easyinsight.service.service.requirement.ReqPoolRelBaseService;
import com.netease.hz.bdms.easyinsight.service.service.requirement.TaskProcessService;
import com.netease.hz.bdms.easyinsight.service.service.terminalrelease.TerminalReleaseService;
import javafx.util.Pair;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author: xumengqiang
 * @date: 2021/12/9 10:20
 */
@Slf4j
@Component
public class ObjectFacade implements InitializingBean {

    @Resource
    private RealtimeConfigAdapter realtimeConfigAdapter;

    @Autowired
    ObjectHelper objectHelper;

    @Autowired
    ObjectBasicService objectBasicService;

    @Autowired
    ImageRelationService imageRelationService;

    @Autowired
    ObjTagService objTagService;

    @Autowired
    ObjChangeHistoryService objChangeHistoryService;

    @Autowired
    RequirementPoolHelper requirementPoolHelper;

    @Autowired
    ObjTerminalTrackerService objTerminalTrackerService;

    @Autowired
    ObjTrackerEventService objTrackerEventService;

    @Autowired
    EventService eventService;

    @Autowired
    SpmCheckHistoryService spmCheckHistoryService;

    @Autowired
    LineageHelper lineageHelper;

    @Autowired
    AllTrackerReleaseService trackerReleaseService;

    @Autowired
    ReqPoolRelBaseService reqPoolRelBaseService;

    @Autowired
    TerminalService terminalService;

    @Autowired
    TerminalReleaseService terminalReleaseService;

    @Autowired
    TagService tagService;

    @Autowired
    TerminalVersionInfoService terminalVersionInfoService;

    @Autowired
    TaskProcessService taskProcessService;

    @Autowired
    SpmTagService spmTagService;

    @Autowired
    SpmInfoService spmInfoService;

    @Resource
    private TrackerDiffHelper trackerDiffHelper;

    @Resource
    private ObjCidInfoService objCidInfoService;

    @Resource
    private CacheAdapter cacheAdapter;
    /**
     * ???????????????????????? key: appId
     */
    private static CheckObjImageNotEmptyConfig checkObjImageNotEmptyConfig = new CheckObjImageNotEmptyConfig();

    @Override
    public void afterPropertiesSet() throws Exception {
        realtimeConfigAdapter.listenJSON("checkObjImageNotEmpty", (s) ->  checkObjImageNotEmptyConfig = JsonUtils.parseObject(s, CheckObjImageNotEmptyConfig.class));
    }

    @Data
    public static class CheckObjImageNotEmptyConfig {
        private boolean defaultValue = false;
        private Map<Long, Boolean> appValues = new HashMap<>();
    }

    /**
     * ????????????
     *
     * @param param ????????????????????????????????????????????????
     * @return oid -> objId
     */
    @MethodLog
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Map<String, Long> createObject(ObjectCreateParam param) {
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "????????????????????????");
        Preconditions.checkArgument(null != param, "?????????????????????????????????");
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(param.getBasics()), "?????????????????????????????????");
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(param.getTrackers()), "?????????????????????????????????");
        for (ObjectBasicCreateParam basic : param.getBasics()) {
            ParamCheckUtil.checkOid(basic.getOid());
        }

        // 1. ?????????????????????????????? ??????oid??????
        List<ObjectBasicCreateParam> objBasicInfoList = param.getBasics();
        List<String> objectOidList = objBasicInfoList.stream()
                .map(ObjectBasicCreateParam::getOid).distinct()
                .collect(Collectors.toList());
        List<String> objectNameList = objBasicInfoList.stream()
                .map(ObjectBasicCreateParam::getName).distinct()
                .collect(Collectors.toList());
        objBasicInfoList.forEach(o -> objectHelper.checkOidByType(o.getOid(), o.getType()));
        objBasicInfoList.forEach(o -> objectHelper.checkBridge(o.getBridgeSubAppId(), o.getBridgeSubTerminalId(), o.getSpecialType()));
        objectHelper.checkObjExists(objectOidList, objectNameList, objBasicInfoList.get(0).getType());

        // ???????????????????????????
        param.getTrackers().forEach(trackerCreateParam -> objectHelper.checkParentExist(appId, trackerCreateParam.getParentObjs(), trackerCreateParam.getTerminalId(), param.getReqPoolId()));

        // 2. ????????????????????????
        Map<String, Long> result = new HashMap<>();
        for(ObjectBasicCreateParam objectBasicInfo : param.getBasics()){

            // 2.1 ???????????????????????? (????????????????????????????????????????????????????????????????????????)
            // ?????????????????????
            ObjectBasic objectBasic = BeanConvertUtils.convert(objectBasicInfo, ObjectBasic.class);
            if(null == objectBasic) {
                log.error("convert to object basic failure!");
                throw new ObjException("???????????????????????????????????????????????????!");
            }
            ObjectExtDTO objectExtDTO = new ObjectExtDTO();
            objectExtDTO.setSubAppId(objectBasicInfo.getBridgeSubAppId());
            objectExtDTO.setSubTerminalId(objectBasicInfo.getBridgeSubTerminalId() == null ? 0L : objectBasicInfo.getBridgeSubTerminalId());
            objectExtDTO.setBasicTag(convertObjBasicTag(objectBasicInfo.getOid(), objectBasicInfo.getType(), objectBasicInfo.getBasicTag()));
            objectExtDTO.setAnalyseCid(objectExtDTO.isAnalyseCid());
            objectBasic.setExt(JsonUtils.toJson(objectExtDTO));
            final Long objId = objectBasicService.insert(objectBasic);
            // ??????????????????/??????????????????????????????`eis_obj_change_history`???????????????
            EisObjChangeHistory objChangeHistory = new EisObjChangeHistory();
            objChangeHistory.setObjId(objId);
            objChangeHistory.setReqPoolId(param.getReqPoolId());
            objChangeHistory.setType(OperationTypeEnum.CREATE.getOperationType());
            objChangeHistory.setConsistency(param.getConsistency());
            final Long objChangeHistoryId = objChangeHistoryService.insert(objChangeHistory);
            // ??????????????????????????????
            List<String> imgUrls = objectBasicInfo.getImgUrls();
            checkImageUrlsNotEmpty(appId, imgUrls);
            List<ImageRelationDTO> imageRelationDTOS = Lists.newArrayList();
            for (String imgUrl : imgUrls) {
                ImageRelationDTO imageRelationDTO = new ImageRelationDTO();
                imageRelationDTO.setEntityId(objChangeHistoryId)
                        .setEntityType(EntityTypeEnum.OBJHISTORY.getType())
                        .setUrl(imgUrl);
                imageRelationDTOS.add(imageRelationDTO);
            }
            imageRelationService.createBatch(imageRelationDTOS);
            // ????????????????????????
            List<Long> tagIds = objectBasicInfo.getTagIds();
            List<ObjTagSimpleDTO> objTagSimpleDTOS = Lists.newArrayList();
            for (Long tagId : tagIds) {
                ObjTagSimpleDTO objTagSimpleDTO = new ObjTagSimpleDTO();
                objTagSimpleDTO.setObjId(objId)
                        .setHistoryId(objChangeHistoryId)
                        .setTagId(tagId)
                        .setAppId(appId);
                objTagSimpleDTOS.add(objTagSimpleDTO);
            }
            objTagService.createBatch(objTagSimpleDTOS);

            // ??????CID??????
            objCidInfoService.update(appId, objId, objectBasicInfo.getCidTagInfos());

            // 2.2 ?????????????????????????????????????????????
            List<Long> trackerIds = objectHelper.createObjectTrackersInfo(
                    objId, objChangeHistoryId, param.getReqPoolId(), param.getTrackers());

            // 2.3 ????????????spm???
            requirementPoolHelper.updateSpmPool(
                    param.getReqPoolId(), Sets.newHashSet(trackerIds), OperationTypeEnum.CREATE, false);
            result.put(objectBasicInfo.getOid(), objId);
        }
        return result;
    }

    /**
     * ?????????????????????
     * @param id
     * @param name
     * @return
     */
    public ObjectBasic updateName(long id, String name) {
        ObjectBasic objectBasic = objectBasicService.getById(id);
        if (objectBasic == null) {
            return null;
        }
        objectBasic.setName(name);
        objectBasicService.update(objectBasic);
        return objectBasicService.getById(id);
    }


    /**
     * ????????????
     *
     * @param param ??????????????????
     */
    @MethodLog
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void changeObject(ObjectChangeParam param, ObjDetailsVO objDetails){
        // ????????????
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "????????????????????????");
        Preconditions.checkArgument(null != param, "?????????????????????????????????");
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(param.getTrackers()),
                "?????????????????????????????????");
        for (ObjectTrackerChangeParam tracker : param.getTrackers()) {
            if (tracker.getId() == null) {
                throw new CommonException("??????????????????????????????????????????????????????????????????????????????");
            }
        }

        // ????????????????????????
        Long objId = param.getId();
        Long reqPoolId = param.getReqPoolId();
        if(objectHelper.isChanged(objId, reqPoolId)){
            throw new ObjException(String.format("oid={%s}????????????????????????????????????????????????", param.getOid()));
        }

        // ???????????????????????????
        param.getTrackers().forEach(trackerChangeParam -> objectHelper.checkParentExist(appId, trackerChangeParam.getParentObjs(), trackerChangeParam.getTerminalId(), param.getReqPoolId()));

        // 1. ????????????????????????
        // 1.1 ?????????????????????(?????????????????????????????????????????????????????????oid???name?????????????????????)
        ObjectBasic objectBasic = new ObjectBasic();
        objectBasic.setId(objId)
                .setDescription(param.getDescription())
                .setPriority(param.getPriority());
        objectBasicService.update(objectBasic);
        // 1.2 ????????????????????????
        EisObjChangeHistory objChangeHistory = new EisObjChangeHistory();
        objChangeHistory.setObjId(objId);
        objChangeHistory.setReqPoolId(reqPoolId);
        // ???????????????????????????
        boolean reuse = checkReuse(param, objDetails);
        objChangeHistory.setType(reuse ? OperationTypeEnum.REUSER.getOperationType() : OperationTypeEnum.CHANGE.getOperationType());
        objChangeHistory.setConsistency(param.getConsistency());
        final Long objChangeHistoryId = objChangeHistoryService.insert(objChangeHistory);
        // 1.3 ??????????????????????????????
        List<String> imgUrls = param.getImgUrls();
        List<ImageRelationDTO> imageRelationDTOS = Lists.newArrayList();
        for (String imgUrl : imgUrls) {
            ImageRelationDTO imageRelationDTO = new ImageRelationDTO();
            imageRelationDTO.setEntityId(objChangeHistoryId)
                    .setEntityType(EntityTypeEnum.OBJHISTORY.getType())
                    .setUrl(imgUrl);
            imageRelationDTOS.add(imageRelationDTO);
        }
        imageRelationService.createBatch(imageRelationDTOS);

        // 2. ????????????????????????
        List<Long> trackerIds = objectHelper.changeObjectTrackerInfo(
                objId, objChangeHistoryId, reqPoolId, param.getTrackers());

        // 3. ???????????? spm??????????????? ??????
        requirementPoolHelper.updateSpmPool(
                reqPoolId, Sets.newHashSet(trackerIds), OperationTypeEnum.CHANGE,false);
    }

    /**
     * ????????????????????????
     *
     * @param param ????????????????????????
     */
    @MethodLog
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void editObjectBasic(ObjectBasicChangeParam param){
        // ????????????
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "????????????????????????");
        Preconditions.checkArgument(null != param, "?????????????????????????????????");
        ParamCheckUtil.checkOid(param.getOid());

        // ????????????/??????????????????
        Long objId = param.getId();
        Long reqPoolId = param.getReqPoolId();
        List<EisObjChangeHistory> objChangeHistoryList = objChangeHistoryService
                .getByObjAndReqPoolId(objId, reqPoolId);
        if(objChangeHistoryList.size() != 1){
            log.info("objId???{}????????????reqId???{}?????????????????????????????????????????????????????????", objId, reqPoolId);
            throw new ObjException("??????????????????????????????????????????????????????????????????????????????????????????");
        }

        // ????????????????????????
        EisObjChangeHistory objChangeHistory = objChangeHistoryList.get(0);
        Long objHistoryId = objChangeHistory.getId();

        // 1. ????????????????????????
        // ?????????????????????
        ObjectBasic objectBasic = BeanConvertUtils.convert(param, ObjectBasic.class);
        if(null == objectBasic){
            log.error("convert to object basic failure!");
            throw new ObjException("?????????????????????????????????????????????");
        }
        objectBasic.setSpecialType(null);
        ObjectExtDTO ext = getExt(objId);
        // ???????????????????????????????????????????????????????????????????????????????????????
        ext.setBasicTag(convertObjBasicTag(param.getOid(), param.getType(), param.getBasicTag()));
        ext.setAnalyseCid(param.isAnalyseCid());
        objectBasic.setExt(JsonUtils.toJson(ext));
        objectBasicService.update(objectBasic);
        // ?????????????????????????????? (??????????????????)
        imageRelationService.deleteImageRelation(Collections.singletonList(objHistoryId),
                EntityTypeEnum.OBJHISTORY.getType());
        List<String> imgUrls = param.getImgUrls();
        checkImageUrlsNotEmpty(appId, imgUrls);
        List<ImageRelationDTO> imageRelationDTOS = Lists.newArrayList();
        for (String imgUrl : imgUrls) {
            ImageRelationDTO imageRelationDTO = new ImageRelationDTO();
            imageRelationDTO.setEntityId(objHistoryId)
                    .setEntityType(EntityTypeEnum.OBJHISTORY.getType())
                    .setUrl(imgUrl);
            imageRelationDTOS.add(imageRelationDTO);
        }
        imageRelationService.createBatch(imageRelationDTOS);
        // ?????????????????????????????? (??????????????????)
        objTagService.deleteObjTagByObjIds(Collections.singletonList(objId));
        List<Long> tagIds = param.getTagIds();
        List<ObjTagSimpleDTO> objTagSimpleDTOS = Lists.newArrayList();
        for (Long tagId : tagIds) {
            ObjTagSimpleDTO objTagSimpleDTO = new ObjTagSimpleDTO();
            objTagSimpleDTO.setObjId(objId)
                    .setAppId(appId)
                    .setHistoryId(objHistoryId)
                    .setTagId(tagId);
            objTagSimpleDTOS.add(objTagSimpleDTO);
        }
        objTagService.createBatch(objTagSimpleDTOS);
        // ??????CID??????
        objCidInfoService.update(appId, objId, param.getCidTagInfos());
    }

    /**
     * ????????????
     *
     * @param param ??????????????????
     * @return ????????????????????????????????????
     */
    @MethodLog
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public boolean editObject(ObjectEditParam param){
        // ????????????
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "????????????????????????");
        Preconditions.checkArgument(null != param, "?????????????????????????????????");
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(param.getTrackers()),
                "?????????????????????????????????");
        ParamCheckUtil.checkOid(param.getOid());


        // ?????????????????????????????????????????????????????????
        fixParamBindsConsitency(param);

        // ????????????/??????????????????
        Long objId = param.getId();
        Long reqPoolId = param.getReqPoolId();
        List<EisObjChangeHistory> objChangeHistoryList = objChangeHistoryService
                .getByObjAndReqPoolId(objId, reqPoolId);
        if(objChangeHistoryList.size() != 1){
            log.info("objId???{}????????????reqId???{}?????????????????????????????????????????????????????????", objId, reqPoolId);
            throw new ObjException("??????????????????????????????????????????????????????????????????????????????????????????");
        }

        // ??????????????????????????????
        EisTaskProcess taskProcessQuery = new EisTaskProcess();
        taskProcessQuery.setObjId(objId);
        taskProcessQuery.setReqPoolId(reqPoolId);
        List<EisTaskProcess> taskProcesses = taskProcessService.search(taskProcessQuery);
        for (EisTaskProcess taskProcess : taskProcesses) {
            if(ProcessStatusEnum.ONLINE.getState().equals(taskProcess.getStatus())){
                throw new CommonException("??????????????????????????????????????????????????????????????????");
            }
        }

        // ?????????????????????????????????trackers???????????????
        if (ObjTypeEnum.POPOVER.getType().equals(param.getType())) {
            List<ObjectTrackerEditParam> trackers = param.getTrackers();
            if (CollectionUtils.isNotEmpty(trackers)) {
                trackers.forEach(tracker -> tracker.setParentObjs(new ArrayList<>(0)));
            }
        }

        // ???????????????????????????
        param.getTrackers().forEach(trackerEditParam -> objectHelper.checkParentExist(appId, trackerEditParam.getParentObjs(), trackerEditParam.getTerminalId(), param.getReqPoolId()));

        // ???????????????????????????????????????
        EisObjChangeHistory objChangeHistory = objChangeHistoryList.get(0);
        Long objHistoryId = objChangeHistory.getId();
        boolean isInMergeConflict = ConflictStatusEnum.fromStatus(objChangeHistory.getConflictStatus()) == ConflictStatusEnum.MERGE_CONFLICT;
        // ??????????????????????????????????????????????????????????????????????????????????????????
        if (isInMergeConflict && !param.isResolveConflict()) {
            throw new CommonException("?????????????????????????????????????????????????????????????????????");
        }


        // 1. ????????????????????????
        // ?????????????????????
        ObjectBasic objectBasic = BeanConvertUtils.convert(param, ObjectBasic.class);
        if(null == objectBasic){
            log.error("convert to object basic failure!");
            throw new ObjException("?????????????????????????????????????????????");
        }
        ObjectBasic objectOriginBasic = objectBasicService.getById(objId);
        // ?????????????????????????????????OID
        boolean isOidChanged = !StringUtils.equals(objectOriginBasic.getOid(), objectBasic.getOid());
        if (isOidChanged) {
            Integer operationType = objChangeHistory.getType();
            boolean canEditBasic = OperationTypeEnum.CREATE.getOperationType().equals(operationType) || OperationTypeEnum.TERMINAL_ADD.getOperationType().equals(operationType);
            if (!canEditBasic) {
                throw new CommonException("?????????????????????????????????OID");
            }
        }

        if (objectBasic.getSpecialType() == null) {
            objectBasic.setSpecialType(ObjSpecialTypeEnum.NORMAL.getName());
        }
        if (objectBasic.getSpecialType().equals(ObjSpecialTypeEnum.BRIDGE.getName())) {
            // ??????????????????????????????
            objectHelper.checkBridge(param.getBridgeSubAppId(), param.getBridgeSubTerminalId(), objectBasic.getSpecialType());
        }
        ObjectExtDTO objectExtDTO = new ObjectExtDTO();
        objectExtDTO.setSubAppId(param.getBridgeSubAppId());
        objectExtDTO.setSubTerminalId(param.getBridgeSubTerminalId() == null ? 0L : param.getBridgeSubTerminalId());
        objectExtDTO.setBasicTag(convertObjBasicTag(param.getOid(), param.getType(), param.getBasicTag()));
        objectExtDTO.setAnalyseCid(param.isAnalyseCid());
        objectBasic.setExt(JsonUtils.toJson(objectExtDTO));
        objectBasicService.update(objectBasic);

        // ??????????????????????????????
        objChangeHistory.setConsistency(param.getConsistency());
        objChangeHistoryService.update(objChangeHistory);

        // ?????????????????????????????? (??????????????????)
        imageRelationService.deleteImageRelation(Collections.singletonList(objHistoryId),
                EntityTypeEnum.OBJHISTORY.getType());
        List<String> imgUrls = param.getImgUrls();
        checkImageUrlsNotEmpty(appId, imgUrls);
        List<ImageRelationDTO> imageRelationDTOS = Lists.newArrayList();
        for (String imgUrl : imgUrls) {
            ImageRelationDTO imageRelationDTO = new ImageRelationDTO();
            imageRelationDTO.setEntityId(objHistoryId)
                    .setEntityType(EntityTypeEnum.OBJHISTORY.getType())
                    .setUrl(imgUrl);
            imageRelationDTOS.add(imageRelationDTO);
        }
        imageRelationService.createBatch(imageRelationDTOS);
        // ?????????????????????????????? (??????????????????)
        objTagService.deleteObjTagByObjIds(Collections.singletonList(objId));
        List<Long> tagIds = param.getTagIds();
        List<ObjTagSimpleDTO> objTagSimpleDTOS = Lists.newArrayList();
        for (Long tagId : tagIds) {
            ObjTagSimpleDTO objTagSimpleDTO = new ObjTagSimpleDTO();
            objTagSimpleDTO.setObjId(objId)
                    .setAppId(appId)
                    .setHistoryId(objHistoryId)
                    .setTagId(tagId);
            objTagSimpleDTOS.add(objTagSimpleDTO);
        }
        objTagService.createBatch(objTagSimpleDTOS);
        // ??????CID??????
        objCidInfoService.update(appId, objId, param.getCidTagInfos());

        // 2. ????????????????????????
        Set<Long> trackerIdsBeforeEdit = new HashSet<>();
        if (param.getTrackers() != null) {
            param.getTrackers().forEach(t -> {
                if (t != null && t.getId() != null) {
                    trackerIdsBeforeEdit.add(t.getId());
                }
            });
        }
        List<Long> newTrackerIds = objectHelper.editObjectTrackerInfo(
                objId, objHistoryId, param.getReqPoolId(), param.getTrackers());

        //????????????????????????spm??????
        boolean update = !objectBasic.getName().equals(objectOriginBasic.getName()) || !objectBasic.getOid().equals(objectOriginBasic.getOid()) || !objectBasic.getType().equals(objectOriginBasic.getType());

        ObjDetailsVO objDetails = getObjectByHistory(objId, objHistoryId, reqPoolId);
        boolean change = checkEditChange(param, objDetails);

        // 3. ???????????? spm??????????????? ??????
        List<UpdateSpmPoolParam> updateSpmPoolParams = new ArrayList<>();
        newTrackerIds.forEach(newTrackerId -> {
            UpdateSpmPoolParam p = new UpdateSpmPoolParam();
            p.setTrackerId(newTrackerId);
            if (trackerIdsBeforeEdit.contains(newTrackerId)) {
                Integer type = objChangeHistory.getType();
                p.setOperationTypeEnum(OperationTypeEnum.CHANGE);
                p.setEdit(true);
            } else {
                // ??????????????????????????????????????????
                p.setOperationTypeEnum(OperationTypeEnum.CREATE);
                p.setEdit(false);
            }
            updateSpmPoolParams.add(p);
        });
        requirementPoolHelper.updateSpmPoolNew(reqPoolId, updateSpmPoolParams, update || change);
        // ???????????????????????????????????????
        if (isInMergeConflict) {
            objChangeHistoryService.updateConflictStatus(reqPoolId, Collections.singleton(objId), ConflictStatusEnum.RESOLVED.getStatus());
            return true;
        }
        return false;
    }

    /**
     * ?????????????????????????????????????????????????????????
     */
    private void fixParamBindsConsitency(ObjectEditParam param) {
        if (!Boolean.TRUE.equals(param.getConsistency())) {
            return;
        }
        List<ObjectTrackerEditParam> trackers = param.getTrackers();
        ObjectTrackerEditParam first = null;
        List<ObjectTrackerEditParam> toFix = new ArrayList<>();
        for (ObjectTrackerEditParam tracker : trackers) {
            TerminalSimpleDTO terminalSimpleDTO = terminalService.getById(tracker.getTerminalId());
            if (terminalSimpleDTO == null) {
                throw new CommonException("??????????????????");
            }
            boolean isAndroidOrIPhone = "android".equalsIgnoreCase(terminalSimpleDTO.getName()) ||
                    "ios".equalsIgnoreCase(terminalSimpleDTO.getName()) ||
                    "iphone".equalsIgnoreCase(terminalSimpleDTO.getName());
            if (isAndroidOrIPhone) {
                if (first == null) {
                    first = tracker;
                } else {
                    toFix.add(tracker);
                }
            }
        }

        if (first != null && CollectionUtils.isNotEmpty(toFix)) {
            for (ObjectTrackerEditParam fix : toFix) {
                fix.setParamBinds(first.getParamBinds());
            }
        }
    }

    /**
     * ?????????????????????????????????????????????
     *
     * @param objId ??????ID
     * @param objHistoryId ??????????????????ID
     */
    @MethodLog
    public ObjDetailsVO getObjectForChange(Long objId, Long objHistoryId){

        // 1. ???????????????????????????
        ObjectInfoDTO objectBasicInfo = objectHelper.getObjectBasicInfo(objId, objHistoryId);

        // 2. ?????????????????????????????????
        List<ObjectTrackerInfoDTO> objectTrackerInfoList = objectHelper
                .getObjTrackersInfo(objId, objHistoryId, true);

        // 3. ????????????
        ObjDetailsVO objDetailsVO = BeanConvertUtils.convert(objectBasicInfo, ObjDetailsVO.class);
        if(objDetailsVO == null){
            log.warn("??????????????????????????????????????????????????????");
            throw new ObjException("????????????????????????????????????????????????");
        }
        objDetailsVO.setTrackers(objectTrackerInfoList);
        objDetailsVO.setBasicTag(new ObjBasicTagDTO());
        objDetailsVO.getBasicTag().setObjSubType(objectBasicInfo.getObjSubType());
        objDetailsVO.getBasicTag().setBizGroup(objectBasicInfo.getBizGroup());
        composeCidInfo(objectBasicInfo.getAppId(), objDetailsVO);
        return objDetailsVO;
    }

    public ObjDetailsVO getObjectByHistory(Long objId, Long objHistoryId, Long reqPoolId) {
        return getObjectByHistory(objId, objHistoryId, reqPoolId, false);
    }

    @MethodLog
    public ObjDetailsVO getObjectByHistory(Long objId, Long objHistoryId, Long reqPoolId, boolean composeDiff){

        reviseOid(objId);

        // 1. ???????????????????????????
        ObjectInfoDTO objectBasicInfo = objectHelper.getObjectBasicInfo(objId, objHistoryId);

        // 2. ?????????????????????????????????
        List<ObjectTrackerInfoDTO> objectTrackerInfoList = objectHelper
                .getObjTrackersInfo(objId, objHistoryId, false);

        // 3. ????????????
        ObjDetailsVO objDetailsVO = BeanConvertUtils.convert(objectBasicInfo, ObjDetailsVO.class);
        if(objDetailsVO == null){
            log.warn("??????????????????????????????????????????????????????");
            throw new ObjException("????????????????????????????????????????????????");
        }
        objDetailsVO.setTrackers(objectTrackerInfoList);
        objDetailsVO.setBasicTag(new ObjBasicTagDTO());
        objDetailsVO.getBasicTag().setObjSubType(objectBasicInfo.getObjSubType());
        objDetailsVO.getBasicTag().setBizGroup(objectBasicInfo.getBizGroup());
        composeCidInfo(objectBasicInfo.getAppId(), objDetailsVO);

        // ??????????????????????????????
        if(reqPoolId != null) {
            EisTaskProcess taskProcessQuery = new EisTaskProcess();
            taskProcessQuery.setObjId(objId);
            taskProcessQuery.setReqPoolId(reqPoolId);
            List<EisTaskProcess> taskProcesses = taskProcessService.search(taskProcessQuery);
            for (EisTaskProcess taskProcess : taskProcesses) {
                if (ProcessStatusEnum.ONLINE.getState().equals(taskProcess.getStatus())) {
                    objDetailsVO.setOnlineRecord(true);
                }
            }
        }

        if (composeDiff) {
            composeObjDetailsDiff(reqPoolId, objId, objDetailsVO);
        }

        return objDetailsVO;
    }

    private void reviseOid(Long objId) {
        ObjectBasic objectBasic = objectBasicService.getById(objId);
        if (objectBasic == null) {
            throw new CommonException("objId???" + objId + "??????????????????");
        }
        String oid = objectBasic.getOid();
        String lowerCaseOid = StringUtils.lowerCase(oid);
        if (!StringUtils.equals(oid, lowerCaseOid)) {
            // ????????????
            objectBasic.setOid(lowerCaseOid);
            objectBasicService.update(objectBasic);
            log.info("oid????????? objId={} newOid={}", objId, lowerCaseOid);
        }
    }

    /**
     * ??????????????????????????????????????????????????????????????????
     *
     * ???????????????????????????????????????????????????/???????????????
     *
     * @param objId     ??????ID
     * @param reqPoolId ?????????ID
     * @return
     */
    @Deprecated
    public ObjDetailsVO getObjectByReqPoolId(Long objId, Long reqPoolId){
        // 1. ?????? ??????????????? ???????????? ????????????
        List<EisObjChangeHistory> objChangeHistoryList = objChangeHistoryService
                .getByObjAndReqPoolId(objId, reqPoolId);
        if(objChangeHistoryList.size() != 1){
            log.warn("objId???{}????????????reqId???{}?????????????????????????????????????????????????????????", objId, reqPoolId);
            throw new CommonException("?????????????????????????????????????????????????????????????????????");
        }

        EisObjChangeHistory objChangeHistory = objChangeHistoryList.get(0);
        Long objHistoryId = objChangeHistory.getId();
        // 2. ??????????????????
        return getObjectByHistory(objId, objHistoryId, reqPoolId);
    }

    /**
     * ???????????????????????????????????????????????????release?????????????????????
     */
    public ObjDetailsVO getBaseLineDiff(Long objId, Long reqPoolId) {
        // 1. ?????? ??????????????? ???????????? ????????????
        List<EisObjChangeHistory> objChangeHistoryList = objChangeHistoryService
                .getByObjAndReqPoolId(objId, reqPoolId);
        if(objChangeHistoryList.size() != 1){
            log.warn("objId???{}????????????reqId???{}?????????????????????????????????????????????????????????", objId, reqPoolId);
            throw new CommonException("?????????????????????????????????????????????????????????????????????");
        }

        EisObjChangeHistory objChangeHistory = objChangeHistoryList.get(0);
        Long objHistoryId = objChangeHistory.getId();

        // ???????????????????????????
        ObjDetailsVO current = getObjectByHistory(objId, objHistoryId, reqPoolId, false);
        return getBaseLineChange(current, objId, reqPoolId);
    }

    private ObjDetailsVO getBaseLineChange(ObjDetailsVO current, Long objId, Long reqPoolId) {
        ObjDetailsVO result = null;

        Long appId = EtContext.get(ContextConstant.APP_ID);
        Set<Long> allTerminalIds = terminalService.getByAppId(appId)
                .stream()
                .map(o -> o.getId()).collect(Collectors.toSet());

        long maxTerminalReleaseId = 0L; // ?????????????????????????????????objHistory
        List<ObjectTrackerInfoDTO> trackerDiff = new ArrayList<>();
        for (Long t : allTerminalIds) {
            EisReqPoolRelBaseRelease currentUse = reqPoolRelBaseService.getCurrentUse(reqPoolId, t);
            long currentBaseLineReleaseId = currentUse == null ? 0L : currentUse.getBaseReleaseId();
            EisTerminalReleaseHistory latestRelease = terminalReleaseService.getLatestRelease(t);
            long targetBaseLineReleaseId = latestRelease == null ? 0L : latestRelease.getId();
            maxTerminalReleaseId = Math.max(maxTerminalReleaseId, targetBaseLineReleaseId);
            // ??????????????????trackerDiff
            ObjectTrackerInfoDTO trackerDiffOfTerminal = trackerDiffHelper.getReleaseTrackerDiff(objId, targetBaseLineReleaseId, currentBaseLineReleaseId);
            if (trackerDiffOfTerminal != null) {
                trackerDiff.add(trackerDiffOfTerminal);
            }
        }

        // ?????????????????????????????????
        if (maxTerminalReleaseId > 0L) {
            ObjectInfoDTO objectInfoDTO = getObjInfoByReleaseIdAndObjId(maxTerminalReleaseId, objId);
            if (objectInfoDTO != null) {
                result = getObjectByHistory(objId, objectInfoDTO.getHistoryId(), reqPoolId, false);
            }
        }
        // ?????????????????????????????????????????????????????????
        if (result == null) {
            result = JsonUtils.parseObject(JsonUtils.toJson(current), ObjDetailsVO.class);
        }
        // ??????trckers?????????????????????diff
        if (result == null) {
            throw new CommonException("??????????????????????????????");
        }

        result.setTrackers(trackerDiff);
        return result;
    }

    public ObjDetailsVO getObjectByReqPoolIdWithDiff(Long objId, Long reqPoolId) {
        reviseOid(objId);


        // 1. ?????? ??????????????? ???????????? ????????????
        List<EisObjChangeHistory> objChangeHistoryList = objChangeHistoryService
                .getByObjAndReqPoolId(objId, reqPoolId);
        if (CollectionUtils.isEmpty(objChangeHistoryList)) {
            throw new CommonException("?????????????????????objId???" + objId + "?????????");
        }
        if (objChangeHistoryList.size() > 1) {
            throw new CommonException("??????????????????????????????????????????????????????");
        }

        EisObjChangeHistory objChangeHistory = objChangeHistoryList.get(0);
        Long objHistoryId = objChangeHistory.getId();
        // 2. ????????????????????????
        ObjDetailsVO objDetailsVO = getObjectByHistory(objId, objHistoryId, reqPoolId);
        composeObjDetailsDiff(reqPoolId, objId, objDetailsVO);
        return objDetailsVO;
    }

    /**
     * ??????????????????
     *
     * @param objId     ??????ID
     * @param trackerId ??????ID
     * @return
     */
    @Deprecated
    public ObjDetailsVO getObjectByTrackerId(Long objId, Long trackerId){
        // 1. ??????trackerId ?????? historyId
        EisObjTerminalTracker objTerminalTracker = objTerminalTrackerService.getById(trackerId);
        if(null == objTerminalTracker){
            log.warn("objId={}????????????????????????????????????trackerId={}?????????", objId, trackerId);
            throw new CommonException("???????????????????????????");
        }
        // 2. ??????????????????
        Long objHistoryId = objTerminalTracker.getObjHistoryId();
        return getObjectByHistory(objId, objHistoryId, objTerminalTracker.getReqPoolId());
    }

    public List<ObjectBasic> getCandidateParentObjects(List<Long> terminalIds, Long reqPoolId) {
        Long appId = EtContext.get(ContextConstant.APP_ID);
        return objectHelper.getCandidateParentObjects(appId, terminalIds, reqPoolId);
    }

    /**
     * ??????????????????
     *
     * @param objId             ??????ID
     * @param terminalId        ??????ID
     * @param terminalReleaseId ????????????ID
     * @return
     */
    public List<Map<String, Object>> getObjExampleData(Long objId, Long terminalId, Long terminalReleaseId) {
        // ????????????
        Preconditions.checkArgument(null != objId, "??????ID????????????");
        Preconditions.checkArgument(null != terminalId, "??????ID????????????");
        Preconditions.checkArgument(null != terminalReleaseId, "??????????????????ID????????????");

        // ??????????????????
        List<Map<String, Object>> result = Lists.newArrayList();
        EisObjTerminalTracker query = new EisObjTerminalTracker();
        query.setObjId(objId);
        query.setTerminalId(terminalId);
        query.setTerminalReleaseId(terminalReleaseId);
        List<EisObjTerminalTracker> objTrackerList = objTerminalTrackerService.search(query);
        if(CollectionUtils.isEmpty(objTrackerList)){
            return Lists.newArrayList();
        }
        EisObjTerminalTracker objTracker = objTrackerList.get(0);

        Long trackerId = objTracker.getId();
        // 1. ???????????????????????????????????????
        List<CheckHistorySimpleDTO> checkHistoryList = spmCheckHistoryService
                .getByTrackerId(trackerId);
        Map<TwoTuple<String, String>, Object> eventInfoToLogMap = new HashMap<>();
        if (checkHistoryList.size() > 0) {
            for (CheckHistorySimpleDTO elem : checkHistoryList) {
                TwoTuple<String, String> eventInfo = new TwoTuple<>(elem.getEventCode(), elem.getEventName());
                eventInfoToLogMap.putIfAbsent(eventInfo, elem.getLog());
            }
        }
        // 2. ??????????????????????????????
        List<ObjTrackerEventSimpleDTO> objTrackerEventSimpleDTOList = objTrackerEventService
                .getByTrackerId(Collections.singletonList(trackerId));
        List<Long> eventIdList = objTrackerEventSimpleDTOList.stream()
                .map(ObjTrackerEventSimpleDTO::getEventId)
                .collect(Collectors.toList());
        List<EventSimpleDTO> eventSimpleDTOList = eventService.getEventByIds(eventIdList);
        List<TwoTuple<String, String>> eventInfoList = eventSimpleDTOList.stream()
                .map(event -> new TwoTuple<>(event.getCode(), event.getName()))
                .collect(Collectors.toList());
        // 3. ?????????????????????????????????????????????????????????
        for (TwoTuple<String, String> eventInfo : eventInfoList) {
            Object currLog = eventInfoToLogMap.getOrDefault(eventInfo, "");
            Map<String, Object> item = new HashMap<>();
            item.put("event_code", eventInfo.getFirst());
            item.put("event_name", eventInfo.getSecond());
            item.put("data", currLog);
            result.add(item);
        }
        return result;
    }

    /**
     * ?????????????????????????????????  // todo ????????????
     *
     * @param releasedId ????????????ID
     * @param type       ????????????
     * @param tagIds     ??????ID??????
     * @param search     ????????????
     * @return
     */
    public ObjTreeVO getReleasedObjTree(
            Long releasedId, Integer type, List<Long> tagIds, String search, String orderBy, String orderRule){
        Preconditions.checkArgument(null != releasedId, "????????????ID???????????????");

        ObjTreeVO result = new ObjTreeVO();
        // 1. ?????????????????????
        LinageGraph graph = lineageHelper.genReleasedLinageGraph(releasedId);

        Map<Long, Set<Long>> parentsMap = graph.getParentsMap();
        Set<Long> objIds = Sets.newHashSet();
        for (Long objId : parentsMap.keySet()) {
            objIds.add(objId);
            objIds.addAll(parentsMap.getOrDefault(objId, Sets.newHashSet()));
        }

        // 2. ????????????????????????????????????????????????
        List<ObjectBasic> allList = objectBasicService.getByIds(objIds);
        List<ObjectBasic> objectBasicList = new ArrayList<>(allList);

        // ????????????????????????
        if(null != type){
            objectBasicList = objectBasicList.stream()
                    .filter(k -> type.equals(k.getType()))
                    .collect(Collectors.toList());
        }
        // ??????????????????
        if(CollectionUtils.isNotEmpty(tagIds)){
            List<ObjTagSimpleDTO> objTagSimpleDTOS = objTagService.selectObjTags(tagIds, null, null);
            List<Long> objIdFilteredByTags = objTagSimpleDTOS.stream()
                    .map(ObjTagSimpleDTO::getObjId)
                    .collect(Collectors.toList());
            objectBasicList = objectBasicList.stream()
                    .filter(k -> objIdFilteredByTags.contains(k.getId()))
                    .collect(Collectors.toList());
        }
        // ???????????????????????????
        if (CollectionUtils.isEmpty(objectBasicList)) {
            return result;
        }

        // 4. ????????????????????????????????????????????????????????????
        Set<Long> selectedObjIds = objectBasicList.stream()
                .map(ObjectBasic::getId)
                .collect(Collectors.toSet());

        List<Node> rootNodes = lineageHelper.getObjTree(graph, selectedObjIds);

        // 3. ????????????????????????
        // ????????????????????????Oid??????

        // ?????????????????????????????????????????????SPM??????SPM?????????????????????oid??????
        String spmToExpand = null;
        if (StringUtils.isNotBlank(search)) {
            Map<String, Long> oidToObjIdMap = objectBasicList.stream().collect(Collectors.toMap(ObjectBasic::getOid, ObjectBasic::getId, (oldV, newV) -> oldV));
            Map<Long, ObjectBasic> allObjBasicMap = objectBasicList.stream().collect(Collectors.toMap(ObjectBasic::getId, o -> o, (oldV, newV) -> oldV));
            Pair<String, List<Node>> p = filterRootNodes(rootNodes, search, objectBasicList, oidToObjIdMap, allObjBasicMap);
            rootNodes = p.getValue();
            spmToExpand = p.getKey();
        }

        Set<Long> objIdSet = lineageHelper.getObjIdsFromTree(rootNodes);

        // 5. ??????????????????
        List<ObjectInfoDTO> objectBasicInfoDTOList = this.getObjInfoByReleaseId(releasedId, allList);
        Map<Long, ObjectInfoDTO> objInfoMap = objectBasicInfoDTOList.stream()
                .filter(k -> objIdSet.contains(k.getId()))
                .collect(Collectors.toMap(ObjectInfoDTO::getId, Function.identity()));

        // 6. ??????
        rootNodes.sort(new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                ObjectInfoDTO obj1 = objInfoMap.get(o1.getObjId());
                ObjectInfoDTO obj2 = objInfoMap.get(o2.getObjId());
                Date time1 = new Date(0L);
                Date time2 = new Date(0L);
                if (orderBy.equals("createTime")) {
                    if (obj1 != null && obj1.getCreateTime() != null) {
                        time1 = obj1.getCreateTime();
                    }
                    if (obj2 != null && obj2.getCreateTime() != null) {
                        time2 = obj2.getCreateTime();
                    }
                } else {
                    if (obj1 != null && obj1.getUpdateTime() != null) {
                        time1 = obj1.getUpdateTime();
                    }
                    if (obj2 != null && obj2.getUpdateTime() != null) {
                        time2 = obj2.getUpdateTime();
                    }
                }
                return time1.compareTo(time2);
            }
        });
        if(orderRule.equals("ascend")){
            rootNodes = Lists.reverse(rootNodes);
        }

        // ?????????????????????
        Long appId = EtContext.get(ContextConstant.APP_ID);
        composeBizGroup(appId, rootNodes, objIdSet);

        // ??????????????????
        result.setTree(rootNodes);
        result.setObjInfoMap(objInfoMap);
        result.setSpmsToExpand(StringUtils.isEmpty(spmToExpand) ? new ArrayList<>(0) : Arrays.asList(spmToExpand));
        return result;
    }

    private void composeBizGroup(Long appId, List<Node> rootNodes, Set<Long> objIdSet) {
        List<ObjectBasic> all = objectBasicService.getByIds(objIdSet);
        Map<Long, String> bizGroupMap = new HashMap<>();
        all.forEach(obj -> {
            ObjectExtDTO objectExtDTO = parseExt(obj.getExt());
            String bizGroup = objectExtDTO.getBasicTag() == null ? null : objectExtDTO.getBasicTag().getBizGroup();
            if (StringUtils.isNotBlank(bizGroup)) {
                bizGroupMap.put(obj.getId(), bizGroup);
            }
        });
        if (MapUtils.isEmpty(bizGroupMap)) {
            return;
        }
        // ??????
        rootNodes.forEach(rootNode -> doComposeBizGroup(rootNode, bizGroupMap, null));
    }

    /**
     * ????????????????????????????????????
     * @param current
     * @param dataMap
     */
    private void doComposeBizGroup(Node current, Map<Long, String> dataMap, String parentBizGroup) {
        current.setBizGroup(parentBizGroup);    // ??????????????????
        String bizGroupOfCurrentObjId = dataMap.get(current.getObjId());
        if (bizGroupOfCurrentObjId != null) {
            current.setBizGroup(bizGroupOfCurrentObjId);    // ????????????objId??????????????????
        }
        if (CollectionUtils.isNotEmpty(current.getChildren())) {
            current.getChildren().forEach(child -> {
                doComposeBizGroup(child, dataMap, current.getBizGroup());
            });
        }
    }

    /**
     * ????????????????????????????????????????????????????????????????????????
     *
     * @param terminalId ???ID
     * @param reqPoolId  ?????????ID
     * @param search     ??????Oid
     * @return
     */
    public ObjTreeVO getBaseTree(Long terminalId, Long reqPoolId, String search, String tagSearch){
        Preconditions.checkArgument(null != terminalId, "???ID???????????????");

        ObjTreeVO result = new ObjTreeVO();
        // 1. ?????????????????????????????????????????????????????????
        TerminalSimpleDTO terminalSimpleDTO = terminalService.getById(terminalId);
        if (terminalSimpleDTO == null) {
            throw new CommonException("terminalId??????");
        }
        EisReqPoolRelBaseRelease reqPoolRelBaseRelease = reqPoolRelBaseService
                .getCurrentUse(reqPoolId, terminalId);
        if(null == reqPoolRelBaseRelease){
            String errMessage = String.format(
                    "?????????reqPoolId={%d}?????????terminalId={%d}????????????????????????????????????", reqPoolId, terminalId);
            log.warn(errMessage);
            throw new CommonException(errMessage);
        }
        Long baseReleasedId = reqPoolRelBaseRelease.getBaseReleaseId();

        // 2. ???????????????????????????????????????????????? // ??????????????????????????????????????????
        LinageGraph graph = lineageHelper.genReqLinageGraph(baseReleasedId, terminalId, reqPoolId);
        Map<Long, Set<Long>> parentsMap = graph.getParentsMap();
        Set<Long> objIds = Sets.newHashSet();
        for (Long objId : parentsMap.keySet()) {
            objIds.add(objId);
            Set<Long> parentIds = parentsMap.getOrDefault(objId, Sets.newHashSet());
            parentIds = parentIds.stream().filter(Objects::nonNull).collect(Collectors.toSet());
            objIds.addAll(parentIds);
        }
        List<ObjectBasic> objectBasicList = objectBasicService.getByIds(objIds);


        //
        Long appId = EtContext.get(ContextConstant.APP_ID);
        ObjectBasic objQuery = new ObjectBasic();
//        objQuery.setAppId(appId);
        List<ObjectBasic> objs = objectBasicService.search(objQuery);
        Map<Long,String> objIdToOidMap = new HashMap<>();
        Map<String,Long> oidToObjIdMap = new HashMap<>();
        for (ObjectBasic obj : objs) {
            objIdToOidMap.put(obj.getId(),obj.getOid());
            oidToObjIdMap.put(obj.getOid(),obj.getId());
        }
        Map<String, String> oidSpmMap = new HashMap<>();
        Set<Long> objIdsOfGraph = graph.getAllObjIds();
        List<List<String>> allSpmsAsOidList = new ArrayList<>();
        for (Long objIdOfGraph : objIdsOfGraph) {
            List<List<Long>> spmsByObjIdAsList = lineageHelper.getObjIdSpms(graph,objIdOfGraph);
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
            String spm = String.join("|",spmAsOidList);
            oidSpmMap.put(targetOid,spm);
        }

        //??????spm tag ??????
        SpmInfo spmInfoQuery = new SpmInfo();
        spmInfoQuery.setAppId(appId);
        spmInfoQuery.setTerminalId(terminalId);
        List<SpmInfoDTO> spmInfoList = spmInfoService.search(appId, spmInfoQuery);
        Map<String , List<TagSimpleDTO>> spmTagInfoMap = new HashMap<>();

        // ????????????DB

        // ??????spmId -> SpmTagSimpleDTO??????
        Set<Long> spmIds = new HashSet<>();
        for (SpmInfoDTO spmInfo : spmInfoList) {
            spmIds.add(spmInfo.getId());
        }
        List<SpmTagSimpleDTO> spmTags = spmTagService.getBySpmIds(spmIds);
        if (spmTags == null) {
            spmTags = new ArrayList<>(1);
        }
        Map<Long, List<SpmTagSimpleDTO>> spmTagSimpleDTOMap = spmTags.stream().collect(Collectors.groupingBy(SpmTagSimpleDTO::getSpmId));

        // ??????tagId -> TagSimpleDTO??????
        List<Long> tagIds = spmTags.stream().map(SpmTagSimpleDTO::getTagId).collect(Collectors.toList());
        List<TagSimpleDTO> tagSimpleDTOList = tagService.getByIds(tagIds);
        Map<Long, TagSimpleDTO> tagSimpleDTOMap = new HashMap<>();
        tagSimpleDTOList.forEach(tagSimpleDTO -> tagSimpleDTOMap.put(tagSimpleDTO.getId(), tagSimpleDTO));


        for (SpmInfoDTO spmInfo : spmInfoList) {
            List<SpmTagSimpleDTO> spmTagSimpleDTOS = spmTagSimpleDTOMap.get(spmInfo.getId());
            if (CollectionUtils.isEmpty(spmTagSimpleDTOS)) {
                spmTagInfoMap.put(spmInfo.getSpm(), new ArrayList<>(0));
                continue;
            }
            Set<Long> tagIdSet = spmTagSimpleDTOS.stream().map(SpmTagSimpleDTO::getTagId).collect(Collectors.toSet());
            List<TagSimpleDTO> tags = new ArrayList<>(0);
            if (CollectionUtils.isNotEmpty(tagIdSet)) {
                tagIdSet.forEach(tagId -> {
                    TagSimpleDTO tagSimpleDTO = tagSimpleDTOMap.get(tagId);
                    if (tagSimpleDTO != null) {
                        tags.add(tagSimpleDTO);
                    }
                });
            }
            spmTagInfoMap.put(spmInfo.getSpm(), tags);
        }

        // 3. ????????????????????????
        if (StringUtils.isNotBlank(tagSearch)) {
            objectBasicList = objectBasicList.stream()
                    .filter(k -> {
                        // ??????????????????
                        String spm = oidSpmMap.get(k.getOid());
                        List<TagSimpleDTO> tagSimpleDTOS = spmTagInfoMap.get(spm);
                        return CollectionUtils.isNotEmpty(tagSimpleDTOS) && CollectionUtils.isNotEmpty(tagSimpleDTOS.stream().filter(tagSimpleDTO -> tagSimpleDTO.getName().contains(tagSearch)).collect(Collectors.toList()));
                    }).collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(objectBasicList)) {
            return result;
        }

        // 4.????????????????????????????????????????????????????????????
        Set<Long> selectedObjIds = objectBasicList.stream()
                .map(ObjectBasic::getId)
                .collect(Collectors.toSet());
        List<Node> rootNodes = lineageHelper.getObjTree(graph, selectedObjIds);

        String spmToExpand = null;
        if (StringUtils.isNotBlank(search)) {
            Map<Long, ObjectBasic> allObjBasicMap = objectBasicList.stream().collect(Collectors.toMap(ObjectBasic::getId, o -> o, (oldV, newV) -> oldV));
            Pair<String, List<Node>> p = filterRootNodes(rootNodes, search, objectBasicList, oidToObjIdMap, allObjBasicMap);
            rootNodes = p.getValue();
            spmToExpand = p.getKey();
        }

        Set<Long> objIdSet = lineageHelper.getObjIdsFromTree(rootNodes);

        // 5. ??????????????????
        List<ObjectInfoDTO> objectBasicInfoDTOList = this.getObjInfoByReqPoolId(baseReleasedId, reqPoolId, graph.getAllObjIds());
        Set<Long> allBridges = objectBasicInfoDTOList.stream().filter(o -> ObjSpecialTypeEnum.BRIDGE.getName().equals(o.getSpecialType())).map(ObjectInfoDTO::getId).collect(Collectors.toSet());
        Set<Long> outerSpaceObjIds = new HashSet<>();
        allBridges.forEach(b -> {
            // ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            if (CollectionUtils.isNotEmpty(graph.getChildrenMap().get(b))) {
                Set<Long> parents = getAllParents(graph.getParentsMap(), b);
                outerSpaceObjIds.addAll(parents);
            }
        });
        objectBasicInfoDTOList.forEach(o -> {
            if (outerSpaceObjIds.contains(o.getId())) {
                o.setOtherAppId(o.getAppId());
            }
        });
        for(ObjectInfoDTO objectInfoDTO : objectBasicInfoDTOList){
            // ??????????????????
            String spm = oidSpmMap.get(objectInfoDTO.getOid());
            List<TagSimpleDTO> tagSimpleDTOS = spmTagInfoMap.get(spm);
            objectInfoDTO.setTags(tagSimpleDTOS);
        }
        Map<Long, ObjectInfoDTO> objInfoMap = objectBasicInfoDTOList.stream()
                .filter(k -> objIdSet.contains(k.getId()))
                .collect(Collectors.toMap(ObjectInfoDTO::getId, Function.identity()));

        // ??????????????????
        result.setTree(rootNodes);
        result.setObjInfoMap(objInfoMap);
        result.setSpmsToExpand(StringUtils.isEmpty(spmToExpand) ? new ArrayList<>(0) : Arrays.asList(spmToExpand));
        return result;
    }

    private Pair<String, List<Node>> filterRootNodes(List<Node> rootNodes, String search,
                                                     List<ObjectBasic> objectBasicList,
                                                     Map<String, Long> oidToObjIdMap,
                                                     Map<Long, ObjectBasic> allObjBasicMap) {
        String spmToExpand = null;
        boolean isMatchObjectOnly = false;
        if (StringUtils.isNotBlank(search) && search.startsWith("*")) {
            isMatchObjectOnly = true;
            search = search.substring(1);
        }
        Set<String> allOids = objectBasicList.stream().map(ObjectBasic::getOid).collect(Collectors.toSet());
        boolean isOid = allOids.contains(search);
        boolean isSpm = search.contains("|");
        if (isSpm) {
            String spmByObjId = CommonUtil.transSpmByOidToSpmByObjId(oidToObjIdMap, search);
            rootNodes = lineageHelper.filterObjTreeBySpm(rootNodes, spmByObjId);
            spmToExpand = search;
        } else if (isOid) {
            if (isMatchObjectOnly) {
                rootNodes = lineageHelper.filterObjTreeByObject(rootNodes, search, allObjBasicMap);
            } else {
                Long objIdByOid = oidToObjIdMap.get(search);
                Set<Long> rootObjIds = rootNodes.stream().map(Node::getObjId).collect(Collectors.toSet());
                boolean isRootOid = objIdByOid != null && rootObjIds.contains(objIdByOid);
                if (isRootOid) {
                    // ???????????????????????????SPM??????
                    String spmByObjId = CommonUtil.transSpmByOidToSpmByObjId(oidToObjIdMap, search);
                    rootNodes = lineageHelper.filterObjTreeBySpm(rootNodes, spmByObjId);
                    spmToExpand = search;
                } else {
                    // ????????????????????????????????????
                    rootNodes = lineageHelper.filterObjTreeByObject(rootNodes, search, allObjBasicMap);
                }
            }
        } else {
            rootNodes = lineageHelper.filterObjTreeByObject(rootNodes, search, allObjBasicMap);
        }
        return new Pair<>(spmToExpand, rootNodes);
    }

    /**
     * ?????????????????? ???????????????????????? ?????????/????????????????????????
     *
     * @param terminalId ???ID
     * @param reqPoolId ?????????ID????????????
     * @param objId ??????ID????????????
     * @return
     */
    public ObjLineageGraphVO getReqPoolObjLineageGraph(Long terminalId, Long reqPoolId, final Long objId){
        Preconditions.checkArgument(null != terminalId, "???ID????????????");
        Preconditions.checkArgument(null != reqPoolId, "?????????ID????????????");
        Preconditions.checkArgument(null != objId, "??????ID????????????");

        // 1. ??????????????????
        EisReqPoolRelBaseRelease reqPoolRelBaseRelease = reqPoolRelBaseService
                .getCurrentUse(reqPoolId, terminalId);
        if(null == reqPoolRelBaseRelease){
            log.warn("??????terminalId={}??????????????????????????????????????????", terminalId);
            throw new CommonException("???????????????????????????????????????????????????");
        }
        Long basedReleasedId = reqPoolRelBaseRelease.getBaseReleaseId();

        // 2. ????????????????????????????????????????????????????????????????????????
        TotalLineageGraph graph = lineageHelper.getTotalLineageGraph(
                basedReleasedId, terminalId, reqPoolId);

        // 3. ?????????????????????
        List<Node> rootNodes = lineageHelper.getObjLineageTree(graph, objId);
        Set<Long> objIdSet = lineageHelper.getObjIdsFromTree(rootNodes);

        // 4. ?????????????????????????????????
        Map<Long, Set<Long>> addedRelationMap = graph.getAddedRelationMap();
        Map<Long, Set<Long>> deletedRelationMap = graph.getDeletedRelationMap();
        // ????????????
        Queue<TwoTuple<Long, Node>> queue = Lists.newLinkedList();
        rootNodes.forEach(node -> queue.offer(new TwoTuple<>(0L, node)));
        while(!queue.isEmpty()){
            // ??????????????????
            TwoTuple<Long, Node> currTupleNode = queue.poll();
            Long currParentId = currTupleNode.getFirst();
            Node currNode = currTupleNode.getSecond();
            Long currObjId = currNode.getObjId();
            // ????????????/????????????
            if(addedRelationMap.containsKey(currParentId)
                    && addedRelationMap.get(currParentId).contains(currObjId)){
                currNode.setType(LineageTypeEnum.ADDED.getType());
            }else if(deletedRelationMap.containsKey(currParentId)
                    && deletedRelationMap.get(currParentId).contains(currObjId)){
                currNode.setType(LineageTypeEnum.DELETED.getType());
            }
            // ???????????????
            List<Node> childrenNodes = currNode.getChildren();
            if(CollectionUtils.isNotEmpty(childrenNodes)) {
                childrenNodes.forEach(node -> queue.offer(new TwoTuple<>(currObjId, node)));
            }
        }

        // 5. ??????????????????
        List<ObjectInfoDTO> objectInfoDTOList = this.getObjInfoByReqPoolId(basedReleasedId, reqPoolId, graph.getAllObjIds());
        Set<Long> allBridges = objectInfoDTOList.stream().filter(o -> ObjSpecialTypeEnum.BRIDGE.getName().equals(o.getSpecialType())).map(ObjectInfoDTO::getId).collect(Collectors.toSet());
        Set<Long> outerSpaceObjIds = new HashSet<>();
        allBridges.forEach(b -> {
            // ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            if (CollectionUtils.isNotEmpty(graph.getChildrenMap().get(b))) {
                Set<Long> parents = getAllParents(graph.getParentsMap(), b);
                outerSpaceObjIds.addAll(parents);
            }
        });
        objectInfoDTOList.forEach(o -> {
            if (outerSpaceObjIds.contains(o.getId())) {
                o.setOtherAppId(o.getAppId());
            }
        });

        Map<Long, ObjectInfoDTO> objInfoMap = objectInfoDTOList.stream()
                .filter(k -> objIdSet.contains(k.getId()))
                .collect(Collectors.toMap(ObjectInfoDTO::getId, Function.identity()));

        // ????????????
        ObjLineageGraphVO objLineageGraphVO = new ObjLineageGraphVO();
        objLineageGraphVO.setTree(rootNodes);
        objLineageGraphVO.setObjInfoMap(objInfoMap);

        return objLineageGraphVO;
    }

    private Set<Long> getAllParents(Map<Long, Set<Long>> parentsMap, Long objId) {
        Set<Long> result = new HashSet<>();
        addToSet(result, parentsMap, Collections.singleton(objId));
        return result;
    }

    private void addToSet(Set<Long> result, Map<Long, Set<Long>> parentsMap, Set<Long> currentLevel) {
        result.addAll(currentLevel);
        currentLevel.forEach(c -> {
            Set<Long> parentLevel = parentsMap.get(c);
            if (CollectionUtils.isNotEmpty(parentLevel)) {
                addToSet(result, parentsMap, parentLevel);
            }
        });
    }

    /**
     * ??????????????????????????? ??????????????????????????????
     *
     * @param releasedId ????????????
     * @param objId ??????ID
     * @return
     */
    public ObjLineageGraphVO getReleasedObjLineageGraph(Long releasedId, Long objId){
        Preconditions.checkArgument(null != releasedId, "????????????ID????????????");
        Preconditions.checkArgument(null != objId, "??????ID????????????");

        // 1. ?????????????????????
        LinageGraph graph = lineageHelper.genReleasedLinageGraph(releasedId);

        Map<Long, Set<Long>> parentsMap = graph.getParentsMap();
        Set<Long> objIds = Sets.newHashSet();
        for (Long o : parentsMap.keySet()) {
            objIds.add(o);
            objIds.addAll(parentsMap.getOrDefault(o, Sets.newHashSet()));
        }
        // 2. ????????????????????????????????????????????????
        List<ObjectBasic> allList = objectBasicService.getByIds(objIds);

        // 2. ????????????????????????
        List<Node> rootNodes = lineageHelper.getObjLineageTree(graph, objId);
        Set<Long> objIdSet = lineageHelper.getObjIdsFromTree(rootNodes);

        // 3. ???????????????????????????
        List<ObjectInfoDTO> objectBasicInfoDTOList = this.getObjInfoByReleaseId(releasedId, allList);
        Map<Long, ObjectInfoDTO> objInfoMap = objectBasicInfoDTOList.stream()
                .filter(k -> objIdSet.contains(k.getId()))
                .collect(Collectors.toMap(ObjectInfoDTO::getId, Function.identity()));
        // ????????????
        ObjLineageGraphVO objLineageGraphVO = new ObjLineageGraphVO();
        objLineageGraphVO.setTree(rootNodes);
        objLineageGraphVO.setObjInfoMap(objInfoMap);

        return objLineageGraphVO;
    }

    /**
     * ??????????????????????????? ??????????????????
     *
     * @return
     */
    public ObjAggregateVO getObjAggregateInfo(){
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "?????????????????????");

        ObjAggregateVO objAggregateVO = new ObjAggregateVO();
        // 1. ??????????????????
        List<TerminalSimpleDTO> terminalSimpleDTOS = terminalService.getByAppId(appId);
        List<CommonAggregateDTO> terminals = Lists.newArrayList();
        for (TerminalSimpleDTO terminalSimpleDTO : terminalSimpleDTOS) {
            CommonAggregateDTO terminal = new CommonAggregateDTO();
            terminal.setKey(terminalSimpleDTO.getId().toString())
                    .setValue(terminalSimpleDTO.getName());
            terminals.add(terminal);
        }
        // 2. ?????????????????????????????????
        EisTerminalReleaseHistory query = new EisTerminalReleaseHistory();
        query.setAppId(appId);
        List<EisTerminalReleaseHistory> releaseHistoryList = terminalReleaseService.search(query);
        Map<Long, EisTerminalReleaseHistory> releaseHistoryMap = releaseHistoryList.stream()
                .collect(Collectors.toMap(EisTerminalReleaseHistory::getId, Function.identity()));
        Set<Long> latestReleaseIds = releaseHistoryList.stream()
                .filter(EisTerminalReleaseHistory::getLatest)
                .map(EisTerminalReleaseHistory::getId)
                .collect(Collectors.toSet());
        List<EisTerminalReleaseHistory> sortedReleaseHistoryList = Lists.newArrayList();
        for (Long latestReleaseId : latestReleaseIds) {
            Long releaseId = latestReleaseId;
            while(0L != releaseId){
                EisTerminalReleaseHistory currReleaseHistory = releaseHistoryMap.get(releaseId);
                if(null == currReleaseHistory) break;
                sortedReleaseHistoryList.add(currReleaseHistory);
                releaseId = currReleaseHistory.getPreReleaseId();
            }
        }

        Set<Long> terminalVersionIds = releaseHistoryList.stream()
                .map(EisTerminalReleaseHistory::getTerminalVersionId)
                .collect(Collectors.toSet());
        List<EisTerminalVersionInfo> terminalVersionInfoList = terminalVersionInfoService
                .getByIds(terminalVersionIds);
        Map<Long, EisTerminalVersionInfo> terminalVersionInfoMap = terminalVersionInfoList.stream()
                .collect(Collectors.toMap(EisTerminalVersionInfo::getId, Function.identity()));

        List<CommonRelationAggregateDTO> releases = Lists.newArrayList();
        for (EisTerminalReleaseHistory terminalReleaseHistory : sortedReleaseHistoryList) {
            Long terminalVersionId = terminalReleaseHistory.getTerminalVersionId();
            EisTerminalVersionInfo terminalVersionInfo = terminalVersionInfoMap.get(terminalVersionId);
            if(null == terminalVersionInfo){
                log.warn("????????????releaseId={}??????????????????terminalVersionId={}",
                        terminalReleaseHistory.getId(), terminalVersionId);
            }
            // ????????????
            String terminalVersionName = terminalVersionInfo == null ? "None" : terminalVersionInfo.getName();
            CommonRelationAggregateDTO release = new CommonRelationAggregateDTO();
            release.setAssociatedKey(terminalReleaseHistory.getTerminalId().toString())
                    .setValue(String.format("%s-%d", terminalVersionName,
                            terminalReleaseHistory.getId()))
                    .setKey(terminalReleaseHistory.getId().toString());

            releases.add(release);
        }
        // 3. ?????????????????????????????????
        List<CommonAggregateDTO> types = Lists.newArrayList();
        for (ObjTypeEnum typeEnum : ObjTypeEnum.values()) {
            CommonAggregateDTO type = new CommonAggregateDTO();
            type.setKey(typeEnum.getType().toString())
                    .setValue(typeEnum.getName());
            types.add(type);
        }
        // 4. ???????????????????????????
        TagSimpleDTO tagQuery = new TagSimpleDTO();
        tagQuery.setAppId(appId)
                .setType(TagTypeEnum.OBJ_TAG.getType());
        List<TagSimpleDTO> tagList = tagService.search(tagQuery);
        List<CommonAggregateDTO> tags = Lists.newArrayList();
        for (TagSimpleDTO tagSimpleDTO : tagList) {
            CommonAggregateDTO tag = new CommonAggregateDTO();
            tag.setKey(tagSimpleDTO.getId().toString())
                    .setValue(tagSimpleDTO.getName());
            tags.add(tag);
        }
        // ??????????????????
        objAggregateVO.setTerminals(terminals)
                .setReleases(releases)
                .setTags(tags)
                .setTypes(types);
        return objAggregateVO;
    }

    /**
     * ???????????????????????? ???????????????
     *
     * @param objId ??????ID
     * @param reqPoolId ?????????ID
     * @return
     */
    public ObjCascadeAggregateVO getObjCascadeAggregateInfo(Long objId, Long reqPoolId){
        // ????????????
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "????????????????????????");

        ObjCascadeAggregateVO objCascadeAggregateVO = new ObjCascadeAggregateVO();

        // ????????????
        EisObjTerminalTracker trackerQuery = new EisObjTerminalTracker();
        trackerQuery.setAppId(appId);
        trackerQuery.setObjId(objId);
        trackerQuery.setReqPoolId(reqPoolId);
        List<EisObjTerminalTracker> objTerminalTrackerList = objTerminalTrackerService.search(trackerQuery);
        Set<Long> terminalIds = objTerminalTrackerList.stream()
                .map(EisObjTerminalTracker::getTerminalId)
                .collect(Collectors.toSet());
        List<TerminalSimpleDTO> terminalSimpleDTOList = terminalService.getByIds(terminalIds);

        List<CommonAggregateDTO> terminals = Lists.newArrayList();
        for (TerminalSimpleDTO terminalSimpleDTO : terminalSimpleDTOList) {
            CommonAggregateDTO commonAggregateDTO = new CommonAggregateDTO();
            commonAggregateDTO.setKey(terminalSimpleDTO.getId().toString());
            commonAggregateDTO.setValue(terminalSimpleDTO.getName());

            terminals.add(commonAggregateDTO);
        }

        // ????????????
        objCascadeAggregateVO.setTerminals(terminals);
        return objCascadeAggregateVO;
    }


    /**
     * ????????????????????????????????????
     *
     * @param terminalId ??????ID
     * @param objId      ??????ID
     * @return
     */
    public List<ObjReleaseVO> getObjReleasedHistory(final Long terminalId, final Long objId){
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "?????????????????????");

        // 1. ?????? eis_all_tracker_release ???
        EisAllTrackerRelease query = new EisAllTrackerRelease();
        query.setObjId(objId);
        List<EisAllTrackerRelease> trackerReleaseList = trackerReleaseService.search(query);

        // 2. ??????????????????
        Set<Long> trackerIds = trackerReleaseList.stream()
                .map(EisAllTrackerRelease::getTrackerId)
                .collect(Collectors.toSet());
        List<EisObjTerminalTracker> trackerList = objTerminalTrackerService.getByIds(trackerIds);

        Set<Long> terminalIds = trackerList.stream()
                .map(EisObjTerminalTracker::getTerminalId)
                .collect(Collectors.toSet());
        List<TerminalSimpleDTO> terminalSimpleDTOList = terminalService.getByIds(terminalIds);
        Map<Long, String> terminalNameMap = terminalSimpleDTOList.stream()
                .collect(Collectors.toMap(TerminalSimpleDTO::getId, TerminalSimpleDTO::getName));

        Set<Long> terminalReleaseIds = trackerReleaseList.stream()
                .map(EisAllTrackerRelease::getTerminalReleaseId)
                .collect(Collectors.toSet());
        List<EisTerminalReleaseHistory> releaseHistoryList = terminalReleaseService.getByIds(terminalReleaseIds);
        Map<Long, EisTerminalReleaseHistory> releaseHistoryMap = releaseHistoryList.stream()
                .collect(Collectors.toMap(EisTerminalReleaseHistory::getId, Function.identity()));

        Set<Long> terminalVersionIds = releaseHistoryList.stream()
                .map(EisTerminalReleaseHistory::getTerminalVersionId)
                .collect(Collectors.toSet());
        List<EisTerminalVersionInfo> terminalVersionInfoList = terminalVersionInfoService
                .getByIds(terminalVersionIds);
        Map<Long, EisTerminalVersionInfo> terminalVersionInfoMap = terminalVersionInfoList.stream()
                .collect(Collectors.toMap(EisTerminalVersionInfo::getId, Function.identity()));

        // 3. ???????????????
        List<ObjReleaseVO> objReleaseVOList = Lists.newArrayList();
        for (EisObjTerminalTracker objTerminalTracker : trackerList) {
            Long currObjId = objTerminalTracker.getObjId();
            Long currObjHistoryId = objTerminalTracker.getObjHistoryId();
            Long currTerminalId = objTerminalTracker.getTerminalId();
            Long currReleaseId = objTerminalTracker.getTerminalReleaseId();
            Long terminalVersionId = 0L ;
            // ??????????????????
            if(!Objects.equals(currTerminalId, terminalId)) continue;

            ObjReleaseVO objReleaseVO = new ObjReleaseVO();
            objReleaseVO.setId(currObjId);
            objReleaseVO.setHistoryId(currObjHistoryId);
            objReleaseVO.setReleaseId(currReleaseId);
            objReleaseVO.setTerminalName(terminalNameMap.get(currTerminalId));
            EisTerminalReleaseHistory currReleaseHistory = releaseHistoryMap.get(currReleaseId);
            if(null != currReleaseHistory){
                objReleaseVO.setReleaseTime(currReleaseHistory.getCreateTime());
                objReleaseVO.setReleaser(currReleaseHistory.getCreateName());
                terminalVersionId = currReleaseHistory.getTerminalVersionId();
            }
            EisTerminalVersionInfo terminalVersionInfo = terminalVersionInfoMap.get(terminalVersionId);
            if(null != terminalVersionInfo){
                objReleaseVO.setTerminalVersionName(terminalVersionInfo.getName());
            }
            objReleaseVOList.add(objReleaseVO);
        }

        return objReleaseVOList;
    }

    // todo ????????????
    public List<BaseReleaseVO> getBaseReleaseVO(Long reqPoolId){
        // ????????????
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "?????????????????????");
        Preconditions.checkArgument(null != reqPoolId, "?????????ID????????????");

        List<TerminalSimpleDTO> terminalSimpleDTOList = terminalService.getByAppId(appId);

        List<BaseReleaseVO> result = Lists.newArrayList();
        for (TerminalSimpleDTO terminalSimpleDTO : terminalSimpleDTOList) {
            Long terminalId = terminalSimpleDTO.getId();
            String terminalName = terminalSimpleDTO.getName();
            BaseReleaseVO baseReleaseVO = new BaseReleaseVO();

            // 1. ?????????????????????????????????????????????????????????
            EisReqPoolRelBaseRelease reqPoolRelBaseRelease = reqPoolRelBaseService
                    .getCurrentUse(reqPoolId, terminalId);
            if(null == reqPoolRelBaseRelease){
                String errMessage = String.format(
                        "?????????reqPoolId={%d}?????????terminalId={%d}????????????????????????????????????", reqPoolId, terminalId);
                log.warn(errMessage);
                continue;
            }
            Long releasedId = reqPoolRelBaseRelease.getBaseReleaseId();
            if(releasedId == 0L) continue;
            // 2. ??????????????????
            EisTerminalReleaseHistory terminalReleaseHistory = terminalReleaseService.getById(releasedId);
            if(null == terminalReleaseHistory){
                String errMessage = String.format(
                        "????????????baseReleaseId={%d}????????????????????????", releasedId);
                log.warn(errMessage);
                continue;
            }
            // 3. ?????????????????????
            Long terminalVersionId = terminalReleaseHistory.getTerminalVersionId();
            EisTerminalVersionInfo terminalVersionInfo = terminalVersionInfoService.getById(terminalVersionId);
            if(null == terminalVersionInfo){
                String errMessage = String.format(
                        "????????????terminalVersionId={%d}?????????????????????", terminalVersionId);
                log.warn(errMessage);
                continue;
            }
            // 4. ????????????
            baseReleaseVO.setTerminalId(terminalId);
            baseReleaseVO.setTerminalName(terminalName);
            baseReleaseVO.setBaseReleaseId(releasedId);
            baseReleaseVO.setTerminalVersionId(terminalVersionId);
            baseReleaseVO.setTerminalVersionName(terminalVersionInfo.getName());

            result.add(baseReleaseVO);
        }

        return result;
    }

    private ObjectInfoDTO getObjInfoByReleaseIdAndObjId(Long releaseId, Long objId) {
        if (releaseId == null || objId == null) {
            return null;
        }
        // ????????????
        EisAllTrackerRelease trackerRelease = trackerReleaseService.getByReleaseIdAndObjId(releaseId, objId);
        if (trackerRelease == null) {
            return null;
        }
        ObjectBasic objectBasic = objectBasicService.getById(objId);
        if (objectBasic == null) {
            return null;
        }
        Long trackerId = trackerRelease.getTrackerId();
        if (trackerId == null) {
            return null;
        }
        EisObjTerminalTracker objTracker = objTerminalTrackerService.getById(trackerId);
        if (objTracker == null) {
            return null;
        }
        // ????????????
        ObjectInfoDTO objectBasicInfoDTO = ObjectHelper.convertToDTO(objectBasic);
        objectBasicInfoDTO.setTrackerId(objTracker.getId());
        objectBasicInfoDTO.setReqPoolId(objTracker.getReqPoolId());
        objectBasicInfoDTO.setHistoryId(objTracker.getObjHistoryId());
        return objectBasicInfoDTO;
    }

    // todo ????????????
    private List<ObjectInfoDTO> getObjInfoByReleaseId(Long releasedId, List<ObjectBasic> allObjectBasicList) {
        Preconditions.checkArgument(null != releasedId, "????????????ID???????????????");
        // ????????????
        List<EisAllTrackerRelease> trackerReleaseList = trackerReleaseService.getByReleaseId(releasedId);
        Set<Long> objIds = trackerReleaseList.stream()
                .map(EisAllTrackerRelease::getObjId)
                .collect(Collectors.toSet());
        List<ObjectBasic> objectBasicList = objectBasicService.getByIds(objIds);
        Set<Long> tackerIds = trackerReleaseList.stream()
                .map(EisAllTrackerRelease::getTrackerId)
                .collect(Collectors.toSet());
        List<EisObjTerminalTracker> objTrackerList = objTerminalTrackerService.getByIds(tackerIds);
        Map<Long, EisObjTerminalTracker> objTrackerMap = objTrackerList.stream()
                .collect(Collectors.toMap(EisObjTerminalTracker::getObjId, Function.identity()));
        // ????????????
        List<ObjectInfoDTO> objectBasicInfoDTOList = Lists.newArrayList();
        for (ObjectBasic objectBasic : objectBasicList) {
            Long objId = objectBasic.getId();
            ObjectInfoDTO objectBasicInfoDTO = ObjectHelper.convertToDTO(objectBasic);
            EisObjTerminalTracker objTerminalTracker = objTrackerMap.get(objId);
            if(null != objectBasicInfoDTO && null != objTerminalTracker){
                objectBasicInfoDTO.setTrackerId(objTerminalTracker.getId());
                objectBasicInfoDTO.setReqPoolId(objTerminalTracker.getReqPoolId());
                objectBasicInfoDTO.setHistoryId(objTerminalTracker.getObjHistoryId());
                objectBasicInfoDTOList.add(objectBasicInfoDTO);
            }
        }

        Set<Long> currentObjOids = objectBasicInfoDTOList.stream().map(ObjectInfoDTO::getId).collect(Collectors.toSet());

        // ?????????????????????
        allObjectBasicList.forEach(objectBasic -> {
            if (!currentObjOids.contains(objectBasic.getId())) {
                // ???????????????????????????????????????????????????
                ObjectInfoDTO objectBasicInfoDTO = ObjectHelper.convertToDTO(objectBasic);
                objectBasicInfoDTO.setOtherAppId(objectBasic.getAppId());
                objectBasicInfoDTOList.add(objectBasicInfoDTO);
            }
        });

        return objectBasicInfoDTOList;
    }

    private List<ObjectInfoDTO> getObjInfoByReqPoolId(Long baseReleaseId, Long reqPoolId, Set<Long> allObjIds){
        // 1. ????????????
        List<EisAllTrackerRelease> trackerReleaseList = trackerReleaseService.getByReleaseId(baseReleaseId);
        Set<Long> objIds = trackerReleaseList.stream()
                .map(EisAllTrackerRelease::getObjId)
                .collect(Collectors.toSet());
        List<EisObjChangeHistory> objChangeHistoryList = objChangeHistoryService.getByReqPoolId(reqPoolId);
        Set<Long> diffObjIds = objChangeHistoryList.stream()
                .map(EisObjChangeHistory::getObjId)
                .collect(Collectors.toSet());
        // ?????? ?????????????????????/????????????
        objIds.addAll(diffObjIds);

        Set<Long> query = new HashSet<>(objIds);
        query.addAll(allObjIds);
        List<ObjectBasic> objectBasicList = objectBasicService.getByIds(query);
        Set<Long> tackerIds = trackerReleaseList.stream()
                .map(EisAllTrackerRelease::getTrackerId)
                .collect(Collectors.toSet());
        List<EisObjTerminalTracker> objTrackerList = objTerminalTrackerService.getByIds(tackerIds);
        Map<Long, Long> objIdToHistoryIdMap = objTrackerList.stream()
                .collect(Collectors.toMap(EisObjTerminalTracker::getObjId, EisObjTerminalTracker::getObjHistoryId));
        objChangeHistoryList.forEach( objChangeHistory -> {
            Long objId = objChangeHistory.getObjId();
            Long historyId = objChangeHistory.getId();
            objIdToHistoryIdMap.put(objId, historyId);
        });
        // 2. ????????????
        List<ObjectInfoDTO> objectBasicInfoDTOList = Lists.newArrayList();
        Long appId = EtContext.get(ContextConstant.APP_ID);
        for (ObjectBasic objectBasic : objectBasicList) {
            Long objId = objectBasic.getId();
            ObjectInfoDTO objectBasicInfoDTO = ObjectHelper.convertToDTO(objectBasic);
            if(null != objectBasicInfoDTO){
                // ???????????? ?????????????????? ????????????
                objectBasicInfoDTO.setHistoryId(objIdToHistoryIdMap.get(objId));
                objectBasicInfoDTO.setCanChange(!diffObjIds.contains(objId));
                objectBasicInfoDTOList.add(objectBasicInfoDTO);
                if (!appId.equals(objectBasicInfoDTO.getAppId())) {
                    objectBasicInfoDTO.setOtherAppId(objectBasicInfoDTO.getAppId());
                    objectBasicInfoDTO.setCanChange(false);
                }
            }
        }

        return  objectBasicInfoDTOList;
    }

    /**
     * ?????????spmList????????????objSubTypeEnums?????????
     */
    public List<String> filterSpmByObjSubType(long appId, List<String> spmList, Set<ObjSubTypeEnum> objSubTypeEnums) {
        if (CollectionUtils.isEmpty(spmList) || CollectionUtils.isEmpty(objSubTypeEnums)) {
            return new ArrayList<>(0);
        }
        Map<String, Set<String>> oidsGroupingByObjSubType = getOidsGroupingByObjSubType(appId).getMap();
        Set<String> whiteListOids = new HashSet<>();
        objSubTypeEnums.forEach(objSubTypeEnum -> {
            Set<String> oidsOfSubType = oidsGroupingByObjSubType.get(objSubTypeEnum.getOidPrefix());
            if (CollectionUtils.isNotEmpty(oidsOfSubType)) {
                whiteListOids.addAll(oidsOfSubType);
            }
        });
        return spmList.stream().filter(o -> {
            if(StringUtils.isBlank(o)) {
                return false;
            }
            List<String> oids = CommonUtil.transSpmToOidList(o);
            if (CollectionUtils.isEmpty(oids)) {
                return false;
            }
            return whiteListOids.contains(oids.get(0));
        }).collect(Collectors.toList());
    }

    public OidsGroupingByObjSubTypeVO getOidsGroupingByObjSubType(long appId) {
        return CacheUtils.getAndSetIfAbsent(() -> "getOidsGroupingByObjSubType_" + appId,
                () -> {
                    List<ObjectBasic> allObjs = objectBasicService.search(new ObjectBasic().setAppId(appId));
                    OidsGroupingByObjSubTypeVO result = new OidsGroupingByObjSubTypeVO().setMap(new HashMap<>());
                    if (CollectionUtils.isEmpty(allObjs)) {
                        return result;
                    }
                    allObjs.forEach(o -> {
                        ObjectExtDTO objectExtDTO = parseExt(o.getExt());
                        String objSubType = ObjSubTypeEnum.UNKNOWN.getOidPrefix();
                        if (objectExtDTO.getBasicTag() != null && StringUtils.isNotBlank(objectExtDTO.getBasicTag().getObjSubType())) {
                            objSubType = objectExtDTO.getBasicTag().getObjSubType();
                        }
                        if (ObjSubTypeEnum.UNKNOWN.getOidPrefix().equals(objSubType)) {
                            return;
                        }
                        Set<String> set = result.getMap().computeIfAbsent(objSubType, k -> new HashSet<>());
                        set.add(o.getOid());
                    });
                    return result;
                }, (key) -> cacheAdapter.get(key),
                (key, value) -> cacheAdapter.setWithExpireTime(key, value, 3600),
                OidsGroupingByObjSubTypeVO.class);
    }

    public Map<Integer, List<String>> objSubTypes() {
        Map<Integer, List<String>> result = new HashMap<>();
        Arrays.stream(ObjSubTypeEnum.values()).filter(o -> o != ObjSubTypeEnum.UNKNOWN).forEach(objSubTypeEnum -> {
            List<String> list = result.computeIfAbsent(objSubTypeEnum.getDefaultParentObjType().getType(), k -> new ArrayList<>());
            list.add(objSubTypeEnum.getOidPrefix());
        });
        return result;
    }



    /**
     * ???????????????????????????????????? ????????????????????????
     * @param reqPoolId
     * @param terminalId
     * @return
     */
    public boolean checkLoop(Long reqPoolId, Long terminalId){
        Preconditions.checkArgument(null != reqPoolId, "?????????ID????????????");
        Preconditions.checkArgument(null != terminalId, "??????ID????????????");
        EisReqPoolRelBaseRelease baseRelease = reqPoolRelBaseService
                .getCurrentUse(reqPoolId, terminalId);
        LinageGraph graph = lineageHelper
                .genReqLinageGraph(baseRelease.getBaseReleaseId(), terminalId, reqPoolId);
        return lineageHelper.loopCheck(graph.getParentsMap())
                || lineageHelper.loopCheck(graph.getChildrenMap());
    }

    public boolean checkReuse(ObjectChangeParam param, ObjDetailsVO objDetails){
        //
        if(!param.getId().equals(objDetails.getId())) return false;
        if(!param.getConsistency().equals(objDetails.getConsistency())) return false;
        if(param.getDescription() != null && !param.getDescription().equals(objDetails.getDescription())) return false;
        if(!param.getImgUrls().equals(objDetails.getImgUrls())) return false;
        if(!param.getOid().equals(objDetails.getOid())) return false;
        if(!param.getPriority().equals(objDetails.getPriority())) return false;
        if(!param.getTagIds().equals(objDetails.getTags().stream().map(TagSimpleDTO::getId).collect(Collectors.toList()))) return false;

        List<ObjectTrackerChangeParam> paramTrackers= param.getTrackers();
        List<ObjectTrackerInfoDTO> objectTrackers = objDetails.getTrackers();
        if(paramTrackers.size() != objectTrackers.size()) return false;

        for(int i=0; i<paramTrackers.size(); i++){
            ObjectTrackerChangeParam trackerParam = paramTrackers.get(i);
            ObjectTrackerInfoDTO trackerInfoDTO = objectTrackers.get(i);
            if(!trackerParam.getEventIds().equals(trackerInfoDTO.getEvents().stream().map(EventSimpleDTO::getId).collect(Collectors.toList()))) return false;
            if(trackerParam.getEventParamVersionIdMap() != null && !trackerParam.getEventParamVersionIdMap().equals(trackerInfoDTO.getEventParamVersionIdMap())) return false;
            if(trackerParam.getPubParamPackageId() != null && !trackerParam.getPubParamPackageId().equals(trackerInfoDTO.getPubParamPackageId())) return false;
            if(!trackerParam.getParentObjs().equals(trackerInfoDTO.getParentObjects().stream().map(ObjectBasicDTO::getId).collect(Collectors.toList()))) return false;
            if(!trackerParam.getTerminalId().equals(trackerInfoDTO.getTerminal().getId())) return false;

            List<ParamBindItermParam> paramBindItermParams = trackerParam.getParamBinds();
            List<ParamBindItemDTO> paramBindItemDTOS = trackerInfoDTO.getPrivateParam();
            if(paramBindItermParams.size() != paramBindItemDTOS.size()) return false;
            for(int j=0; j<paramBindItermParams.size(); j++){
                ParamBindItermParam paramBindItermParam = paramBindItermParams.get(j);
                ParamBindItemDTO paramBindItemDTO = paramBindItemDTOS.get(j);
                if(!paramBindItermParam.getParamId().equals(paramBindItemDTO.getId())) return false;
            }
        }

        return true;
    }

    public boolean checkEditChange(ObjectEditParam param, ObjDetailsVO objDetails){
        //
        List<ObjectTrackerEditParam> paramTrackers= param.getTrackers();
        List<ObjectTrackerInfoDTO> objectTrackers = objDetails.getTrackers();
        if(paramTrackers.size() != objectTrackers.size()) {
            return true;
        }

        for(int i=0; i<paramTrackers.size(); i++){
            ObjectTrackerEditParam trackerParam = paramTrackers.get(i);
            ObjectTrackerInfoDTO trackerInfoDTO = objectTrackers.get(i);
            boolean isEventIdSame = CollectionUtils.isEqualCollection(trackerParam.getEventIds(), trackerInfoDTO.getEvents().stream().map(EventSimpleDTO::getId).collect(Collectors.toList()));
            if(!isEventIdSame) {
                log.info("eventId?????????");
                return true;
            }

            if (MapUtils.isNotEmpty(trackerInfoDTO.getEventParamVersionIdMap())) {
                Set<Long> toRemove = new HashSet<>();
                trackerInfoDTO.getEventParamVersionIdMap().forEach((k, v) -> {
                    if (v == null || v.equals(0L)) {
                        toRemove.add(k);
                    }
                });
                toRemove.forEach(k -> {
                    trackerInfoDTO.getEventParamVersionIdMap().remove(k);
                });
            }
            boolean isSameEventParamVersionIdMap = isSameMap(trackerParam.getEventParamVersionIdMap(), trackerInfoDTO.getEventParamVersionIdMap());
            if(!isSameEventParamVersionIdMap) {
                log.info("eventParamVersionIdMap?????????");
                return true;
            }
            if(!Objects.equals(trackerParam.getPubParamPackageId(), trackerInfoDTO.getPubParamPackageId())) {
                log.info("PubParamPackageId?????????");
                return true;
            }
            if(!CollectionUtils.isEqualCollection(trackerParam.getParentObjs(), trackerInfoDTO.getParentObjects().stream().map(ObjectBasicDTO::getId).collect(Collectors.toList()))) {
                log.info("parentObjs ?????????");
                return true;
            }
            if(!Objects.equals(trackerParam.getTerminalId(), trackerInfoDTO.getTerminal().getId())) {
                log.info("TerminalId?????????");
                return true;
            }

            List<ParamBindItermParam> paramBindItermParams = trackerParam.getParamBinds();
            List<ParamBindItemDTO> paramBindItemDTOS = trackerInfoDTO.getPrivateParam();
            if(paramBindItermParams.size() != paramBindItemDTOS.size()) {
                log.info("paramBindItermParams?????????");
                return true;
            }
            for(int j=0; j<paramBindItermParams.size(); j++){
                ParamBindItermParam paramBindItermParam = paramBindItermParams.get(j);
                ParamBindItemDTO paramBindItemDTO = paramBindItemDTOS.get(j);
                if(!Objects.equals(paramBindItermParam.getParamId(), paramBindItemDTO.getId())) {
                    log.info("paramId ?????????");
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * ???????????????????????????????????????????????????
     * @param objId
     * @param objDetailsVO
     */
    private void composeObjDetailsDiff(Long reqPoolId, Long objId, ObjDetailsVO objDetailsVO) {
        if (objDetailsVO == null) {
            return;
        }
        if (CollectionUtils.isEmpty(objDetailsVO.getTrackers())) {
            return;
        }

        // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        List<EisObjChangeHistory> changeHistories = objChangeHistoryService.getByReqPoolId(reqPoolId);
        if (CollectionUtils.isEmpty(changeHistories)) {
            return;
        }
        Set<Long> changedObjs = changeHistories.stream().map(o -> o.getObjId()).collect(Collectors.toSet());
        if (!changedObjs.contains(objId)) {
            return;
        }

        objDetailsVO.getTrackers().forEach(trackerInfoDTO -> {
            trackerDiffHelper.composeTrackerDiff(objId, trackerInfoDTO);
        });
    }

    public Set<ReqPoolObjDTO> scanInFactConsistency() {
        List<EisObjChangeHistory> allNotConsistency = objChangeHistoryService.getAllNotConsistency();
        Set<ReqPoolObjDTO> allReqPoolObjs = allNotConsistency.stream().map(o -> new ReqPoolObjDTO().setObjId(o.getObjId()).setReqPoolId(o.getReqPoolId())).collect(Collectors.toSet());
        // ????????????????????????????????????
        Set<ReqPoolObjDTO> result = new HashSet<>();
        allReqPoolObjs.forEach(o -> {
            Long objId = o.getObjId();
            Long reqPoolId = o.getReqPoolId();
            List<EisObjChangeHistory> objChangeHistoryList = objChangeHistoryService
                    .getByObjAndReqPoolId(objId, reqPoolId);
            if(objChangeHistoryList.size() != 1){
                return;
            }
            EisObjChangeHistory eisObjChangeHistory = objChangeHistoryList.get(0);
            Long objHistoryId = eisObjChangeHistory.getId();
            if (eisObjChangeHistory.getConsistency()) {
                return;
            }
            EtContext.put(ContextConstant.APP_ID, objectBasicService.getById(objId).getAppId());
            List<ObjectTrackerInfoDTO> objectTrackerInfoList = objectHelper
                    .getObjTrackersInfo(objId, objHistoryId, false);
            objectTrackerInfoList = objectTrackerInfoList.stream().filter(t -> TerminalTypeEnum.APP.getType().equals(t.getTerminal().getTerminalType())).collect(Collectors.toList());
            if (objectTrackerInfoList.size() != 2) {
                return;
            }
            ObjectTrackerInfoDTO trackerA = objectTrackerInfoList.get(0);
            ObjectTrackerInfoDTO trackerB = objectTrackerInfoList.get(1);
            boolean same = isSame(trackerA, trackerB);
            if (same) {
                log.info("reqPoolId={} objId={} ????????????", reqPoolId, objId);
                result.add(o);
            }
        });
        return result;
    }

    private boolean isSame(ObjectTrackerInfoDTO trackerA, ObjectTrackerInfoDTO trackerB) {
        // 1. ????????????
        if (trackerA.getPrivateParam().size() != trackerB.getPrivateParam().size()) {
            return false;
        }
        Map<Long, ParamBindItemDTO> paramMapA = trackerA.getPrivateParam().stream().
                collect(Collectors.toMap(ParamBindItemDTO::getId, o -> o, (oldV, newV) -> oldV));
        Map<Long, ParamBindItemDTO> paramMapB = trackerB.getPrivateParam().stream().
                collect(Collectors.toMap(ParamBindItemDTO::getId, o -> o, (oldV, newV) -> oldV));
        if (!isSameSet(paramMapA.keySet(), paramMapB.keySet())) {
            return false;
        }
        for (Map.Entry<Long, ParamBindItemDTO> entry : paramMapA.entrySet()) {
            Long paramId = entry.getKey();
            ParamBindItemDTO pA = entry.getValue();
            ParamBindItemDTO pB = paramMapB.get(paramId);
            if (!pA.getNotEmpty().equals(pB.getNotEmpty())) {
                return false;
            }
            if (!pA.getMust().equals(pB.getMust())) {
                return false;
            }
            if (!pA.getIsEncode().equals(pB.getIsEncode())) {
                return false;
            }
            if (!isSameSet(new HashSet<>(pA.getSelectedValues()), new HashSet<>(pB.getSelectedValues()))) {
                return false;
            }
        }

        // 2. ????????????
        List<EventSimpleDTO> eventsA = trackerA.getEvents();
        List<EventSimpleDTO> eventsB = trackerB.getEvents();

        if (eventsA.size() != eventsB.size()) {
            return false;
        }

        Map<Long, EventSimpleDTO> eventMapA = trackerA.getEvents().stream().
                collect(Collectors.toMap(EventSimpleDTO::getId, o -> o, (oldV, newV) -> oldV));
        Map<Long, EventSimpleDTO> eventMapB = trackerB.getEvents().stream().
                collect(Collectors.toMap(EventSimpleDTO::getId, o -> o, (oldV, newV) -> oldV));
        if (!isSameSet(eventMapA.keySet(), eventMapA.keySet())) {
            return false;
        }

        for (Map.Entry<Long, EventSimpleDTO> entry : eventMapA.entrySet()) {
            Long eventId = entry.getKey();
            Long versionIDA = trackerA.getEventParamVersionIdMap().get(eventId);
            Long versionIDB = trackerB.getEventParamVersionIdMap().get(eventId);
            if (!Objects.equals(versionIDA, versionIDB)) {
                return false;
            }
        }

        // 3. ???????????????
        if (!isSameSet(trackerA.getParentObjects().stream().map(ObjectBasicDTO::getId).collect(Collectors.toSet()),
                trackerB.getParentObjects().stream().map(ObjectBasicDTO::getId).collect(Collectors.toSet()))) {
            return false;
        }
        return true;
    }

    private boolean isSameSet(Set<Long> setA, Set<Long> setB) {
        if (CollectionUtils.isNotEmpty(Sets.difference(setA, setB))) {
            return false;
        }
        if (CollectionUtils.isNotEmpty(Sets.difference(setB, setA))) {
            return false;
        }
        return true;
    }

    private boolean isSameMap(Map<Long, Long> m1, Map<Long, Long> m2) {
        if (m1 == null && m2 == null) {
            return true;
        }
        // ????????????????????????????????????????????????????????????????????????????????????
        if (m1 == null || m2 == null) {
            return false;
        }
        // ?????????????????????????????????
        if (!CollectionUtils.isEqualCollection(m1.keySet(), m2.keySet())) {
            return false;
        }
        for (Long key : m1.keySet()) {
            Long v1 = m1.get(key);
            Long v2 = m2.get(key);
            if (!Objects.equals(v1, v2)) {
                return false;
            }
        }
        return true;
    }

    private static ObjBasicTagDTO convertObjBasicTag(String oid, Integer objType, ObjBasicTagDTO paramObjBasicTagDTO) {
        if (paramObjBasicTagDTO == null) {
            paramObjBasicTagDTO = new ObjBasicTagDTO();
        }
        if (StringUtils.isEmpty(paramObjBasicTagDTO.getObjSubType())) {
            // ?????????????????????oid????????????
            paramObjBasicTagDTO.setObjSubType(calcDefaultObjSubType(oid, objType).getOidPrefix());
        } else {
            // ???????????????
            ObjSubTypeEnum objSubTypeEnum = ObjSubTypeEnum.fromPrefix(paramObjBasicTagDTO.getObjSubType());
            paramObjBasicTagDTO.setObjSubType(objSubTypeEnum.getOidPrefix());
        }
        return paramObjBasicTagDTO;
    }

    private static ObjSubTypeEnum calcDefaultObjSubType(String oid, Integer objType) {
        ObjTypeEnum objTypeEnum = ObjTypeEnum.fromType(objType);
        if (objTypeEnum == ObjTypeEnum.POPOVER) {
            ObjSubTypeEnum subTypeMatchByOid = ObjSubTypeEnum.matchOid(oid);
            if (subTypeMatchByOid != ObjSubTypeEnum.UNKNOWN && subTypeMatchByOid.getDefaultParentObjType() == ObjTypeEnum.ELEMENT) {
                return subTypeMatchByOid;
            }
            return ObjSubTypeEnum.PANEL;
        }
        if (objTypeEnum == ObjTypeEnum.PAGE) {
            return ObjSubTypeEnum.PAGE;
        }
        // ???oid???subTypeEnum???????????????????????????subTypeEnum
        if (objTypeEnum == ObjTypeEnum.ELEMENT) {
            ObjSubTypeEnum subTypeMatchByOid = ObjSubTypeEnum.matchOid(oid);
            if (subTypeMatchByOid != ObjSubTypeEnum.UNKNOWN && subTypeMatchByOid.getDefaultParentObjType() == ObjTypeEnum.ELEMENT) {
                return subTypeMatchByOid;
            }
        }
        return ObjSubTypeEnum.UNKNOWN;
    }

    private void composeCidInfo(Long appId, ObjDetailsVO objDetailsVO) {
        objDetailsVO.setCidTagInfos(objCidInfoService.getCidTagInfos(appId, objDetailsVO.getId()));
    }

    /**
     * ???????????????ObjectExtDTO
     */
    public ObjectExtDTO getExt(long objId) {
        ObjectBasic objectBasic = objectBasicService.getById(objId);
        if (objectBasic == null) {
            return null;
        }
        String originExt = objectBasic.getExt();
        return parseExt(originExt);
    }

    private static ObjectExtDTO parseExt(String ext) {
        if (StringUtils.isBlank(ext)) {
            return new ObjectExtDTO();
        }
        return JsonUtils.parseObject(ext, ObjectExtDTO.class);
    }

    private void checkImageUrlsNotEmpty(Long appId, List<String> imgUrls) {
        if (CollectionUtils.isNotEmpty(imgUrls)) {
            return;
        }
        boolean isMustCheckImageUrls = mustCheckImageUrls(appId);
        if (isMustCheckImageUrls) {
            throw new CommonException("???????????????");
        }
    }

    private boolean mustCheckImageUrls(Long appId) {
        if (checkObjImageNotEmptyConfig == null || appId == null) {
            return false;
        }
        if (checkObjImageNotEmptyConfig.getAppValues() == null) {
            return checkObjImageNotEmptyConfig.isDefaultValue();
        }
        Boolean byAppId = checkObjImageNotEmptyConfig.getAppValues().get(appId);
        if (byAppId == null) {
            return checkObjImageNotEmptyConfig.isDefaultValue();
        }
        return byAppId;
    }
}
