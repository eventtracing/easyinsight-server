package com.netease.hz.bdms.easyinsight.service.helper;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.netease.eis.adapters.NotifyUserAdapter;
import com.netease.eis.adapters.RealtimeConfigAdapter;
import com.netease.hz.bdms.easyinsight.common.dto.app.AppSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.message.NotifyContentDTO;
import com.netease.hz.bdms.easyinsight.common.dto.terminal.TerminalSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.enums.ProcessStatusEnum;
import com.netease.hz.bdms.easyinsight.common.enums.ReqPoolTypeEnum;
import com.netease.hz.bdms.easyinsight.common.exception.CommonException;
import com.netease.hz.bdms.easyinsight.common.obj.TwoTuple;
import com.netease.hz.bdms.easyinsight.dao.model.EisReqPool;
import com.netease.hz.bdms.easyinsight.dao.model.EisReqTask;
import com.netease.hz.bdms.easyinsight.dao.model.EisRequirementInfo;
import com.netease.hz.bdms.easyinsight.dao.model.EisTaskProcess;
import com.netease.hz.bdms.easyinsight.service.service.AppService;
import com.netease.hz.bdms.easyinsight.service.service.TerminalService;
import com.netease.hz.bdms.easyinsight.service.service.requirement.ReqPoolBasicService;
import com.netease.hz.bdms.easyinsight.service.service.requirement.ReqTaskService;
import com.netease.hz.bdms.easyinsight.service.service.requirement.RequirementInfoService;
import com.netease.hz.bdms.easyinsight.service.service.requirement.TaskProcessService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author: xumengqiang
 * @date: 2021/10/18 10:44
 */
@Slf4j
@Component
public class NotifyHelper implements InitializingBean {

    @Resource
    private RequirementInfoService requirementInfoService;

    @Resource
    private AppService appService;

    @Resource
    private ReqTaskService reqTaskService;

    @Resource
    private TerminalService terminalService;

    @Resource
    private TaskProcessService taskProcessService;

    @Resource
    private ReqPoolBasicService reqPoolBasicService;

    @Resource
    private NotifyUserAdapter notifyUserAdapter;

    @Resource
    private RealtimeConfigAdapter realtimeConfigAdapter;

    private static String eisHost = null;

    private ExecutorService executor = new ThreadPoolExecutor(1, 1, 10L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(500),
            new ThreadPoolExecutor.DiscardPolicy() {
            });

    /**
     * ???spm???????????? ?????????????????????????????? ????????????
     *
     * @param taskProcessIds
     */
    public void notifyAfterUpdateSpmStatus(Set<Long> taskProcessIds) {
        List<EisTaskProcess> taskProcessList = taskProcessService.getBatchByIds(taskProcessIds);
        executor.submit(() -> {
            if (CollectionUtils.isNotEmpty(taskProcessList)) {
                // ???????????? ??????????????????
                process(taskProcessList);
            }
        });
    }

    /**
     * ????????????
     *
     * @param userEmail ????????????
     * @param content   ????????????
     */
    private void sendMessage(String userEmail, String content) {
        if (StringUtils.isBlank(userEmail) || StringUtils.isBlank(content)) {
            return;
        }
        notifyUserAdapter.sendNotifyTextContent(userEmail, "???EasyInsight???????????????", content);
    }


    /**
     * ????????????spm??????????????????
     *
     * @param taskProcesses ???????????????
     */
    private void process(List<EisTaskProcess> taskProcesses) {
        // ????????????
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(taskProcesses), "taskProcesses????????????");

        // 1. ????????????
        List<NotifyContentDTO> notifyContentDTOList = this.getNotifyContents(taskProcesses);

        // 2. ??????????????????????????????????????????????????????????????????????????????
        Map<TwoTuple<String, Long>, List<NotifyContentDTO>> contentMap = Maps.newHashMap();
        for (NotifyContentDTO notifyContentDTO : notifyContentDTOList) {
            String userEmail = notifyContentDTO.getUserEmail();
            Long appId = notifyContentDTO.getAppId();

            TwoTuple<String, Long> twoTupleKey = new TwoTuple<>(userEmail, appId);
            List<NotifyContentDTO> contentDTOList = contentMap
                    .computeIfAbsent(twoTupleKey, k -> Lists.newArrayList());
            contentDTOList.add(notifyContentDTO);
        }

        // 3. ????????????
        for (TwoTuple<String, Long> key : contentMap.keySet()) {
            String userEmail = key.getFirst();
            Long appId = key.getSecond();
            List<NotifyContentDTO> notifyContentDTOS = contentMap.get(key);
            if (CollectionUtils.isNotEmpty(notifyContentDTOS)) {
                // ????????????????????????
                String content = this.convertToPersonContent(notifyContentDTOS, appId);
                // ????????????
                this.sendMessage(userEmail, content);
            }
        }
    }

    /**
     * @param taskProcesses ????????????
     * @return
     */
    private List<NotifyContentDTO> getNotifyContents(List<EisTaskProcess> taskProcesses) {
       if (CollectionUtils.isEmpty(taskProcesses)) {
           throw new CommonException("taskProcesses????????????");
       }

        // 1. ????????????
        taskProcesses = taskProcesses.stream()
                .filter(k -> ReqPoolTypeEnum.SPM_DEV.getReqPoolType().equals(k.getReqPoolType()))
                .filter(k -> !ProcessStatusEnum.ONLINE.getState().equals(k.getStatus()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(taskProcesses)) {
            return Lists.newArrayList();
        }

        // 2. ??????????????????
        Set<Long> reqPoolIds = taskProcesses.stream()
                .map(EisTaskProcess::getReqPoolId)
                .collect(Collectors.toSet());
        List<EisReqPool> reqPoolList = reqPoolBasicService.getByIds(reqPoolIds);
        Map<Long, EisReqPool> reqPoolMap = reqPoolList.stream()
                .collect(Collectors.toMap(EisReqPool::getId, Function.identity()));

        Set<Long> taskIds = taskProcesses.stream()
                .map(EisTaskProcess::getTaskId)
                .collect(Collectors.toSet());
        List<EisReqTask> reqTaskList = reqTaskService.getByIds(taskIds);
        Map<Long, EisReqTask> reqTaskMap = reqTaskList.stream()
                .collect(Collectors.toMap(EisReqTask::getId, Function.identity()));

        Set<Long> requirementIds = reqTaskList.stream()
                .map(EisReqTask::getRequirementId)
                .collect(Collectors.toSet());
        List<EisRequirementInfo> requirementInfoList = requirementInfoService.getByIds(requirementIds);
        Map<Long, EisRequirementInfo> requirementInfoMap = requirementInfoList.stream()
                .collect(Collectors.toMap(EisRequirementInfo::getId, Function.identity()));

        Set<Long> terminalIds = reqTaskList.stream()
                .map(EisReqTask::getTerminalId)
                .collect(Collectors.toSet());
        List<TerminalSimpleDTO> terminalSimpleDTOList = terminalService.getByIds(terminalIds);
        Map<Long, TerminalSimpleDTO> terminalSimpleMap = terminalSimpleDTOList.stream()
                .collect(Collectors.toMap(TerminalSimpleDTO::getId, Function.identity()));

        // 3. ????????????SPM ?????????????????????
        List<NotifyContentDTO> notifyContentDTOList = Lists.newArrayList();
        for (EisTaskProcess taskProcess : taskProcesses) {
            // ??????????????????
            Long taskId = taskProcess.getTaskId();
            Long reqPoolId = taskProcess.getReqPoolId();
            EisReqPool reqPool = reqPoolMap.get(reqPoolId);
            EisReqTask reqTask = reqTaskMap.get(taskId);
            if (reqTask == null || null == reqPool) {
                continue;
            }

            Long requireId = reqTask.getRequirementId();
            Long terminalId = reqTask.getTerminalId();
            EisRequirementInfo requirementInfo = requirementInfoMap.get(requireId);
            TerminalSimpleDTO terminalSimpleDTO = terminalSimpleMap.get(terminalId);
            if (null == requirementInfo || null == terminalSimpleDTO) {
                continue;
            }

            Long appId = reqTask.getAppId();
            Integer processStatus = taskProcess.getStatus();

            List<UserSimpleDTO> userList = Lists.newArrayList();
            // ??????????????????????????????????????????????????????????????????
            if (ProcessStatusEnum.VERIFY_FINISHED.getState().equals(processStatus) || ProcessStatusEnum.DEV_FINISHED.getState().equals(processStatus)) {
                String ownerEmail = reqTask.getOwnerEmail();
                String ownerName = reqTask.getOwnerName();
                UserSimpleDTO owner = new UserSimpleDTO();
                owner.setUserName(ownerName);
                owner.setEmail(ownerEmail);
                userList.add(owner);
            }
            // ????????????????????????????????????????????????
            if (ProcessStatusEnum.TEST_FINISHED.getState().equals(processStatus)) {
                String creatorEmail = reqTask.getCreateEmail();
                String creatorName = reqTask.getCreateName();
                UserSimpleDTO owner = new UserSimpleDTO();
                owner.setUserName(creatorName);
                owner.setEmail(creatorEmail);
                userList.add(owner);
            }

            // ??????????????????????????????
            for (UserSimpleDTO currUser : userList) {
                // ????????????
                Map<Integer, Integer> backlog = new HashMap<>();
                backlog.put(processStatus, 1);
                NotifyContentDTO notifyContentDTO = new NotifyContentDTO();
                notifyContentDTO.setAppId(appId)
                        .setRequireId(requireId)
                        .setRequireIssueKey(requirementInfo.getReqIssueKey())
                        .setRequireName(requirementInfo.getReqName())
                        .setTaskId(taskId)
                        .setTaskName(reqTask.getTaskName())
                        .setStatus(reqTask.getStatus())
                        .setTerminalVersionName(reqTask.getTerminalVersion())
                        .setTerminalId(terminalId)
                        .setTerminalName(terminalSimpleDTO.getName())
                        .setUserEmail(currUser.getEmail())
                        .setUserName(currUser.getUserName())
                        .setBacklog(backlog);
                // ????????????
                notifyContentDTOList.add(notifyContentDTO);
            }
        }
        // 4. ??????????????? ????????????
        Map<TwoTuple<String, Long>, Map<Long, NotifyContentDTO>> contentMap = Maps.newHashMap();
        for (NotifyContentDTO notifyContentDTO : notifyContentDTOList) {
            String userEmail = notifyContentDTO.getUserEmail();
            Long appId = notifyContentDTO.getAppId();
            TwoTuple<String, Long> twoTupleKey = new TwoTuple<>(userEmail, appId);
            Long taskId = notifyContentDTO.getTaskId();
            Map<Integer, Integer> backlog = notifyContentDTO.getBacklog();
            Map<Long, NotifyContentDTO> taskIdToContentMap = contentMap
                    .computeIfAbsent(twoTupleKey, k -> Maps.newHashMap());
            if (taskIdToContentMap.containsKey(taskId)) {
                NotifyContentDTO currNotifyContentDTO = taskIdToContentMap.get(taskId);
                Map<Integer, Integer> currBacklog = currNotifyContentDTO.getBacklog();
                for (Integer processStatus : backlog.keySet()) {
                    currBacklog.put(processStatus, currBacklog.getOrDefault(processStatus, 0) + 1);
                }
            } else {
                taskIdToContentMap.put(taskId, notifyContentDTO);
            }
        }
        // ????????????
        return contentMap.values().stream()
                .flatMap(k -> k.values().stream())
                .collect(Collectors.toList());
    }

    /**
     * ????????????????????????
     *
     * @param taskIdToContentMap ??????????????????????????????????????????
     * @param appId              ????????????
     * @return
     */
    private String convertToSummaryContent(Map<Long, List<NotifyContentDTO>> taskIdToContentMap, Long appId) {

        // ????????????
        String template = "?????????%s \n" + "?????????%s \n" + "?????????%s \n" + "???????????????\n" + "%s";

        List<String> contentList = Lists.newArrayList();
        AppSimpleDTO appSimpleDTO = appService.getAppById(appId);
        String appName = appSimpleDTO == null ? "error" : appSimpleDTO.getName();

        // ???????????????
        for (Long taskId : taskIdToContentMap.keySet()) {
            // ?????????????????????????????????????????????
            List<NotifyContentDTO> notifyContentDTOS = taskIdToContentMap.get(taskId);
            String requireName = "";
            String taskName = "";
            String terminalName = "";
            List<String> backlogInfo = Lists.newArrayList();
            for (NotifyContentDTO contentDTO : notifyContentDTOS) {
                requireName = contentDTO.getRequireName();
                taskName = contentDTO.getTaskName();
                terminalName = contentDTO.getTerminalName();
                String userName = contentDTO.getUserName();
                Map<Integer, Integer> backlog = contentDTO.getBacklog();
                for (Integer status : backlog.keySet()) {
                    String curr = String.format("%s-%d???%sSPM",
                            userName, backlog.get(status), ProcessStatusEnum.fromState(status).getDesc());
                    backlogInfo.add(curr);
                }
            }
            String part = String.format(template, requireName, taskName,
                    terminalName, String.join("\n", backlogInfo));
            contentList.add(part);
        }
        String startSentence = String.format("???EasyInsight??????????????????????????? \n" +
                "??????????????????????????????%s????????????????????????\n", appName);

        String content = String.join("\n\n", contentList);
        return startSentence + content;
    }

    /**
     * ?????????????????????????????????
     *
     * @param contentDTOList ???????????????????????????????????????
     * @param appId          ??????ID
     * @return
     */
    private String convertToPersonContent(List<NotifyContentDTO> contentDTOList, Long appId) {
        if (StringUtils.isBlank(eisHost)) {
            throw new CommonException("????????????eis.http.host??????????????????????????????URL");
        }
        // ????????????
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(contentDTOList),
                "contentDTOList????????????");
        // ????????????
        String template = "?????????%s \n" + "?????????%s \n" + "?????????%s \n" + "???????????????\n" + "%s";
        // ??????????????????
        Integer totalCount = 0;
        List<String> contentList = Lists.newArrayList();
        AppSimpleDTO appSimpleDTO = appService.getAppById(appId);
        String appName = appSimpleDTO == null ? "error" : appSimpleDTO.getName();
        for (NotifyContentDTO contentDTO : contentDTOList) {
            String reqIssueKey = contentDTO.getRequireIssueKey();
            String requireName = contentDTO.getRequireName();
            String taskName = contentDTO.getTaskName();
            String terminalName = contentDTO.getTerminalName();
            List<String> backlogInfo = Lists.newArrayList();
            Map<Integer, Integer> backlog = contentDTO.getBacklog();
            totalCount += backlog.values().stream().reduce(Integer::sum).orElse(0);
            for (Integer status : backlog.keySet()) {
                String url = String.format(eisHost + "/tracker/requirement/task?appId=%d&issue=%s", appId, reqIssueKey);
                String curr = String.format("%d???%sSPM???%s ",
                        backlog.get(status), ProcessStatusEnum.fromState(status).getTodoDesc(), url);
                backlogInfo.add(curr);
            }
            String part = String.format(template, requireName, taskName,
                    terminalName, String.join("\n", backlogInfo));
            contentList.add(part);
        }
        String startSentence = String.format("?????????????????????????????????????????????%s?????? %d ????????????????????????????????????????????????????????????\n",
                appName, totalCount);
        String content = String.join("\n\n", contentList);
        return startSentence + content;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        realtimeConfigAdapter.listenString("eis.http.host", s -> eisHost = s);
    }
}
