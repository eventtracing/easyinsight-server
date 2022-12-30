package com.netease.hz.bdms.eistest.ws.dto;

import com.google.common.collect.Maps;
import com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.statistic.EventCheckResultDTO;
import com.netease.hz.bdms.easyinsight.common.enums.BuryPointErrorCategoryEnum;
import com.netease.hz.bdms.easyinsight.common.enums.BuryPointLogTypeEnum;
import com.netease.hz.bdms.easyinsight.common.enums.CheckResultEnum;
import com.netease.hz.bdms.easyinsight.service.service.util.LogUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理埋点日志实时测试的统计结果
 *
 * @author wangliangyuan
 * @date 2021-09-09 上午 10:57
 */
@Slf4j
@Component
@Data
public class BuryPointStats {


    /**
     * 当前会话下统计到的 新版本日志 中事件类型code的个数映射关系
     * key = 事件类型code,
     * value = <spm, num>
     */
    private final Map<String, Map<String, Integer>> eventCodeToSpmToNumMap = new ConcurrentHashMap<>();
    /**
     * 当前会话下统计到的 旧版本日志 中事件类型code的个数映射关系
     * key = 事件类型code,
     * value = <spm, num>
     */
    private final Map<String, Map<String, Integer>> eventCodeToMspmToNumInOldVersionMap = new ConcurrentHashMap<>();

    /**
     * map, 用来统计未知(即未定义)的事件类型
     * key = 未知(即未定义)的事件类型 code
     * value = 总数
     */
    private final Map<String, Integer> undefinedEventCodeToCountMap = new ConcurrentHashMap<>();

    /**
     * 前端页面上的 [树模式校验] 的统计结果
     * key = spm,
     * value = <事件类型 code,EventCheckResultDTO>
     */
    private final Map<String, Map<String, EventCheckResultDTO>> spmToEventToCheckResultMap = new ConcurrentHashMap<>();

    /**
     * 前端页面上的 未匹配spm 的统计结果
     * key = spm,
     * value = <事件类型 code,EventCheckResultDTO>
     */
    private final Map<String, Long> spmUnknownCheckResultMap = new ConcurrentHashMap<>();

    /**
     * 前端页面上的 纯事件 的统计结果
     * key = spm,
     * value = <事件类型 code,EventCheckResultDTO>
     */
    private final Map<String, EventCheckResultDTO> EventToCheckResultMap = new ConcurrentHashMap<>();

    /**
     * 统计错误信息的 map,
     * key = 事件类型 code,
     * value = 每个错误类型出现的次数
     */
    private final Map<String, EnumMap<BuryPointErrorCategoryEnum, Integer>> eventCodeToErrorStatisticsMap = new ConcurrentHashMap<>();

    /**
     * 前端页面上的 校验未通过 的统计结果
     * key = logType,
     * value = <校验未通过的数目>
     */
    private final Map<String, Integer> logToCheckFailedNumMap = new ConcurrentHashMap<>();

    public void clearAllStatisticsResultInTargetConversation() {
        getEventCodeToSpmToNumMap().clear();
        log.info("事件-SPM统计清理完成");
        getEventCodeToMspmToNumInOldVersionMap().clear();
        log.info("老版本事件-SPM统计清理完成");
        getUndefinedEventCodeToCountMap().clear();
        log.info("未定义区统计清理完成");
        getSpmToEventToCheckResultMap().clear();
        log.info("树模式校验结果清理完成");
        getEventCodeToErrorStatisticsMap().clear();
        log.info("错误统计清理完成");
    }

    /**
     * 计算新版本日志中的每个spm的数目
     *
     * @param eventCode    事件类型Code
     * @param spm          spm，若为空，则归为未知类型
     * @param num          数目
     */
    public void saveStatistic(String eventCode, String spm, Integer num) {
        Map<String, Map<String, Integer>> eventCodeToSpmToNumMap = this.getLogStatistic();
        Map<String, Integer> spmToNumMap = eventCodeToSpmToNumMap.computeIfAbsent(eventCode, k -> Maps.newHashMap());

        if (StringUtils.isBlank(spm)) {
            // 当spm为空时，统计为未知spm
            spm = LogUtil.UNKNOWN_SPM_KEYWORD;
        }

        Integer count = spmToNumMap.getOrDefault(spm, 0);
        count += num;
        spmToNumMap.put(spm, count);
    }

    /**
     * 获取新版本日志中的每个spm的数目的统计
     *
     * @return 新版本日志中的每个spm的数目的统计
     */
    public Map<String, Map<String, Integer>> getLogStatistic() {
        return this.getEventCodeToSpmToNumMap();
    }

    /**
     * 计算旧版本日志中的每个spm的数目
     *
     * @param eventCode 事件类型Code
     * @param spm       spm，若为空，则归为未知类型
     * @param num       数目
     */
    public void saveMspmStatisticInOldVersion(String eventCode, String spm, Integer num) {
        Map<String, Map<String, Integer>> eventCodeToMspmToNumInOldVersionMap = this.getLogStatisticInOldVersion();
        Map<String, Integer> mspmToNumMap = eventCodeToMspmToNumInOldVersionMap.computeIfAbsent(eventCode, k -> Maps.newHashMap());

        if (StringUtils.isBlank(spm)) {
            // 当spm为空时，统计为未知mspm
            spm = LogUtil.UNKNOWN_MSPM_KEYWORD;
        }

        Integer count = mspmToNumMap.getOrDefault(spm, 0);
        count += num;
        mspmToNumMap.put(spm, count);
    }

    /**
     * 获取旧版本日志中的每个spm的数目的统计
     *
     * @return 旧版本日志中的每个spm的数目的统计
     */
    public Map<String, Map<String, Integer>> getLogStatisticInOldVersion() {
        return this.getEventCodeToMspmToNumInOldVersionMap();
    }

    /**
     * 统计错误信息
     *
     * @param eventCode 事件 code
     * @param code      BuryPointErrorCategoryEnum#code
     * @param count     code 对应的总数
     */
    public void saveErrorStatistics(String eventCode, Integer code, Integer count) {
        Map<String, EnumMap<BuryPointErrorCategoryEnum, Integer>> eventCodeToErrorStatisticsMap = this.getErrorStatistics();
        EnumMap<BuryPointErrorCategoryEnum, Integer> errorCategoryEnumIntegerEnumMap =
                eventCodeToErrorStatisticsMap.computeIfAbsent(eventCode, k -> Maps.newEnumMap(BuryPointErrorCategoryEnum.class));

        BuryPointErrorCategoryEnum errorCategoryEnum = BuryPointErrorCategoryEnum.match(code);

        Integer sum = errorCategoryEnumIntegerEnumMap.getOrDefault(errorCategoryEnum, 0);
        sum += count;
        errorCategoryEnumIntegerEnumMap.put(errorCategoryEnum, sum);
    }

    /**
     * 获取错误统计
     *
     * @return 错误统计 map
     */
    public Map<String, EnumMap<BuryPointErrorCategoryEnum, Integer>> getErrorStatistics() {
        return this.getEventCodeToErrorStatisticsMap();
    }

    /**
     * 统计未知(即未定义)的事件类型
     *
     * @param eventCode 未知(即未定义)的事件 code
     * @param count     累加的数量
     */
    public void saveUndefinedEventStatistics(String eventCode, Integer count) {
        Map<String, Integer> undefinedEventCodeToCountMap = this.getUndefinedEventStatistics();
        if (StringUtils.isBlank(eventCode)) {
            eventCode = LogUtil.UNKNOWN_EVENT;
        }
        Integer sum = undefinedEventCodeToCountMap.getOrDefault(eventCode, 0);
        sum += count;
        undefinedEventCodeToCountMap.put(eventCode, sum);
    }

    /**
     * 获取未知(即未定义)的事件类型的统计结果
     *
     * @return 未知(即未定义)的事件类型的统计 map
     */
    public Map<String, Integer> getUndefinedEventStatistics() {
        return this.getUndefinedEventCodeToCountMap();
    }

    /**
     * 统计事件类型的校验结果
     *
     * @param spm         超级位置模型
     * @param eventCode   事件类型 code
     * @param checkResult 事件类型的校验结果
     */
    public void saveEventCheckResultStatistics(String spm, String eventCode, Integer checkResult) {
        Map<String, Map<String, EventCheckResultDTO>> spmToEventToCheckResultMap = this.getEventCheckResultStatistics();

        Map<String, EventCheckResultDTO> onlyEventToCheckResultMap = this.getOnlyEventCheckResultStatistics();

        Map<String, Integer> logToCheckFailedNumMap = this.getCheckFailedNumMap();

        if (StringUtils.isBlank(spm)) {
            spm = LogUtil.UNKNOWN_SPM_KEYWORD;
            EventCheckResultDTO onlyEventCheckResultDTO = onlyEventToCheckResultMap.computeIfAbsent(eventCode, k -> EventCheckResultDTO.init());
            if(checkResult.equals(CheckResultEnum.PASS.getResult())){
                Integer passSum = onlyEventCheckResultDTO.getPassSum();
                passSum++;
                onlyEventCheckResultDTO.setPassSum(passSum);
            }else if(checkResult.equals(CheckResultEnum.NOT_PASS.getResult())){
                Integer failSum = onlyEventCheckResultDTO.getFailSum();
                failSum++;
                onlyEventCheckResultDTO.setFailSum(failSum);
            }
        }
        Map<String, EventCheckResultDTO> eventToCheckResultMap = spmToEventToCheckResultMap.computeIfAbsent(spm, k -> Maps.newHashMap());

        if (StringUtils.isBlank(eventCode)) {
            eventCode = LogUtil.UNKNOWN_EVENT;
        }
        EventCheckResultDTO eventCheckResultDTO = eventToCheckResultMap.computeIfAbsent(eventCode, k -> EventCheckResultDTO.init());

        CheckResultEnum checkResultEnum = CheckResultEnum.fromResult(checkResult);
        log.info("事件统计switch");
        switch (checkResultEnum) {
            case PASS: {
                Integer passSum = eventCheckResultDTO.getPassSum();
                passSum++;
                eventCheckResultDTO.setPassSum(passSum);
                break;
            }
            case NOT_PASS: {
                Integer failSum = eventCheckResultDTO.getFailSum();
                failSum++;
                eventCheckResultDTO.setFailSum(failSum);

                //
                Integer checkFailNum = logToCheckFailedNumMap.computeIfAbsent(BuryPointLogTypeEnum.INSIGHT.name(), k -> NumberUtils.INTEGER_ZERO);
                checkFailNum++;
                logToCheckFailedNumMap.put(BuryPointLogTypeEnum.INSIGHT.name(), checkFailNum);
                break;
            }
            case NO_MATCH_SPM:{
                //
                Integer checkFailNum = logToCheckFailedNumMap.computeIfAbsent(BuryPointLogTypeEnum.INSIGHT.name(), k -> NumberUtils.INTEGER_ZERO);
                checkFailNum++;
                logToCheckFailedNumMap.put(BuryPointLogTypeEnum.INSIGHT.name(), checkFailNum);
                break;
            }
            default: {
                // do nothing
            }
        }
    }


    /**
     * 统计未匹配spm的校验结果
     * @param spm
     */
    public void saveUnknownSpmResultStatistics(String spm) {

        Map<String, Long> unknownSpmCheckResult = this.getUnknownSpmCheckResultStatistics();

        Long unknownNum = unknownSpmCheckResult.computeIfAbsent(spm, k -> NumberUtils.LONG_ZERO);
        unknownNum++;
        unknownSpmCheckResult.put(spm, unknownNum);

    }

    /**
     * 获取事件类型校验的统计结果
     *
     * @return 事件类型校验的统计结果
     */
    public Map<String, Map<String, EventCheckResultDTO>> getEventCheckResultStatistics() {
        return this.getSpmToEventToCheckResultMap();
    }

    public Map<String,  EventCheckResultDTO> getOnlyEventCheckResultStatistics() {
        return this.getEventToCheckResultMap();
    }

    public Map<String,  Long> getUnknownSpmCheckResultStatistics() {
        return this.getSpmUnknownCheckResultMap();
    }

    public Map<String, Integer> getCheckFailedNumMap(){
        return this.getLogToCheckFailedNumMap();
    }

}
