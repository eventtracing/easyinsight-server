package com.netease.hz.bdms.easyinsight.service.service;

import com.netease.hz.bdms.easyinsight.common.dto.realtimetest.TestHistoryRecordDTO;
import java.util.List;


public interface RealTimeTestRecordService {

    List<TestHistoryRecordDTO> getTestHistory(Long code, Long taskId, Integer result, Long userId, String reqName, String terminal, String baseVer, Long startTime, Long endTime, Long appId, String orderBy, String orderRule, Integer offset, Integer count);

    List<TestHistoryRecordDTO> getTestHistoryByTaskId(Long taskId);

    TestHistoryRecordDTO getTestHistoryById(Long id);

    Long saveTestHistory(TestHistoryRecordDTO testHistoryRecordDTO);

    Integer getTestHistorySize(Long userId, Long taskId, Integer result, String reqName, String terminal, String baseVer, Long startTime, Long endTime, Long appId);

}
