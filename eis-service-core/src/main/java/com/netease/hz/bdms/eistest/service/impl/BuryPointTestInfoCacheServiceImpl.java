package com.netease.hz.bdms.eistest.service.impl;

import com.netease.eis.adapters.CacheAdapter;
import com.netease.hz.bdms.easyinsight.common.dto.checkhistory.CheckHistorySimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.rulecheck.RuleCheckSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.realtimetest.TestHistoryRecordDTO;
import com.netease.hz.bdms.easyinsight.common.enums.BuryPointLogTypeEnum;
import com.netease.hz.bdms.easyinsight.common.enums.TestStatusEnum;
import com.netease.hz.bdms.easyinsight.common.param.auth.TestStatisticInfoParam;
import com.netease.hz.bdms.easyinsight.common.param.auth.UserBaseInfoParam;
import com.netease.hz.bdms.easyinsight.common.util.BeanConvertUtils;
import com.netease.hz.bdms.easyinsight.common.util.JsonUtils;
import com.netease.hz.bdms.eistest.client.ProcessorRpcAdapter;
import com.netease.hz.bdms.eistest.service.BuryPointTestInfoCacheService;
import com.netease.hz.bdms.eistest.cache.BuryPointProcessorKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class BuryPointTestInfoCacheServiceImpl implements BuryPointTestInfoCacheService {

    @Resource
    private CacheAdapter cacheAdapter;

    @Autowired
    ProcessorRpcAdapter processorRpcAdapter;

    private static final int expiredTime = 30 * 24 * 3600;

    @Override
    public Triple<Long, Long, Long> getLogWebCount(String code) {

        String insightCountKey = BuryPointProcessorKey.getBuryPointLogCountKey(code, BuryPointLogTypeEnum.INSIGHT.getCode());
        String insightCountStr = cacheAdapter.get(insightCountKey);
        long firstCount = StringUtils.isBlank(insightCountStr) ? 0 : Long.parseLong(insightCountStr);

        String oldVesionCountKey = BuryPointProcessorKey.getBuryPointLogCountKey(code, BuryPointLogTypeEnum.OLDVERSION.getCode());
        String oldVersionCountStr = cacheAdapter.get(oldVesionCountKey);
        long secondCount = StringUtils.isBlank(oldVersionCountStr) ? 0 : Long.parseLong(oldVersionCountStr);

        String exceptionCountKey = BuryPointProcessorKey.getBuryPointLogCountKey(code, BuryPointLogTypeEnum.EXCEPTION.getCode());
        String exceptionCountStr = cacheAdapter.get(exceptionCountKey);
        long thirdCount = StringUtils.isBlank(exceptionCountStr) ? 0 : Long.parseLong(exceptionCountStr);

        return Triple.of(firstCount, secondCount, thirdCount);
    }

    @Override
    public List<String> getInsightLogByPage(String code, int logType, long userIndex) {
        String cacheKey = BuryPointProcessorKey.getBuryPointLogKey(code, logType);
        long offset = userIndex == 0 ? -1 : -userIndex;
        return cacheAdapter.lrange(cacheKey, 0, offset);
    }

    @Override
    public long getInsightUserLogCount(String code, int logType) {
        String logUserCountKey = BuryPointProcessorKey.getBuryPointLogUserCountKey(code, logType);
        String logUserCountStr = cacheAdapter.get(logUserCountKey);
        return StringUtils.isBlank(logUserCountStr) ? 0 : Long.parseLong(logUserCountStr);
    }

    @Override
    public void setInsightLogWebCount(String code, int logType) {
        String logCountKey = BuryPointProcessorKey.getBuryPointLogCountKey(code, logType);
        String cacheKey = BuryPointProcessorKey.getBuryPointLogKey(code, logType);
        Long logCount = cacheAdapter.llen(cacheKey);
        cacheAdapter.setWithExpireTime(logCountKey, String.valueOf(logCount), expiredTime);

    }

    @Override
    public void setLogWebCount(String code, int logType, long logCount) {
        String logCountKey = BuryPointProcessorKey.getBuryPointLogCountKey(code, logType);
        cacheAdapter.setWithExpireTime(logCountKey, String.valueOf(logCount), expiredTime);
    }

    @Override
    public void setInsightUserLogCount(String code, int logType, int userOperate) {
        String logUserCountKey = BuryPointProcessorKey.getBuryPointLogUserCountKey(code, logType);
        String logWebCountKey = BuryPointProcessorKey.getBuryPointLogCountKey(code, logType);

        String logWebCountStr = cacheAdapter.get(logWebCountKey);
        long logWebCount = StringUtils.isBlank(logWebCountStr) ? 0 : Long.parseLong(logWebCountStr);

        long logUserCount;
        if(userOperate == 1){
            logUserCount = logWebCount;
        }else{
            logUserCount = NumberUtils.LONG_ZERO;
        }
        cacheAdapter.setWithExpireTime(logUserCountKey, String.valueOf(logUserCount), expiredTime);
    }

    @Override
    public void saveInsightLog(String code, RuleCheckSimpleDTO ruleCheck, Map<String,Long> eventBuryPointMap) {
        if(ruleCheck != null) {
//            if(ruleCheck.getCheckResult().equals(CheckResultEnum.NOT_PASS.getResult())){
//                String countKey = BuryPointProcessorKey.getBuryPointLogCheckErrorCountKey(code);
//                String countStr = redisClusterService.get(countKey);
//                int count = 1;
//                if(StringUtils.isNotBlank(countStr)){
//                    count+=Integer.parseInt(countStr);
//                }
//                redisClusterService.setWithExpireTime(countKey, String.valueOf(count), expiredTime);
//            }

            //
            String userkey = BuryPointProcessorKey.getBuryPointUserKey(code);
            String userInfoStr = cacheAdapter.get(userkey);
            UserBaseInfoParam param = JsonUtils.parseObject(userInfoStr, UserBaseInfoParam.class);
            UserSimpleDTO userSimpleDTO = new UserSimpleDTO();
            if(param != null){
                userSimpleDTO.setUserName(param.getUserName());
                userSimpleDTO.setEmail(param.getEmail());
            }

            List<CheckHistorySimpleDTO> resourceRequest = new ArrayList<>();
            CheckHistorySimpleDTO checkHistorySimple = BeanConvertUtils.convert(ruleCheck, CheckHistorySimpleDTO.class);
            if (checkHistorySimple != null) {
                checkHistorySimple.setIndicators(ruleCheck.getDetectionIndicator());
                checkHistorySimple.setSaver(userSimpleDTO);
                checkHistorySimple.setSaveTime(System.currentTimeMillis());
                checkHistorySimple.setType(1);
                //
                if(StringUtils.isBlank(ruleCheck.getSpm())){
                    //事件埋点
                    checkHistorySimple.setTrackerId(eventBuryPointMap.get(ruleCheck.getEventCode()));
                    checkHistorySimple.setSpm("");
                }
            }

            resourceRequest.add(checkHistorySimple);
            processorRpcAdapter.saveTestRecords(resourceRequest);
        }
    }

    @Override
    public boolean saveTestRecordInfo(TestStatisticInfoParam param) {
        String key = BuryPointProcessorKey.getUserTestRecordStaKey(param.getCode());
        //
        cacheAdapter.setWithExpireTime(key, JsonUtils.toJson(param), 86400 * 30); // 30d
        TestHistoryRecordDTO testHistoryRecordDTO = new TestHistoryRecordDTO();
        testHistoryRecordDTO.setCode(Long.parseLong(param.getCode()));
        testHistoryRecordDTO.setStatus(TestStatusEnum.RESULT.getStatus());
        testHistoryRecordDTO.setTestResult(param.getTestResult());
        processorRpcAdapter.saveTestHistoryRecord(testHistoryRecordDTO);

        return true;
    }
}
