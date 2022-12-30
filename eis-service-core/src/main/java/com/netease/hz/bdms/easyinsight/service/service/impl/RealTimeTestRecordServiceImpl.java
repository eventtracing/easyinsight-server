package com.netease.hz.bdms.easyinsight.service.service.impl;


import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.realtimetest.TestHistoryRecordDTO;
import com.netease.hz.bdms.easyinsight.common.enums.TestStatusEnum;
import com.netease.hz.bdms.easyinsight.common.util.BeanConvertUtils;
import com.netease.hz.bdms.easyinsight.common.util.JsonUtils;
import com.netease.hz.bdms.easyinsight.dao.TestHistoryMapper;
import com.netease.hz.bdms.easyinsight.dao.model.TestHistoryRecord;
import com.netease.hz.bdms.easyinsight.service.service.RealTimeTestRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RealTimeTestRecordServiceImpl implements RealTimeTestRecordService {

    private static final Map<String, String> orderByMap = ImmutableMap
            .of("saveTime", "saveTime", "updateTime", "updateTime", "failedNum", "failedNum");
    private static final Map<String, String> orderRuleMap = ImmutableMap
            .of("descend", "desc", "ascend", "asc");

    @Autowired
    private TestHistoryMapper testHistoryMapper;

    @Override
    public List<TestHistoryRecordDTO> getTestHistory(Long code, Long taskId, Integer result, Long userId, String reqName, String terminal, String baseVer, Long startTime, Long endTime, Long appId, String orderBy, String orderRule, Integer offset, Integer count) {

        Preconditions.checkArgument(null != startTime, "开始时间不能为空");
        Preconditions.checkArgument(null != endTime, "结束时间不能为空");
        String realOrderBy = orderByMap.get(orderBy);
        String realOrderRule = orderRuleMap.get(orderRule);

        if(code != null){
            TestHistoryRecord testHistoryRecordInTable = testHistoryMapper.selectById(code);
            return Collections.singletonList(do2Dto(testHistoryRecordInTable));
        }
        List<TestHistoryRecord> testHistoryRecords = testHistoryMapper.selectHistoryRecords(userId, taskId, result, terminal, baseVer, reqName, startTime, endTime, realOrderBy, realOrderRule, offset, count, appId);
        return testHistoryRecords.stream().map(this::do2Dto).collect(Collectors.toList());

    }

    @Override
    public List<TestHistoryRecordDTO> getTestHistoryByTaskId(Long taskId) {

        List<TestHistoryRecord> testHistoryRecords = testHistoryMapper.selectByTaskId(taskId);
        return testHistoryRecords.stream().map(this::do2Dto).collect(Collectors.toList());

    }

    @Override
    public TestHistoryRecordDTO getTestHistoryById(Long id) {
        TestHistoryRecord testHistoryRecord = testHistoryMapper.selectById(id);
        return this.do2Dto(testHistoryRecord);

    }

    @Override
    public Long saveTestHistory(TestHistoryRecordDTO testHistoryRecordDTO) {

        int saveType = testHistoryRecordDTO.getStatus();
        TestHistoryRecord testHistoryRecord = dto2Do(testHistoryRecordDTO);
        TestHistoryRecord testHistoryRecordInTable = testHistoryMapper.selectById(testHistoryRecordDTO.getCode());
        Long appId = EtContext.get(ContextConstant.APP_ID);
        if(appId == null){
            appId = testHistoryRecordDTO.getAppId();
        }
        Integer ret;
        if(saveType == TestStatusEnum.INIT.getStatus() || testHistoryRecordInTable == null){
            if(testHistoryRecord.getUserId() == null) testHistoryRecord.setUserId(NumberUtils.LONG_ZERO);
            if(testHistoryRecord.getUserName() == null) testHistoryRecord.setUserName(StringUtils.EMPTY);
            if(testHistoryRecord.getReqName() == null) testHistoryRecord.setReqName(StringUtils.EMPTY);
            if(testHistoryRecord.getAppVersion() == null) testHistoryRecord.setAppVersion(StringUtils.EMPTY);
            if(testHistoryRecord.getBaseVersion() == null) testHistoryRecord.setBaseVersion(StringUtils.EMPTY);
            if(testHistoryRecord.getTerminal() == null) testHistoryRecord.setTerminal(StringUtils.EMPTY);
            if(testHistoryRecord.getFailedNum() == null) testHistoryRecord.setFailedNum(NumberUtils.LONG_ZERO);
            if(testHistoryRecord.getSaveTime() == null) testHistoryRecord.setSaveTime(NumberUtils.LONG_ZERO);
            if(testHistoryRecord.getAppId() == null) testHistoryRecord.setAppId(appId);
            testHistoryRecord.setTestResult(NumberUtils.INTEGER_ZERO);
            testHistoryRecord.setCreateTime(new Timestamp(System.currentTimeMillis()));
            testHistoryRecord.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            ret = testHistoryMapper.insert(testHistoryRecord);
        }else {
            ret =  testHistoryMapper.updateSelective(testHistoryRecord);
        }

        return ret != null && ret > NumberUtils.INTEGER_ZERO ? testHistoryRecord.getId() : NumberUtils.LONG_ZERO;
    }

    @Override
    public Integer getTestHistorySize(Long userId, Long taskId, Integer result, String reqName, String terminal, String baseVer, Long startTime, Long endTime, Long appId) {
        Preconditions.checkArgument(null != startTime, "开始时间不能为空");
        Preconditions.checkArgument(null != endTime, "结束时间不能为空");

        return testHistoryMapper.selectSizeFromRecords(userId, taskId, result, terminal, baseVer, reqName, startTime, endTime, appId);
    }


    public TestHistoryRecord dto2Do(TestHistoryRecordDTO testHistoryRecordDTO) {
        TestHistoryRecord testHistoryRecord = BeanConvertUtils.convert(testHistoryRecordDTO, TestHistoryRecord.class);
        if (null != testHistoryRecord) {
            Long code = testHistoryRecordDTO.getCode();
            testHistoryRecord.setId(code);
        }
        return testHistoryRecord;
    }

    public TestHistoryRecordDTO do2Dto(TestHistoryRecord testHistoryRecord) {
        TestHistoryRecordDTO recordDTO = BeanConvertUtils.convert(testHistoryRecord, TestHistoryRecordDTO.class);
        if (null != recordDTO) {
            Long code = testHistoryRecord.getId();
            recordDTO.setCode(code);

            Map<String, String> extMap = JsonUtils.parseMap(recordDTO.getExtInfo());
            recordDTO.setTargetUrl(extMap != null ? extMap.get("targetUrl") : "");
        }
        return recordDTO;
    }
}
