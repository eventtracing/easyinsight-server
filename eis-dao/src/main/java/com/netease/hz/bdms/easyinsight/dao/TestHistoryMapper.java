package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.TestHistoryRecord;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestHistoryMapper {

  List<TestHistoryRecord> selectHistoryRecords(@Param("userId") Long userId, @Param("taskId") Long taskId,
                                               @Param("testResult") Integer testResult, @Param("terminal") String terminal,
                                               @Param("baseVer") String baseVer, @Param("reqName") String reqName,
                                               @Param("startTime") Long startTime, @Param("endTime") Long endTime,
                                               @Param("orderBy") String orderBy, @Param("orderRule") String orderRule,
                                               @Param("offset") Integer offset, @Param("count") Integer count, @Param("appId") Long appId);

  Integer selectSizeFromRecords(@Param("userId") Long userId, @Param("taskId") Long taskId,
                                @Param("testResult") Integer testResult, @Param("terminal") String terminal,
                                @Param("baseVer") String baseVer, @Param("reqName") String reqName,
                                @Param("startTime") Long startTime, @Param("endTime") Long endTime,
                                @Param("appId") Long appId);


  Integer insert(TestHistoryRecord testHistoryRecord);

  TestHistoryRecord selectById(Long id);

  List<TestHistoryRecord> selectByTaskId(Long taskId);

  TestHistoryRecord selectByIdAndTime(Long id, Long startTime, Long endTime);

  Integer updateSelective(TestHistoryRecord testHistoryRecord);

  Integer update(TestHistoryRecord testHistoryRecord);

}
