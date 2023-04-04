package com.netease.hz.bdms.easyinsight.web.core.controller;

import com.netease.hz.bdms.easyinsight.common.dto.common.PagingResultDTO;
import com.netease.hz.bdms.easyinsight.common.dto.realtimetest.TestHistoryRecordDTO;
import com.netease.hz.bdms.easyinsight.common.enums.TestResultEnum;
import com.netease.hz.bdms.easyinsight.common.exception.CommonException;
import com.netease.hz.bdms.easyinsight.common.http.HttpResult;
import com.netease.hz.bdms.easyinsight.common.vo.task.*;
import com.netease.hz.bdms.easyinsight.dao.model.EisReqTask;
import com.netease.hz.bdms.easyinsight.service.facade.ReqTaskFacade;
import com.netease.hz.bdms.easyinsight.service.service.RealTimeTestRecordService;
import com.netease.hz.bdms.easyinsight.service.service.impl.AuditReportService;
import com.netease.hz.bdms.easyinsight.service.service.impl.LockService;
import com.netease.hz.bdms.easyinsight.service.service.requirement.ReqTaskService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/eis/task")
public class ReqTaskController {

    @Resource
    private LockService lockService;

    @Autowired
    ReqTaskFacade reqTaskFacade;

    @Autowired
    RealTimeTestRecordService realTimeTestRecordService;

    @Resource
    private AuditReportService auditReportService;

    @Resource
    private ReqTaskService reqTaskService;

    /**
     * 分页查询
     * @param queryVo
     * @return
     */
    @PostMapping("/list")
    public HttpResult queryPagingList(@RequestBody TaskPagingQueryVO queryVo){
        return HttpResult.success(reqTaskFacade.queryPagingList(queryVo));
    }

    /**
     * 分页查询，增加了任务进度返回
     * @param queryVo
     * @return
     */
    @PostMapping("/list/full")
    public HttpResult queryFullPagingList(@RequestBody TaskPagingQueryVO queryVo) {
        PagingResultDTO<ReqTaskVO> result = reqTaskFacade.queryPagingList(queryVo);
        List<ReqTaskVO> tasks = result.getList();
        if (CollectionUtils.isNotEmpty(tasks)) {
            List<Long> taskIds = tasks.stream().map(ReqTaskVO::getId).collect(Collectors.toList());
            if (taskIds.size() > 100) {
                taskIds = taskIds.subList(0, 100);
            }
            Map<Long, TaskProcessVO> m = reqTaskFacade.getProcessVosBatch(taskIds);
            if (MapUtils.isNotEmpty(m)) {
                tasks.forEach(t -> t.setTaskProcess(m.get(t.getId())));
            }
        }
        return HttpResult.success(result);
    }

    /**
     * 查询任务关联的流程视图
     * @param vo
     * @return
     */
    @PostMapping("/processView")
    public HttpResult getTaskProcessView(@RequestBody TaskProcessViewQueryVO vo){
        if (vo == null) {
            throw new CommonException("参数为空");
        }
        if (vo.getTaskId() == null) {
            throw new CommonException("参数taskId为空");
        }
        return HttpResult.success(reqTaskFacade.getProcessVo(vo));
    }

    /**
     * 任务向下一个节点流转
     * @param id
     * @return
     */
    @GetMapping("/forward")
    public HttpResult transTaskStatusToNext(@RequestParam(name = "id") Long id,
                                            @RequestParam(name = "code", required = false) Long code,
                                            @RequestParam(name = "comment", required = false) String comment,
                                            @RequestParam(name = "alert", required = false) boolean alert) {
        String lockKey = "lock_taskforward_" + id;
        lockService.tryLock(lockKey);
        try {
            boolean isTransferToTestComplete = StringUtils.isNotBlank(comment);
            if (!isTransferToTestComplete) {
                reqTaskFacade.transTaskStatusToNext(id, false);
                return HttpResult.success();
            }
            // 流转到测试完成
            TestHistoryRecordDTO testHistoryRecordDTO = realTimeTestRecordService.getTestHistoryById(code);
            if (testHistoryRecordDTO != null && !testHistoryRecordDTO.getTestResult().equals(TestResultEnum.UNPASS.getType())) {
                reqTaskFacade.transTaskStatusToNext(id, true);
                return HttpResult.success();
            }
            //发送popo通知（测试记录不是通过）
            processIntoTestOverAlert(id, comment);
            throw new CommonException("测试记录为不通过");
        } finally {
            lockService.releaseLock(lockKey);
        }
    }

    private void processIntoTestOverAlert(Long taskId, String comment) {
        EisReqTask reqTask = reqTaskService.getById(taskId);
        List<TestHistoryRecordDTO> recordDTOList = realTimeTestRecordService.getTestHistoryByTaskId(taskId);
        String detailStr =  "操作埋点任务 " + reqTask.getTaskName() + " 到测试完成，任务测试记录为:";
        if(CollectionUtils.isEmpty(recordDTOList)){
            detailStr += "0/0/0/0";
        }else {
            long passNum = recordDTOList.stream().filter(dto -> dto.getTestResult().equals(TestResultEnum.PASS.getType())).count();
            long unPassNum = recordDTOList.stream().filter(dto -> dto.getTestResult().equals(TestResultEnum.UNPASS.getType())).count();
            long partPassNum = recordDTOList.stream().filter(dto -> dto.getTestResult().equals(TestResultEnum.PARTPASS.getType())).count();
            detailStr = detailStr + recordDTOList.size() + "/" + passNum + "/" + partPassNum + "/" + unPassNum;
        }
        auditReportService.buryPointTestAlert(detailStr, comment);
    }

    /**
     * 任务回退
     * @param id
     * @return
     */
    @GetMapping("/backward")
    public HttpResult backwardTaskStatus(Long id){
        reqTaskFacade.backwardTaskStatus(id);
        return HttpResult.success();
    }

    /**
     * 流程向下个节点流转
     * @param id
     * @return
     */
    @GetMapping("/process/forward")
    public HttpResult transProcessStatusToNext(Long id){
        reqTaskFacade.transProcessStatusToNext(id);
        return HttpResult.success();
    }

    /**
     * 流程回退
     * @param id
     * @return
     */
    @GetMapping("/process/backward")
    public HttpResult backwardProcessStatus(Long id){
        reqTaskFacade.backwardProcessStatus(id);
        return HttpResult.success();
    }

    /**
     * 批量向下个节点流转
     * @param vo
     * @return
     */
    @PostMapping("/forwardBatch")
    public HttpResult transToNextStatusBatch(@RequestBody TransStatusVO vo){
        reqTaskFacade.transToNextStatusBatch(vo);
        return HttpResult.success();
    }

    /**
     * 批量回退
     * @param vo
     * @return
     */
    @PostMapping("/backwardBatch")
    public HttpResult backwardStatusBatch(@RequestBody TransStatusVO vo){
        reqTaskFacade.backwardStatusBatch(vo);
        return HttpResult.success();
    }

    /**
     * 批量转交
     * @param deliverVO
     * @return
     */
    @PostMapping("/deliverBatch")
    public HttpResult deliverBatch(@RequestBody DeliverVO deliverVO){
        reqTaskFacade.deliverBatch(deliverVO);
        return HttpResult.success();
    }

    /**
     * 设置端版本
     * @param taskId
     * @param terminalVersion
     * @return
     */
    @GetMapping("/setTerminalVersion")
    public HttpResult setTerminalVersion(Long taskId, String terminalVersion){
        reqTaskFacade.setTerminalVersion(taskId,terminalVersion);
        return HttpResult.success();
    }

    /**
     * 设置迭代
     * @param taskId
     * @param sprint
     * @return
     */
    @GetMapping("/setSprint")
    public HttpResult setSprint(Long taskId, String sprint){
        reqTaskFacade.setSprint(taskId,sprint);
        return HttpResult.success();
    }

    /**
     * 查询条件聚合
     * @return
     */
    @GetMapping("/aggre")
    public HttpResult getSearchAggres(){
        return HttpResult.success(reqTaskFacade.getSearchAggre());
    }

}
