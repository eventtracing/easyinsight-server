package com.netease.hz.bdms.easyinsight.service.facade;

import com.fasterxml.jackson.core.type.TypeReference;
import com.netease.eis.adapters.CacheAdapter;
import com.netease.eis.adapters.RealtimeConfigAdapter;
import com.netease.hz.bdms.easyinsight.common.dto.param.parambind.ParamEmptyRateDTO;
import com.netease.hz.bdms.easyinsight.common.vo.requirement.UnDevelopedEventVO;
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
import com.netease.hz.bdms.easyinsight.service.service.obj.UserBuryPointService;
import com.netease.hz.bdms.easyinsight.service.service.requirement.ReqEventObjRelationService;
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

    @Autowired
    UserBuryPointService userBuryPointService;

    @Resource
    private TrackerDiffHelper trackerDiffHelper;

    @Resource
    private ObjCidInfoService objCidInfoService;

    @Resource
    private CacheAdapter cacheAdapter;

    @Autowired
    ReqEventObjRelationService reqEventObjRelationService;

    @Autowired
    EventPoolFacade eventPoolFacade;

    /**
     * 是否一定要传图片 key: appId
     */
    private static Map<String, CheckConfig> checkConfigs = new HashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        realtimeConfigAdapter.listenJSON("checkConfigs", (s) ->  checkConfigs = JsonUtils.parseObject(s, new TypeReference<Map<String, CheckConfig>>() {}));
    }

    @Data
    public static class CheckConfig {
        private boolean defaultValue = false;
        private Map<Long, Boolean> appValues = new HashMap<>();
    }

    /**
     * 新建对象
     *
     * @param param 对象信息，包含基本信息、埋点信息
     * @return oid -> objId
     */
    @MethodLog
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Map<String, Long> createObject(ObjectCreateParam param) {
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "未指定产品信息！");
        Preconditions.checkArgument(null != param, "新建对象信息不能为空！");
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(param.getBasics()), "对象基本信息不能为空！");
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(param.getTrackers()), "对象埋点信息不能为空！");
        if (param.getTerminalBigType() == null) {
            param.setTerminalBigType(TerminalBigTypeEnum.CLIENT.getType());
        }
        for (ObjectBasicCreateParam basic : param.getBasics()) {
            ParamCheckUtil.checkOid(basic.getOid());
        }

        // 0. 检查
        checkTerminalId(param.getTrackers().stream().map(o -> o.getTerminalId()).collect(Collectors.toSet()), param.getTerminalBigType());

        // 1. 检查对象是否已经存在 检查oid命名
        List<ObjectBasicCreateParam> objBasicInfoList = param.getBasics();
        List<String> objectOidList = objBasicInfoList.stream()
                .map(ObjectBasicCreateParam::getOid).distinct()
                .collect(Collectors.toList());
        List<String> objectNameList = objBasicInfoList.stream()
                .map(ObjectBasicCreateParam::getName).distinct()
                .collect(Collectors.toList());
        objBasicInfoList.forEach(o -> objectHelper.checkOidByType(o.getOid(), o.getType()));
        objBasicInfoList.forEach(o -> objectHelper.checkBridge(o.getBridgeSubAppId(), o.getBridgeSubTerminalId(), o.getSpecialType()));
        if (mustCheckOidUnique(appId)) {
            objectHelper.checkObjExists(objectOidList, objectNameList, objBasicInfoList.get(0).getType());
        }

        // 检查父对象是否存在
        param.getTrackers().forEach(trackerCreateParam -> objectHelper.checkParentExist(appId, trackerCreateParam.getParentObjs(), trackerCreateParam.getTerminalId(), param.getReqPoolId()));

        // 2. 依次插入对象信息
        Map<String, Long> result = new HashMap<>();
        for(ObjectBasicCreateParam objectBasicInfo : param.getBasics()){

            // 2.1 处理对象基本信息 (对象元信息、变更信息、关联图片信息、关联标签信息)
            // 插入对象元信息
            ObjectBasic objectBasic = BeanConvertUtils.convert(objectBasicInfo, ObjectBasic.class);
            if(null == objectBasic) {
                log.error("convert to object basic failure!");
                throw new ObjException("转化对象基本信息失败，无法完成创建!");
            }
            ObjectExtDTO objectExtDTO = new ObjectExtDTO();
            objectExtDTO.setSubAppId(objectBasicInfo.getBridgeSubAppId());
            objectExtDTO.setSubTerminalId(objectBasicInfo.getBridgeSubTerminalId() == null ? 0L : objectBasicInfo.getBridgeSubTerminalId());
            objectExtDTO.setBasicTag(convertObjBasicTag(objectBasicInfo.getOid(), objectBasicInfo.getType(), objectBasicInfo.getBasicTag()));
            objectExtDTO.setAnalyseCid(objectExtDTO.isAnalyseCid());
            objectBasic.setExt(JsonUtils.toJson(objectExtDTO));
            final Long objId = objectBasicService.insert(objectBasic);
            //写入用户埋点表
            if(param.getUserPointId() != null && param.getUserPointId() > 0) {
                EisUserPointInfo userPointInfo = userBuryPointService.getById(param.getUserPointId());
                if (userPointInfo != null && userPointInfo.getId() > 0) {
                    Map<String, String> extMap = JsonUtils.parseObject(userPointInfo.getExtInfo(), new TypeReference<Map<String, String>>() {
                    });
                    extMap.put("objName", objectBasic.getName());
                    extMap.put("oid", objectBasic.getOid());
                    userBuryPointService.updateExtById(userPointInfo.getId(), JsonUtils.toJson(extMap));
                }
            }
            // 处理对象新建/变更标识信息，即向表`eis_obj_change_history`中插入记录
            EisObjChangeHistory objChangeHistory = new EisObjChangeHistory();
            objChangeHistory.setObjId(objId);
            objChangeHistory.setReqPoolId(param.getReqPoolId());
            objChangeHistory.setType(OperationTypeEnum.CREATE.getOperationType());
            objChangeHistory.setConsistency(param.getConsistency());
            final Long objChangeHistoryId = objChangeHistoryService.insert(objChangeHistory);
            List<EisObjChangeHistory> objChangeHistoryList = objChangeHistoryService.getByObjAndReqPoolId(objId, param.getReqPoolId());
            if(objChangeHistoryList.size() > 1) {
                log.info("重复操做插入objChangeHistory {} {}", objId, param.getReqPoolId());
                throw new CommonException("请勿重复操作！");
            }
            // 插入对象关联图片信息
            List<String> imgUrls = objectBasicInfo.getImgUrls();
            checkImageUrlsNotEmpty(appId, objectBasic.getType(), imgUrls);
            List<ImageRelationDTO> imageRelationDTOS = Lists.newArrayList();
            for (String imgUrl : imgUrls) {
                ImageRelationDTO imageRelationDTO = new ImageRelationDTO();
                imageRelationDTO.setEntityId(objChangeHistoryId)
                        .setEntityType(EntityTypeEnum.OBJHISTORY.getType())
                        .setUrl(imgUrl);
                imageRelationDTOS.add(imageRelationDTO);
            }
            imageRelationService.createBatch(imageRelationDTOS);
            // 插入对象标签信息
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

            // 更新CID映射
            objCidInfoService.update(appId, objId, objectBasicInfo.getCidTagInfos());

            // 2.2 处理对象埋点以及参数绑定等信息
            List<Long> trackerIds = objectHelper.createObjectTrackersInfo(
                    objId, objChangeHistoryId, param.getReqPoolId(), param.getTrackers());

            // 2.3 需求关联spm池
            requirementPoolHelper.updateSpmPool(
                    param.getReqPoolId(), Sets.newHashSet(trackerIds), OperationTypeEnum.CREATE, false);
            result.put(objectBasicInfo.getOid(), objId);
        }
        return result;
    }

    private void checkTerminalId(Set<Long> terminalIds, Integer terminalBigType) {
        // 每次创建埋点输入的端数量都非常有限，因此这里直接循环调用
        terminalIds.forEach(terminalId -> {
            if (terminalId == null) {
                throw new CommonException("未指定端");
            }
            TerminalSimpleDTO terminalSimpleDTO = terminalService.getById(terminalId);
            if (terminalSimpleDTO == null) {
                throw new CommonException("terminalId无效");
            }
            if (!Objects.equals(terminalBigType, terminalSimpleDTO.getType())) {
                throw new CommonException("服务端埋点只能建在服务端下，客户端埋点只能建在客户端下，请检查端是不是选错了");
            }
        });
    }

    /**
     * 仅更新对象名字
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
     * 变更对象
     *
     * @param param 对象变更信息
     */
    @MethodLog
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void changeObject(ObjectChangeParam param, ObjDetailsVO objDetails){
        // 参数检查
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "未指定产品信息！");
        Preconditions.checkArgument(null != param, "对象变更信息不能为空！");
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(param.getTrackers()),
                "对象埋点信息不能为空！");
        for (ObjectTrackerChangeParam tracker : param.getTrackers()) {
            if (tracker.getId() == null) {
                throw new CommonException("变更操作不支持增加端，请先拉空变更，再在编辑时增加端");
            }
        }

        // 检查能否进行变更
        Long objId = param.getId();
        Long reqPoolId = param.getReqPoolId();
        if(objectHelper.isChanged(objId, reqPoolId)){
            throw new ObjException(String.format("oid={%s}的对象在当前需求下已发生过变更！", param.getOid()));
        }

        // 检查父对象是否存在
        param.getTrackers().forEach(trackerChangeParam -> objectHelper.checkParentExist(appId, trackerChangeParam.getParentObjs(), trackerChangeParam.getTerminalId(), param.getReqPoolId()));

        // 1. 处理对象基本信息
        // 1.1 对象元信息更新(变更操作只能修改部分不重要的元信息，像oid、name等字段不能改动)
        ObjectBasic objectBasic = new ObjectBasic();
        objectBasic.setId(objId)
                .setDescription(param.getDescription())
                .setPriority(param.getPriority());
        objectBasicService.update(objectBasic);
        // 1.2 插入对象变更信息
        EisObjChangeHistory objChangeHistory = new EisObjChangeHistory();
        objChangeHistory.setObjId(objId);
        objChangeHistory.setReqPoolId(reqPoolId);
        // 对象是否是复用开发
        boolean reuse = checkReuse(param, objDetails);
        objChangeHistory.setType(reuse ? OperationTypeEnum.REUSER.getOperationType() : OperationTypeEnum.CHANGE.getOperationType());
        objChangeHistory.setConsistency(param.getConsistency());
        final Long objChangeHistoryId = objChangeHistoryService.insert(objChangeHistory);
        List<EisObjChangeHistory> objChangeHistoryList = objChangeHistoryService.getByObjAndReqPoolId(objId, reqPoolId);
        if(objChangeHistoryList.size() > 1) {
            log.info("重复操做插入objChangeHistory {} {}", objId, reqPoolId);
            throw new CommonException("请勿重复操作！");
        }
        // 1.3 插入对象关联图片信息
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

        // 2. 处理对象埋点信息
        List<Long> trackerIds = objectHelper.changeObjectTrackerInfo(
                objId, objChangeHistoryId, reqPoolId, param.getTrackers());

        // 3. 处理对象 spm需求关联池 信息
        requirementPoolHelper.updateSpmPool(
                reqPoolId, Sets.newHashSet(trackerIds), OperationTypeEnum.CHANGE,false);
    }

    /**
     * 编辑对象基础信息
     *
     * @param param 对象基础变更信息
     */
    @MethodLog
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void editObjectBasic(ObjectBasicChangeParam param){
        // 参数检查
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "未指定产品信息！");
        Preconditions.checkArgument(null != param, "对象编辑信息不能为空！");
        ParamCheckUtil.checkOid(param.getOid());

        // 对象新建/变更记录查询
        Long objId = param.getId();
        Long reqPoolId = param.getReqPoolId();
        List<EisObjChangeHistory> objChangeHistoryList = objChangeHistoryService
                .getByObjAndReqPoolId(objId, reqPoolId);
        if(objChangeHistoryList.size() != 1){
            log.info("objId为{}的对象在reqId为{}的需求下无副本信息或存在多个副本信息！", objId, reqPoolId);
            throw new ObjException("对象在当前需求下无副本信息或存在多个副本信息，编辑操作失败！");
        }

        // 更新对象变更记录
        EisObjChangeHistory objChangeHistory = objChangeHistoryList.get(0);
        Long objHistoryId = objChangeHistory.getId();

        // 1. 更新对象基本信息
        // 更新对象元信息
        ObjectBasic objectBasic = BeanConvertUtils.convert(param, ObjectBasic.class);
        if(null == objectBasic){
            log.error("convert to object basic failure!");
            throw new ObjException("对象基本信息有误，无法完成编辑");
        }
        objectBasic.setSpecialType(null);
        ObjectBasic originBasic = objectBasicService.getById(objId);
        if (originBasic == null) {
            throw new CommonException("objId = " + objId + " 对象不存在");
        }
        // 编辑基本信息时，不可修改涉及埋点规则的字段
        if (!StringUtils.equals(originBasic.getOid(), param.getOid())) {
            throw new CommonException("编辑基本信息时，不可改变线上埋点规则。因此对象oid不可改变");
        }
        if (!Objects.equals(originBasic.getType(), param.getType())) {
            throw new CommonException("编辑基本信息时，不可改变线上埋点规则。因此对象类型不可改变");
        }
        ObjectExtDTO ext = getExt(originBasic);
        // 只更新基本属性，不修改更改是否是桥梁、也不支持更改桥梁属性
        ext.setBasicTag(convertObjBasicTag(param.getOid(), param.getType(), param.getBasicTag()));
        ext.setAnalyseCid(param.isAnalyseCid());
        objectBasic.setExt(JsonUtils.toJson(ext));
        objectBasicService.update(objectBasic);
        // 更新对象关联图片信息 (先删除后新建)
        imageRelationService.deleteImageRelation(Collections.singletonList(objHistoryId),
                EntityTypeEnum.OBJHISTORY.getType());
        List<String> imgUrls = param.getImgUrls();
        checkImageUrlsNotEmpty(appId, objectBasic.getType(), imgUrls);
        List<ImageRelationDTO> imageRelationDTOS = Lists.newArrayList();
        for (String imgUrl : imgUrls) {
            ImageRelationDTO imageRelationDTO = new ImageRelationDTO();
            imageRelationDTO.setEntityId(objHistoryId)
                    .setEntityType(EntityTypeEnum.OBJHISTORY.getType())
                    .setUrl(imgUrl);
            imageRelationDTOS.add(imageRelationDTO);
        }
        imageRelationService.createBatch(imageRelationDTOS);
        // 更新对象关联标签信息 (先删除后新建)
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
        // 更新CID映射
        objCidInfoService.update(appId, objId, param.getCidTagInfos());
    }

    /**
     * 编辑对象
     *
     * @param param 对象更新信息
     * @return 是否为解决冲突的编辑操作
     */
    @MethodLog
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public boolean editObject(ObjectEditParam param){
        // 参数检查
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "未指定产品信息！");
        Preconditions.checkArgument(null != param, "对象编辑信息不能为空！");
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(param.getTrackers()),
                "对象埋点信息不能为空！");
        ParamCheckUtil.checkOid(param.getOid());

        checkTerminalId(param.getTrackers().stream().map(o -> o.getTerminalId()).collect(Collectors.toSet()), param.getTerminalBigType());

        // 勾选多端同步时，参数绑定不一致问题处理
        fixParamBindsConsitency(param);

        // 对象新建/变更记录查询
        Long objId = param.getId();
        Long reqPoolId = param.getReqPoolId();
        List<EisObjChangeHistory> objChangeHistoryList = objChangeHistoryService
                .getByObjAndReqPoolId(objId, reqPoolId);
        if(objChangeHistoryList.size() != 1){
            log.info("objId为{}的对象在reqId为{}的需求下无副本信息或存在多个副本信息！", objId, reqPoolId);
            throw new ObjException("对象在当前需求下无副本信息或存在多个副本信息，编辑操作失败！");
        }

        // 判断对象是否已经上线
        EisTaskProcess taskProcessQuery = new EisTaskProcess();
        taskProcessQuery.setObjId(objId);
        taskProcessQuery.setReqPoolId(reqPoolId);
        List<EisTaskProcess> taskProcesses = taskProcessService.search(taskProcessQuery);
        for (EisTaskProcess taskProcess : taskProcesses) {
            if(ProcessStatusEnum.ONLINE.getState().equals(taskProcess.getStatus())){
                throw new CommonException("对象在当前需求组下已有发布上线记录，无法编辑");
            }
        }

        // 改为浮层对象时，应清除trackers里的父对象
        if (ObjTypeEnum.POPOVER.getType().equals(param.getType())) {
            List<ObjectTrackerEditParam> trackers = param.getTrackers();
            if (CollectionUtils.isNotEmpty(trackers)) {
                trackers.forEach(tracker -> tracker.setParentObjs(new ArrayList<>(0)));
            }
        }

        // 检查父对象是否存在
        param.getTrackers().forEach(trackerEditParam -> objectHelper.checkParentExist(appId, trackerEditParam.getParentObjs(), trackerEditParam.getTerminalId(), param.getReqPoolId()));

        // 检查是否为冲突情况下的编辑
        EisObjChangeHistory objChangeHistory = objChangeHistoryList.get(0);
        Long objHistoryId = objChangeHistory.getId();
        boolean isInMergeConflict = ConflictStatusEnum.fromStatus(objChangeHistory.getConflictStatus()) == ConflictStatusEnum.MERGE_CONFLICT;
        // 冲突中，必须在三段式对比编辑界面编辑，因此校验前端在这个界面
        if (isInMergeConflict && !param.isResolveConflict()) {
            throw new CommonException("当前对象存在合并基线冲突，请刷新页面后解决冲突");
        }


        // 1. 更新对象基本信息
        // 更新对象元信息
        ObjectBasic objectBasic = BeanConvertUtils.convert(param, ObjectBasic.class);
        if(null == objectBasic){
            log.error("convert to object basic failure!");
            throw new ObjException("对象基本信息有误，无法完成编辑");
        }
        ObjectBasic objectOriginBasic = objectBasicService.getById(objId);
        // 只有新增对象才可以更新OID
        boolean isOidChanged = !StringUtils.equals(objectOriginBasic.getOid(), objectBasic.getOid());
        if (isOidChanged) {
            Integer operationType = objChangeHistory.getType();
            boolean canEditBasic = OperationTypeEnum.CREATE.getOperationType().equals(operationType) || OperationTypeEnum.TERMINAL_ADD.getOperationType().equals(operationType);
            if (!canEditBasic) {
                throw new CommonException("只有新增对象才可以更新OID");
            }
        }
        ObjTypeEnum originType = ObjTypeEnum.fromType(objectBasic.getType());
        ObjTypeEnum targetType = ObjTypeEnum.fromType(param.getType());
        if (originType.getNamespace() != targetType.getNamespace()) {
            throw new CommonException("请确认勾选的对象类型：无法在客户端对象和服务端对象之间相互转换。");
        }

        if (objectBasic.getSpecialType() == null) {
            objectBasic.setSpecialType(ObjSpecialTypeEnum.NORMAL.getName());
        }
        if (objectBasic.getSpecialType().equals(ObjSpecialTypeEnum.BRIDGE.getName())) {
            // 校验是否有子空间设置
            objectHelper.checkBridge(param.getBridgeSubAppId(), param.getBridgeSubTerminalId(), objectBasic.getSpecialType());
        }
        ObjectExtDTO objectExtDTO = new ObjectExtDTO();
        objectExtDTO.setSubAppId(param.getBridgeSubAppId());
        objectExtDTO.setSubTerminalId(param.getBridgeSubTerminalId() == null ? 0L : param.getBridgeSubTerminalId());
        objectExtDTO.setBasicTag(convertObjBasicTag(param.getOid(), param.getType(), param.getBasicTag()));
        objectExtDTO.setAnalyseCid(param.isAnalyseCid());
        objectBasic.setExt(JsonUtils.toJson(objectExtDTO));
        objectBasicService.update(objectBasic);

        // 更新是否多端一致字段
        objChangeHistory.setConsistency(param.getConsistency());
        objChangeHistoryService.update(objChangeHistory);

        // 更新对象关联图片信息 (先删除后新建)
        imageRelationService.deleteImageRelation(Collections.singletonList(objHistoryId),
                EntityTypeEnum.OBJHISTORY.getType());
        List<String> imgUrls = param.getImgUrls();
        checkImageUrlsNotEmpty(appId, objectBasic.getType(), imgUrls);
        List<ImageRelationDTO> imageRelationDTOS = Lists.newArrayList();
        for (String imgUrl : imgUrls) {
            ImageRelationDTO imageRelationDTO = new ImageRelationDTO();
            imageRelationDTO.setEntityId(objHistoryId)
                    .setEntityType(EntityTypeEnum.OBJHISTORY.getType())
                    .setUrl(imgUrl);
            imageRelationDTOS.add(imageRelationDTO);
        }
        imageRelationService.createBatch(imageRelationDTOS);
        // 更新对象关联标签信息 (先删除后新建)
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
        // 更新CID映射
        objCidInfoService.update(appId, objId, param.getCidTagInfos());

        // 2. 更新对象埋点信息
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

        //判断是否更新任务spm状态
        boolean update = !objectBasic.getName().equals(objectOriginBasic.getName()) || !objectBasic.getOid().equals(objectOriginBasic.getOid()) || !objectBasic.getType().equals(objectOriginBasic.getType());

        ObjDetailsVO objDetails = getObjectByHistory(objId, objHistoryId, reqPoolId);
        boolean change = checkEditChange(param, objDetails);

        // 3. 更新对象 spm需求关联池 信息
        List<UpdateSpmPoolParam> updateSpmPoolParams = new ArrayList<>();
        newTrackerIds.forEach(newTrackerId -> {
            UpdateSpmPoolParam p = new UpdateSpmPoolParam();
            p.setTrackerId(newTrackerId);
            if (trackerIdsBeforeEdit.contains(newTrackerId)) {
                Integer type = objChangeHistory.getType();
                p.setOperationTypeEnum(OperationTypeEnum.CHANGE);
                p.setEdit(true);
            } else {
                // 这种是编辑时新增，走新增流程
                p.setOperationTypeEnum(OperationTypeEnum.CREATE);
                p.setEdit(false);
            }
            updateSpmPoolParams.add(p);
        });
        requirementPoolHelper.updateSpmPoolNew(reqPoolId, updateSpmPoolParams, update || change);
        // 冲突情况下编辑后，冲突解决
        if (isInMergeConflict) {
            objChangeHistoryService.updateConflictStatus(reqPoolId, Collections.singleton(objId), ConflictStatusEnum.RESOLVED.getStatus());
            return true;
        }
        return false;
    }

    /**
     * 勾选多端同步时，参数绑定不一致问题处理
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
                throw new CommonException("所选端不存在");
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
     * 变更对象时，获取对象的基本信息
     *
     * @param objId 对象ID
     * @param objHistoryId 对象变更历史ID
     */
    @MethodLog
    public ObjDetailsVO getObjectForChange(Long objId, Long objHistoryId){

        // 1. 获取对象的基本信息
        ObjectInfoDTO objectBasicInfo = objectHelper.getObjectBasicInfo(objId, objHistoryId);

        // 2. 获取对象关联的埋点信息
        List<ObjectTrackerInfoDTO> objectTrackerInfoList = objectHelper
                .getObjTrackersInfo(objId, objHistoryId, true);

        // 3. 组合信息
        ObjDetailsVO objDetailsVO = BeanConvertUtils.convert(objectBasicInfo, ObjDetailsVO.class);
        if(objDetailsVO == null){
            log.warn("对象信息转化有误，获取对象详情失败！");
            throw new ObjException("信息转化失败，无法获取对象详情！");
        }
        objDetailsVO.setTrackers(objectTrackerInfoList);
        objDetailsVO.setBasicTag(new ObjBasicTagDTO());
        objDetailsVO.getBasicTag().setObjSubType(objectBasicInfo.getObjSubType());
        objDetailsVO.getBasicTag().setBizGroup(objectBasicInfo.getBizGroup());
        objDetailsVO.getBasicTag().setBizGroupName(objectBasicInfo.getBizGroupName());
        composeCidInfo(objectBasicInfo.getAppId(), objDetailsVO);
        return objDetailsVO;
    }

    public ObjDetailsVO getObjectByHistory(Long objId, Long objHistoryId, Long reqPoolId) {
        return getObjectByHistory(objId, objHistoryId, reqPoolId, false);
    }

    @MethodLog
    public ObjDetailsVO getObjectByHistory(Long objId, Long objHistoryId, Long reqPoolId, boolean composeDiff){

        reviseOid(objId);

        // 1. 获取对象的基本信息
        ObjectInfoDTO objectBasicInfo = objectHelper.getObjectBasicInfo(objId, objHistoryId);

        // 2. 获取对象关联的埋点信息
        List<ObjectTrackerInfoDTO> objectTrackerInfoList = objectHelper
                .getObjTrackersInfo(objId, objHistoryId, false);

        // 获取对象关联的事件埋点信息
        List<EisEventObjRelation> eisEventObjRelations = reqEventObjRelationService.getByObjId(objId);
        Set<Long> entityIds = eisEventObjRelations.stream().map(EisEventObjRelation::getEventPoolEntityId).collect(Collectors.toSet());
        List<UnDevelopedEventVO> unDevelopedEventVOS = eventPoolFacade.getReqPoolEvents(entityIds);

        // 3. 组合信息
        ObjDetailsVO objDetailsVO = BeanConvertUtils.convert(objectBasicInfo, ObjDetailsVO.class);
        if(objDetailsVO == null){
            log.warn("对象信息转化有误，获取对象详情失败！");
            throw new ObjException("信息转化失败，无法获取对象详情！");
        }
        objDetailsVO.setRelationInfos(unDevelopedEventVOS);
        objDetailsVO.setTrackers(objectTrackerInfoList);
        objDetailsVO.setBasicTag(new ObjBasicTagDTO());
        objDetailsVO.getBasicTag().setObjSubType(objectBasicInfo.getObjSubType());
        objDetailsVO.getBasicTag().setBizGroup(objectBasicInfo.getBizGroup());
        objDetailsVO.getBasicTag().setBizGroupName(objectBasicInfo.getBizGroupName());
        composeCidInfo(objectBasicInfo.getAppId(), objDetailsVO);

        // 判断对象是否已经上线
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
            throw new CommonException("objId为" + objId + "的对象不存在");
        }
        String oid = objectBasic.getOid();
        String lowerCaseOid = StringUtils.lowerCase(oid);
        if (!StringUtils.equals(oid, lowerCaseOid)) {
            // 订正逻辑
            objectBasic.setOid(lowerCaseOid);
            objectBasicService.update(objectBasic);
            log.info("oid已订正 objId={} newOid={}", objId, lowerCaseOid);
        }
    }

    /**
     * 获取对象详情（包括对象基本信息以及埋点信息）
     *
     * 接口废弃，只能查询当前需求组下新建/变更的对象
     *
     * @param objId     对象ID
     * @param reqPoolId 需求组ID
     * @return
     */
    @Deprecated
    public ObjDetailsVO getObjectByReqPoolId(Long objId, Long reqPoolId){
        // 1. 通过 需求组信息 获取对象 历史变更
        List<EisObjChangeHistory> objChangeHistoryList = objChangeHistoryService
                .getByObjAndReqPoolId(objId, reqPoolId);
        if(objChangeHistoryList.size() != 1){
            log.warn("objId为{}的对象在reqId为{}的需求下无副本信息或存在多个副本信息！", objId, reqPoolId);
            throw new CommonException("对象在当前需求下无副本信息或存在多个副本信息！");
        }

        EisObjChangeHistory objChangeHistory = objChangeHistoryList.get(0);
        Long objHistoryId = objChangeHistory.getId();
        // 2. 获取对象详情
        return getObjectByHistory(objId, objHistoryId, reqPoolId);
    }

    /**
     * 获取对象在当前需求组基线和线上最新release基线之间的变化
     */
    public ObjDetailsVO getBaseLineDiff(Long objId, Long reqPoolId) {
        // 1. 通过 需求组信息 获取对象 历史变更
        List<EisObjChangeHistory> objChangeHistoryList = objChangeHistoryService
                .getByObjAndReqPoolId(objId, reqPoolId);
        if(objChangeHistoryList.size() != 1){
            log.warn("objId为{}的对象在reqId为{}的需求下无副本信息或存在多个副本信息！", objId, reqPoolId);
            throw new CommonException("对象在当前需求下无副本信息或存在多个副本信息！");
        }

        EisObjChangeHistory objChangeHistory = objChangeHistoryList.get(0);
        Long objHistoryId = objChangeHistory.getId();

        // 当前状态，用于编辑
        ObjDetailsVO current = getObjectByHistory(objId, objHistoryId, reqPoolId, false);
        return getBaseLineChange(current, objId, reqPoolId);
    }

    private ObjDetailsVO getBaseLineChange(ObjDetailsVO current, Long objId, Long reqPoolId) {
        ObjDetailsVO result = null;

        Long appId = EtContext.get(ContextConstant.APP_ID);
        Set<Long> allTerminalIds = terminalService.getByAppId(appId)
                .stream()
                .map(o -> o.getId()).collect(Collectors.toSet());

        long maxTerminalReleaseId = 0L; // 用于查找已发布的最新的objHistory
        List<ObjectTrackerInfoDTO> trackerDiff = new ArrayList<>();
        for (Long t : allTerminalIds) {
            EisReqPoolRelBaseRelease currentUse = reqPoolRelBaseService.getCurrentUse(reqPoolId, t);
            long currentBaseLineReleaseId = currentUse == null ? 0L : currentUse.getBaseReleaseId();
            EisTerminalReleaseHistory latestRelease = terminalReleaseService.getLatestRelease(t);
            long targetBaseLineReleaseId = latestRelease == null ? 0L : latestRelease.getId();
            maxTerminalReleaseId = Math.max(maxTerminalReleaseId, targetBaseLineReleaseId);
            // 组装每个端的trackerDiff
            ObjectTrackerInfoDTO trackerDiffOfTerminal = trackerDiffHelper.getReleaseTrackerDiff(objId, targetBaseLineReleaseId, currentBaseLineReleaseId);
            if (trackerDiffOfTerminal != null) {
                trackerDiff.add(trackerDiffOfTerminal);
            }
        }

        // 获取最新基线下对象信息
        if (maxTerminalReleaseId > 0L) {
            ObjectInfoDTO objectInfoDTO = getObjInfoByReleaseIdAndObjId(maxTerminalReleaseId, objId);
            if (objectInfoDTO != null) {
                result = getObjectByHistory(objId, objectInfoDTO.getHistoryId(), reqPoolId, false);
            }
        }
        // 如果最新基线下没有，则使用当前对象信息
        if (result == null) {
            result = JsonUtils.parseObject(JsonUtils.toJson(current), ObjDetailsVO.class);
        }
        // 替换trckers为上面算出来的diff
        if (result == null) {
            throw new CommonException("获取基线之间差异失败");
        }

        result.setTrackers(trackerDiff);
        return result;
    }

    public ObjDetailsVO getObjectByReqPoolIdWithDiff(Long objId, Long reqPoolId) {
        reviseOid(objId);


        // 1. 通过 需求组信息 获取对象 历史变更
        List<EisObjChangeHistory> objChangeHistoryList = objChangeHistoryService
                .getByObjAndReqPoolId(objId, reqPoolId);
        if (CollectionUtils.isEmpty(objChangeHistoryList)) {
            throw new CommonException("需求池下没找到objId为" + objId + "的对象");
        }
        if (objChangeHistoryList.size() > 1) {
            throw new CommonException("对象在当前需求池下存在多个副本信息！");
        }

        EisObjChangeHistory objChangeHistory = objChangeHistoryList.get(0);
        Long objHistoryId = objChangeHistory.getId();
        // 2. 获取当前对象详情
        ObjDetailsVO objDetailsVO = getObjectByHistory(objId, objHistoryId, reqPoolId);
        composeObjDetailsDiff(reqPoolId, objId, objDetailsVO);
        return objDetailsVO;
    }

    /**
     * 获取对象详情
     *
     * @param objId     对象ID
     * @param trackerId 埋点ID
     * @return
     */
    @Deprecated
    public ObjDetailsVO getObjectByTrackerId(Long objId, Long trackerId){
        // 1. 通过trackerId 获取 historyId
        EisObjTerminalTracker objTerminalTracker = objTerminalTrackerService.getById(trackerId);
        if(null == objTerminalTracker){
            log.warn("objId={}的对象未查询到对应的埋点trackerId={}信息！", objId, trackerId);
            throw new CommonException("未查询到对象详情！");
        }
        // 2. 查询对象详情
        Long objHistoryId = objTerminalTracker.getObjHistoryId();
        return getObjectByHistory(objId, objHistoryId, objTerminalTracker.getReqPoolId());
    }

    public List<ObjectBasic> getCandidateParentObjects(List<Long> terminalIds, Long reqPoolId) {
        Long appId = EtContext.get(ContextConstant.APP_ID);
        return objectHelper.getCandidateParentObjects(appId, terminalIds, reqPoolId);
    }

    /**
     * 获取样例数据
     *
     * @param objId             对象ID
     * @param terminalId        终端ID
     * @param terminalReleaseId 终端版本ID
     * @return
     */
    public List<Map<String, Object>> getObjExampleData(Long objId, Long terminalId, Long terminalReleaseId) {
        // 参数检查
        Preconditions.checkArgument(null != objId, "对象ID不能为空");
        Preconditions.checkArgument(null != terminalId, "终端ID不能为空");
        Preconditions.checkArgument(null != terminalReleaseId, "终端发布版本ID不能为空");

        // 样例数据查询
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
        // 1. 查询该埋点已保存的样例数据
        List<CheckHistorySimpleDTO> checkHistoryList = spmCheckHistoryService
                .getByTrackerId(trackerId);
        Map<TwoTuple<String, String>, Object> eventInfoToLogMap = new HashMap<>();
        if (checkHistoryList.size() > 0) {
            for (CheckHistorySimpleDTO elem : checkHistoryList) {
                TwoTuple<String, String> eventInfo = new TwoTuple<>(elem.getEventCode(), elem.getEventName());
                eventInfoToLogMap.putIfAbsent(eventInfo, elem.getLog());
            }
        }
        // 2. 查询该埋点原有的事件
        List<ObjTrackerEventSimpleDTO> objTrackerEventSimpleDTOList = objTrackerEventService
                .getByTrackerId(Collections.singletonList(trackerId));
        List<Long> eventIdList = objTrackerEventSimpleDTOList.stream()
                .map(ObjTrackerEventSimpleDTO::getEventId)
                .collect(Collectors.toList());
        List<EventSimpleDTO> eventSimpleDTOList = eventService.getEventByIds(eventIdList);
        List<TwoTuple<String, String>> eventInfoList = eventSimpleDTOList.stream()
                .map(event -> new TwoTuple<>(event.getCode(), event.getName()))
                .collect(Collectors.toList());
        // 3. 整理所有对象埋点的关联事件及其样例数据
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
     * 获取已上线对象的列表树  // todo 分页功能
     *
     * @param releasedId 发布版本ID
     * @param type       对象类型
     * @param tagIds     标签ID集合
     * @param search     搜索条件
     * @return
     */
    public ObjTreeVO getReleasedObjTree(
            Long releasedId, Integer type, List<Long> tagIds, String search, String orderBy, String orderRule){
        Preconditions.checkArgument(null != releasedId, "发布版本ID不能为空！");

        ObjTreeVO result = new ObjTreeVO();
        // 1. 获取全量血缘图
        LinageGraph graph = lineageHelper.genReleasedLinageGraph(releasedId);

        Map<Long, Set<Long>> parentsMap = graph.getParentsMap();
        Set<Long> objIds = Sets.newHashSet();
        for (Long objId : parentsMap.keySet()) {
            objIds.add(objId);
            objIds.addAll(parentsMap.getOrDefault(objId, Sets.newHashSet()));
        }

        // 2. 查询当前发布版本下的所有对象信息
        List<ObjectBasic> allList = objectBasicService.getByIds(objIds);
        List<ObjectBasic> objectBasicList = new ArrayList<>(allList);

        // 根据对象类型搜索
        if(null != type){
            objectBasicList = objectBasicList.stream()
                    .filter(k -> type.equals(k.getType()))
                    .collect(Collectors.toList());
        }
        // 根据标签搜索
        if(CollectionUtils.isNotEmpty(tagIds)){
            List<ObjTagSimpleDTO> objTagSimpleDTOS = objTagService.selectObjTags(tagIds, null, null);
            List<Long> objIdFilteredByTags = objTagSimpleDTOS.stream()
                    .map(ObjTagSimpleDTO::getObjId)
                    .collect(Collectors.toList());
            objectBasicList = objectBasicList.stream()
                    .filter(k -> objIdFilteredByTags.contains(k.getId()))
                    .collect(Collectors.toList());
        }
        // 若为空集，直接返回
        if (CollectionUtils.isEmpty(objectBasicList)) {
            return result;
        }

        // 4. 构建简易的对象层级关系（获取对象列表树）
        Set<Long> selectedObjIds = objectBasicList.stream()
                .map(ObjectBasic::getId)
                .collect(Collectors.toSet());

        List<Node> rootNodes = lineageHelper.getObjTree(graph, selectedObjIds);

        // 3. 根据条件筛选对象
        // 根据对象名称或者Oid过滤

        // 获取匹配方法，这里会根据是否是SPM，走SPM匹配，或按名字oid匹配
        String spmToExpand = null;
        if (StringUtils.isNotBlank(search)) {
            Map<String, Long> oidToObjIdMap = objectBasicList.stream().collect(Collectors.toMap(ObjectBasic::getOid, ObjectBasic::getId, (oldV, newV) -> oldV));
            Map<Long, ObjectBasic> allObjBasicMap = objectBasicList.stream().collect(Collectors.toMap(ObjectBasic::getId, o -> o, (oldV, newV) -> oldV));
            Pair<String, List<Node>> p = filterRootNodes(rootNodes, search, objectBasicList, oidToObjIdMap, allObjBasicMap);
            rootNodes = p.getValue();
            spmToExpand = p.getKey();
        }

        Set<Long> objIdSet = lineageHelper.getObjIdsFromTree(rootNodes);

        // 5. 对象详细信息
        List<ObjectInfoDTO> objectBasicInfoDTOList = this.getObjInfoByReleaseId(releasedId, allList);
        Map<Long, ObjectInfoDTO> objInfoMap = objectBasicInfoDTOList.stream()
                .filter(k -> objIdSet.contains(k.getId()))
                .collect(Collectors.toMap(ObjectInfoDTO::getId, Function.identity()));

        // 6. 排序
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

        // 组装业务线信息
        Long appId = EtContext.get(ContextConstant.APP_ID);
        composeBizGroup(appId, rootNodes, objIdSet);

        // 构造返回结果
        result.setTree(rootNodes);
        result.setObjInfoMap(objInfoMap);
        result.setSpmsToExpand(StringUtils.isEmpty(spmToExpand) ? new ArrayList<>(0) : Arrays.asList(spmToExpand));
        return result;
    }

    /**
     * 根据对象id搜索子树
     * @param terminalId 端类型
     * @param oid 搜索条件
     * @return
     */
    public ObjTreeVO searchTreeByOid(String oid, Long terminalId){
        EisTerminalReleaseHistory latestRelease = terminalReleaseService.getLatestRelease(terminalId);
        Preconditions.checkArgument(null != latestRelease, "发布版本为空！");
        Long releasedId = latestRelease.getId();
        ObjTreeVO result = new ObjTreeVO();
        //获取全量血缘图
        LinageGraph graph = lineageHelper.genReleasedLinageGraph(releasedId);

        Map<Long, Set<Long>> parentsMap = graph.getParentsMap();
        Set<Long> objIds = Sets.newHashSet();
        for (Long objId : parentsMap.keySet()) {
            objIds.add(objId);
            objIds.addAll(parentsMap.getOrDefault(objId, Sets.newHashSet()));
        }
        //查询当前发布版本下的所有对象信息
        List<ObjectBasic> allList = objectBasicService.getByIds(objIds);
        List<ObjectBasic> objectBasicList = new ArrayList<>(allList);
        Map<String, Long> oidToObjIdMap = objectBasicList.stream().collect(Collectors.toMap(ObjectBasic::getOid, ObjectBasic::getId, (oldV, newV) -> oldV));
        //若为空集，直接返回
        if (CollectionUtils.isEmpty(objectBasicList)) {
            return result;
        }
        //构建简易的对象层级关系（获取对象列表树）
        Set<Long> selectedObjIds = objectBasicList.stream()
                .map(ObjectBasic::getId)
                .collect(Collectors.toSet());
        Long targetObjId = oidToObjIdMap.get(oid);
        List<Node> rootNodes = lineageHelper.getSonTree(graph, selectedObjIds, targetObjId);
        if (StringUtils.isNotBlank(oid)) {
            String spmByObjId = CommonUtil.transSpmByOidToSpmByObjId(oidToObjIdMap, oid);
            rootNodes = lineageHelper.filterObjTreeBySpm(rootNodes, spmByObjId);
        }
        Set<Long> objIdSet = lineageHelper.getObjIdsFromTree(rootNodes);
        //对象详细信息
        List<ObjectInfoDTO> objectBasicInfoDTOList = this.getObjInfoByReleaseId(releasedId, allList);
        Map<Long, ObjectInfoDTO> objInfoMap = objectBasicInfoDTOList.stream()
                .filter(k -> objIdSet.contains(k.getId()))
                .collect(Collectors.toMap(ObjectInfoDTO::getId, Function.identity()));
        //遍历树，拼上oid name
        for(Node root : rootNodes){
            traverseTree(root, objInfoMap);
        }
        //构造返回结果
        result.setTree(rootNodes);
        result.setObjInfoMap(objInfoMap);
        return result;
    }


    /**
     * 搜索对象路径
     * @param oid 对象名称
     * @param terminalId 端类型
     * @return
     */
    public ObjTreeVO getPathTreeByOid(String oid, Long terminalId){
        EisTerminalReleaseHistory latestRelease = terminalReleaseService.getLatestRelease(terminalId);
        Preconditions.checkArgument(null != latestRelease, "发布版本为空！");
        Long releasedId = latestRelease.getId();

        ObjTreeVO result = new ObjTreeVO();
        // 1. 获取全量血缘图
        LinageGraph graph = lineageHelper.genReleasedLinageGraph(releasedId);

        Map<Long, Set<Long>> parentsMap = graph.getParentsMap();
        Set<Long> objIds = Sets.newHashSet();
        for (Long objId : parentsMap.keySet()) {
            objIds.add(objId);
            objIds.addAll(parentsMap.getOrDefault(objId, Sets.newHashSet()));
        }

        // 2. 查询当前发布版本下的所有对象信息
        List<ObjectBasic> allList = objectBasicService.getByIds(objIds);
        List<ObjectBasic> objectBasicList = new ArrayList<>(allList);

        // 若为空集，直接返回
        if (CollectionUtils.isEmpty(objectBasicList)) {
            return result;
        }

        // 4. 构建简易的对象层级关系（获取对象列表树）
        Set<Long> selectedObjIds = objectBasicList.stream()
                .map(ObjectBasic::getId)
                .collect(Collectors.toSet());

        List<Node> rootNodes = lineageHelper.getObjTree(graph, selectedObjIds);

        // 3. 根据条件筛选对象
        // 根据对象名称或者Oid过滤

        // 获取匹配方法，这里会根据是否是SPM，走SPM匹配，或按名字oid匹配
        if (StringUtils.isNotBlank(oid)) {
            Map<String, Long> oidToObjIdMap = objectBasicList.stream().collect(Collectors.toMap(ObjectBasic::getOid, ObjectBasic::getId, (oldV, newV) -> oldV));
            Map<Long, ObjectBasic> allObjBasicMap = objectBasicList.stream().collect(Collectors.toMap(ObjectBasic::getId, o -> o, (oldV, newV) -> oldV));
            Pair<String, List<Node>> p = filterRootNodes(rootNodes, oid, objectBasicList, oidToObjIdMap, allObjBasicMap);
            rootNodes = p.getValue();
        }

        Set<Long> objIdSet = lineageHelper.getObjIdsFromTree(rootNodes);

        // 5. 对象详细信息
        List<ObjectInfoDTO> objectBasicInfoDTOList = this.getObjInfoByReleaseId(releasedId, allList);
        Map<Long, ObjectInfoDTO> objInfoMap = objectBasicInfoDTOList.stream()
                .filter(k -> objIdSet.contains(k.getId()))
                .collect(Collectors.toMap(ObjectInfoDTO::getId, Function.identity()));

        //遍历树，拼上oid name
        for(Node root : rootNodes){
            traverseTree(root, objInfoMap);
            if(root.getOid().equals(oid)){
                root.setChildren(new ArrayList<>());
            }
        }

        // 构造返回结果
        result.setTree(rootNodes);
        result.setObjInfoMap(objInfoMap);
        return result;
    }


    private void traverseTree(Node root, Map<Long, ObjectInfoDTO> objInfoMap){
        if(root.getObjId() != null && objInfoMap.get(root.getObjId()) != null){
            root.setOid(objInfoMap.get(root.getObjId()).getOid());
            root.setObjName(objInfoMap.get(root.getObjId()).getName());
        }
        if(CollectionUtils.isNotEmpty(root.getChildren())){
            for(Node childNode : root.getChildren()){
                traverseTree(childNode, objInfoMap);
            }
        }
    }

    private void composeBizGroup(Long appId, List<Node> rootNodes, Set<Long> objIdSet) {
        List<ObjectBasic> all = objectBasicService.getByIds(objIdSet);
        Map<Long, String> bizGroupMap = new HashMap<>();
        all.forEach(obj -> {
            ObjectExtDTO objectExtDTO = parseExt(obj.getExt());
            String bizGroupName = objectExtDTO.getBasicTag() == null ? null : objectExtDTO.getBasicTag().getBizGroupName();
            if (StringUtils.isNotBlank(bizGroupName)) {
                bizGroupMap.put(obj.getId(), bizGroupName);
            }
        });
        if (MapUtils.isEmpty(bizGroupMap)) {
            return;
        }
        // 递归
        rootNodes.forEach(rootNode -> doComposeBizGroup(rootNode, bizGroupMap, null));
    }

    /**
     * 设置当前层级，并递归儿子
     * @param current
     * @param dataMap
     */
    private void doComposeBizGroup(Node current, Map<Long, String> dataMap, String parentBizGroup) {
        //current.setBizGroup(parentBizGroup);    // 默认继承父亲
        current.setBizGroupName(parentBizGroup);// 默认继承父亲
        String bizGroupOfCurrentObjId = dataMap.get(current.getObjId());
        if (bizGroupOfCurrentObjId != null) {
            current.setBizGroup(bizGroupOfCurrentObjId);    // 如果当前objId有指定则覆盖
            current.setBizGroupName(bizGroupOfCurrentObjId);
        }
        if (CollectionUtils.isNotEmpty(current.getChildren())) {
            current.getChildren().forEach(child -> {
                doComposeBizGroup(child, dataMap, current.getBizGroupName());
            });
        }
    }

    /**
     * 新建对象、变更对象时，获取某终端下基础对象列表树
     *
     * @param terminalId 端ID
     * @param reqPoolId  需求组ID
     * @param search     对象Oid
     * @return
     */
    public ObjTreeVO getBaseTree(Long terminalId, Long reqPoolId, String search, String tagSearch){
        Preconditions.checkArgument(null != terminalId, "端ID不能为空！");

        ObjTreeVO result = new ObjTreeVO();
        // 1. 查询当前需求组在某终端下的基线发布版本
        TerminalSimpleDTO terminalSimpleDTO = terminalService.getById(terminalId);
        if (terminalSimpleDTO == null) {
            throw new CommonException("terminalId无效");
        }
        EisReqPoolRelBaseRelease reqPoolRelBaseRelease = reqPoolRelBaseService
                .getCurrentUse(reqPoolId, terminalId);
        if(null == reqPoolRelBaseRelease){
            String errMessage = String.format(
                    "需求组reqPoolId={%d}在终端terminalId={%d}下未查询到基线版本信息！", reqPoolId, terminalId);
            log.warn(errMessage);
            throw new CommonException(errMessage);
        }
        Long baseReleasedId = reqPoolRelBaseRelease.getBaseReleaseId();

        // 2. 查询当前发布版本下的所有对象信息 // 插入基线就包含的父空间血缘图
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

        //查询spm tag 信息
        SpmInfo spmInfoQuery = new SpmInfo();
        spmInfoQuery.setAppId(appId);
        spmInfoQuery.setTerminalId(terminalId);
        List<SpmInfoDTO> spmInfoList = spmInfoService.search(appId, spmInfoQuery);
        Map<String , List<TagSimpleDTO>> spmTagInfoMap = new HashMap<>();

        // 批量查询DB

        // 计算spmId -> SpmTagSimpleDTO映射
        Set<Long> spmIds = new HashSet<>();
        for (SpmInfoDTO spmInfo : spmInfoList) {
            spmIds.add(spmInfo.getId());
        }
        List<SpmTagSimpleDTO> spmTags = spmTagService.getBySpmIds(spmIds);
        if (spmTags == null) {
            spmTags = new ArrayList<>(1);
        }
        Map<Long, List<SpmTagSimpleDTO>> spmTagSimpleDTOMap = spmTags.stream().collect(Collectors.groupingBy(SpmTagSimpleDTO::getSpmId));

        // 计算tagId -> TagSimpleDTO映射
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

        // 3. 根据对象标签过滤
        if (StringUtils.isNotBlank(tagSearch)) {
            objectBasicList = objectBasicList.stream()
                    .filter(k -> {
                        // 关联标签信息
                        String spm = oidSpmMap.get(k.getOid());
                        List<TagSimpleDTO> tagSimpleDTOS = spmTagInfoMap.get(spm);
                        return CollectionUtils.isNotEmpty(tagSimpleDTOS) && CollectionUtils.isNotEmpty(tagSimpleDTOS.stream().filter(tagSimpleDTO -> tagSimpleDTO.getName().contains(tagSearch)).collect(Collectors.toList()));
                    }).collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(objectBasicList)) {
            return result;
        }

        // 4.构建简易的对象层级关系（获取对象列表树）
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

        // 5. 对象详细信息
        List<ObjectInfoDTO> objectBasicInfoDTOList = this.getObjInfoByReqPoolId(baseReleasedId, reqPoolId, graph.getAllObjIds());
        Set<Long> allBridges = objectBasicInfoDTOList.stream().filter(o -> ObjSpecialTypeEnum.BRIDGE.getName().equals(o.getSpecialType())).map(ObjectInfoDTO::getId).collect(Collectors.toSet());
        Set<Long> outerSpaceObjIds = new HashSet<>();
        allBridges.forEach(b -> {
            // 桥梁下挂载了其他对象，那么这个桥梁一定是挂载了其他端。此时把上面的标记为外部空间对象
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
            // 关联标签信息
            String spm = oidSpmMap.get(objectInfoDTO.getOid());
            List<TagSimpleDTO> tagSimpleDTOS = spmTagInfoMap.get(spm);
            objectInfoDTO.setTags(tagSimpleDTOS);
        }
        Map<Long, ObjectInfoDTO> objInfoMap = objectBasicInfoDTOList.stream()
                .filter(k -> objIdSet.contains(k.getId()))
                .collect(Collectors.toMap(ObjectInfoDTO::getId, Function.identity()));

        // 构造返回结果
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
                    // 如果搜索根节点，按SPM搜索
                    String spmByObjId = CommonUtil.transSpmByOidToSpmByObjId(oidToObjIdMap, search);
                    rootNodes = lineageHelper.filterObjTreeBySpm(rootNodes, spmByObjId);
                    spmToExpand = search;
                } else {
                    // 不是根节点，则按对象搜索
                    rootNodes = lineageHelper.filterObjTreeByObject(rootNodes, search, allObjBasicMap);
                }
            }
        } else {
            rootNodes = lineageHelper.filterObjTreeByObject(rootNodes, search, allObjBasicMap);
        }
        return new Pair<>(spmToExpand, rootNodes);
    }

    /**
     * 需求管理模块 获取某个需求组下 某新建/变更对象的血缘图
     *
     * @param terminalId 端ID
     * @param reqPoolId 需求组ID不能为空
     * @param objId 对象ID不能为空
     * @return
     */
    public ObjLineageGraphVO getReqPoolObjLineageGraph(Long terminalId, Long reqPoolId, final Long objId){
        Preconditions.checkArgument(null != terminalId, "端ID不能为空");
        Preconditions.checkArgument(null != reqPoolId, "需求组ID不能为空");
        Preconditions.checkArgument(null != objId, "对象ID不能为空");

        // 1. 基本信息查询
        EisReqPoolRelBaseRelease reqPoolRelBaseRelease = reqPoolRelBaseService
                .getCurrentUse(reqPoolId, terminalId);
        if(null == reqPoolRelBaseRelease){
            log.warn("终端terminalId={}未获取到对应的基线发布版本！", terminalId);
            throw new CommonException("未获取到当前终端下的基线发布版本！");
        }
        Long basedReleasedId = reqPoolRelBaseRelease.getBaseReleaseId();

        // 2. 获取全量血缘图（非终态血缘图，血缘关系只增不删）
        TotalLineageGraph graph = lineageHelper.getTotalLineageGraph(
                basedReleasedId, terminalId, reqPoolId);

        // 3. 构建对象血缘树
        List<Node> rootNodes = lineageHelper.getObjLineageTree(graph, objId);
        Set<Long> objIdSet = lineageHelper.getObjIdsFromTree(rootNodes);

        // 4. 标注新增血缘和删除血缘
        Map<Long, Set<Long>> addedRelationMap = graph.getAddedRelationMap();
        Map<Long, Set<Long>> deletedRelationMap = graph.getDeletedRelationMap();
        // 层次遍历
        Queue<TwoTuple<Long, Node>> queue = Lists.newLinkedList();
        rootNodes.forEach(node -> queue.offer(new TwoTuple<>(0L, node)));
        while(!queue.isEmpty()){
            // 当前节点出队
            TwoTuple<Long, Node> currTupleNode = queue.poll();
            Long currParentId = currTupleNode.getFirst();
            Node currNode = currTupleNode.getSecond();
            Long currObjId = currNode.getObjId();
            // 标注新增/删除信息
            if(addedRelationMap.containsKey(currParentId)
                    && addedRelationMap.get(currParentId).contains(currObjId)){
                currNode.setType(LineageTypeEnum.ADDED.getType());
            }else if(deletedRelationMap.containsKey(currParentId)
                    && deletedRelationMap.get(currParentId).contains(currObjId)){
                currNode.setType(LineageTypeEnum.DELETED.getType());
            }
            // 子节点入队
            List<Node> childrenNodes = currNode.getChildren();
            if(CollectionUtils.isNotEmpty(childrenNodes)) {
                childrenNodes.forEach(node -> queue.offer(new TwoTuple<>(currObjId, node)));
            }
        }

        // 5. 获取对象详情
        List<ObjectInfoDTO> objectInfoDTOList = this.getObjInfoByReqPoolId(basedReleasedId, reqPoolId, graph.getAllObjIds());
        Set<Long> allBridges = objectInfoDTOList.stream().filter(o -> ObjSpecialTypeEnum.BRIDGE.getName().equals(o.getSpecialType())).map(ObjectInfoDTO::getId).collect(Collectors.toSet());
        Set<Long> outerSpaceObjIds = new HashSet<>();
        allBridges.forEach(b -> {
            // 桥梁下挂载了其他对象，那么这个桥梁一定是挂载了其他端。此时把上面的标记为外部空间对象
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

        // 构建结果
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
     * 已上线对象管理模块 获取某个对象的血缘图
     *
     * @param releasedId 发布版本
     * @param objId 对象ID
     * @return
     */
    public ObjLineageGraphVO getReleasedObjLineageGraph(Long releasedId, Long objId){
        Preconditions.checkArgument(null != releasedId, "发布版本ID不能为空");
        Preconditions.checkArgument(null != objId, "对象ID不能为空");

        // 1. 获取全局血缘图
        LinageGraph graph = lineageHelper.genReleasedLinageGraph(releasedId);

        Map<Long, Set<Long>> parentsMap = graph.getParentsMap();
        Set<Long> objIds = Sets.newHashSet();
        for (Long o : parentsMap.keySet()) {
            objIds.add(o);
            objIds.addAll(parentsMap.getOrDefault(o, Sets.newHashSet()));
        }
        // 2. 查询当前发布版本下的所有对象信息
        List<ObjectBasic> allList = objectBasicService.getByIds(objIds);

        // 2. 获取对象的血缘树
        List<Node> rootNodes = lineageHelper.getObjLineageTree(graph, objId);
        Set<Long> objIdSet = lineageHelper.getObjIdsFromTree(rootNodes);

        // 3. 获取对象的详细信息
        List<ObjectInfoDTO> objectBasicInfoDTOList = this.getObjInfoByReleaseId(releasedId, allList);
        Map<Long, ObjectInfoDTO> objInfoMap = objectBasicInfoDTOList.stream()
                .filter(k -> objIdSet.contains(k.getId()))
                .collect(Collectors.toMap(ObjectInfoDTO::getId, Function.identity()));
        // 构建结果
        ObjLineageGraphVO objLineageGraphVO = new ObjLineageGraphVO();
        objLineageGraphVO.setTree(rootNodes);
        objLineageGraphVO.setObjInfoMap(objInfoMap);

        return objLineageGraphVO;
    }

    /**
     * 已上线对象管理模块 聚合信息查询
     *
     * @return
     */
    public ObjAggregateVO getObjAggregateInfo(){
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "未指定产品信息");

        ObjAggregateVO objAggregateVO = new ObjAggregateVO();
        // 1. 获取终端信息
        List<TerminalSimpleDTO> terminalSimpleDTOS = terminalService.getByAppId(appId);
        List<CommonAggregateDTO> terminals = Lists.newArrayList();
        for (TerminalSimpleDTO terminalSimpleDTO : terminalSimpleDTOS) {
            CommonAggregateDTO terminal = new CommonAggregateDTO();
            terminal.setKey(terminalSimpleDTO.getId().toString())
                    .setValue(terminalSimpleDTO.getName());
            terminals.add(terminal);
        }
        // 2. 获取全部的发布版本信息
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
                log.warn("发布版本releaseId={}无发布版本号terminalVersionId={}",
                        terminalReleaseHistory.getId(), terminalVersionId);
            }
            // 构建信息
            String terminalVersionName = terminalVersionInfo == null ? "None" : terminalVersionInfo.getName();
            CommonRelationAggregateDTO release = new CommonRelationAggregateDTO();
            release.setAssociatedKey(terminalReleaseHistory.getTerminalId().toString())
                    .setValue(String.format("%s-%d", terminalVersionName,
                            terminalReleaseHistory.getId()))
                    .setKey(terminalReleaseHistory.getId().toString());

            releases.add(release);
        }
        // 3. 获取全部的对象类型信息
        List<CommonAggregateDTO> types = Lists.newArrayList();
        for (ObjTypeEnum typeEnum : ObjTypeEnum.values()) {
            CommonAggregateDTO type = new CommonAggregateDTO();
            type.setKey(typeEnum.getType().toString())
                    .setValue(typeEnum.getName());
            types.add(type);
        }
        // 4. 获取全部的标签信息
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
        // 构建返回结果
        objAggregateVO.setTerminals(terminals)
                .setReleases(releases)
                .setTags(tags)
                .setTypes(types);
        return objAggregateVO;
    }

    /**
     * 对象详情抽屉页面 的聚合信息
     *
     * @param objId 对象ID
     * @param reqPoolId 需求组ID
     * @return
     */
    public ObjCascadeAggregateVO getObjCascadeAggregateInfo(Long objId, Long reqPoolId){
        // 参数校验
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "未指定产品信息！");

        ObjCascadeAggregateVO objCascadeAggregateVO = new ObjCascadeAggregateVO();

        // 信息查询
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

        // 信息填充
        objCascadeAggregateVO.setTerminals(terminals);
        return objCascadeAggregateVO;
    }


    /**
     * 获取对象发布版本历史信息
     *
     * @param terminalId 终端ID
     * @param objId      对象ID
     * @return
     */
    public List<ObjReleaseVO> getObjReleasedHistory(final Long terminalId, final Long objId){
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "未指定产品信息");

        // 1. 读取 eis_all_tracker_release 表
        EisAllTrackerRelease query = new EisAllTrackerRelease();
        query.setObjId(objId);
        List<EisAllTrackerRelease> trackerReleaseList = trackerReleaseService.search(query);

        // 2. 相关信息查询
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

        // 3. 构建结果集
        List<ObjReleaseVO> objReleaseVOList = Lists.newArrayList();
        for (EisObjTerminalTracker objTerminalTracker : trackerList) {
            Long currObjId = objTerminalTracker.getObjId();
            Long currObjHistoryId = objTerminalTracker.getObjHistoryId();
            Long currTerminalId = objTerminalTracker.getTerminalId();
            Long currReleaseId = objTerminalTracker.getTerminalReleaseId();
            Long terminalVersionId = 0L ;
            // 根据终端过滤
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

    // todo 代码整理
    public List<BaseReleaseVO> getBaseReleaseVO(Long reqPoolId){
        // 参数检查
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "未指定产品信息");
        Preconditions.checkArgument(null != reqPoolId, "需求组ID不能为空");

        List<TerminalSimpleDTO> terminalSimpleDTOList = terminalService.getByAppId(appId);

        List<BaseReleaseVO> result = Lists.newArrayList();
        for (TerminalSimpleDTO terminalSimpleDTO : terminalSimpleDTOList) {
            Long terminalId = terminalSimpleDTO.getId();
            String terminalName = terminalSimpleDTO.getName();
            BaseReleaseVO baseReleaseVO = new BaseReleaseVO();

            // 1. 查询当前需求组在某终端下的基线发布版本
            EisReqPoolRelBaseRelease reqPoolRelBaseRelease = reqPoolRelBaseService
                    .getCurrentUse(reqPoolId, terminalId);
            if(null == reqPoolRelBaseRelease){
                String errMessage = String.format(
                        "需求组reqPoolId={%d}在终端terminalId={%d}下未查询到基线版本信息！", reqPoolId, terminalId);
                log.warn(errMessage);
                continue;
            }
            Long releasedId = reqPoolRelBaseRelease.getBaseReleaseId();
            if(releasedId == 0L) continue;
            // 2. 查询发布信息
            EisTerminalReleaseHistory terminalReleaseHistory = terminalReleaseService.getById(releasedId);
            if(null == terminalReleaseHistory){
                String errMessage = String.format(
                        "未查询到baseReleaseId={%d}的发布版本信息！", releasedId);
                log.warn(errMessage);
                continue;
            }
            // 3. 查询端版本信息
            Long terminalVersionId = terminalReleaseHistory.getTerminalVersionId();
            EisTerminalVersionInfo terminalVersionInfo = terminalVersionInfoService.getById(terminalVersionId);
            if(null == terminalVersionInfo){
                String errMessage = String.format(
                        "未查询到terminalVersionId={%d}的端版本信息！", terminalVersionId);
                log.warn(errMessage);
                continue;
            }
            // 4. 信息填充
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
        // 信息查询
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
        // 信息聚合
        ObjectInfoDTO objectBasicInfoDTO = ObjectHelper.convertToDTO(objectBasic);
        objectBasicInfoDTO.setTrackerId(objTracker.getId());
        objectBasicInfoDTO.setReqPoolId(objTracker.getReqPoolId());
        objectBasicInfoDTO.setHistoryId(objTracker.getObjHistoryId());
        return objectBasicInfoDTO;
    }

    // todo 代码整理
    private List<ObjectInfoDTO> getObjInfoByReleaseId(Long releasedId, List<ObjectBasic> allObjectBasicList) {
        Preconditions.checkArgument(null != releasedId, "发布版本ID不能为空！");
        // 信息查询
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
        // 信息聚合
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

        // 补充父空间对象
        allObjectBasicList.forEach(objectBasic -> {
            if (!currentObjOids.contains(objectBasic.getId())) {
                // 不在当前基线里的对象，是其他空间的
                ObjectInfoDTO objectBasicInfoDTO = ObjectHelper.convertToDTO(objectBasic);
                objectBasicInfoDTO.setOtherAppId(objectBasic.getAppId());
                objectBasicInfoDTOList.add(objectBasicInfoDTO);
            }
        });

        return objectBasicInfoDTOList;
    }

    private List<ObjectInfoDTO> getObjInfoByReqPoolId(Long baseReleaseId, Long reqPoolId, Set<Long> allObjIds){
        // 1. 信息查询
        List<EisAllTrackerRelease> trackerReleaseList = trackerReleaseService.getByReleaseId(baseReleaseId);
        Set<Long> objIds = trackerReleaseList.stream()
                .map(EisAllTrackerRelease::getObjId)
                .collect(Collectors.toSet());
        List<EisObjChangeHistory> objChangeHistoryList = objChangeHistoryService.getByReqPoolId(reqPoolId);
        Set<Long> diffObjIds = objChangeHistoryList.stream()
                .map(EisObjChangeHistory::getObjId)
                .collect(Collectors.toSet());
        // 合并 基础对象与新增/变更对象
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
        // 2. 信息聚合
        List<ObjectInfoDTO> objectBasicInfoDTOList = Lists.newArrayList();
        Long appId = EtContext.get(ContextConstant.APP_ID);
        for (ObjectBasic objectBasic : objectBasicList) {
            Long objId = objectBasic.getId();
            ObjectInfoDTO objectBasicInfoDTO = ObjectHelper.convertToDTO(objectBasic);
            if(null != objectBasicInfoDTO){
                // 额外设置 对象变更历史 变更标识
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
     * 过滤出spmList中，符合objSubTypeEnums的部分
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
     * 获取某需求组在给定终端下 血缘关系是否成环
     * @param reqPoolId
     * @param terminalId
     * @return
     */
    public boolean checkLoop(Long reqPoolId, Long terminalId){
        Preconditions.checkArgument(null != reqPoolId, "需求组ID不能为空");
        Preconditions.checkArgument(null != terminalId, "终端ID不能为空");
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
                log.info("eventId有变化");
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
                log.info("eventParamVersionIdMap有变化");
                return true;
            }
            if(!Objects.equals(trackerParam.getPubParamPackageId(), trackerInfoDTO.getPubParamPackageId())) {
                log.info("PubParamPackageId有变化");
                return true;
            }
            if(!CollectionUtils.isEqualCollection(trackerParam.getParentObjs(), trackerInfoDTO.getParentObjects().stream().map(ObjectBasicDTO::getId).collect(Collectors.toList()))) {
                log.info("parentObjs 有变化");
                return true;
            }
            if(!Objects.equals(trackerParam.getTerminalId(), trackerInfoDTO.getTerminal().getId())) {
                log.info("TerminalId有变化");
                return true;
            }

            List<ParamBindItermParam> paramBindItermParams = trackerParam.getParamBinds();
            List<ParamBindItemDTO> paramBindItemDTOS = trackerInfoDTO.getPrivateParam();
            if(paramBindItermParams.size() != paramBindItemDTOS.size()) {
                log.info("paramBindItermParams有变化");
                return true;
            }
            for(int j=0; j<paramBindItermParams.size(); j++){
                ParamBindItermParam paramBindItermParam = paramBindItermParams.get(j);
                ParamBindItemDTO paramBindItemDTO = paramBindItemDTOS.get(j);
                if(!Objects.equals(paramBindItermParam.getParamId(), paramBindItemDTO.getId())) {
                    log.info("paramId 有变化");
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 组装对象详情里跟以前版本对比的差异
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

        // 如果需求池下改对象未变更，则不能展示对比（如果展示该对比，则展示了非本需求池里的对比）
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
        // 检查每个对象是否实际一致
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
                log.info("reqPoolId={} objId={} 实际一致", reqPoolId, objId);
                result.add(o);
            }
        });
        return result;
    }

    private boolean isSame(ObjectTrackerInfoDTO trackerA, ObjectTrackerInfoDTO trackerB) {
        // 1. 比较私参
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

        // 2. 比较事件
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

        // 3. 比较父对象
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
        // 不都为空，但其中一者为空，那么另一者不为空，所以一定不等
        if (m1 == null || m2 == null) {
            return false;
        }
        // 两者都不为空，逐个比较
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
            // 根据对象名字、oid自动生成
            paramObjBasicTagDTO.setObjSubType(calcDefaultObjSubType(oid, objType).getOidPrefix());
        } else {
            // 由参数指定
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
        // 若oid以subTypeEnum名字打头，则属于此subTypeEnum
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
     * 获取对象的ObjectExtDTO
     */
    public ObjectExtDTO getExt(ObjectBasic objectBasic) {
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

    private void checkImageUrlsNotEmpty(Long appId, Integer objType, List<String> imgUrls) {
        if (CollectionUtils.isNotEmpty(imgUrls)) {
            return;
        }
        // 服务端埋点不需要图片
        ObjTypeEnum objTypeEnum = ObjTypeEnum.fromType(objType);
        if (objTypeEnum.getNamespace() == ObjTypeNamespaceEnum.SERVER) {
            return;
        }
        boolean isMustCheckImageUrls = mustCheckImageUrls(appId);
        if (isMustCheckImageUrls) {
            throw new CommonException("请上传图片");
        }
    }

    private boolean mustCheckOidUnique(Long appId) {
        if (MapUtils.isEmpty(checkConfigs)) {
            return false;
        }
        CheckConfig config = checkConfigs.get("checkOidUnique");
        return judgeByAppIdAndCheckConfig(config, appId);
    }

    private boolean mustCheckImageUrls(Long appId) {
        if (MapUtils.isEmpty(checkConfigs)) {
            return false;
        }
        CheckConfig config = checkConfigs.get("checkObjImageNotEmpty");
        return judgeByAppIdAndCheckConfig(config, appId);
    }

    private boolean judgeByAppIdAndCheckConfig(CheckConfig config, Long appId) {
        if (config == null || appId == null) {
            return false;
        }
        if (config.getAppValues() == null) {
            return config.isDefaultValue();
        }
        Boolean byAppId = config.getAppValues().get(appId);
        if (byAppId == null) {
            return config.isDefaultValue();
        }
        return byAppId;
    }
}
