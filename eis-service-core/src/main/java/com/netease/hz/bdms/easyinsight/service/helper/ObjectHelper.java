package com.netease.hz.bdms.easyinsight.service.helper;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.netease.hz.bdms.easyinsight.dao.EisTaskProcessMapper;
import com.netease.hz.bdms.easyinsight.service.service.*;
import com.netease.hz.bdms.easyinsight.common.bo.lineage.LinageGraph;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.event.EventSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.image.ImageRelationDTO;
import com.netease.hz.bdms.easyinsight.common.dto.obj.ObjectBasicDTO;
import com.netease.hz.bdms.easyinsight.common.dto.obj.ObjectExtDTO;
import com.netease.hz.bdms.easyinsight.common.dto.obj.ObjectInfoDTO;
import com.netease.hz.bdms.easyinsight.common.dto.obj.ObjectTrackerInfoDTO;
import com.netease.hz.bdms.easyinsight.common.dto.obj.tracker.event.ObjTrackerEventSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.parambind.ParamBindItemDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.parambind.ParamBindSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.parambind.ParamBindValueSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.tag.ObjTagSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.tag.TagSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.terminal.TerminalSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.enums.*;
import com.netease.hz.bdms.easyinsight.common.exception.CommonException;
import com.netease.hz.bdms.easyinsight.common.exception.ObjException;
import com.netease.hz.bdms.easyinsight.common.param.obj.ObjectTrackerChangeParam;
import com.netease.hz.bdms.easyinsight.common.param.obj.ObjectTrackerCreateParam;
import com.netease.hz.bdms.easyinsight.common.param.obj.ObjectTrackerEditParam;
import com.netease.hz.bdms.easyinsight.common.param.param.paramBind.ParamBindItermParam;
import com.netease.hz.bdms.easyinsight.common.query.Search;
import com.netease.hz.bdms.easyinsight.common.util.BeanConvertUtils;
import com.netease.hz.bdms.easyinsight.common.util.JsonUtils;
import com.netease.hz.bdms.easyinsight.dao.model.*;
import com.netease.hz.bdms.easyinsight.service.service.impl.AppRelationService;
import com.netease.hz.bdms.easyinsight.service.service.obj.*;
import com.netease.hz.bdms.easyinsight.service.service.requirement.ReqPoolRelBaseService;
import com.netease.hz.bdms.easyinsight.service.service.requirement.ReqSpmPoolService;
import com.netease.hz.bdms.easyinsight.service.service.requirement.TaskProcessService;
import com.netease.hz.bdms.easyinsight.service.service.terminalrelease.TerminalReleaseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: xumengqiang
 * @date: 2021/12/9 16:54
 */
@Slf4j
@Component
public class ObjectHelper {

    private static final String OID_PREFIX_PANEL = "panel_";
    private static final String OID_PREFIX_ELEMENT_MOD = "mod_";
    private static final String OID_PREFIX_ELEMENT_BTN = "btn_";
    private static final String OID_PREFIX_ELEMENT_CELL = "cell_";

    @Resource
    private ParamBindHelper paramBindHelper;

    @Resource
    private ObjTerminalTrackerService objTerminalTrackerService;

    @Resource
    private ObjectBasicService objectBasicService;

    @Resource
    private ImageRelationService imageRelationService;

    @Resource
    private ObjTagService objTagService;

    @Resource
    private ReqObjRelationService reqObjRelationService;

    @Resource
    private ObjTrackerEventService objTrackerEventService;

    @Resource
    private ParamBindService paramBindService;

    @Resource
    private ParamBindValueService paramBindValueService;

    @Resource
    private ObjectRelationHelper objectRelationHelper;

    @Resource
    private ObjChangeHistoryService objChangeHistoryService;

    @Resource
    private ReqObjRelationService objRelationService;

    @Resource
    private TagService tagService;

    @Resource
    private TerminalService terminalService;

    @Resource
    private EventService eventService;

    @Resource
    private ReqPoolRelBaseService reqPoolRelBaseService;

    @Resource
    private AllTrackerReleaseService allTrackerReleaseService;

    @Resource
    private LineageHelper lineageHelper;

    @Resource
    private ObjRelationReleaseService objRelationReleaseService;

    @Resource
    private TerminalReleaseService terminalReleaseService;

    @Resource
    private AppRelationService appRelationService;

    @Resource
    private TrackerContentService trackerContentService;

    @Resource
    private ReqSpmPoolService reqSpmPoolService;

    @Resource
    private TaskProcessService taskProcessService;


    public static final Long virtualRootNode = -12345L;

    public static final String whiteListStr = "page_rn,page_h5_biz,page_miniprogram";

    /**
     * 检查对象是否已经存在
     *
     * @param objOids 对象oid集合
     * @param objNames 对象name集合
     */
    public void checkObjExists(List<String> objOids, List<String> objNames, Integer type){
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "未指定产品信息");

        List<String> whiteList = Arrays.asList(whiteListStr.split(","));
        List<ObjectBasic> objBasicSimpleList1 = new ArrayList<>();
        if(type.equals(ObjTypeEnum.PAGE.getType()) && !whiteList.contains(objOids.get(0))) {
            Search search = new Search();
            search.setSearch(objOids.get(0));
            objBasicSimpleList1 = objectBasicService.searchLike(search);
            objBasicSimpleList1 = objBasicSimpleList1.stream()
                    .filter(o -> StringUtils.equals(search.getSearch(), o.getOid()))
                    .collect(Collectors.toList());
        }else {
            objBasicSimpleList1 = objectBasicService.getByLikeOid(appId, objOids.get(0));
        }
        List<ObjectBasic> objBasicSimpleList2 = objectBasicService.getByNames(appId, objNames);
        if(CollectionUtils.isNotEmpty(objBasicSimpleList1)){
            Set<String> oidExistingSet = objBasicSimpleList1.stream()
                    .map(ObjectBasic::getOid)
                    .collect(Collectors.toSet());
            throw new CommonException(String.format("oid={%s} 在空间appId={%d}已存在,请与数据负责人={%s}沟通", oidExistingSet, objBasicSimpleList1.get(0).getAppId(),objBasicSimpleList1.get(0).getCreateName()));
        }
        if(CollectionUtils.isNotEmpty(objBasicSimpleList2)){
            Set<String> nameExistingSet = objBasicSimpleList2.stream()
                    .map(ObjectBasic::getName)
                    .collect(Collectors.toSet());
            throw new CommonException(String.format("name={%s}已存在", nameExistingSet));
        }
    }

    /**
     * 查询对象是否在当前需求下已经进行过变更操作
     *
     * @param objId 对象ID
     * @param reqPoolId 需求ID
     * @return
     */
    public Boolean isChanged(Long objId, Long reqPoolId){
        // 参数检查
        Preconditions.checkArgument(null != objId, "对象ID不能为空");
        Preconditions.checkArgument(null != reqPoolId, "需求ID不能为空");

        // 查询
        List<EisObjChangeHistory> objChangeHistoryList = objChangeHistoryService
                .getByObjAndReqPoolId(objId, reqPoolId);

        return CollectionUtils.isNotEmpty(objChangeHistoryList);
    }


    /**
     * 创建对象埋点相关信息，包括父子关系、参数绑定信息、埋点信息
     *
     * @param objId         对象基本信息ID
     * @param objHistoryId  对象变更标识ID
     * @param trackersInfo  埋点信息
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public List<Long> createObjectTrackersInfo(Long objId, Long objHistoryId, Long reqPoolId,
                                               List<ObjectTrackerCreateParam> trackersInfo){
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "未指定产品信息");
        Preconditions.checkArgument(null != objId, "对象ID不能为空");
        Preconditions.checkArgument(null != objHistoryId, "对象变更ID不能为空");
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(trackersInfo), "对象埋点信息不能为空");

        // 依次插入埋点及其相关信息
        List<Long> trackerIds = Lists.newArrayList();
        for (ObjectTrackerCreateParam trackerInfo : trackersInfo) {
            // 1. 插入对象埋点信息 （新建对象不会出现环路，无需校验）
            EisObjTerminalTracker objTerminalTracker = BeanConvertUtils
                    .convert(trackerInfo, EisObjTerminalTracker.class);
            if(null == objTerminalTracker) {
                continue;
            }
            objTerminalTracker.setObjId(objId);
            objTerminalTracker.setObjHistoryId(objHistoryId);
            objTerminalTracker.setAppId(appId);
            objTerminalTracker.setReqPoolId(reqPoolId);
            objTerminalTracker.setTerminalReleaseId(0L);
            objTerminalTrackerService.insert(objTerminalTracker);
            Long trackerId = objTerminalTracker.getId();
            trackerIds.add(trackerId);
            // 2. 插入对象父子关系信息
            List<Long> parentsObjId = trackerInfo.getParentObjs();
            if(CollectionUtils.isEmpty(parentsObjId)){
                // 根节点，也需插入一条空记录
                parentsObjId = Lists.newArrayList((Long) null);
            }
            List<EisReqObjRelation> reqObjRelations = Lists.newArrayList();
            for(Long parentObjId: parentsObjId) {
                // 字段填充
                EisReqObjRelation reqObjRelation = new EisReqObjRelation();
                reqObjRelation.setObjId(objId);
                reqObjRelation.setReqPoolId(reqPoolId);
                reqObjRelation.setParentObjId(parentObjId);
                reqObjRelation.setTerminalId(trackerInfo.getTerminalId());
                reqObjRelation.setAppId(appId);
                // 加入列表
                reqObjRelations.add(reqObjRelation);
            }
            reqObjRelationService.insertBatch(reqObjRelations);

            // 3. 插入参数绑定信息(事件参数、对象私参)
            // 3.1 处理埋点上的关联事件信息，表`eis_obj_tracker_event`
            List<Long> eventIdList = trackerInfo.getEventIds();
            if(CollectionUtils.isEmpty(eventIdList)){
                // 新建、变更、编辑对象，需至少绑定一个事件
                throw new CommonException("请至少配置一个事件！");
            }
            Map<Long, Long> eventIdToParamVersionIdMap = Optional.ofNullable(
                    trackerInfo.getEventParamsVersionIdMap()).orElse(Maps.newHashMap());
            List<ObjTrackerEventSimpleDTO> eventSimpleDTOS = Lists.newArrayList();
            for (Long eventId : eventIdList) {
                // 构建 对象埋点关联事件记录
                ObjTrackerEventSimpleDTO eventSimpleDTO = new ObjTrackerEventSimpleDTO();
                Long eventParamVersionId = eventIdToParamVersionIdMap.getOrDefault(eventId, 0L);
                eventSimpleDTO.setEventId(eventId)
                        .setTrackerId(trackerId)
                        .setCreateTime(new Timestamp(System.currentTimeMillis()))
                        .setUpdateTime(new Timestamp(System.currentTimeMillis()))
                        .setEventParamVersionId(eventParamVersionId);
                // 加入列表
                eventSimpleDTOS.add(eventSimpleDTO);
            }
            objTrackerEventService.createTrackerEvents(eventSimpleDTOS);
            // 3.2 处理埋点上的对象私参信息
            List<ParamBindItermParam> paramItems = Optional.ofNullable(
                    trackerInfo.getParamBinds()).orElse(Lists.newArrayList());
            for(ParamBindItermParam paramItem: paramItems){

                ParamBindSimpleDTO paramBind = new ParamBindSimpleDTO();
                paramBind.setEntityId(trackerId)
                        .setAppId(appId)
                        .setEntityType(EntityTypeEnum.OBJTRACKER.getType())
                        .setParamId(paramItem.getParamId())
                        .setMust(paramItem.getMust())
                        .setNotEmpty(paramItem.getNotEmpty())
                        .setDescription(paramItem.getDescription())
                        .setIsEncode(paramItem.getIsEncode())
                        .setSource(paramItem.getSource())
                        .setSourceDetail(paramItem.getSourceDetail());
                Long bindId = paramBindService.createParamBind(paramBind);
                List<ParamBindValueSimpleDTO> paramBindValueList = Lists.newArrayList();
                for(Long valueId: paramItem.getValues()){
                    // 构建 实体绑定的参数 对应的取值信息
                    ParamBindValueSimpleDTO paramBindValue = new ParamBindValueSimpleDTO();
                    paramBindValue.setBindId(bindId)
                            .setParamValueId(valueId)
                            .setAppId(appId);
                    // 加入列表
                    paramBindValueList.add(paramBindValue);
                }
                paramBindValueService.createParamBindValue(paramBindValueList);
            }
            // 4. 更新额外的绑定在tracker上的内容
            trackerContentService.updateAll(trackerId, TrackerContentService.toTrackerContents(trackerId, trackerInfo.getTrackerContents()));
        }
        return trackerIds;
    }

    /**
     * 变更对象埋点相关信息（血缘关系信息、埋点基本信息、事件关联信息、对象私参信息）
     *
     * @param objId 对象ID
     * @param objHistoryId 对象历史变更ID
     * @param reqPoolId 需求组ID
     * @param trackersChangeInfo 对象变更后的埋点信息
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public List<Long> changeObjectTrackerInfo(Long objId, Long objHistoryId, Long reqPoolId,
                                              List<ObjectTrackerChangeParam> trackersChangeInfo){
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "未指定产品信息");
        Preconditions.checkArgument(null != objId, "对象ID不能为空");
        Preconditions.checkArgument(null != reqPoolId, "需求组ID不能为空");
        Preconditions.checkArgument(null != objHistoryId, "对象变更ID不能为空");
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(trackersChangeInfo), "对象埋点信息不能为空");

        // 依次插入埋点及其相关信息
        List<Long> trackerIds = Lists.newArrayList();
        for (ObjectTrackerChangeParam trackerChangeInfo : trackersChangeInfo) {
            // 1. 血缘关系的环路检测
            Long terminalId = trackerChangeInfo.getTerminalId();
            EisReqPoolRelBaseRelease reqPoolRelBaseRelease = reqPoolRelBaseService
                    .getCurrentUse(reqPoolId, terminalId);
            if (null == reqPoolRelBaseRelease) {
                log.warn("需求组reqPoolId={}在终端terminalId={}下无对应的基线发布版本！", reqPoolId, terminalId);
                throw new CommonException("当前需求组在对应终端下无基线发布版本！");
            }
            Long basedReleaseId = reqPoolRelBaseRelease.getBaseReleaseId();
            LinageGraph graph = lineageHelper.getTotalLineageGraph(basedReleaseId, terminalId, reqPoolId);
            List<Long> objParentsId = trackerChangeInfo.getParentObjs();
            if (objectRelationHelper.checkLoop(objId, objParentsId, graph.getParentsMap())) {
                log.error("对象ID为={}, 父对象为={}构成环路，变更失败！", objId, objParentsId);
                throw new CommonException("血缘关系出现环路，请检查父对象配置是否有误！");
            }

            // 2. 构建对象埋点信息
            EisObjTerminalTracker objTerminalTracker = new EisObjTerminalTracker();
            objTerminalTracker.setObjId(objId);
            objTerminalTracker.setTerminalId(terminalId);
            objTerminalTracker.setReqPoolId(reqPoolId);
            objTerminalTracker.setObjHistoryId(objHistoryId);
            objTerminalTracker.setTerminalReleaseId(basedReleaseId);
            objTerminalTracker.setPreTrackerId(trackerChangeInfo.getId());
            objTerminalTracker.setPubParamPackageId(trackerChangeInfo.getPubParamPackageId());
            objTerminalTrackerService.insert(objTerminalTracker);
            Long trackerId = objTerminalTracker.getId();
            trackerIds.add(trackerId);
            // 3. 构建对象血缘关系信息
            List<Long> parentsObjId = trackerChangeInfo.getParentObjs();
            if (CollectionUtils.isEmpty(parentsObjId)) {
                // 根节点，也需插入一条空记录
                parentsObjId = Lists.newArrayList((Long) null);
            }else if(parentsObjId.contains(virtualRootNode)) {
                // 挂在根结点，也需插入一条空记录
                parentsObjId.remove(virtualRootNode);
                parentsObjId.add(null);
            }
            List<EisReqObjRelation> reqObjRelations = Lists.newArrayList();
            for (Long parentObjId : parentsObjId) {
                // 字段填充
                EisReqObjRelation reqObjRelation = new EisReqObjRelation();
                reqObjRelation.setObjId(objId);
                reqObjRelation.setReqPoolId(reqPoolId);
                reqObjRelation.setParentObjId(parentObjId);
                reqObjRelation.setTerminalId(trackerChangeInfo.getTerminalId());
                reqObjRelation.setAppId(appId);
                // 加入列表
                reqObjRelations.add(reqObjRelation);
            }
            reqObjRelationService.insertBatch(reqObjRelations);

            // 4. 插入参数绑定信息(事件参数、对象私参)
            // 4.1 处理埋点上的关联事件信息，表`eis_obj_tracker_event`
            List<ObjTrackerEventSimpleDTO> eventSimpleDTOS = Lists.newArrayList();
            List<Long> eventIdList = trackerChangeInfo.getEventIds();
            if (CollectionUtils.isEmpty(eventIdList)) {
                throw new CommonException("请至少配置一个事件！");
            }
            Map<Long, Long> eventIdToParamVersionIdMap = Optional.ofNullable(
                    trackerChangeInfo.getEventParamVersionIdMap()).orElse(Maps.newHashMap());
            for (Long eventId : eventIdList) {
                // 构建 对象埋点关联事件记录
                ObjTrackerEventSimpleDTO eventSimpleDTO = new ObjTrackerEventSimpleDTO();
                Long eventParamVersionId = eventIdToParamVersionIdMap.getOrDefault(eventId, 0L);
                eventSimpleDTO.setEventId(eventId)
                        .setTrackerId(trackerId)
                        .setCreateTime(new Timestamp(System.currentTimeMillis()))
                        .setUpdateTime(new Timestamp(System.currentTimeMillis()))
                        .setEventParamVersionId(eventParamVersionId);

                // 加入列表
                eventSimpleDTOS.add(eventSimpleDTO);
            }
            objTrackerEventService.createTrackerEvents(eventSimpleDTOS);
            // 4.2 处理埋点上的对象私参信息
            List<ParamBindItermParam> paramItems = Optional.ofNullable(
                    trackerChangeInfo.getParamBinds()).orElse(Lists.newArrayList());
            for (ParamBindItermParam paramItem : paramItems) {
                ParamBindSimpleDTO paramBind = new ParamBindSimpleDTO();
                paramBind.setEntityId(trackerId)
                        .setAppId(appId)
                        .setEntityType(EntityTypeEnum.OBJTRACKER.getType())
                        .setParamId(paramItem.getParamId())
                        .setMust(paramItem.getMust())
                        .setNotEmpty(paramItem.getNotEmpty())
                        .setDescription(paramItem.getDescription())
                        .setIsEncode(paramItem.getIsEncode());
                Long bindId = paramBindService.createParamBind(paramBind);
                List<ParamBindValueSimpleDTO> paramBindValueList = Lists.newArrayList();
                for (Long valueId : paramItem.getValues()) {
                    // 构建 实体绑定的参数 对应的取值信息
                    ParamBindValueSimpleDTO paramBindValue = new ParamBindValueSimpleDTO();
                    paramBindValue.setBindId(bindId)
                            .setParamValueId(valueId);
                    // 加入列表
                    paramBindValueList.add(paramBindValue);
                }
                paramBindValueService.createParamBindValue(paramBindValueList);
            }
            // 5. 更新额外的绑定在tracker上的内容
            trackerContentService.updateAll(trackerId, TrackerContentService.toTrackerContents(trackerId, trackerChangeInfo.getTrackerContents()));
        }
        return trackerIds;
    }

    /**
     * 更新对象埋点相关信息（先删后增）
     *
     * @param objId 对象ID
     * @param objHistoryId 对象变更历史ID
     * @param reqPoolId 需求ID
     * @param trackersEditInfo 更新后的埋点信息
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public List<Long> editObjectTrackerInfo(Long objId, Long objHistoryId, Long reqPoolId,
                                            List<ObjectTrackerEditParam> trackersEditInfo){
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "未指定产品信息");
        Preconditions.checkArgument(null != objId, "对象ID不能为空");
        Preconditions.checkArgument(null != objHistoryId, "对象变更ID不能为空");
        if (CollectionUtils.isEmpty(trackersEditInfo)) {
            throw new CommonException("对象埋点信息不能为空");
        }

        // 1. 查询先前的所有埋点信息 及 编辑后保留的埋点信息
        EisObjTerminalTracker trackerQuery = new EisObjTerminalTracker();
        trackerQuery.setObjId(objId);
        trackerQuery.setObjHistoryId(objHistoryId);
        List<EisObjTerminalTracker> formerTrackerList = objTerminalTrackerService.search(trackerQuery);

        Set<Long> reservedTrackerIds = trackersEditInfo.stream()
                .map(ObjectTrackerEditParam::getId).collect(Collectors.toSet());

        // 2. 删除所有埋点的关联信息
        for (EisObjTerminalTracker objTerminalTracker : formerTrackerList) {
            Long trackerId = objTerminalTracker.getId();
            Long terminalId = objTerminalTracker.getTerminalId();
            // 2.1 删除埋点信息
            if(!reservedTrackerIds.contains(trackerId)){
                objTerminalTrackerService.deleteById(trackerId);
                EisReqPoolSpm spmPoolQuery = new EisReqPoolSpm();
                spmPoolQuery.setReqPoolId(reqPoolId);
                spmPoolQuery.setObjId(objId);
                spmPoolQuery.setObjHistoryId(objHistoryId);
                spmPoolQuery.setTerminalId(terminalId);
                List<EisReqPoolSpm> reqPoolSpms = reqSpmPoolService.search(spmPoolQuery);
                if(CollectionUtils.isNotEmpty(reqPoolSpms)){
                    reqSpmPoolService.deleteByIds(reqPoolSpms.stream().map(EisReqPoolSpm::getId).collect(Collectors.toSet()));
                }
                Set<Long> entityIds = reqPoolSpms.stream().map(EisReqPoolSpm::getId).collect(Collectors.toSet());
                if(CollectionUtils.isNotEmpty(entityIds)) {
                    taskProcessService.deleteUnReleasedProcessesByReqPoolEntityIds(ReqPoolTypeEnum.SPM_DEV, entityIds);
                }
            }
            // 2.2 删除血缘关系信息
            reqObjRelationService.delete(objId, reqPoolId, terminalId);
            // 2.3 删除埋点关联的事件信息
            objTrackerEventService.deleteEventByTrackerId(Lists.newArrayList(trackerId));
            // 2.4 删除原参数绑定及其取值信息
            List<Long> paramBindIds = paramBindService.getParamBindIdsByEntityIds(
                    Collections.singletonList(trackerId), EntityTypeEnum.OBJTRACKER.getType(), 0L, appId);
            paramBindService.deleteByIds(Sets.newHashSet(paramBindIds));
            paramBindValueService.deleteByBindIds(paramBindIds);

        }
        // 3. 更新部分埋点信息及其关联信息
        List<Long> trackerIdList = Lists.newArrayList();
        for (ObjectTrackerEditParam trackerEditInfo : trackersEditInfo) {

            Long terminalId = trackerEditInfo.getTerminalId();
            // 3.1 血缘关系的环路检测
            EisReqPoolRelBaseRelease reqPoolRelBaseRelease = reqPoolRelBaseService
                    .getCurrentUse(reqPoolId, terminalId);
            if(null == reqPoolRelBaseRelease){
                log.warn("需求组reqPoolId={}在终端terminalId={}下无对应的基线发布版本！", reqPoolId, terminalId);
                continue;
            }
            Long basedReleaseId = reqPoolRelBaseRelease.getBaseReleaseId();
            LinageGraph graph = lineageHelper.getTotalLineageGraph(basedReleaseId, terminalId, reqPoolId);
            List<Long> objParentsId = trackerEditInfo.getParentObjs();
            if(objectRelationHelper.checkLoop(objId, objParentsId, graph.getParentsMap())){
                log.error("对象ID为={}, 父对象为={}出现构成环路，变更失败！", objId, objParentsId);
                throw new CommonException("血缘关系出现环路，请检查父对象配置是否有误！");
            }

            // 3.2 更新/新建 对象埋点信息
            Long trackerId = trackerEditInfo.getId();
            EisObjTerminalTracker objTerminalTracker = new EisObjTerminalTracker();
            if(null != trackerId) {
                // 更新埋点
                objTerminalTracker.setId(trackerId);
                objTerminalTracker.setPubParamPackageId(trackerEditInfo.getPubParamPackageId());
                objTerminalTrackerService.update(objTerminalTracker);
            }else {
                // 新建埋点
                objTerminalTracker.setObjId(objId);
                objTerminalTracker.setTerminalId(terminalId);
                objTerminalTracker.setReqPoolId(reqPoolId);
                objTerminalTracker.setObjHistoryId(objHistoryId);
                objTerminalTracker.setTerminalReleaseId(basedReleaseId);
                objTerminalTracker.setPreTrackerId(0L);
                objTerminalTracker.setPubParamPackageId(trackerEditInfo.getPubParamPackageId());
                objTerminalTrackerService.insert(objTerminalTracker);
                trackerId = objTerminalTracker.getId();
            }
            trackerIdList.add(trackerId);

            // 3.3 更新对象血缘关系信息
            List<Long> parentsObjId = trackerEditInfo.getParentObjs();
            if(CollectionUtils.isEmpty(parentsObjId)){
                // 根节点，父对象要设置为null
                parentsObjId = Lists.newArrayList((Long)null);
            }else if(parentsObjId.contains(virtualRootNode)) {
                // 挂在根结点，也需插入一条空记录
                parentsObjId.remove(virtualRootNode);
                parentsObjId.add(null);
            }
            List<EisReqObjRelation> reqObjRelations = Lists.newArrayList();
            for(Long parentObjId: parentsObjId) {
                // 字段填充
                EisReqObjRelation reqObjRelation = new EisReqObjRelation();
                reqObjRelation.setObjId(objId);
                reqObjRelation.setReqPoolId(reqPoolId);
                reqObjRelation.setParentObjId(parentObjId);
                reqObjRelation.setTerminalId(terminalId);
                reqObjRelation.setAppId(appId);
                // 加入列表
                reqObjRelations.add(reqObjRelation);
            }
            reqObjRelationService.insertBatch(reqObjRelations);

            // 3.4 处理埋点上的关联事件信息，表`eis_obj_tracker_event`
            List<ObjTrackerEventSimpleDTO> eventSimpleDTOS = Lists.newArrayList();
            List<Long> eventIdList = trackerEditInfo.getEventIds();
            if(CollectionUtils.isEmpty(eventIdList)){
                throw new CommonException("请至少配置一个事件！");
            }
            Map<Long, Long> eventIdToParamVersionIdMap = Optional.ofNullable(
                    trackerEditInfo.getEventParamVersionIdMap()).orElse(Maps.newHashMap());
            for (Long eventId : eventIdList) {
                // 构建 对象埋点关联事件记录
                ObjTrackerEventSimpleDTO eventSimpleDTO = new ObjTrackerEventSimpleDTO();
                Long eventParamVersionId = eventIdToParamVersionIdMap.getOrDefault(eventId, 0L);
                eventSimpleDTO.setEventId(eventId)
                        .setTrackerId(trackerId)
                        .setCreateTime(new Timestamp(System.currentTimeMillis()))
                        .setUpdateTime(new Timestamp(System.currentTimeMillis()))
                        .setEventParamVersionId(eventParamVersionId);
                // 加入列表
                eventSimpleDTOS.add(eventSimpleDTO);
            }
            objTrackerEventService.createTrackerEvents(eventSimpleDTOS);

            // 3.5 处理埋点上的对象私参信息
            List<ParamBindItermParam> paramItems = Optional.ofNullable(
                    trackerEditInfo.getParamBinds()).orElse(Lists.newArrayList());
            for (ParamBindItermParam paramItem : paramItems) {
                ParamBindSimpleDTO paramBind = new ParamBindSimpleDTO();
                paramBind.setEntityId(trackerId)
                        .setAppId(appId)
                        .setEntityType(EntityTypeEnum.OBJTRACKER.getType())
                        .setParamId(paramItem.getParamId())
                        .setMust(paramItem.getMust())
                        .setNotEmpty(paramItem.getNotEmpty())
                        .setDescription(paramItem.getDescription())
                        .setIsEncode(paramItem.getIsEncode())
                        .setSource(paramItem.getSource())
                        .setSourceDetail(paramItem.getSourceDetail());
                Long bindId = paramBindService.createParamBind(paramBind);

                List<ParamBindValueSimpleDTO> paramBindValueList = Lists.newArrayList();
                for (Long valueId : paramItem.getValues()) {
                    // 构建 实体绑定的参数 对应的取值信息
                    ParamBindValueSimpleDTO paramBindValue = new ParamBindValueSimpleDTO();
                    paramBindValue.setBindId(bindId)
                            .setParamValueId(valueId)
                            .setAppId(appId);
                    // 加入列表
                    paramBindValueList.add(paramBindValue);
                }
                paramBindValueService.createParamBindValue(paramBindValueList);
            }

            // 4. 更新额外的绑定在tracker上的内容
            trackerContentService.updateAll(trackerId, TrackerContentService.toTrackerContents(trackerId, trackerEditInfo.getTrackerContents()));
        }
        return trackerIdList;
    }

    /**
     * 删除对象副本，即删除某需求下某对象的【新建/变更操作】涉及的相关信息
     * 包括对象的关联图片信息、对象关联标签信息、埋点信息、埋点关联事件信息、对象私参信息、对象血缘关系、对象新建/变更记录
     *
     * @param objId 对象ID
     * @param reqPoolId 需求ID
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void deleteObjectDuplication(Long objId, Long reqPoolId){
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "产品ID不能为空");
        Preconditions.checkArgument(null != objId, "对象ID不能为空！");
        Preconditions.checkArgument(null != reqPoolId, "需求组ID不能为空");
        // 检查对象有无该副本
        List<EisObjChangeHistory> objChangeHistoryList = objChangeHistoryService.getByObjAndReqPoolId(objId, reqPoolId);
        if(objChangeHistoryList.size() != 1){
            log.warn("objId为{}的对象在reqPoolId为{}的需求组下无副本信息或存在多个副本信息！", objId, reqPoolId);
            throw new CommonException("对象在当前需求下无副本信息或存在多个副本信息，删除操作失败！");
        }
        EisObjChangeHistory objChangeHistory = objChangeHistoryList.get(0);
        Long objChangeHistoryId = objChangeHistory.getId();
        Integer operationType = objChangeHistory.getType();
        // 如果是新建对象，需要检测是否存在引用关系
        if(OperationTypeEnum.CREATE.getOperationType().equals(operationType)){
            EisReqObjRelation queryRelation = new EisReqObjRelation();
            queryRelation.setReqPoolId(reqPoolId);
            queryRelation.setParentObjId(objId);
            queryRelation.setAppId(appId);
            List<EisReqObjRelation> reqObjRelations = reqObjRelationService.search(queryRelation);
            reqObjRelations = reqObjRelations.stream().filter(eisReqObjRelation -> eisReqObjRelation.getParentObjId() != null).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(reqObjRelations)){
                ObjectBasic byId = objectBasicService.getById(objId);
                List<Long> sonIds = reqObjRelations.stream().map(EisReqObjRelation::getObjId).collect(Collectors.toList());
                List<ObjectBasic> sons = objectBasicService.getByIds(sonIds);
                List<String> sonOids = CollectionUtils.isEmpty(sons) ? new ArrayList<>() : sons.stream().map(ObjectBasic::getOid).collect(Collectors.toList());
                log.warn("新建对象objId={} {}存在引用关系{}，无法删除！", objId, byId == null ? null : byId.getOid(), JsonUtils.toJson(sonOids));
                throw new CommonException(String.format("新建对象 objId={%d} oid={%s}存在引用关系：%s，无法删除！", objId, byId == null ? null : byId.getOid(), JsonUtils.toJson(sonOids)));
            }
            // 对象是首次新建，则一并删除对象元信息
            objectBasicService.deleteByIds(Collections.singletonList(objId));
        }
        // 1. 删除对象关联的图片信息
        imageRelationService.deleteImageRelation(Collections.singletonList(objChangeHistoryId),
                EntityTypeEnum.OBJHISTORY.getType());

        // 2. 删除对象埋点信息 (先查询后删除)
        EisObjTerminalTracker queryTracker = new EisObjTerminalTracker();
        queryTracker.setObjHistoryId(objChangeHistoryId);
        queryTracker.setReqPoolId(reqPoolId);
        queryTracker.setObjId(objId);
        queryTracker.setAppId(appId);
        List<EisObjTerminalTracker> objTerminalTrackers = objTerminalTrackerService.search(queryTracker);
        List<Long> trackerIds = objTerminalTrackers.stream()
                .map(EisObjTerminalTracker::getId)
                .collect(Collectors.toList());
        objTerminalTrackerService.deleteByIds(trackerIds);

        // 3. 删除埋点所关联的事件
        objTrackerEventService.deleteEventByTrackerId(trackerIds);

        // 4. 删除对象绑定私参及其取值信息 (先查询后删除)
        List<ParamBindSimpleDTO> paramBindItemDTOS = paramBindService.getByEntityIds(
                trackerIds, Lists.newArrayList(EntityTypeEnum.OBJTRACKER.getType()), 0L, appId);
        Set<Long> paramBindIds = paramBindItemDTOS.stream()
                .map(ParamBindSimpleDTO::getParamId)
                .collect(Collectors.toSet());
        paramBindService.deleteByIds(paramBindIds);
        List<ParamBindValueSimpleDTO> paramBindValueSimpleDTOS = paramBindValueService
                .getByBindIds(paramBindIds);
        List<Long> paramBindValueIds = paramBindValueSimpleDTOS.stream()
                .map(ParamBindValueSimpleDTO::getId)
                .collect(Collectors.toList());
        paramBindValueService.deleteByBindIds(paramBindValueIds);

        // 5. 删除埋点血缘关系 (先查询后删除)
        EisReqObjRelation queryRelation = new EisReqObjRelation();
        queryRelation.setReqPoolId(reqPoolId);
        queryRelation.setObjId(objId);
        queryRelation.setAppId(appId);
        List<EisReqObjRelation> reqObjRelations = reqObjRelationService.search(queryRelation);
        List<Long> objRelationIds = reqObjRelations.stream()
                .map(EisReqObjRelation::getId)
                .collect(Collectors.toList());
        reqObjRelationService.deleteByIds(objRelationIds);

        // 6. 删除对象 新建/变更 记录
        objChangeHistoryService.deleteByIds(Lists.newArrayList(objChangeHistoryId));
    }

    /**
     * 获取对象基本信息
     *
     * @param objId 对象ID
     * @param objHistoryId 需求ID
     */
    public ObjectInfoDTO getObjectBasicInfo(Long objId, Long objHistoryId){
        // 参数检查
        Preconditions.checkArgument(null != objId, "对象ID不能为空");
        Preconditions.checkArgument(null != objHistoryId, "对象变更历史ID不能为空");

        // 1. 获取对象元信息
        ObjectBasic objectBasic = objectBasicService.getById(objId);
        if(null == objectBasic){
            log.warn("未查询到id为{}的对象基本信息！", objId);
            throw new ObjException("未查询到对象的基本信息！");
        }
        ObjectInfoDTO objectBasicInfo = convertToDTO(objectBasic);
        if(null == objectBasicInfo){
            log.warn("对象基本信息转化失败");
            throw new ObjException("对象基本信息转化失败！");
        }

        // 2. 查询对象变更历史信息
        EisObjChangeHistory objChangeHistory = objChangeHistoryService.getById(objHistoryId);
        objectBasicInfo.setHistoryId(objHistoryId);
        objectBasicInfo.setConsistency(objChangeHistory.getConsistency());

        // 3. 获取关联图片信息
        List<ImageRelationDTO> imageRelationDTOS = imageRelationService
                .getByEntityId(Lists.newArrayList(objHistoryId));
        List<String> imgUrls = imageRelationDTOS.stream()
                .map(ImageRelationDTO::getUrl).collect(Collectors.toList());
        objectBasicInfo.setImgUrls(imgUrls);

        // 4. 获取关联标签信息
        List<ObjTagSimpleDTO> objTagSimpleDTOS = objTagService
                .getByObjIds(Sets.newHashSet(objId));
        List<Long> tagIds = objTagSimpleDTOS.stream()
                .map(ObjTagSimpleDTO::getTagId).collect(Collectors.toList());
        List<TagSimpleDTO> tagSimpleDTOS = tagService.getByIds(tagIds);
        objectBasicInfo.setTags(tagSimpleDTOS);

        return objectBasicInfo;
    }


    /**
     * 查询对象关联的埋点信息
     *
     * @param objId 对象ID
     * @param objHistoryId 对象变更历史ID
     */
    public List<ObjectTrackerInfoDTO> getObjTrackersInfo(Long objId, Long objHistoryId, boolean forChange){
        // 获取需求ID
        EisObjChangeHistory objChangeHistory = objChangeHistoryService.getById(objHistoryId);
        if(null == objChangeHistory){
            log.warn("未查询到objId={}的对象对应的historyId={}变更历史信息！", objId, objHistoryId);
            throw new CommonException("未查询到对象变更历史信息！");
        }
        Long reqPoolId = objChangeHistory.getReqPoolId();
        // 获取对象关联埋点信息
        EisObjTerminalTracker queryTracker = new EisObjTerminalTracker();
        queryTracker.setReqPoolId(reqPoolId);
        queryTracker.setObjHistoryId(objHistoryId);
        queryTracker.setObjId(objId);
        List<EisObjTerminalTracker> objTerminalTrackerList = objTerminalTrackerService.search(queryTracker);

        List<ObjectTrackerInfoDTO> results = Lists.newArrayList();
        Set<Long> terminalIds = objTerminalTrackerList.stream()
                .map(EisObjTerminalTracker::getTerminalId).collect(Collectors.toSet());
        // 依次构建埋点及其相关信息
        for (EisObjTerminalTracker objTracker : objTerminalTrackerList) {
            ObjectTrackerInfoDTO objectTrackerInfoDTO = new ObjectTrackerInfoDTO();
            Long trackerId  = objTracker.getId();
            Long appId = objTracker.getAppId();
            // 设置基本信息
            objectTrackerInfoDTO.setId(trackerId);
            objectTrackerInfoDTO.setPreTrackerId(objTracker.getPreTrackerId());
            objectTrackerInfoDTO.setPubParamPackageId(objTracker.getPubParamPackageId());

            // 终端信息
            Long terminalId = objTracker.getTerminalId();
            TerminalSimpleDTO terminalSimpleDTO = terminalService.getById(terminalId);
            objectTrackerInfoDTO.setTerminal(terminalSimpleDTO);

            // 父对象集合
            List<ObjectBasicDTO> parentObjects;
            if(forChange){
                parentObjects = this.getObjectLatestParents(objId, terminalIds);
            }else {
                parentObjects = this.getObjectParentsByReqPooId(objId, reqPoolId, terminalId);
            }
            objectTrackerInfoDTO.setParentObjects(parentObjects);
            // 埋点关联的私参信息
            List<ParamBindItemDTO> privateParams = paramBindHelper.getParamBinds(
                    appId, trackerId, EntityTypeEnum.OBJTRACKER.getType(), 0L);
            objectTrackerInfoDTO.setPrivateParam(privateParams);

            // 埋点关联的事件信息
            List<ObjTrackerEventSimpleDTO> objTrackerEventSimpleDTOS = objTrackerEventService
                    .getByTrackerId(Collections.singletonList(trackerId));
            List<Long> eventIds = objTrackerEventSimpleDTOS.stream()
                    .map(ObjTrackerEventSimpleDTO::getEventId)
                    .collect(Collectors.toList());
            List<EventSimpleDTO> eventSimpleDTOS = eventService.getEventByIds(eventIds);
            Map<Long, Long> eventIdToParamPackageId = objTrackerEventSimpleDTOS.stream()
                    .collect(Collectors.toMap(ObjTrackerEventSimpleDTO::getEventId,
                            ObjTrackerEventSimpleDTO::getEventParamVersionId));
            objectTrackerInfoDTO.setEventParamVersionIdMap(eventIdToParamPackageId);
            objectTrackerInfoDTO.setEvents(eventSimpleDTOS);
            Map<String, String> trackerContents = TrackerContentService.fromTrackerContents(trackerContentService.getAllByTrackerId(trackerId));
            objectTrackerInfoDTO.setTrackerContents(trackerContents);
            // 加入列表
            results.add(objectTrackerInfoDTO);
        }
        return results;
    }

    /**
     * 查询trackerId
     */
    public Long getObjTrackerId(Long objId, Long terminalId, Long objHistoryId){
        // 获取对象关联埋点信息
        EisObjTerminalTracker queryTracker = new EisObjTerminalTracker();
        queryTracker.setObjHistoryId(objHistoryId);
        queryTracker.setObjId(objId);
        queryTracker.setTerminalId(terminalId);
        List<EisObjTerminalTracker> objTerminalTrackerList = objTerminalTrackerService.search(queryTracker);
        if (CollectionUtils.isEmpty(objTerminalTrackerList)) {
            throw new CommonException("未查询到对象历史埋点信息！");
        }
        EisObjTerminalTracker objTracker = objTerminalTrackerList.get(0);
        return objTracker.getId();
    }

    /**
     * 查询对象关联的埋点信息
     *
     * @param objId 对象ID
     * @param trackerId 对象trackerId
     */
    public ObjectTrackerInfoDTO getObjTrackersInfo(Long objId, Long trackerId){
        if (trackerId == null || objId == null) {
            return null;
        }
        EisObjTerminalTracker objTracker = objTerminalTrackerService.getById(trackerId);
        if (objTracker == null) {
            return null;
        }
        Set<Long> terminalIds = new HashSet<>();
        terminalIds.add(objTracker.getTerminalId());
        // 依次构建埋点及其相关信息
        ObjectTrackerInfoDTO objectTrackerInfoDTO = new ObjectTrackerInfoDTO();
        Long appId = objTracker.getAppId();
        // 设置基本信息
        objectTrackerInfoDTO.setId(trackerId);
        objectTrackerInfoDTO.setPreTrackerId(objTracker.getPreTrackerId());
        objectTrackerInfoDTO.setPubParamPackageId(objTracker.getPubParamPackageId());

        // 终端信息
        Long terminalId = objTracker.getTerminalId();
        TerminalSimpleDTO terminalSimpleDTO = terminalService.getById(terminalId);
        objectTrackerInfoDTO.setTerminal(terminalSimpleDTO);

        // 父对象集合
        List<ObjectBasicDTO> parentObjects = this.getObjectLatestParents(objId, terminalIds);
        objectTrackerInfoDTO.setParentObjects(parentObjects);
        // 埋点关联的私参信息
        List<ParamBindItemDTO> privateParams = paramBindHelper.getParamBinds(
                appId, trackerId, EntityTypeEnum.OBJTRACKER.getType(), 0L);
        objectTrackerInfoDTO.setPrivateParam(privateParams);

        // 埋点关联的事件信息
        List<ObjTrackerEventSimpleDTO> objTrackerEventSimpleDTOS = objTrackerEventService
                .getByTrackerId(Collections.singletonList(trackerId));
        List<Long> eventIds = objTrackerEventSimpleDTOS.stream()
                .map(ObjTrackerEventSimpleDTO::getEventId)
                .collect(Collectors.toList());
        List<EventSimpleDTO> eventSimpleDTOS = eventService.getEventByIds(eventIds);
        Map<Long, Long> eventIdToParamPackageId = objTrackerEventSimpleDTOS.stream()
                .collect(Collectors.toMap(ObjTrackerEventSimpleDTO::getEventId,
                        ObjTrackerEventSimpleDTO::getEventParamVersionId));
        objectTrackerInfoDTO.setEventParamVersionIdMap(eventIdToParamPackageId);
        objectTrackerInfoDTO.setEvents(eventSimpleDTOS);
        return objectTrackerInfoDTO;
    }

    /**
     * 获取对象关联的父对象集合
     *
     * @param objId 对象ID
     * @param reqPoolId 需求组ID
     * @param terminalId 终端ID
     * @return
     */
    public List<ObjectBasicDTO> getObjectParentsByReqPooId(Long objId, Long reqPoolId, Long terminalId){
        // 参数检查
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "未指定产品信息");
        Preconditions.checkArgument(null != objId, "对象ID不能为空");
        Preconditions.checkArgument(null != reqPoolId, "需求ID不能为空");
        Preconditions.checkArgument(null != terminalId, "终端ID不能为空");
        // 1. 查询表`eis_obj_req_relation`获取父对象ID集合
        EisReqObjRelation queryObjRelation = new EisReqObjRelation();
        queryObjRelation.setObjId(objId);
        queryObjRelation.setReqPoolId(reqPoolId);
        queryObjRelation.setTerminalId(terminalId);
        queryObjRelation.setAppId(appId);
        List<EisReqObjRelation> objRelationList = objRelationService.search(queryObjRelation);
        List<Long> parentObjectIds = objRelationList.stream()
                .map(EisReqObjRelation::getParentObjId)
                .collect(Collectors.toList());
        // 2. 查询父对象信息
        List<ObjectBasic> parentObjects = objectBasicService.getByIds(parentObjectIds);
        if(parentObjectIds.size() >= 2 && parentObjectIds.contains(null)){
            // 插入虚拟根结点
            ObjectBasic objectBasic = new ObjectBasic();
            objectBasic.setId(ObjectHelper.virtualRootNode);
            objectBasic.setOid("virtual_root");
            objectBasic.setName("虚拟根节点");
            parentObjects.add(0, objectBasic);
        }

        // 数据转化
        List<ObjectBasicDTO> result = Lists.newArrayList();
        for (ObjectBasic parentObject : parentObjects) {
            ObjectBasicDTO objectBasicDTO = BeanConvertUtils.convert(parentObject, ObjectBasicDTO.class);
            if(null != objectBasicDTO) {
                result.add(objectBasicDTO);
            }
        }
        return result;
    }

    /**
     * 获取对象最新已上线的父对象集合(不同端下取交集)
     *
     * @param objId 对象ID
     * @param terminalIds 终端ID集合
     * @return
     */
    public List<ObjectBasicDTO> getObjectLatestParents(Long objId, Set<Long> terminalIds){
        // 参数检查
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "未指定产品信息");
        Preconditions.checkArgument(null != objId, "对象ID不能为空");
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(terminalIds), "终端ID集合不能为空");

        // 1. 获取不同端下最新父对象集合
        List<Set<Long>> parentIds = new ArrayList<>();
        for (Long terminalId : terminalIds) {
            // 获取指定端下的最新发布版本ID
            EisTerminalReleaseHistory terminalRelease = terminalReleaseService.getLatestRelease(terminalId);
            Long terminalReleaseId = terminalRelease.getId();

            // 从表`eis_obj_all_relation_release`获取目标对象的最新父子关系
            EisObjAllRelationRelease query = new EisObjAllRelationRelease();
            query.setObjId(objId);
            query.setAppId(appId);
            query.setTerminalReleaseId(terminalReleaseId);
            List<EisObjAllRelationRelease> objAllRelationReleaseList = objRelationReleaseService.search(query);
            Set<Long> currParentIds = objAllRelationReleaseList.stream()
                    .map(EisObjAllRelationRelease::getParentObjId)
                    .collect(Collectors.toSet());
            parentIds.add(currParentIds);
        }
        // 2. 求取交集
        Set<Long> commonParentIds = parentIds.stream().reduce((s1, s2) ->{
            s1.retainAll(s2);
            return s1;
        }).orElse(Collections.emptySet());

        // 3. 查询父对象信息
        List<ObjectBasic> parentObjects = objectBasicService.getByIds(commonParentIds);

        // 数据转化
        List<ObjectBasicDTO> result = Lists.newArrayList();
        for (ObjectBasic parentObject : parentObjects) {
            ObjectBasicDTO objectBasicDTO = BeanConvertUtils.convert(parentObject, ObjectBasicDTO.class);
            if(null != objectBasicDTO) {
                result.add(objectBasicDTO);
            }
        }
        return result;
    }

    public Map<Long, List<EisObjTerminalTracker>> getTrackersOfReqPoolBases(Set<Long> baseReleaseIds){
        List<EisAllTrackerRelease> trackersOfBase = allTrackerReleaseService.getByReleaseIds(baseReleaseIds);
        Map<Long, List<Long>> trackerIdToBaseReleaseId = new HashMap<>();
        trackersOfBase.forEach(t -> {
            List<Long> l = trackerIdToBaseReleaseId.computeIfAbsent(t.getTrackerId(), k -> new ArrayList<>());
            l.add(t.getTerminalReleaseId());
        });

        Set<Long> trackerIds = trackersOfBase.stream().map(EisAllTrackerRelease::getTrackerId).collect(Collectors.toSet());
        List<EisObjTerminalTracker> trackers = objTerminalTrackerService.getByIds(trackerIds);
        Map<Long, List<EisObjTerminalTracker>> result = new HashMap<>();
        trackers.forEach(t -> {
            Long trackerId = t.getId();
            List<Long> baseReleaseIdsOfTrackerId = trackerIdToBaseReleaseId.get(trackerId);
            if (baseReleaseIdsOfTrackerId != null) {
                baseReleaseIdsOfTrackerId.forEach(b -> {
                    List<EisObjTerminalTracker> resultOfBaseReleaseId = result.computeIfAbsent(b, k -> new ArrayList<>());
                    resultOfBaseReleaseId.add(t);
                });
            }
        });
        return result;
    }


    public List<EisObjTerminalTracker> getTrackersOfReqPoolBase(Long baseReleaseId){
        EisAllTrackerRelease allTrackerReleaseQuery = new EisAllTrackerRelease();
        allTrackerReleaseQuery.setTerminalReleaseId(baseReleaseId);
        List<EisAllTrackerRelease> trackersOfBase = allTrackerReleaseService.search(allTrackerReleaseQuery);
        Set<Long> trackerIds = trackersOfBase.stream().map(e -> e.getTrackerId()).collect(Collectors.toSet());
        List<EisObjTerminalTracker> trackers = objTerminalTrackerService.getByIds(trackerIds);
        return trackers;
    }

    /**
     * 检测oid是否符合type命名规范
     */
    public void checkOidByType(String oid, Integer type) {
        if (!StringUtils.equals(oid, StringUtils.lowerCase(oid))) {
            throw new CommonException("oid命名不支持大写字母");
        }
        if (ObjTypeEnum.POPOVER.getType().equals(type)) {
            if (!StringUtils.startsWith(oid, OID_PREFIX_PANEL)) {
                throw new CommonException("浮层对象oid命名需要以'" + OID_PREFIX_PANEL + "'开头");
            }
        }
        if (ObjTypeEnum.ELEMENT.getType().equals(type)) {
            boolean valid = StringUtils.startsWith(oid, OID_PREFIX_ELEMENT_MOD) || StringUtils.startsWith(oid, OID_PREFIX_ELEMENT_CELL) || StringUtils.startsWith(oid, OID_PREFIX_ELEMENT_BTN);
            if (!valid) {
                throw new CommonException("元素对象oid命名必须为以下前缀" + JsonUtils.toJson(Arrays.asList(OID_PREFIX_ELEMENT_MOD, OID_PREFIX_ELEMENT_CELL, OID_PREFIX_ELEMENT_BTN)));
            }
        }
    }

    public void checkBridge(Long subAppId, Long subTerminalId, String specialType) {
        if (!ObjSpecialTypeEnum.BRIDGE.getName().equals(specialType)) {
            return;
        }
        if (subAppId == null) {
            throw new CommonException("桥梁未指定子空间appId");
        }
        Long appId = EtContext.get(ContextConstant.APP_ID);
        if (appId == null) {
            throw new CommonException("当前空间appId获取失败，请在请求中指定appId");
        }
        // 挂载同名端
        if (subTerminalId == null || subTerminalId.equals(0L)) {
            if (appId.equals(subAppId)) {
                throw new CommonException("挂载同名端时，只支持挂载其他空间的同名端");
            }
            appRelationService.ensureRelationExist(appId, subAppId);
            return;
        }
        // 挂载指定端(subTerminalId > 0)
        TerminalSimpleDTO subTerminal = terminalService.getById(subTerminalId);
        if (subTerminal == null) {
            throw new CommonException("参数错误，挂载子端不存在");
        }
        if (!subTerminal.getAppId().equals(subAppId)) {
            throw new CommonException("参数错误，挂载子空间APP与子空间端不匹配");
        }
        if (TerminalTypeEnum.of(subTerminal.getName()) == TerminalTypeEnum.APP) {
            throw new CommonException("子空间端不能挂载APP端，只能挂载WEB等");
        }
        // 挂载同APP下指定端
        if (appId.equals(subAppId)) {
            return;
        }
        // 挂载不同APP下指定端
        appRelationService.ensureRelationExist(appId, subAppId);
        return;
    }

    public static ObjectInfoDTO convertToDTO(ObjectBasic objectBasic) {
        if (objectBasic == null) {
            return null;
        }
        ObjectInfoDTO result = BeanConvertUtils.convert(objectBasic, ObjectInfoDTO.class);
        if (result == null) {
            return null;
        }
        if (result.getSpecialType() == null) {
            result.setSpecialType(ObjSpecialTypeEnum.NORMAL.getName());
        }
        String ext = objectBasic.getExt();
        if (StringUtils.isNotBlank(ext)) {
            ObjectExtDTO objectExtDTO = JsonUtils.parseObject(ext, ObjectExtDTO.class);
            if (objectExtDTO != null) {
                result.setBridgeSubAppId(objectExtDTO.getSubAppId());
                result.setBridgeSubTerminalId(objectExtDTO.getSubTerminalId() == null ? 0L : objectExtDTO.getSubTerminalId());
                result.setObjSubType(objectExtDTO.getBasicTag() == null ? ObjSubTypeEnum.UNKNOWN.getOidPrefix() : objectExtDTO.getBasicTag().getObjSubType());
                result.setBizGroup(objectExtDTO.getBasicTag() == null ? null : objectExtDTO.getBasicTag().getBizGroup());
                result.setBizGroupName(objectExtDTO.getBasicTag() == null ? null : objectExtDTO.getBasicTag().getBizGroupName());
                result.setAnalyseCid(objectExtDTO.isAnalyseCid());
            }
        }
        return result;
    }

    public void checkParentExist(Long appId, List<Long> parentIds, Long terminalId, Long reqPoolId) {
        if (CollectionUtils.isNotEmpty(parentIds)) {
            Set<Long> candidateParentObjIds = getCandidateParentObjects(appId, Collections.singletonList(terminalId), reqPoolId)
                    .stream().map(ObjectBasic::getId)
                    .collect(Collectors.toSet());
            parentIds.forEach(parentId -> {
                if (!candidateParentObjIds.contains(parentId)) {
                    ObjectBasic parentObjectBasic = objectBasicService.getById(parentId);
                    String terminalName = terminalService.getById(terminalId).getName();

                    throw new CommonException("父对象"
                            + (parentObjectBasic == null ? parentId : parentObjectBasic.getOid())
                            + "在" + terminalName + "下不存在，请检查。可能是因为该对象未在" + terminalName + "上线，若已上线，请将需求组的" + terminalName + "基线更新至最新");
                }
            });
        }
    }

    /**
     * 获取当前对象的候选父对象集合（当前端下，所有已上线对象以及当前需求池中的新建对象）
     *
     * @param terminalIds 端ID
     * @param reqPoolId   需求池ID
     * @return
     */
    public List<ObjectBasic> getCandidateParentObjects(Long appId, List<Long> terminalIds, Long reqPoolId){
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(terminalIds), "端ID集合不能为空");
        Preconditions.checkArgument(null != reqPoolId, "需求组ID不能为空");
        // 1. 获取当前需求池下新建/变更对象 对应的埋点
        EisObjTerminalTracker queryTracker = new EisObjTerminalTracker();
        queryTracker.setAppId(appId);
        List<EisObjTerminalTracker> objTrackerList = objTerminalTrackerService.search(queryTracker);

        // 2. 过滤出已上线或者当前需求池中新建/变更的埋点
        objTrackerList = objTrackerList.stream()
                .filter(k -> k.getTerminalReleaseId() != 0L
                        || Objects.equals(k.getReqPoolId(), reqPoolId))
                .collect(Collectors.toList());

        // 3. 构建对象关联的终端信息
        Map<Long, Set<Long>> objIdToTerminalSetMap = Maps.newHashMap();
        objTrackerList.forEach(objTracker -> {
            Long objId = objTracker.getObjId();
            Long terminalId = objTracker.getTerminalId();
            Set<Long> relationTerminals = objIdToTerminalSetMap
                    .computeIfAbsent(objId, k -> Sets.newHashSet());
            relationTerminals.add(terminalId);
        });

        // 4. 构建候选父对象集
        Set<Long> objIds = objIdToTerminalSetMap.keySet().stream()
                .filter(objId -> objIdToTerminalSetMap.get(objId).containsAll(terminalIds))
                .collect(Collectors.toSet());
        List<ObjectBasic> candidateParentObjects = objectBasicService.getByIds(objIds);

        // 5. 插入虚拟根结点
        ObjectBasic objectBasic = new ObjectBasic();
        objectBasic.setId(ObjectHelper.virtualRootNode);
        objectBasic.setOid("virtual_root");
        objectBasic.setName("虚拟根节点");
        candidateParentObjects.add(0, objectBasic);

        // 6. 插入跨空间的桥梁节点
        Set<ObjectBasic> parentAppBridges = new HashSet<>();
        terminalIds.forEach(terminalId -> {
            List<ObjectBasic> list = appRelationService.getParentBridgeCandidatesByReqPoolId(appId, terminalId, reqPoolId);
            if (CollectionUtils.isNotEmpty(list)) {
                parentAppBridges.addAll(list);
            }
        });
        if (CollectionUtils.isNotEmpty(parentAppBridges)) {
            candidateParentObjects.addAll(parentAppBridges);
        }

        return candidateParentObjects;
    }
}
