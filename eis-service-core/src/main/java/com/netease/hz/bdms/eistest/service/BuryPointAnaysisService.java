package com.netease.hz.bdms.eistest.service;

import com.netease.hz.bdms.easyinsight.common.dto.realtimetest.RealTimeTestResourceDTO;
import com.netease.hz.bdms.easyinsight.service.service.audit.BuryPointRule;
import com.netease.hz.bdms.eistest.entity.BuryPointLogExceptionStatisticsResult;
import com.netease.hz.bdms.eistest.entity.BuryPointLogRuleCheckDto;
import com.netease.hz.bdms.eistest.entity.BuryPointValidationAndStatistics;
import com.netease.hz.bdms.eistest.entity.AppPushLogAction;
import com.netease.hz.bdms.eistest.ws.session.AppSession;

import java.util.List;
import java.util.Map;

public interface BuryPointAnaysisService {

    void parseBuryPointResource(String conversation, String message, AppSession as) throws InterruptedException;

    void parseBuryPointResource(String conversation, AppSession as, String content, String action) throws InterruptedException;

    Map<String, BuryPointRule> getBuryPointStatistics(RealTimeTestResourceDTO realTimeTestResourceDTO);

    List<BuryPointLogRuleCheckDto> parseBuryPointLog(List<String> buryPointLogString, int logType,String oid, String evnetId, String spm, String searchStr, String checkType);

    Map<String, Object> parseBuryPointToES(BuryPointValidationAndStatistics validationAndStats, BuryPointLogExceptionStatisticsResult exceptionStatisticsResult, String code, AppPushLogAction appPushLogAction);

    List<Map<String, Object>> parseParamToES(BuryPointValidationAndStatistics validationAndStats, BuryPointRule rule, BuryPointRule eventRule, String code);

    List<Map<String, Object>> parseCacheLogToES(List<String> buryPointLogString, int logType, long code, long index);

}
