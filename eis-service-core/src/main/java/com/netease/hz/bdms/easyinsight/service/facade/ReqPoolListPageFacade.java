package com.netease.hz.bdms.easyinsight.service.facade;

import com.alibaba.fastjson.JSONObject;
import com.netease.eis.adapters.CacheAdapter;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.common.CommonAggregateDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.obj.UserPointInfoDTO;
import com.netease.hz.bdms.easyinsight.common.dto.require.ReqPoolCountsDTO;
import com.netease.hz.bdms.easyinsight.common.dto.terminal.TerminalSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.enums.ProcessStatusEnum;
import com.netease.hz.bdms.easyinsight.common.enums.ReqSourceEnum;
import com.netease.hz.bdms.easyinsight.common.exception.CommonException;
import com.netease.hz.bdms.easyinsight.common.query.ReqPoolPageQuery;
import com.netease.hz.bdms.easyinsight.common.util.BeanConvertUtils;
import com.netease.hz.bdms.easyinsight.common.util.JsonUtils;
import com.netease.hz.bdms.easyinsight.common.vo.requirement.*;
import com.netease.hz.bdms.easyinsight.dao.model.*;
import com.netease.hz.bdms.easyinsight.service.service.ObjChangeHistoryService;
import com.netease.hz.bdms.easyinsight.service.service.TerminalService;
import com.netease.hz.bdms.easyinsight.service.service.UserService;
import com.netease.hz.bdms.easyinsight.service.helper.MergeConflictHelper;
import com.netease.hz.bdms.easyinsight.service.helper.RequirementPoolHelper;
import com.netease.hz.bdms.easyinsight.service.helper.TerminalVersionInfoHelper;
import com.netease.hz.bdms.easyinsight.service.service.impl.VersionLinkService;
import com.netease.hz.bdms.easyinsight.service.service.obj.UserBuryPointService;
import com.netease.hz.bdms.easyinsight.service.service.requirement.*;
import com.netease.hz.bdms.easyinsight.service.service.terminalrelease.TerminalReleaseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReqPoolListPageFacade {

    @Autowired
    RequirementInfoService requirementInfoService;
    @Autowired
    ReqPoolBasicService reqPoolBasicService;
    @Autowired
    ReqTaskService reqTaskService;
    @Autowired
    TaskProcessService taskProcessService;
    @Autowired
    UserService userService;
    @Autowired
    TerminalService terminalService;
    @Autowired
    RequirementPoolHelper requirementPoolHelper;
    @Autowired
    ReqPoolRelBaseService reqPoolRelBaseService;
    @Autowired
    TerminalReleaseService terminalReleaseService;
    @Autowired
    UserBuryPointService userBuryPointService;
    @Autowired
    TerminalVersionInfoHelper terminalVersionInfoHelper;
    @Resource
    private VersionLinkService versionLinkService;
    @Resource
    private MergeConflictHelper mergeConflictHelper;
    @Resource
    private ObjChangeHistoryService objChangeHistoryService;
    @Resource
    private ReqTaskFacade reqTaskFacade;
    @Resource
    private ReqDesignFacade reqDesignFacade;
    @Resource
    private CacheAdapter cacheAdapter;

    private ExecutorService executor = new ThreadPoolExecutor(1, 1, 10L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(200),
            new ThreadPoolExecutor.DiscardPolicy() {
            });

    public List<EisRequirementInfo> queryAll(){
        Long appId = EtContext.get(ContextConstant.APP_ID);
        EisRequirementInfo reqInfoQuery = new EisRequirementInfo();
        reqInfoQuery.setAppId(appId);
        List<EisRequirementInfo> requirementInfos = requirementInfoService.search(reqInfoQuery);
        requirementInfos = requirementInfos.stream().filter(e -> e.getReqPoolId() != null).collect(Collectors.toList());
        return requirementInfos;
    }

    public List<ReqPoolPagingListVO> pagingQuery(Long reqPoolId, Long reqId, String dataOwnerEmail,String creatorEmail, Integer status,String search,String order){
        Long appId = EtContext.get(ContextConstant.APP_ID);
        List<ReqPoolPagingListVO> result = new ArrayList<>();
        ReqPoolPageQuery reqPoolPageQuery = new ReqPoolPageQuery();
        reqPoolPageQuery.setAppId(appId);
        reqPoolPageQuery.setDataOwner(dataOwnerEmail);
        reqPoolPageQuery.setId(reqPoolId);
        List<EisReqPool> reqPoolBasicList = reqPoolBasicService.queryForPage(reqPoolPageQuery);
        if(CollectionUtils.isEmpty(reqPoolBasicList)){
            return new ArrayList<>();
        }
        EisRequirementInfo reqInfoQuery = new EisRequirementInfo();
        reqInfoQuery.setOmState(status);
        reqInfoQuery.setOwnerEmail(creatorEmail);
        reqInfoQuery.setAppId(appId);
        reqInfoQuery.setId(reqId);
        List<EisRequirementInfo> requirementInfos = requirementInfoService.search(reqInfoQuery);
        requirementInfos = requirementInfos.stream().filter(e -> e.getReqPoolId() != null).collect(Collectors.toList());
        //存在如果筛选需求的条件且需求筛选结果为空，则直接返回空集合
        boolean hasReqFilter = status != null || creatorEmail != null || search != null || reqId != null;
        if(hasReqFilter && CollectionUtils.isEmpty(requirementInfos)){
            return new ArrayList<>();
        }
        if(!StringUtils.isEmpty(search)){
            requirementInfos = requirementInfos.stream()
                    .filter(e -> search.equals(e.getReqIssueKey())||search.equals(e.getReqName()))
                    .collect(Collectors.toList());
        }
//        if(CollectionUtils.isEmpty(requirementInfos)){
//            return new ArrayList<>();
//        }
        Map<Long,List<EisRequirementInfo>> reqInfoGroupByPoolId = new HashMap<>();
        reqInfoGroupByPoolId = requirementInfos.stream().collect(Collectors.groupingBy(EisRequirementInfo::getReqPoolId));
        Set<Long> reqPoolIdsOfReqInfos = new HashSet<>();
        for (EisRequirementInfo requirementInfo : requirementInfos) {
            reqPoolIdsOfReqInfos.add(requirementInfo.getReqPoolId());
        }
        if(hasReqFilter){
            reqPoolBasicList = reqPoolBasicList.stream().filter(e -> reqPoolIdsOfReqInfos.contains(e.getId())).collect(Collectors.toList());
        }

        // 组装是否有合并基线冲突
        Set<Long> mergeConflictReqPoolIds = mergeConflictHelper.getMergeConflictReqPoolIds();
        // 组装对象数 任务数 事件数 已经指派数 全部指派数
        Map<Long, ReqPoolCountsDTO> reqPoolCounts = getReqPoolCounts(reqPoolBasicList.stream().map(EisReqPool::getId).collect(Collectors.toSet()));
        for (EisReqPool eisReqPool : reqPoolBasicList) {
            boolean hasMergeConflict = mergeConflictReqPoolIds.contains(eisReqPool.getId());
            ReqPoolPagingListVO vo = new ReqPoolPagingListVO();
            vo.setId(eisReqPool.getId());
            vo.setMergeConflict(hasMergeConflict);
            vo.setName(eisReqPool.getName());
            if(!StringUtils.isEmpty(eisReqPool.getDataOwners())){
                List<UserSimpleDTO> dataOwners = JsonUtils.parseList(eisReqPool.getDataOwners(),UserSimpleDTO.class);
                Set<String> dataOwnerNameSet = new HashSet<>();
                if(dataOwners != null){
                    for (UserSimpleDTO dataOwner : dataOwners) {
                        dataOwnerNameSet.add(dataOwner.getUserName());
                    }
                }
                vo.setDataOwners(String.join(",",dataOwnerNameSet));
            }
            if(eisReqPool.getCreateTime() != null){
                vo.setCreateTime(eisReqPool.getCreateTime().getTime());
            }
            vo.setCreatorName(eisReqPool.getCreateName());
            List<EisRequirementInfo> relReqInfos = reqInfoGroupByPoolId.get(eisReqPool.getId());
            List<ReqInfoPagingListVO> reqInfoVoList = new ArrayList<>();
            if(!CollectionUtils.isEmpty(relReqInfos)){
                for (EisRequirementInfo relReqInfo : relReqInfos) {
                    ReqInfoPagingListVO reqInfoVo = new ReqInfoPagingListVO();
                    reqInfoVo.setId(relReqInfo.getId());
                    reqInfoVo.setIssueKey(relReqInfo.getReqIssueKey());
                    reqInfoVo.setName(relReqInfo.getReqName());
                    reqInfoVo.setOmState(relReqInfo.getOmState());
                    String fromName = ReqSourceEnum.fromType(relReqInfo.getSource()).getName();
                    reqInfoVo.setFrom(fromName);
                    reqInfoVo.setPriority(relReqInfo.getPriority());
                    reqInfoVo.setBusinessArea(relReqInfo.getBusinessArea());
                    reqInfoVo.setTeam(relReqInfo.getTeam());
                    reqInfoVo.setViews(relReqInfo.getViews());
                    reqInfoVo.setCreateTime(relReqInfo.getCreateTime().getTime());
                    reqInfoVo.setCreatorName(relReqInfo.getCreateName());
                    reqInfoVo.setMergeConflict(hasMergeConflict);
                    //用户录入埋点 todo 优先级排序
                    List<EisUserPointInfo> userPointInfos = userBuryPointService.searchWithReqId(relReqInfo.getId());
                    reqInfoVo.setUserPointInfos(userPointInfos.stream().map(info -> BeanConvertUtils.convert(info, UserPointInfoDTO.class)).filter(Objects::nonNull).collect(Collectors.toList()));
                    reqInfoVoList.add(reqInfoVo);
                }
            }
            vo.setRequirements(reqInfoVoList);
            ReqPoolCountsDTO reqPoolCountsDTO = reqPoolCounts.get(eisReqPool.getId());
            if (reqPoolCountsDTO != null) {
                vo.setObjCount(reqPoolCountsDTO.getObjCount());
                vo.setTaskCount(reqPoolCountsDTO.getTaskCount());
                vo.setEventCount(reqPoolCountsDTO.getEventCount());
                vo.setAssignedCount(reqPoolCountsDTO.getAssignedCount());
                vo.setTotalAssignCount(reqPoolCountsDTO.getTotalAssignCount());
            }
            result.add(vo);
        }

        if(StringUtils.isEmpty(order) || order.equalsIgnoreCase("desc")){
            result = result.stream().sorted((a,b) -> b.getCreateTime().compareTo(a.getCreateTime())).collect(Collectors.toList());
        }else {
            result = result.stream().sorted((a,b) -> a.getCreateTime().compareTo(b.getCreateTime())).collect(Collectors.toList());
        }
        return result;
    }

    //todo
    private List<UserPointInfoDTO> userPointPriorityOrder(List<EisUserPointInfo> origin){
        //给用户埋点贴上优先级标签
        return new ArrayList<>();
    }

    public ReqPoolEditShowVO getReqPoolEditView(Long id){
        EisReqPool reqPool = reqPoolBasicService.getById(id);
        List<UserDTO> dataOwners = JsonUtils.parseList(reqPool.getDataOwners(),UserDTO.class);
        String name = reqPool.getName();
        String desc = reqPool.getDescription();
        ReqPoolEditShowVO vo = new ReqPoolEditShowVO();
        vo.setName(name);
        vo.setDataOwners(dataOwners);
        vo.setDesc(desc);
        return vo;
    }

    public ReqEditShowVO getReqEditView(Long reqId){
        ReqEditShowVO showVo = new ReqEditShowVO();
        ReqShowVO reqShowVo = new ReqShowVO();
        List<TaskShowVO> taskVos = new ArrayList<>();
        EisRequirementInfo requirementInfo = requirementInfoService.getById(reqId);
        reqShowVo.setReqId(requirementInfo.getId());
        reqShowVo.setReqIssueKey(requirementInfo.getReqIssueKey());
        reqShowVo.setName(requirementInfo.getReqName());
        reqShowVo.setFrom(requirementInfo.getSource());
        reqShowVo.setPriority(requirementInfo.getPriority());
        reqShowVo.setTeam(requirementInfo.getTeam());
        reqShowVo.setBusinessArea(requirementInfo.getBusinessArea());
        reqShowVo.setViews(requirementInfo.getViews());
        reqShowVo.setDescription(requirementInfo.getDescription());
        UserDTO user = new UserDTO();
        user.setEmail(requirementInfo.getOwnerEmail());
        user.setUserName(requirementInfo.getOwnerName());
        showVo.setReqInfo(reqShowVo);

        EisReqTask query = new EisReqTask();
        query.setRequirementId(reqId);
        List<EisReqTask> tasks = reqTaskService.search(query);
        for (EisReqTask task : tasks) {
            TaskShowVO taskShowVO = new TaskShowVO();
            taskShowVO.setId(task.getId());
            taskShowVO.setName(task.getTaskName());
            taskShowVO.setTerminalId(task.getTerminalId());
            UserDTO owner = new UserDTO();
            owner.setUserName(task.getOwnerName());
            owner.setEmail(task.getOwnerEmail());
            taskShowVO.setOwner(owner);
            UserDTO verifier = new UserDTO();
            verifier.setUserName(task.getVerifierName());
            verifier.setEmail(task.getVerifierEmail());
            taskShowVO.setVerifier(verifier);
            taskVos.add(taskShowVO);
        }
        showVo.setTasks(taskVos);
        return showVo;
    }

    /**
     * 创建需求组，并插入需求组关联基线
     * @param createVO
     */
    public long createReqPoolBasic(ReqPoolCreateVO createVO){
        Long appId = EtContext.get(ContextConstant.APP_ID);
        EisReqPool eisReqPool = new EisReqPool();
        eisReqPool.setDataOwners(JSONObject.toJSONString(createVO.getOwners()));
        eisReqPool.setName(createVO.getName());
        eisReqPool.setDescription(createVO.getDesc());
        try{
            reqPoolBasicService.insert(eisReqPool);
        }catch (DuplicateKeyException e){
            log.debug("", e);
            throw new CommonException("需求组名称重复，请重新命名");
        }

        List<TerminalSimpleDTO> terminals = terminalService.getByAppId(appId);
        if(CollectionUtils.isEmpty(terminals)){
            return eisReqPool.getId();
        }
        Set<Long> terminalIds = terminals.stream().map(e -> e.getId()).collect(Collectors.toSet());
        EisTerminalReleaseHistory terminalReleaseHistoryQuery = new EisTerminalReleaseHistory();
        terminalReleaseHistoryQuery.setAppId(appId);
        terminalReleaseHistoryQuery.setLatest(true);
        List<EisTerminalReleaseHistory> releaseHistories = terminalReleaseService.search(terminalReleaseHistoryQuery);
        Map<Long,Long> terminalIdToLatestReleaseId = new HashMap<>();
        for (EisTerminalReleaseHistory releaseHistory : releaseHistories) {
            terminalIdToLatestReleaseId.put(releaseHistory.getTerminalId(),releaseHistory.getId());
        }
        List<EisReqPoolRelBaseRelease> list = new ArrayList<>();
        for (Long terminalId : terminalIds) {
            EisReqPoolRelBaseRelease reqPoolRelBaseRelease = new EisReqPoolRelBaseRelease();
            reqPoolRelBaseRelease.setAutoRebase(true);
            reqPoolRelBaseRelease.setTerminalId(terminalId);
            reqPoolRelBaseRelease.setReqPoolId(eisReqPool.getId());
            reqPoolRelBaseRelease.setCurrentUse(true);
            if(terminalIdToLatestReleaseId.get(terminalId) != null){
                reqPoolRelBaseRelease.setBaseReleaseId(terminalIdToLatestReleaseId.get(terminalId));
            }else {
                reqPoolRelBaseRelease.setBaseReleaseId(0L);
            }
            list.add(reqPoolRelBaseRelease);
        }
        reqPoolRelBaseService.insertBatch(list);
        return eisReqPool.getId();
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<EisReqTask> addRequirementsIntoPool(ReqEntityVO reqCreateVo){
        RequirementInfoVO newRequirement = reqCreateVo.getRequirement();
        List<TaskEntityVO> newTasks = reqCreateVo.getTasks();
        if(CollectionUtils.isEmpty(newTasks)){
            throw new CommonException("当前需求任务为空，无法创建");
        }
        UserDTO creator = newRequirement.getCreator();
        EisRequirementInfo entity = new EisRequirementInfo();
        if(StringUtils.isEmpty(newRequirement.getReqIssueKey())){
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
            String customKey = "CUSTOM" + df.format(new Date());
            entity.setReqIssueKey(customKey);
        }else {
            entity.setReqIssueKey(newRequirement.getReqIssueKey());
        }
        entity.setReqName(newRequirement.getName());
        entity.setSource(newRequirement.getFrom());
        entity.setPriority(newRequirement.getPriority());
        entity.setTeam(newRequirement.getTeam());
        entity.setBusinessArea(newRequirement.getBusinessArea());
        entity.setViews(newRequirement.getViews());
        entity.setOmState(newRequirement.getOmState());
        entity.setOwnerEmail(creator.getEmail());
        entity.setOwnerName(creator.getUserName());
        entity.setReqPoolId(reqCreateVo.getReqPoolId());
        entity.setDescription(newRequirement.getDesc());
        requirementInfoService.insert(entity);
        List<EisReqTask> tasks = new ArrayList<>();
        for (TaskEntityVO newTask : newTasks) {
            EisReqTask taskEntity = new EisReqTask();
            taskEntity.setTaskIssueKey(newTask.getTaskIssueKey());
            taskEntity.setReqIssueKey(newTask.getReqIssueKey());
            taskEntity.setTaskName(newTask.getName());
            taskEntity.setRequirementId(entity.getId());
            taskEntity.setTerminalId(newTask.getTerminalId());
            taskEntity.setStatus(ProcessStatusEnum.START.getState());
            if(newTask.getOwner() != null){
                taskEntity.setOwnerEmail(newTask.getOwner().getEmail());
                taskEntity.setOwnerName(newTask.getOwner().getUserName());
            }
            if(newTask.getVerifier() != null){
                taskEntity.setVerifierEmail(newTask.getVerifier().getEmail());
                taskEntity.setVerifierName(newTask.getVerifier().getUserName());
            }
            taskEntity.setAppId(EtContext.get(ContextConstant.APP_ID));
            UserDTO currentLoginUser = EtContext.get(ContextConstant.USER);
            taskEntity.setCreateEmail(currentLoginUser.getEmail());
            taskEntity.setCreateName(currentLoginUser.getUserName());
            taskEntity.setTerminalVersion(newTask.getVersionName());
            tasks.add(taskEntity);
        }
        reqTaskService.insertBatch(tasks);
        return tasks;
    }

    public void editReqPoolBasic(ReqPoolEditVO vo){
        Long id = vo.getId();
        String name = vo.getName();
        List<UserDTO> dataOwners = vo.getOwners();
        String desc = vo.getDesc();
        EisReqPool eisReqPool = new EisReqPool();
        eisReqPool.setId(id);
        eisReqPool.setDataOwners(JSONObject.toJSONString(dataOwners));
        eisReqPool.setName(name);
        eisReqPool.setDescription(desc);
        try{
            reqPoolBasicService.updateById(eisReqPool);
        }catch (DuplicateKeyException e){
            log.debug("", e);
            throw new CommonException("需求组名称重复，请重新命名");
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void editRequirement(ReqEntityVO reqEntityVo){
        RequirementInfoVO requirementVo = reqEntityVo.getRequirement();
        Long reqId = requirementVo.getReqId();
        if(reqId == null){
            throw new CommonException("需求Id为空");
        }
        UserDTO creator = requirementVo.getCreator();
        String creatorEmail = creator.getEmail();
        String creatorName= creator.getUserName();
        EisRequirementInfo reqUpdateEntity = new EisRequirementInfo();
        reqUpdateEntity.setId(reqId);
        reqUpdateEntity.setOwnerName(creatorName);
        reqUpdateEntity.setOwnerEmail(creatorEmail);
        reqUpdateEntity.setDescription(requirementVo.getDesc());
        requirementInfoService.update(reqUpdateEntity);
        EisReqTask query = new EisReqTask();
        query.setRequirementId(reqId);
        List<EisReqTask> oldList = reqTaskService.search(query);
        Map<Long,String> taskIdToName = new HashMap<>();
        for (EisReqTask task : oldList) {
            taskIdToName.put(task.getId(),task.getTaskName());
        }
        List<TaskEntityVO> tasks = reqEntityVo.getTasks();
        validate(oldList,tasks);
        Set<Long> idsOfEditVo = new HashSet<>();
        List<EisReqTask> insertList = new ArrayList<>();
        for (TaskEntityVO taskEditVO : tasks) {
            if(taskEditVO.getId() != null){
                /**
                 * id存在，则udpate
                 */
                idsOfEditVo.add(taskEditVO.getId());
                EisReqTask reqTask = new EisReqTask();
                reqTask.setId(taskEditVO.getId());
                reqTask.setTaskName(taskEditVO.getName());
                reqTask.setTerminalId(taskEditVO.getTerminalId());
                if(taskEditVO.getOwner() != null){
                    reqTask.setOwnerName(taskEditVO.getOwner().getUserName());
                    reqTask.setOwnerEmail(taskEditVO.getOwner().getEmail());
                }
                if(taskEditVO.getVerifier() != null){
                    reqTask.setVerifierName(taskEditVO.getVerifier().getUserName());
                    reqTask.setVerifierEmail(taskEditVO.getVerifier().getEmail());
                }
                reqTaskService.updateById(reqTask);
            }else {
                EisReqTask taskEntity = new EisReqTask();
                taskEntity.setTaskName(taskEditVO.getName());
                taskEntity.setReqIssueKey(taskEditVO.getReqIssueKey());
                taskEntity.setTaskIssueKey(taskEditVO.getTaskIssueKey());
                taskEntity.setRequirementId(reqId);
                taskEntity.setTerminalId(taskEditVO.getTerminalId());
                taskEntity.setStatus(ProcessStatusEnum.START.getState());
                taskEntity.setAppId(EtContext.get(ContextConstant.APP_ID));
                if(taskEditVO.getOwner() != null){
                    taskEntity.setOwnerEmail(taskEditVO.getOwner().getEmail());
                    taskEntity.setOwnerName(taskEditVO.getOwner().getUserName());
                }
                if(taskEditVO.getVerifier() != null){
                    taskEntity.setVerifierEmail(taskEditVO.getVerifier().getEmail());
                    taskEntity.setVerifierName(taskEditVO.getVerifier().getUserName());
                }
                UserDTO currentLoginUser = EtContext.get(ContextConstant.USER);
                taskEntity.setCreateEmail(currentLoginUser.getEmail());
                taskEntity.setCreateName(currentLoginUser.getUserName());
                insertList.add(taskEntity);
            }
        }
        if(!CollectionUtils.isEmpty(insertList)){
            reqTaskService.insertBatch(insertList);
        }
        //删除多余任务
        Set<Long> taskIdsTobeDelete = new HashSet<>();
        for (EisReqTask reqTask : oldList) {
            if(!idsOfEditVo.contains(reqTask.getId())){
                taskIdsTobeDelete.add(reqTask.getId());
                reqTaskService.deleteById(reqTask.getId());
            }
        }
        List<EisTaskProcess> processes = taskProcessService.getBatchByTaskIds(taskIdsTobeDelete);
        if(!CollectionUtils.isEmpty(processes)){
            Long taskId = processes.get(0).getTaskId();
            String taskName = taskIdToName.get(taskId);
            throw new CommonException("任务" + taskName +"已绑定开发流程，无法删除，请先解除指派");
        }
    }

    public void deleteReqPool(Long reqPoolId){
        requirementPoolHelper.deleteReqPool(reqPoolId);
    }

    public void deleteRequirement(Long id){
        requirementPoolHelper.deleteRequirement(id);
    }

    /**
     * 需求查询，创建人、数据负责人聚合，提供下拉列表查询选项
     * @return
     */
    public ReqSearchAggreVO getRequireSearchAggre(){
        ReqSearchAggreVO reqAggreVO = new ReqSearchAggreVO();
        EisRequirementInfo reqQuery = new EisRequirementInfo();
        List<EisRequirementInfo> reqInfos = requirementInfoService.search(reqQuery);
        EisReqPool reqPoolQuery = new EisReqPool();
        List<EisReqPool> reqPools = reqPoolBasicService.search(reqPoolQuery);
        if(CollectionUtils.isEmpty(reqInfos) && CollectionUtils.isEmpty(reqPools)){
            reqAggreVO.setCreators(new ArrayList<>());
            reqAggreVO.setDataOwners(new ArrayList<>());
            return reqAggreVO;
        }
        Set<String> creatorEmailSet = new HashSet<>();
        Set<String> dataOwnerEmailSet = new HashSet<>();
        List<CommonAggregateDTO> creator = new ArrayList<>();
        List<CommonAggregateDTO> dataOwner = new ArrayList<>();
        for (EisRequirementInfo requirementInfo : reqInfos) {
            if(requirementInfo.getOwnerEmail() != null){
                String creatorEmail = requirementInfo.getOwnerEmail();
                if(!creatorEmailSet.contains(creatorEmail)){
                    creatorEmailSet.add(creatorEmail);
                    CommonAggregateDTO aggreDTO = new CommonAggregateDTO();
                    aggreDTO.setKey(requirementInfo.getOwnerEmail());
                    aggreDTO.setValue(requirementInfo.getOwnerName());
                    creator.add(aggreDTO);
                }
            }
        }
        for(EisReqPool eisReqPool:reqPools){
            List<UserSimpleDTO> dataOwners = JsonUtils.parseList(eisReqPool.getDataOwners(),UserSimpleDTO.class);
            if(dataOwners != null){
                for(UserSimpleDTO owner:dataOwners){
                    String ownerEmail = owner.getEmail();
                    if(StringUtils.isEmpty(owner.getUserName())){
                        continue;
                    }
                    if(!dataOwnerEmailSet.contains(ownerEmail)){
                        dataOwnerEmailSet.add(ownerEmail);
                        CommonAggregateDTO aggreDTO = new CommonAggregateDTO();
                        aggreDTO.setKey(owner.getEmail());
                        aggreDTO.setValue(owner.getUserName());
                        dataOwner.add(aggreDTO);
                    }
                }
            }
        }
        reqAggreVO.setDataOwners(dataOwner);
        reqAggreVO.setCreators(creator);
        return reqAggreVO;
    }

    public ReqAddAggreVO getAddAggre(){
        ReqAddAggreVO vo = new ReqAddAggreVO();
        List<UserSimpleDTO> users = userService.searchUser(null,null,null,null,null);
        List<CommonAggregateDTO> usersAggre = new ArrayList<>();
        for (UserSimpleDTO user : users) {
            CommonAggregateDTO commonAggregateDTO = new CommonAggregateDTO();
            commonAggregateDTO.setKey(user.getEmail());
            commonAggregateDTO.setValue(user.getUserName());
            usersAggre.add(commonAggregateDTO);
        }
        Long appId = EtContext.get(ContextConstant.APP_ID);
        List<TerminalSimpleDTO> terminals = terminalService.getByAppId(appId);
        List<TerminalAggreVO> teminalAggres = new ArrayList<>();
        for (TerminalSimpleDTO terminal : terminals) {
            TerminalAggreVO terminalAggre = new TerminalAggreVO();
            terminalAggre.setId(terminal.getId());
            terminalAggre.setName(terminal.getName());
            teminalAggres.add(terminalAggre);
        }
        vo.setUsers(usersAggre);
        vo.setTerminals(teminalAggres);
        return vo;
    }

    private static String getCacheKey(Long reqPoolId) {
        return "getReqPoolCounts_" + reqPoolId;
    }

    /**
     * 获取需求池计数
     * @param reqPoolIds
     * @return
     */
    public Map<Long, ReqPoolCountsDTO> getReqPoolCounts(Set<Long> reqPoolIds) {
        if (CollectionUtils.isEmpty(reqPoolIds)) {
            return new HashMap<>();
        }
        Set<String> keys = reqPoolIds.stream().map(ReqPoolListPageFacade::getCacheKey).collect(Collectors.toSet());
        Map<String, String> gets = cacheAdapter.gets(keys);
        if (gets == null) {
            gets = new HashMap<>();
        }
        Map<Long, ReqPoolCountsDTO> cachedResults = gets.values().stream().map(s -> JsonUtils.parseObject(s, ReqPoolCountsDTO.class)).collect(Collectors.toMap(ReqPoolCountsDTO::getReqPoolId, o -> o, (oldV, newV) -> oldV));
        Set<Long> missingIds = new HashSet<>();
        Set<Long> expireIds = new HashSet<>();
        reqPoolIds.forEach(reqPoolId -> {
            ReqPoolCountsDTO r = cachedResults.get(reqPoolId);
            if (r == null) {
                missingIds.add(reqPoolId);
                return;
            }
            if (System.currentTimeMillis() > r.getExpire()) {
                expireIds.add(reqPoolId);
            }
        });
        // 不阻塞业务访问，异步刷新
        if (!CollectionUtils.isEmpty(missingIds)) {
            executor.submit(() -> refreshCache(missingIds));
        }
        if (!CollectionUtils.isEmpty(expireIds)) {
            executor.submit(() -> refreshCache(expireIds));
        }
        return cachedResults;
    }

    private void refreshCache(Set<Long> reqPoolIds) {
        if (CollectionUtils.isEmpty(reqPoolIds)) {
            return;
        }
        reqPoolIds.forEach(reqPoolId -> {
            try {
                ReqPoolStatisticVO statistic = reqDesignFacade.getStatistic(reqPoolId);
                doRefreshCache(reqPoolId, statistic);
            } catch (Exception e) {
                log.error("refreshCache failed, reqPoolId={}", reqPoolId, e);
            }
        });
    }

    public void doRefreshCache(Long reqPoolId, ReqPoolStatisticVO statistic) {
        ReqPoolCountsDTO result = new ReqPoolCountsDTO();
        result.setTaskCount(statistic.getTasks());
        result.setEventCount(statistic.getAllEvents());
        result.setAssignedCount(statistic.getAssignedSpms() + statistic.getAllEvents());
        result.setTotalAssignCount(statistic.getAllSpms() + statistic.getAllEvents());
        result.setObjCount(statistic.getObjCount());
        result.setExpire(System.currentTimeMillis() + 3600000L); // 每60分刷新一次
        result.setReqPoolId(reqPoolId);
        cacheAdapter.set(getCacheKey(reqPoolId), JsonUtils.toJson(result)); // 不过期，总过就几百个Key
    }

    /**
     * 若任务已被指派，则无法修改端
     * @param taskEditList
     */
    private void validate(List<EisReqTask> oldList,List<TaskEntityVO> taskEditList){
        Map<Long,TaskEntityVO> taskEditMap = new HashMap<>();
        Map<Long,EisReqTask> oldTaskMap = new HashMap<>();
        Set<Long> oldIds = new HashSet<>();
        for (EisReqTask reqTask : oldList) {
            oldIds.add(reqTask.getId());
        }
        List<EisTaskProcess> taskProcesses = taskProcessService.getBatchByTaskIds(oldIds);
        if(CollectionUtils.isEmpty(taskProcesses)){
            return;
        }
        Map<Long,EisTaskProcess> taskIdToProcessMap = new HashMap<>();
        for (EisTaskProcess taskProcess : taskProcesses) {
            taskIdToProcessMap.put(taskProcess.getTaskId(),taskProcess);
        }
        for (TaskEntityVO taskEditVO : taskEditList) {
            if(taskEditVO.getId() != null){
                taskEditMap.put(taskEditVO.getId(),taskEditVO);
            }
        }
        for (EisReqTask reqTask : oldList) {
            oldTaskMap.put(reqTask.getId(),reqTask);
        }
        for (Long id : taskEditMap.keySet()) {
            EisReqTask oldTask = oldTaskMap.get(id);
            TaskEntityVO editVO = taskEditMap.get(id);
            if(oldTask != null){
                if(oldTask.getTerminalId() == null){
                    continue;
                }
                if(!oldTask.getTerminalId().equals(editVO.getTerminalId()) && taskIdToProcessMap.containsKey(id)){
                    throw new CommonException("任务（" + editVO.getName() + "）" + "已被需求组指派，无法修改端");
                }
            }
        }
    }
}
