package com.netease.hz.bdms.easyinsight.service.facade;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.netease.hz.bdms.easyinsight.common.aop.MethodLog;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.enums.*;
import com.netease.hz.bdms.easyinsight.common.exception.CommonException;
import com.netease.hz.bdms.easyinsight.common.vo.requirement.AggregatedTaskVO;
import com.netease.hz.bdms.easyinsight.common.vo.requirement.ReleasedTaskVO;
import com.netease.hz.bdms.easyinsight.common.vo.requirement.TaskDetailVO;
import com.netease.hz.bdms.easyinsight.dao.model.*;
import com.netease.hz.bdms.easyinsight.service.service.TerminalVersionInfoService;
import com.netease.hz.bdms.easyinsight.service.helper.ReleaseHelper;
import com.netease.hz.bdms.easyinsight.service.helper.ReqTaskHelper;
import com.netease.hz.bdms.easyinsight.service.service.impl.TaskSourceServiceImpl;
import com.netease.hz.bdms.easyinsight.service.service.impl.TerminalVersionBuildverService;
import com.netease.hz.bdms.easyinsight.service.service.impl.VersionLinkService;
import com.netease.hz.bdms.easyinsight.service.service.impl.VersionSourceServiceImpl;
import com.netease.hz.bdms.easyinsight.service.service.requirement.*;
import com.netease.hz.bdms.easyinsight.service.service.terminalrelease.TerminalReleaseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author: xumengqiang
 * @date: 2021/12/22 14:45
 */
@Component
@Slf4j
public class ReleaseFacade {

    @Autowired
    ReqTaskService reqTaskService;

    @Autowired
    TerminalReleaseService terminalReleaseService;

    @Autowired
    RequirementInfoService requirementInfoService;

    @Autowired
    ReqTaskHelper taskHelper;

    @Autowired
    TerminalVersionInfoService terminalVersionInfoService;

    @Autowired
    ReleaseHelper releaseHelper;

    @Autowired
    private TaskSourceServiceImpl taskSourceService;

    @Autowired
    private VersionSourceServiceImpl versionSourceService;

    @Resource
    private SpmFacade spmFacade;

    @Resource
    private TerminalVersionBuildverService terminalVersionBuildverService;

    @Resource
    private VersionLinkService versionLinkService;

    /**
     * 获取已上线的任务发布历史信息
     *
     * @param terminalId 终端ID
     * @param ascendingSort 升序排序
     */
    public List<ReleasedTaskVO> getReleasedTasks(Long terminalId, Boolean ascendingSort){
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "产品ID不能为空");
        Preconditions.checkArgument(null != terminalId, "终端ID不能为空");
        // 1. 查询当前产品下所有任务
        EisReqTask query = new EisReqTask();
        query.setAppId(appId);
        List<EisReqTask> reqTaskList = reqTaskService.search(query);
        // 2. 根据任务状态 过滤出已上线任务
        reqTaskList = reqTaskList.stream()
                .filter(k -> ReqTaskStatusEnum.ONLINE.getState().equals(k.getStatus()))
                .filter(k -> terminalId.equals(k.getTerminalId()))
                .collect(Collectors.toList());
        // 3. 查询发布信息
        Set<Long> terminalReleaseIds = reqTaskList.stream()
                .map(EisReqTask::getTerminalReleaseId)
                .collect(Collectors.toSet());
        List<EisTerminalReleaseHistory> terminalReleaseHistoryList = terminalReleaseService
                .getByIds(terminalReleaseIds);
        // 4. 查询需求信息
        Set<Long> requirementInfoIds =  reqTaskList.stream()
                .map(EisReqTask::getRequirementId)
                .collect(Collectors.toSet());
        List<EisRequirementInfo> requirementInfoList = requirementInfoService.getByIds(requirementInfoIds);
        Map<Long, EisRequirementInfo> requirementInfoMap = requirementInfoList.stream()
                .collect(Collectors.toMap(EisRequirementInfo::getId, Function.identity()));
        // 4. 按 发布版本、端版本 划分任务 <releaseId, <terminalVersionName, [reqTask, ...]>>
        Map<Long, Map<String, List<EisReqTask>>> releaseIdToTaskListMap = Maps.newHashMap();
        for (EisReqTask reqTask : reqTaskList) {
            Long releaseId = reqTask.getTerminalReleaseId();
            String terminalVersionName = reqTask.getTerminalVersion();
            Map<String, List<EisReqTask>> terminalVersionNameToTaskListMap = releaseIdToTaskListMap
                    .computeIfAbsent(releaseId, k-> Maps.newHashMap());
            List<EisReqTask> taskList = terminalVersionNameToTaskListMap
                    .computeIfAbsent(terminalVersionName, k-> Lists.newArrayList());
            taskList.add(reqTask);
        }
        // 5. 构建结果集
        List<ReleasedTaskVO> releasedTaskVOList = Lists.newArrayList();
        for (EisTerminalReleaseHistory terminalReleaseHistory : terminalReleaseHistoryList) {
            // 按照发布版本依次处理
            Long releaseId = terminalReleaseHistory.getId();
            Map<String, List<EisReqTask>> terminalVersionNameToTaskListMap = releaseIdToTaskListMap
                    .getOrDefault(releaseId, Maps.newHashMap());
            for (String terminalVersionName : terminalVersionNameToTaskListMap.keySet()) {
                // 获取当前端版本下 所有 需求名称-任务名称
                List<EisReqTask> taskList = terminalVersionNameToTaskListMap.get(terminalVersionName);
                List<String> taskNames = Lists.newArrayList();
                for (EisReqTask reqTask : taskList) {
                    Long requirementInfoId = reqTask.getRequirementId();
                    if(!requirementInfoMap.containsKey(requirementInfoId)) continue;
                    EisRequirementInfo requirementInfo = requirementInfoMap.get(requirementInfoId);
                    String reqTaskName = requirementInfo.getReqName() + "-" + reqTask.getTaskName();
                    taskNames.add(reqTaskName);
                }
                // 结果构建
                ReleasedTaskVO releasedTaskVO = new ReleasedTaskVO();
                releasedTaskVO.setReleaseId(releaseId)
                        .setReleaseTime(terminalReleaseHistory.getCreateTime())
                        .setReleaserName(terminalReleaseHistory.getCreateName())
                        .setTaskNames(taskNames)
                        .setTerminalVersionName(terminalVersionName);
                releasedTaskVOList.add(releasedTaskVO);
            }
        }
        // 6. 排序
        releasedTaskVOList.sort(Comparator.comparing(ReleasedTaskVO::getReleaseTime));
        if(ascendingSort){
            Collections.reverse(releasedTaskVOList);
        }
        return releasedTaskVOList;
    }

    /**
     * 获取无端版本聚合任务列表
     *
     * @param terminalId 端版本ID
     * @param search 搜索条件( 需求ID、需求名称、任务名称)
     * @param taskStatus 按任务 状态 筛选
     * @param taskSourceStatus 按任务 来源状态 筛选
     * @return
     */
    public List<TaskDetailVO> getTaskList(Long terminalId, String search, ProcessStatusEnum taskStatus, TaskSourceStatusEnum taskSourceStatus) {
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "产品ID不能为空");
        Preconditions.checkArgument(null != terminalId, "终端ID不能为空");
        // 1. 查询当前产品下所有任务
        List<TaskDetailVO> taskDetailVOList = taskHelper.getDetailTask(appId, terminalId, taskStatus);

        // 2. 过滤出无端版本映射的任务
        taskDetailVOList = taskDetailVOList.stream()
                .filter(k -> Strings.isNullOrEmpty(k.getTerminalVersionName()))
                .collect(Collectors.toList());

        // 3. 按照搜索条件过滤
        if(!Strings.isNullOrEmpty(search)){
            // 按需求名称、需求ID、任务名称过滤
            taskDetailVOList = taskDetailVOList.stream()
                    .filter(k -> (k.getReqIssueKey() != null && k.getReqIssueKey().contains(search))
                            || (k.getReqName() != null && k.getReqName().contains(search))
                            || (k.getTaskName() != null && k.getTaskName().contains(search)))
                    .collect(Collectors.toList());
        }
        // 4. 组装来源信息
        taskSourceService.composeSourceInfo(taskDetailVOList);
        // 按来源状态过滤
        if (taskSourceStatus != null) {
            taskDetailVOList = taskDetailVOList.stream()
                    .filter(taskDetailVO -> taskDetailVO != null
                            && taskDetailVO.getSourceStatus() != null
                            && taskDetailVO.getSourceStatus().equals(taskSourceStatus.getType()))
                    .collect(Collectors.toList());
        }
        // 5. 按照任务的来源创建时间排序
        taskDetailVOList.sort(Comparator.comparing(TaskDetailVO::getSourceCreateTime).reversed());
        return taskDetailVOList;
    }

    /**
     * 获取 端版本聚合任务列表
     *
     * @param terminalId 端ID
     * @param search 需求名称、任务名称、需求ID
     * @param terminalVersionName 端版本
     * @param taskStatus 按任务 状态 筛选
     * @param taskSourceStatus 按任务 来源状态 筛选
     * @param versionDeployedFilter 按版本 状态 筛选
     * @param versionSourceStatus 按版本 来源状态 筛选
     */
    public List<AggregatedTaskVO> getTaskListAggregatedByTerminalVersion(
            Long terminalId, String search, String terminalVersionName,
            ProcessStatusEnum taskStatus, TaskSourceStatusEnum taskSourceStatus,
            Boolean versionDeployedFilter, VersionSourceStatusEnum versionSourceStatus) {
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "未指定产品信息");
        Preconditions.checkArgument(null != terminalId, "终端ID不能为空");
        // 1. 查询当前产品在指定端下的任务详情
        List<TaskDetailVO> taskDetailVOList = taskHelper.getDetailTask(appId, terminalId, taskStatus);
        // 组装来源状态
        taskSourceService.composeSourceInfo(taskDetailVOList);
        // 按来源状态过滤
        if (taskSourceStatus != null) {
            taskDetailVOList = taskDetailVOList.stream()
                    .filter(taskDetailVO -> taskDetailVO != null
                            && taskDetailVO.getSourceStatus() != null && taskDetailVO.getSourceStatus().equals(taskSourceStatus.getType()))
                    .collect(Collectors.toList());
        }
        // 2. 过滤出配置端版本的任务
        taskDetailVOList = taskDetailVOList.stream()
                .filter(k -> StringUtils.isNotBlank(k.getTerminalVersionName()))
                .collect(Collectors.toList());
        // 3. 查询端版本信息
        Set<String> terminalVersionNames = taskDetailVOList.stream()
                .map(TaskDetailVO::getTerminalVersionName)
                .collect(Collectors.toSet());
        List<EisTerminalVersionInfo> terminalReleaseHistoryList = terminalVersionInfoService
                .getByNames(terminalVersionNames);
        Map<String, EisTerminalVersionInfo> terminalVersionInfoMap = terminalReleaseHistoryList.stream()
                .collect(Collectors.toMap(EisTerminalVersionInfo::getName, Function.identity()));
        // 4. 按照端版本划分任务
        Map<String, List<TaskDetailVO>> terminalVersionNameToTaskListMap = Maps.newHashMap();
        for (TaskDetailVO detailTask : taskDetailVOList) {
            String currTerminalVersionName = detailTask.getTerminalVersionName();
            List<TaskDetailVO> taskList = terminalVersionNameToTaskListMap
                    .computeIfAbsent(currTerminalVersionName, k -> Lists.newArrayList());
            taskList.add(detailTask);
        }

        // 5. 构建结果集
        List<AggregatedTaskVO> aggregatedTaskVOList = Lists.newArrayList();
        for (String currTerminalVersionName : terminalVersionNameToTaskListMap.keySet()) {
            if(!terminalVersionInfoMap.containsKey(currTerminalVersionName)) continue;

            // 搜索条件 1
            if(StringUtils.isNotBlank(terminalVersionName)
                    && !currTerminalVersionName.contains(terminalVersionName)) continue;

            // 构建聚合任务信息
            EisTerminalVersionInfo terminalVersionInfo = terminalVersionInfoMap.get(currTerminalVersionName);
            List<TaskDetailVO> taskList = terminalVersionNameToTaskListMap.get(currTerminalVersionName);
            // 搜索条件 2
            taskList = taskList.stream()
                    .filter(k -> Strings.isNullOrEmpty(search)
                            || (k.getReqIssueKey()!=null && k.getReqIssueKey().contains(search))
                            || (k.getReqName() != null && k.getReqName().contains(search))
                            || (k.getTaskName() != null && k.getTaskName().contains(search)))
                    .collect(Collectors.toList());
            if(CollectionUtils.isEmpty(taskList)) continue;

            AggregatedTaskVO aggregatedTaskVO = new AggregatedTaskVO();

            aggregatedTaskVO.setTasks(taskList)
                    .setTerminalVersionId(terminalVersionInfo.getId())
                    .setTerminalVersionNum(terminalVersionInfo.getNum())
                    .setTerminalVersionName(terminalVersionInfo.getName())
                    .setCreateTime(terminalVersionInfo.getCreateTime())
                    .setCreateName(terminalVersionInfo.getCreateName())
                    .setSourceName(terminalVersionInfo.getName())
                    .setSource(VersionSourceEnum.OVERMIND.getType()); // TODO : 这个在创建版本时没有落库，只能先写死了
            // 是否上线、最近发布人、最近发布时间
            Boolean deployed = true;
            String latestReleaserName = null;
            Date latestReleaseTime = null;
            for (TaskDetailVO taskDetailVO : taskList) {
                if(!ProcessStatusEnum.ONLINE.getState().equals(taskDetailVO.getTaskStatus())){
                    deployed = false;
                }
                if(latestReleaseTime == null || (taskDetailVO.getReleaseTime() != null &&
                 taskDetailVO.getReleaseTime().compareTo(latestReleaseTime) > 0)){
                    latestReleaserName = taskDetailVO.getReleaserName();
                    latestReleaseTime = taskDetailVO.getReleaseTime();
                }
            }
            aggregatedTaskVO.setDeployed(deployed);
            aggregatedTaskVO.setLatestReleaseTime(latestReleaseTime);
            aggregatedTaskVO.setLatestReleaserName(latestReleaserName);
            aggregatedTaskVO.setMergeConflict(taskList.stream().anyMatch(TaskDetailVO::isMergeConflict));

            aggregatedTaskVOList.add(aggregatedTaskVO);
        }

        // 组装版本source
        versionSourceService.composeSourceInfo(aggregatedTaskVOList);
        // 按版本的上线状态过滤
        if (versionDeployedFilter != null) {
            aggregatedTaskVOList = aggregatedTaskVOList.stream()
                    .filter(aggregatedTaskVO -> aggregatedTaskVO != null
                            && aggregatedTaskVO.getDeployed() != null
                            && aggregatedTaskVO.getDeployed().equals(versionDeployedFilter))
                    .collect(Collectors.toList());
        }
        // 按版本的来源状态过滤
        if (versionSourceStatus != null) {
            aggregatedTaskVOList = aggregatedTaskVOList.stream()
                    .filter(aggregatedTaskVO -> aggregatedTaskVO != null
                            && aggregatedTaskVO.getSourceStatus() != null
                            && aggregatedTaskVO.getSourceStatus().equals(versionSourceStatus.getType()))
                    .collect(Collectors.toList());
        }

        // 按照版本创建时间排序
        aggregatedTaskVOList.sort(Comparator.comparing(AggregatedTaskVO::getCreateTime).reversed());

        // 组装手动设置的build号
        Set<Long> terminalVersionIds = aggregatedTaskVOList.stream().map(AggregatedTaskVO::getTerminalVersionId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> buildVersions = terminalVersionBuildverService.getBuildVersions(terminalVersionIds);
        if (buildVersions != null) {
            aggregatedTaskVOList.forEach(aggregatedTaskVO -> {
                Long terminalVersionId = aggregatedTaskVO.getTerminalVersionId();
                if (terminalVersionId != null) {
                    aggregatedTaskVO.setBuildVersion(buildVersions.get(terminalVersionId));
                }
            });
        }

        // 组装端版本链接
        Set<String> versionNames = aggregatedTaskVOList.stream().map(AggregatedTaskVO::getTerminalVersionName).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<String, String> links = versionLinkService.getLinks(versionNames);
        aggregatedTaskVOList.forEach(vo -> {
            String versionName = vo.getTerminalVersionName();
            if (StringUtils.isNotBlank(versionName)) {
                vo.setTerminalVersionLink(links.get(versionName));
            }
        });

        return aggregatedTaskVOList;
    }

    /**
     * 根据端版本发布上线
     * @param terminalId
     * @param terminalVersionId
     * @param taskIds
     */

    public void releaseByTerminalVersion(Long terminalId, Long terminalVersionId, Set<Long> taskIds){
        //校验任务是不是属于这个端版本
        EisTerminalVersionInfo terminalVersionInfo = terminalVersionInfoService.getById(terminalVersionId);
        List<EisReqTask> tasks = reqTaskService.getByIds(taskIds);
        for (EisReqTask task : tasks) {
            if(!task.getTerminalVersion().equals(terminalVersionInfo.getName())){
                throw new CommonException("任务" + task.getTaskName() + "不属于当前发布的端版本");
            }
        }
        spmFacade.tempDisableListCache();
        releaseHelper.releaseMain(taskIds,terminalId,terminalVersionId);
    }

    /**
     * 根据任务发布上线
     * @param terminalId
     * @param taskId
     */
    @MethodLog
    @Transactional(rollbackFor = Throwable.class)
    public void releaseByTaskId(Long terminalId,Long taskId){
        EisReqTask task = reqTaskService.getById(taskId);
        if(!StringUtils.isEmpty(task.getTerminalVersion())){
            throw new CommonException("该任务已绑定端版本，请按端版本发布");
        }
        // 1. 生成端版本信息
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        String customName = "V" + df.format(new Date());
        EisTerminalVersionInfo newTerminalVersion = new EisTerminalVersionInfo();
        Long appId = EtContext.get(ContextConstant.APP_ID);
        newTerminalVersion.setAppId(appId);
        newTerminalVersion.setName(customName);
        newTerminalVersion.setCreateTime(new Date());
        newTerminalVersion.setUpdateTime(new Date());
        Long terminalVersionId = terminalVersionInfoService.create(newTerminalVersion);
        // 2. 更新任务记录中的端版本字段
        EisReqTask updateEntity = new EisReqTask();
        updateEntity.setId(task.getId());
        updateEntity.setTerminalVersion(customName);
        reqTaskService.updateById(updateEntity);
        // 3. 发布上线
        spmFacade.tempDisableListCache();
        releaseHelper.releaseMain(Sets.newHashSet(taskId),terminalId,terminalVersionId);
    }

}
