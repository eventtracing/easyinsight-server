package com.netease.hz.bdms.easyinsight.service.facade;

import com.netease.hz.bdms.easyinsight.common.aop.MethodLog;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.dto.realtimetest.TestHistoryRecordDTO;
import com.netease.hz.bdms.easyinsight.common.dto.terminal.TerminalSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.enums.*;
import com.netease.hz.bdms.easyinsight.common.exception.CommonException;
import com.netease.hz.bdms.easyinsight.common.vo.task.*;
import com.netease.hz.bdms.easyinsight.dao.model.*;
import com.netease.hz.bdms.easyinsight.service.helper.*;
import com.netease.hz.bdms.easyinsight.service.service.*;
import com.netease.hz.bdms.easyinsight.service.service.requirement.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
@Slf4j
public class HomePageFacade {

    @Resource
    private ReqPoolBasicService reqPoolBasicService;

    @Resource
    private RequirementInfoService requirementInfoService;

    @Resource
    private ReqTaskService reqTaskService;

    @Resource
    private TerminalService terminalService;

    @Resource
    private RealTimeTestRecordService realTimeTestRecordService;

    @Resource
    private MergeConflictHelper mergeConflictHelper;

    @MethodLog
    public List<ReqTaskVO> queryRelatedReqList(Integer status) {

        Long appId = EtContext.get(ContextConstant.APP_ID);
        UserDTO user = EtContext.get(ContextConstant.USER);
        if (appId == null || user == null) {
            throw new CommonException("上下文信息缺失！");
        }
        List<ReqTaskVO> reqTaskVOS = new ArrayList<>();
        List<EisReqTask> tasks = reqTaskService.getByUserAndStatus(user.getEmail(), status);
        if (CollectionUtils.isEmpty(tasks)) {
            return reqTaskVOS;
        }

        List<TerminalSimpleDTO> terminals = terminalService.getByAppId(appId);
        Map<Long, String> terminalMap = new HashMap<>();
        Map<Long, Long> reqIdToPoolIdMap = new HashMap<>();
        Map<Long, EisRequirementInfo> reqMap = new HashMap<>();
        Set<Long> reqIds = new HashSet<>();
        for (EisReqTask task : tasks) {
            reqIds.add(task.getRequirementId());
        }
        List<EisRequirementInfo> relReqs = requirementInfoService.getByIds(reqIds);
        for (EisRequirementInfo requirementInfo : relReqs) {
            reqIdToPoolIdMap.put(requirementInfo.getId(), requirementInfo.getReqPoolId());
            reqMap.put(requirementInfo.getId(), requirementInfo);
        }

        EisReqPool reqPoolQuery = new EisReqPool();
        reqPoolQuery.setAppId(appId);
        List<EisReqPool> reqPools = reqPoolBasicService.search(reqPoolQuery);
        for (TerminalSimpleDTO terminal : terminals) {
            terminalMap.put(terminal.getId(), terminal.getName());
        }

        Set<Long> allConflictReqPoolIds = mergeConflictHelper.getMergeConflictReqPoolIds();

        for (EisReqTask task : tasks) {
            ReqTaskVO vo = new ReqTaskVO();
            EisRequirementInfo requirementInfo = reqMap.get(task.getRequirementId());
            vo.setId(task.getId());
            vo.setTerminalId(task.getTerminalId());
            vo.setTerminal(terminalMap.get(task.getTerminalId()));
            vo.setTerminalVersion(task.getTerminalVersion());
            long reqPoolId = reqIdToPoolIdMap.get(task.getRequirementId());
            vo.setReqPoolId(reqPoolId);
            vo.setMergeConflict(allConflictReqPoolIds.contains(reqPoolId));
            //任务测试记录
            TestRecordResultVO testResult = new TestRecordResultVO();
            int sum = 0;
            int passNum = 0;
            int unPassNum = 0;
            int partPassNum = 0;
            List<TestHistoryRecordDTO> recordDTOS = realTimeTestRecordService.getTestHistoryByTaskId(task.getId());
            for (TestHistoryRecordDTO historyRecord : recordDTOS) {
                if (historyRecord.getTestResult().equals(TestResultEnum.PASS.getType())) {
                    passNum++;
                } else if (historyRecord.getTestResult().equals(TestResultEnum.UNPASS.getType())) {
                    unPassNum++;
                } else if (historyRecord.getTestResult().equals(TestResultEnum.PARTPASS.getType())) {
                    partPassNum++;
                }
                sum++;
            }
            testResult.setSum(sum);
            testResult.setPassNum(passNum);
            testResult.setUnPassNum(unPassNum);
            testResult.setPartPassNum(partPassNum);
            vo.setTestResult(testResult);
            vo.setOwner(task.getOwnerName());
            vo.setVerifier(task.getVerifierName());
            vo.setReqIssueKey(requirementInfo.getReqIssueKey());
            vo.setReqName(requirementInfo.getReqName());
            vo.setTaskName(task.getTaskName());
            vo.setStatus(task.getStatus());
            vo.setSprint(task.getIteration());
            reqTaskVOS.add(vo);
        }

        return reqTaskVOS;
    }

}
