package com.netease.hz.bdms.eistest.service;

import com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.rulecheck.RuleCheckSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.param.auth.TestStatisticInfoParam;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;
import java.util.Map;

public interface BuryPointTestInfoCacheService {

    Triple<Long, Long, Long> getLogWebCount(String code);

    List<String> getInsightLogByPage(String code, int logType, long userIndex);

    long getInsightUserLogCount(String code, int logType);

    @Deprecated
    void setInsightLogWebCount(String code, int logType);

    void setLogWebCount(String code, int logType, long count);

    void setInsightUserLogCount(String code, int logType, int userOperate);

    void saveInsightLog(String code, RuleCheckSimpleDTO ruleCheck, Map<String,Long> eventBuryPointMap);

    boolean saveTestRecordInfo(TestStatisticInfoParam param);

}
