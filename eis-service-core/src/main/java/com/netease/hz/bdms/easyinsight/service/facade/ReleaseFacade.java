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
     * ??????????????????????????????????????????
     *
     * @param terminalId ??????ID
     * @param ascendingSort ????????????
     */
    public List<ReleasedTaskVO> getReleasedTasks(Long terminalId, Boolean ascendingSort){
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "??????ID????????????");
        Preconditions.checkArgument(null != terminalId, "??????ID????????????");
        // 1. ?????????????????????????????????
        EisReqTask query = new EisReqTask();
        query.setAppId(appId);
        List<EisReqTask> reqTaskList = reqTaskService.search(query);
        // 2. ?????????????????? ????????????????????????
        reqTaskList = reqTaskList.stream()
                .filter(k -> ReqTaskStatusEnum.ONLINE.getState().equals(k.getStatus()))
                .filter(k -> terminalId.equals(k.getTerminalId()))
                .collect(Collectors.toList());
        // 3. ??????????????????
        Set<Long> terminalReleaseIds = reqTaskList.stream()
                .map(EisReqTask::getTerminalReleaseId)
                .collect(Collectors.toSet());
        List<EisTerminalReleaseHistory> terminalReleaseHistoryList = terminalReleaseService
                .getByIds(terminalReleaseIds);
        // 4. ??????????????????
        Set<Long> requirementInfoIds =  reqTaskList.stream()
                .map(EisReqTask::getRequirementId)
                .collect(Collectors.toSet());
        List<EisRequirementInfo> requirementInfoList = requirementInfoService.getByIds(requirementInfoIds);
        Map<Long, EisRequirementInfo> requirementInfoMap = requirementInfoList.stream()
                .collect(Collectors.toMap(EisRequirementInfo::getId, Function.identity()));
        // 4. ??? ???????????????????????? ???????????? <releaseId, <terminalVersionName, [reqTask, ...]>>
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
        // 5. ???????????????
        List<ReleasedTaskVO> releasedTaskVOList = Lists.newArrayList();
        for (EisTerminalReleaseHistory terminalReleaseHistory : terminalReleaseHistoryList) {
            // ??????????????????????????????
            Long releaseId = terminalReleaseHistory.getId();
            Map<String, List<EisReqTask>> terminalVersionNameToTaskListMap = releaseIdToTaskListMap
                    .getOrDefault(releaseId, Maps.newHashMap());
            for (String terminalVersionName : terminalVersionNameToTaskListMap.keySet()) {
                // ???????????????????????? ?????? ????????????-????????????
                List<EisReqTask> taskList = terminalVersionNameToTaskListMap.get(terminalVersionName);
                List<String> taskNames = Lists.newArrayList();
                for (EisReqTask reqTask : taskList) {
                    Long requirementInfoId = reqTask.getRequirementId();
                    if(!requirementInfoMap.containsKey(requirementInfoId)) continue;
                    EisRequirementInfo requirementInfo = requirementInfoMap.get(requirementInfoId);
                    String reqTaskName = requirementInfo.getReqName() + "-" + reqTask.getTaskName();
                    taskNames.add(reqTaskName);
                }
                // ????????????
                ReleasedTaskVO releasedTaskVO = new ReleasedTaskVO();
                releasedTaskVO.setReleaseId(releaseId)
                        .setReleaseTime(terminalReleaseHistory.getCreateTime())
                        .setReleaserName(terminalReleaseHistory.getCreateName())
                        .setTaskNames(taskNames)
                        .setTerminalVersionName(terminalVersionName);
                releasedTaskVOList.add(releasedTaskVO);
            }
        }
        // 6. ??????
        releasedTaskVOList.sort(Comparator.comparing(ReleasedTaskVO::getReleaseTime));
        if(ascendingSort){
            Collections.reverse(releasedTaskVOList);
        }
        return releasedTaskVOList;
    }

    /**
     * ????????????????????????????????????
     *
     * @param terminalId ?????????ID
     * @param search ????????????( ??????ID??????????????????????????????)
     * @param taskStatus ????????? ?????? ??????
     * @param taskSourceStatus ????????? ???????????? ??????
     * @return
     */
    public List<TaskDetailVO> getTaskList(Long terminalId, String search, ProcessStatusEnum taskStatus, TaskSourceStatusEnum taskSourceStatus) {
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "??????ID????????????");
        Preconditions.checkArgument(null != terminalId, "??????ID????????????");
        // 1. ?????????????????????????????????
        List<TaskDetailVO> taskDetailVOList = taskHelper.getDetailTask(appId, terminalId, taskStatus);

        // 2. ????????????????????????????????????
        taskDetailVOList = taskDetailVOList.stream()
                .filter(k -> Strings.isNullOrEmpty(k.getTerminalVersionName()))
                .collect(Collectors.toList());

        // 3. ????????????????????????
        if(!Strings.isNullOrEmpty(search)){
            // ????????????????????????ID?????????????????????
            taskDetailVOList = taskDetailVOList.stream()
                    .filter(k -> (k.getReqIssueKey() != null && k.getReqIssueKey().contains(search))
                            || (k.getReqName() != null && k.getReqName().contains(search))
                            || (k.getTaskName() != null && k.getTaskName().contains(search)))
                    .collect(Collectors.toList());
        }
        // 4. ??????????????????
        taskSourceService.composeSourceInfo(taskDetailVOList);
        // ?????????????????????
        if (taskSourceStatus != null) {
            taskDetailVOList = taskDetailVOList.stream()
                    .filter(taskDetailVO -> taskDetailVO != null
                            && taskDetailVO.getSourceStatus() != null
                            && taskDetailVO.getSourceStatus().equals(taskSourceStatus.getType()))
                    .collect(Collectors.toList());
        }
        // 5. ???????????????????????????????????????
        taskDetailVOList.sort(Comparator.comparing(TaskDetailVO::getSourceCreateTime).reversed());
        return taskDetailVOList;
    }

    /**
     * ?????? ???????????????????????????
     *
     * @param terminalId ???ID
     * @param search ????????????????????????????????????ID
     * @param terminalVersionName ?????????
     * @param taskStatus ????????? ?????? ??????
     * @param taskSourceStatus ????????? ???????????? ??????
     * @param versionDeployedFilter ????????? ?????? ??????
     * @param versionSourceStatus ????????? ???????????? ??????
     */
    public List<AggregatedTaskVO> getTaskListAggregatedByTerminalVersion(
            Long terminalId, String search, String terminalVersionName,
            ProcessStatusEnum taskStatus, TaskSourceStatusEnum taskSourceStatus,
            Boolean versionDeployedFilter, VersionSourceStatusEnum versionSourceStatus) {
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "?????????????????????");
        Preconditions.checkArgument(null != terminalId, "??????ID????????????");
        // 1. ????????????????????????????????????????????????
        List<TaskDetailVO> taskDetailVOList = taskHelper.getDetailTask(appId, terminalId, taskStatus);
        // ??????????????????
        taskSourceService.composeSourceInfo(taskDetailVOList);
        // ?????????????????????
        if (taskSourceStatus != null) {
            taskDetailVOList = taskDetailVOList.stream()
                    .filter(taskDetailVO -> taskDetailVO != null
                            && taskDetailVO.getSourceStatus() != null && taskDetailVO.getSourceStatus().equals(taskSourceStatus.getType()))
                    .collect(Collectors.toList());
        }
        // 2. ?????????????????????????????????
        taskDetailVOList = taskDetailVOList.stream()
                .filter(k -> StringUtils.isNotBlank(k.getTerminalVersionName()))
                .collect(Collectors.toList());
        // 3. ?????????????????????
        Set<String> terminalVersionNames = taskDetailVOList.stream()
                .map(TaskDetailVO::getTerminalVersionName)
                .collect(Collectors.toSet());
        List<EisTerminalVersionInfo> terminalReleaseHistoryList = terminalVersionInfoService
                .getByNames(terminalVersionNames);
        Map<String, EisTerminalVersionInfo> terminalVersionInfoMap = terminalReleaseHistoryList.stream()
                .collect(Collectors.toMap(EisTerminalVersionInfo::getName, Function.identity()));
        // 4. ???????????????????????????
        Map<String, List<TaskDetailVO>> terminalVersionNameToTaskListMap = Maps.newHashMap();
        for (TaskDetailVO detailTask : taskDetailVOList) {
            String currTerminalVersionName = detailTask.getTerminalVersionName();
            List<TaskDetailVO> taskList = terminalVersionNameToTaskListMap
                    .computeIfAbsent(currTerminalVersionName, k -> Lists.newArrayList());
            taskList.add(detailTask);
        }

        // 5. ???????????????
        List<AggregatedTaskVO> aggregatedTaskVOList = Lists.newArrayList();
        for (String currTerminalVersionName : terminalVersionNameToTaskListMap.keySet()) {
            if(!terminalVersionInfoMap.containsKey(currTerminalVersionName)) continue;

            // ???????????? 1
            if(StringUtils.isNotBlank(terminalVersionName)
                    && !currTerminalVersionName.contains(terminalVersionName)) continue;

            // ????????????????????????
            EisTerminalVersionInfo terminalVersionInfo = terminalVersionInfoMap.get(currTerminalVersionName);
            List<TaskDetailVO> taskList = terminalVersionNameToTaskListMap.get(currTerminalVersionName);
            // ???????????? 2
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
                    .setSource(VersionSourceEnum.OVERMIND.getType()); // TODO : ?????????????????????????????????????????????????????????
            // ???????????????????????????????????????????????????
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

        // ????????????source
        versionSourceService.composeSourceInfo(aggregatedTaskVOList);
        // ??????????????????????????????
        if (versionDeployedFilter != null) {
            aggregatedTaskVOList = aggregatedTaskVOList.stream()
                    .filter(aggregatedTaskVO -> aggregatedTaskVO != null
                            && aggregatedTaskVO.getDeployed() != null
                            && aggregatedTaskVO.getDeployed().equals(versionDeployedFilter))
                    .collect(Collectors.toList());
        }
        // ??????????????????????????????
        if (versionSourceStatus != null) {
            aggregatedTaskVOList = aggregatedTaskVOList.stream()
                    .filter(aggregatedTaskVO -> aggregatedTaskVO != null
                            && aggregatedTaskVO.getSourceStatus() != null
                            && aggregatedTaskVO.getSourceStatus().equals(versionSourceStatus.getType()))
                    .collect(Collectors.toList());
        }

        // ??????????????????????????????
        aggregatedTaskVOList.sort(Comparator.comparing(AggregatedTaskVO::getCreateTime).reversed());

        // ?????????????????????build???
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

        // ?????????????????????
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
     * ???????????????????????????
     * @param terminalId
     * @param terminalVersionId
     * @param taskIds
     */

    public void releaseByTerminalVersion(Long terminalId, Long terminalVersionId, Set<Long> taskIds){
        //??????????????????????????????????????????
        EisTerminalVersionInfo terminalVersionInfo = terminalVersionInfoService.getById(terminalVersionId);
        List<EisReqTask> tasks = reqTaskService.getByIds(taskIds);
        for (EisReqTask task : tasks) {
            if(!task.getTerminalVersion().equals(terminalVersionInfo.getName())){
                throw new CommonException("??????" + task.getTaskName() + "?????????????????????????????????");
            }
        }
        spmFacade.tempDisableListCache();
        releaseHelper.releaseMain(taskIds,terminalId,terminalVersionId);
    }

    /**
     * ????????????????????????
     * @param terminalId
     * @param taskId
     */
    @MethodLog
    @Transactional(rollbackFor = Throwable.class)
    public void releaseByTaskId(Long terminalId,Long taskId){
        EisReqTask task = reqTaskService.getById(taskId);
        if(!StringUtils.isEmpty(task.getTerminalVersion())){
            throw new CommonException("???????????????????????????????????????????????????");
        }
        // 1. ?????????????????????
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        String customName = "V" + df.format(new Date());
        EisTerminalVersionInfo newTerminalVersion = new EisTerminalVersionInfo();
        Long appId = EtContext.get(ContextConstant.APP_ID);
        newTerminalVersion.setAppId(appId);
        newTerminalVersion.setName(customName);
        Long terminalVersionId = terminalVersionInfoService.create(newTerminalVersion);
        // 2. ???????????????????????????????????????
        EisReqTask updateEntity = new EisReqTask();
        updateEntity.setId(task.getId());
        updateEntity.setTerminalVersion(customName);
        reqTaskService.updateById(updateEntity);
        // 3. ????????????
        spmFacade.tempDisableListCache();
        releaseHelper.releaseMain(Sets.newHashSet(taskId),terminalId,terminalVersionId);
    }

}
