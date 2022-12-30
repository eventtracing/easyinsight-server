package com.netease.hz.bdms.easyinsight.web.core.controller;


import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.checkhistory.CheckHistorySimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.PagingSortDTO;
import com.netease.hz.bdms.easyinsight.common.dto.obj.ObjectTrackerInfoDTO;
import com.netease.hz.bdms.easyinsight.common.dto.realtimetest.BranchCoverIgnoreRequestDTO;
import com.netease.hz.bdms.easyinsight.common.dto.realtimetest.RealTimeTestResourceDTO;
import com.netease.hz.bdms.easyinsight.common.dto.realtimetest.ResourceRequestDTO;
import com.netease.hz.bdms.easyinsight.common.exception.CommonException;
import com.netease.hz.bdms.easyinsight.common.http.HttpResult;
import com.netease.hz.bdms.easyinsight.common.vo.logcheck.BranchCoverageDetailVO;
import com.netease.hz.bdms.easyinsight.common.vo.obj.ObjDetailsVO;
import com.netease.hz.bdms.easyinsight.common.vo.requirement.BranchCoverageIgnoreVO;
import com.netease.hz.bdms.easyinsight.common.vo.requirement.ReqCheckHistoryPagingQueryVO;
import com.netease.hz.bdms.easyinsight.common.vo.requirement.ValidateResultDeleteVO;
import com.netease.hz.bdms.easyinsight.service.facade.BuryPointTestFacade;
import com.netease.hz.bdms.easyinsight.service.facade.ObjectFacade;
import com.netease.hz.bdms.easyinsight.service.service.impl.RealtimeBranchIgnoreService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/et/v1/realtime")
public class BuryPointTestController {


    @Autowired
    BuryPointTestFacade buryPointTestFacade;

    @Resource
    private ObjectFacade objectFacade;

    @Resource
    private RealtimeBranchIgnoreService realtimeBranchIgnoreService;


    /**
     * 实时测试获取源数据
     *
     * @param resourceRequestDTO
     * @return
     * @author yufangzheng
     */
    @PostMapping("/in/resource")
    public HttpResult getResource(@RequestBody ResourceRequestDTO resourceRequestDTO) {
        EtContext.put(ContextConstant.DOMAIN_ID, resourceRequestDTO.getDomainId());
        EtContext.put(ContextConstant.APP_ID, resourceRequestDTO.getAppId());
        RealTimeTestResourceDTO realTimeTestResourceDTO = new RealTimeTestResourceDTO();
        try {
            realTimeTestResourceDTO = buryPointTestFacade.getResourceDto(resourceRequestDTO.getTaskId(), resourceRequestDTO.getTerminalId());
            return HttpResult.success(realTimeTestResourceDTO);
        } finally {
            EtContext.clear();
        }
    }

    @GetMapping("reqTree")
    public HttpResult getTaskTree(Long taskId, Long terminalId) {
        return HttpResult.success(buryPointTestFacade.getReqTree(terminalId, taskId));
    }

    @GetMapping("baseTree")
    public HttpResult getBaseTree(Long terminalId) {
        return HttpResult.success(buryPointTestFacade.getBaseTree(terminalId));
    }

//    /**
//     * 查询条件聚合
//     *
//     * @return
//     * @author yufangzheng
//     */
//    @GetMapping("/aggre")
//    public HttpResult getQueryAggregate() {
//        return HttpResult.success(realTimeTestFacade2.getRealTimeAggreVO());
//    }

//    @PermissionAction(requiredPermission = PermissionEnum.RULE_CHECK)
//    @PostMapping("/validate")
//    public HttpResult validate(@RequestBody ValidateVO validateVO) {
//        realTimeTestFacade2.validate(validateVO.getTrackerIds());
//        return HttpResult.success();
//    }

    @PostMapping("/validate/save")
    public HttpResult saveCheckHistory(@RequestBody List<CheckHistorySimpleDTO> checkHistorySimpleDTOS) {
        return HttpResult.success(buryPointTestFacade.saveCheckHistory(checkHistorySimpleDTOS));
    }

    @DeleteMapping("/validate/cancel")
    public HttpResult saveCheckHistoryCancel(@RequestParam(name = "checkHistoryId") Long checkHistoryId) {
        return HttpResult.success(buryPointTestFacade.deleteCheckHistory(checkHistoryId));
    }

    @GetMapping("/checkresult/aggre")
    public HttpResult getCheckHistoryQueryAggre(@RequestParam(name = "processId") Long processId, @RequestParam Long terminalId, @RequestParam Long objId, @RequestParam Long reqPoolId, @RequestParam Long historyId) {
        processId = handleProcessId(terminalId, objId, reqPoolId, processId, historyId);
        return HttpResult.success(buryPointTestFacade.getCheckHistoryAggreDTO(processId));
    }

    @PostMapping("/checkresult/list")
    public HttpResult getCheckHistory(@RequestBody ReqCheckHistoryPagingQueryVO reqcheckHistoryPagingQueryVO) {
        PagingSortDTO pagingSortDTO = new PagingSortDTO(reqcheckHistoryPagingQueryVO.getCurrentPage(), reqcheckHistoryPagingQueryVO.getPageSize(), reqcheckHistoryPagingQueryVO.getOrderBy(), reqcheckHistoryPagingQueryVO.getOrderRule());
        Long processId = reqcheckHistoryPagingQueryVO.getProcessId();
        String eventCode = reqcheckHistoryPagingQueryVO.getEvents();
        Integer checkResult = reqcheckHistoryPagingQueryVO.getResult();
        String spm = reqcheckHistoryPagingQueryVO.getSpm();
        Long buryPointId = reqcheckHistoryPagingQueryVO.getEventBuryPointId();
        return HttpResult.success(buryPointTestFacade.listPagingResult(spm, buryPointId, processId,eventCode, checkResult, pagingSortDTO));
    }

    @PostMapping("/query/checkresult/list")
    public HttpResult queryCheckHistory(@RequestBody ReqCheckHistoryPagingQueryVO req) {
        Long processId = handleProcessId(req.getTerminalId(), req.getObjId(), req.getReqPoolId(), req.getProcessId(), req.getHistoryId());
        PagingSortDTO pagingSortDTO = new PagingSortDTO(req.getCurrentPage(), req.getPageSize(), req.getOrderBy(), req.getOrderRule());
        String eventCode = req.getEvents();
        Integer checkResult = req.getResult();
        String spm = req.getSpm();
        Long buryPointId = req.getEventBuryPointId();
        return HttpResult.success(buryPointTestFacade.queryPagingResult(spm, buryPointId, processId, eventCode, checkResult, pagingSortDTO));
    }

    /**
     * 如果没传processId，根据其他参数查一下
     */
    private Long handleProcessId(Long terminalId, Long objId, Long reqPoolId, Long processId, Long historyId) {
        if (processId != null) {
            return processId;
        }
        if (objId == null) {
            throw new CommonException("objId is null");
        }
        if (reqPoolId == null) {
            throw new CommonException("reqPoolId is null");
        }
        if (terminalId == null) {
            throw new CommonException("terminalId is null");
        }
        if (historyId == null) {
            throw new CommonException("historyId is null");
        }
        ObjDetailsVO objDetails = objectFacade.getObjectByHistory(objId, historyId, reqPoolId);
        if (CollectionUtils.isEmpty(objDetails.getTrackers())) {
            throw new CommonException("trackers is null");
        }
        List<ObjectTrackerInfoDTO> trackers = objDetails.getTrackers().stream().filter(o -> o.getTerminal() != null && terminalId.equals(o.getTerminal().getId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(trackers)) {
            throw new CommonException("无满足terminalId的tracker");
        }
        return trackers.get(0).getId();
    }

    @PostMapping("/checkresult/delete")
    public HttpResult deleteCheckHistory(@RequestBody ValidateResultDeleteVO validateResultDeleteVO) {
        buryPointTestFacade.deleteValidateRecord(validateResultDeleteVO.getId());
        return HttpResult.success();
    }

    @PostMapping("/branchCover/ignore")
    public HttpResult addBranchCoverIgnore(@RequestBody BranchCoverageIgnoreVO branchCoverageIgnoreVO) {
        realtimeBranchIgnoreService.add(branchCoverageIgnoreVO.getConversationId(), branchCoverageIgnoreVO.getBranches());
        return HttpResult.success(true);
    }

    @PostMapping("/branchCover/ignore/get")
    public HttpResult getBranchCoverIgnore(@RequestBody BranchCoverIgnoreRequestDTO branchCoverIgnoreRequestDTO) {
        if (branchCoverIgnoreRequestDTO == null || StringUtils.isBlank(branchCoverIgnoreRequestDTO.getConversationId())) {
            return HttpResult.success(new ArrayList<>(0));
        }
        List<BranchCoverageDetailVO> branchCoverageDetailVOS = realtimeBranchIgnoreService.listAll(branchCoverIgnoreRequestDTO.getConversationId());
        return HttpResult.success(branchCoverageDetailVOS);
    }

    @RequestMapping("/branchCover/ignore/remove")
    public HttpResult removeBranchCoverIgnore(@RequestParam String conversationId) {
        if (StringUtils.isBlank(conversationId)) {
            return HttpResult.success(new ArrayList<>(0));
        }
        List<BranchCoverageDetailVO> branchCoverageDetailVOS = realtimeBranchIgnoreService.listAll(conversationId);
        realtimeBranchIgnoreService.removeAll(conversationId);
        return HttpResult.success(branchCoverageDetailVOS);
    }
}
