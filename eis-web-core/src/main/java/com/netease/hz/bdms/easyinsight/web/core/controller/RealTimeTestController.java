package com.netease.hz.bdms.easyinsight.web.core.controller;


import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.common.PagingResultDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.PagingSortDTO;
import com.netease.hz.bdms.easyinsight.common.dto.realtimetest.TestHistoryRecordDTO;
import com.netease.hz.bdms.easyinsight.common.dto.realtimetest.TestHistorySearchRequestDTO;
import com.netease.hz.bdms.easyinsight.common.http.HttpResult;
import com.netease.hz.bdms.easyinsight.service.facade.BuryPointTestFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping
public class RealTimeTestController {

    @Autowired
    BuryPointTestFacade buryPointTestFacade;

    /**
     * 实时测试记录搜索
     *
     * @param testHistorySearchRequestDTO
     * @return {@link PagingResultDTO<TestHistoryRecordDTO>}
     * @author yangyichun
     */
    @PostMapping("/realtime/test/history/search")
    public HttpResult<PagingResultDTO<TestHistoryRecordDTO>> getResource(@RequestBody TestHistorySearchRequestDTO testHistorySearchRequestDTO) {

        PagingResultDTO<TestHistoryRecordDTO> pagingResultDTO = new PagingResultDTO<>();
        try {
            PagingSortDTO pagingSortDTO = new PagingSortDTO(testHistorySearchRequestDTO.getCurrentPage(), testHistorySearchRequestDTO.getPageSize(), testHistorySearchRequestDTO.getOrderBy(), testHistorySearchRequestDTO.getOrderRule());
            Long code = testHistorySearchRequestDTO.getCode();
            Long userId = testHistorySearchRequestDTO.getUserId();
            String reqName = testHistorySearchRequestDTO.getReqName();
            String terminal = testHistorySearchRequestDTO.getTerminal();
            String baseVer = testHistorySearchRequestDTO.getBaseVer();
            Long startTime = testHistorySearchRequestDTO.getStartTime();
            Long endTime = testHistorySearchRequestDTO.getEndTime();
            Long appId = testHistorySearchRequestDTO.getAppId();
            Long taskId = testHistorySearchRequestDTO.getTaskId();
            Integer result = testHistorySearchRequestDTO.getResult();
            if(appId == null){
                appId = EtContext.get(ContextConstant.APP_ID);
            }
            pagingResultDTO = buryPointTestFacade.getTestHistoryRecords(code, taskId, result, userId, reqName, terminal, baseVer, startTime, endTime, appId, pagingSortDTO);
            return HttpResult.success(pagingResultDTO);
        } finally {
            EtContext.clear();
        }
    }

    /**
     * 实时测试记录搜索
     *
     * @param taskId
     * @return {@link List<TestHistoryRecordDTO>}
     * @author yangyichun
     */
    @PostMapping("/realtime/test/task/history")
    public HttpResult<List<TestHistoryRecordDTO>> getRecordByTaskId(@RequestBody Long taskId) {
        try {
            List<TestHistoryRecordDTO> recordDTOS = buryPointTestFacade.getTestHistoryRecordsByTaskId(taskId);
            return HttpResult.success(recordDTOS);
        } finally {
            EtContext.clear();
        }
    }

    /**
     * 实时测试记录保存
     *
     * @param testHistoryRecordDTO
     * @return {@link Long}
     * @author yangyichun
     */
    @PostMapping("/realtime/test/history/save")
    public HttpResult<Long> saveTestHistory(@RequestBody TestHistoryRecordDTO testHistoryRecordDTO) {
        if(testHistoryRecordDTO == null){
            return HttpResult.success(null);
        }
        return HttpResult.success(buryPointTestFacade.saveTestHistoryRecord(testHistoryRecordDTO));
    }

}
