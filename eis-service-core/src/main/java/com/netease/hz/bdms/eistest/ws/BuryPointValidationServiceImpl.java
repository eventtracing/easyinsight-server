package com.netease.hz.bdms.eistest.ws;

import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.oldversion.OldVersionLogSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.rulecheck.CompareItemSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.rulecheck.DetectionIndicatorsSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.rulecheck.IndicatorSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.rulecheck.RuleCheckSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.statistic.*;
import com.netease.hz.bdms.easyinsight.common.enums.BuryPointLogTypeEnum;
import com.netease.hz.bdms.easyinsight.common.enums.CheckResultEnum;
import com.netease.hz.bdms.easyinsight.common.util.JsonUtils;
import com.netease.hz.bdms.easyinsight.service.service.audit.BuryPointRule;
import com.netease.hz.bdms.easyinsight.service.service.util.LogUtil;
import com.netease.hz.bdms.easyinsight.common.util.TimeUtil;
import com.netease.hz.bdms.eistest.entity.BuryPointLog;
import com.netease.hz.bdms.eistest.entity.BuryPointValidationAndStatistics;
import com.netease.hz.bdms.eistest.ws.dto.AppStorage;
import com.netease.hz.bdms.eistest.ws.dto.PcStorage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class BuryPointValidationServiceImpl {

    /**
     * 转换PList, EList的结构（在不校验的情况下）
     *
     * @param list key表示oid, value表示对应的日志属性
     * @return 日志集合， key表示日志属性key，value表示匹配结果
     */
    public static List<Map<String, CompareItemSimpleDTO>> parseList(LinkedHashMap<String, Map<String, Object>> list) {
        List<Map<String, CompareItemSimpleDTO>> result = Lists.newArrayList();
        if (MapUtils.isNotEmpty(list)) {
            for (String oid : list.keySet()) {
                Map<String, CompareItemSimpleDTO> resultElement = Maps.newHashMap();

                Map<String, Object> logElement = list.get(oid);
                for (String logKey : logElement.keySet()) {
                    CompareItemSimpleDTO otherInLog = new CompareItemSimpleDTO();
                    otherInLog.setKey(logKey)
                            .setValue(String.valueOf(logElement.get(logKey)))
                            .setCause("")
                            .setComment("");
                    resultElement.put(otherInLog.getKey(), otherInLog);
                }
                result.add(resultElement);
            }
        }
        return result;
    }

    /**
     * 根据eventCode2Spm2NumMap中整理为界面上所能展示的统计结果
     *
     * @param eventCode2Spm2NumMap 事件类型下的埋点日志数据（key表示对象埋点spm, value表示数目）
     * @param eventCode2NameMap    key表示oid, value表示对应的日志属性
     * @return 统计结果
     */
    private static LogStatisticsSimpleDTO parseStats(Map<String, Map<String, Integer>> eventCode2Spm2NumMap,
                                                     Map<String, String> eventCode2NameMap) {
        List<LogEventStatisticSimpleDTO> logEventStatistics = Lists.newArrayList();
        int logNum = 0, objTrackerNum = 0;
        if (MapUtils.isNotEmpty(eventCode2Spm2NumMap)) {
            for (String tmpEventCode : eventCode2Spm2NumMap.keySet()) {
                // 统计spm的数量情况
                int eventLogNum = 0;
                int eventObjTrackerNum = 0;
                List<LogSpmStatisticSimpleDTO> logSpms = Lists.newArrayList();
                Map<String, Integer> spm2NumMap = eventCode2Spm2NumMap.get(tmpEventCode);
                if (MapUtils.isNotEmpty(spm2NumMap)) {
                    for (String tmpSpm : spm2NumMap.keySet()) {
                        Integer tmpSpmNum = spm2NumMap.get(tmpSpm);
                        LogSpmStatisticSimpleDTO logSpm = new LogSpmStatisticSimpleDTO();
                        logSpm.setSpm(tmpSpm);
                        logSpm.setNum(tmpSpmNum);
                        logSpms.add(logSpm);

                        // 当为未知spm，或者mspm时，日志总数目进行累计，埋点总数目不进行累计
                        eventLogNum += tmpSpmNum;
                        logNum += tmpSpmNum;
                        if (!LogUtil.UNKNOWN_MSPM_KEYWORD.equals(tmpSpm) &&
                                !LogUtil.UNKNOWN_SPM_KEYWORD.equals(tmpSpm)) {
                            eventObjTrackerNum++;
                            objTrackerNum++;
                        }
                    }
                }

                String tmpEventName = eventCode2NameMap.containsKey(tmpEventCode) ? eventCode2NameMap.get(tmpEventCode) : "";
                LogEventStatisticSimpleDTO logEventStatistic = new LogEventStatisticSimpleDTO();
                logEventStatistic.setEventCode(tmpEventCode)
                        .setEventName(tmpEventName)
                        .setLogNum(eventLogNum)
                        .setObjTrackerNum(eventObjTrackerNum)
                        .setSpmStatistics(logSpms);
                logEventStatistics.add(logEventStatistic);
            }
        }

        LogStatisticsSimpleDTO logStatistic = new LogStatisticsSimpleDTO();
        logStatistic.setLogNum(logNum)
                .setObjTrackerNum(objTrackerNum)
                .setEventStatistics(logEventStatistics);
        return logStatistic;
    }

    /**
     * 验证以及统计
     *
     * @param buryPointLog 日志JSON， BuryPointLog形式
     * @param logOnly      是否只关注实时日志
     * @return 验证及统计结果
     */
    public BuryPointValidationAndStatistics validateAndStat(AppStorage appStorage, PcStorage pcStorage, BuryPointLog buryPointLog, Boolean logOnly) {
        String eventCode = buryPointLog.getAction();

        Long logServerTime = TimeUtil.parseTimeStr(buryPointLog.getLogTime() != null ? buryPointLog.getLogTime().toString() : null);
        Map<String, Object> logMap = JsonUtils.parseMap(buryPointLog.getContent());
        String spm = LogUtil.getSpm(logMap);

        String spmWithoutPos = LogUtil.removePos(spm);
        String mspmInOldVersion = LogUtil.getMspmInOldVersion(logMap);
        Long index = buryPointLog.getIndex();
        boolean oldVersion = buryPointLog.getEt() == null || !buryPointLog.getEt();
        RuleCheckSimpleDTO ruleCheck = null;
        LogStatisticsSimpleDTO logStatistics;
        LogStatisticsSimpleDTO oldVersionLogStatistics;
        OldVersionLogSimpleDTO oldVersionLog = null;

        Map<String, String> eventCode2NameMap = pcStorage.getMetaInfo().getEventCodeToNameMap();
        // 老版本
        if (oldVersion) {
            oldVersionLog = oldVersionLog(logMap, eventCode, mspmInOldVersion, logServerTime, index);
            oldVersionStatisticSumUp(appStorage, eventCode, mspmInOldVersion);

            // 新版本
        } else {
            String eventName = StringUtils.isNotBlank(eventCode) && eventCode2NameMap != null ? eventCode2NameMap.get(eventCode) : "";
            // 如果是未定义的事件类型
            if (StringUtils.isBlank(eventName)) {
                // 统计未定义的事件类型
                statisticUndefinedEvent(appStorage, eventCode);

                // 如果是已知的事件类型
            } else {
                Map<String, String> oid2NameMap = pcStorage.getMetaInfo().getOid2NameMap();
                if (Boolean.TRUE.equals(logOnly)) {
                    ruleCheck = newLogForNoRule(logMap, spmWithoutPos, eventCode, eventName, logServerTime, index, logOnly);
                } else {
                    //纯事件埋点校验(elist and plist empty)
                    if(MapUtils.isEmpty(LogUtil.getPlist(logMap)) && MapUtils.isEmpty(LogUtil.getElist(logMap))){
                        BuryPointRule rule = pcStorage.getMetaInfo().getEventRule();
                        ruleCheck = rule.validate("event", logMap, eventCode, eventName, oid2NameMap, logServerTime, index);
                    }else {
                        //去除浮层的父对象路径
                        if (spmWithoutPos != null) {
                            spmWithoutPos = pcStorage.getMetaInfo().removePopParentsOfLog(spmWithoutPos);
                        }
                        BuryPointRule rule = pcStorage.getMetaInfo().getBuryPointRule(spmWithoutPos);

                        if (rule == null) {
                            if (eventCode != null && !eventCode.startsWith("ET")) {
                                log.info("spm未匹配，index={},spm={},spmWithoutPos={},eventCode={}", index, spm, spmWithoutPos, eventCode);
                            }
                            //统计未匹配spm
                            statisticUnmatchSpmResult(appStorage, spmWithoutPos);
                            ruleCheck = newLogForNoRule(logMap, spmWithoutPos, eventCode, eventName, logServerTime, index, logOnly);
                        } else {
                            ruleCheck = rule.validate("spm", logMap, eventCode, eventName, oid2NameMap, logServerTime, index);
                            // 浮层没有父对象，所有spm也应截断到浮层为止
                            if (ruleCheck != null && ruleCheck.getSpm() != null) {
                                ruleCheck.setSpm(pcStorage.getMetaInfo().removePopParentsOfLog(ruleCheck.getSpm()));
                            }
                        }
                    }
                }
                statisticSumUp(appStorage, eventCode, spmWithoutPos);

                if (ruleCheck != null) {
                    Integer checkResult = ruleCheck.getCheckResult();
                    // 统计 通过/不通过 的数量
                    statisticEventCheckResult(appStorage, spmWithoutPos, eventCode, checkResult);
                }
            }
        }
        oldVersionLogStatistics = oldVersionStat(appStorage, eventCode2NameMap);
        logStatistics = stat(appStorage, eventCode2NameMap);

        // 获取未定义事件的统计结果
        UndefinedEventStatisticsResultDTO undefinedEventStatistics = getUndefinedEventStatistics(appStorage);
        // 获取校验的统计结果
        List<TreeModeStatisticResultDTO> treeModeStatisticResult = getTreeModeStatisticResult(appStorage);
        EventStatisticResultDTO eventStatisticResult = getEventStatisticResult(appStorage);
        List<UnMatchSpmStatisticResultDTO> unMatchSpmStatisticResult = getUnknownStatisticResult(appStorage, pcStorage.getMetaInfo().getOid2NameMap() );
        return BuryPointValidationAndStatistics.builder()
                .ruleCheck(ruleCheck)
                .statistics(logStatistics)
                .oldVersionStatistics(oldVersionLogStatistics)
                .oldVersionLog(oldVersionLog)
                .spm(spmWithoutPos)
                .index(index)
                // 以下统计结果是 1.4.0 版本新增的
                .undefinedStatistics(undefinedEventStatistics)
                .treeModeStatistic(treeModeStatisticResult)
                .eventStatistic(eventStatisticResult)
                .unMatchSpmStatistic(unMatchSpmStatisticResult)
                .build();
    }

    /**
     * 将老版本日志转换为页面上需要显示的一条日志记录
     *
     * @param logMap        老版本日志原文
     * @param eventCode     事件类型code
     * @param mspm          mspm值
     * @param logServerTime 日志获取时间
     * @param index         序号
     * @return 页面上显示所需的日志记录
     */
    private OldVersionLogSimpleDTO oldVersionLog(Map<String, Object> logMap, String eventCode, String mspm, Long logServerTime, Long index) {
        OldVersionLogSimpleDTO result = new OldVersionLogSimpleDTO();
        Map<String, Object> log = Maps.newHashMap();
        if (MapUtils.isNotEmpty(logMap)) {
            for (String logKey : logMap.keySet()) {
                CompareItemSimpleDTO otherInLog = new CompareItemSimpleDTO();
                String val = null;
                if (logMap.get(logKey) instanceof ArrayList) {
                    val = JSONArray.toJSONString(logMap.get(logKey));
                    val = val.replaceAll("\"", "");
                } else {
                    val = String.valueOf(logMap.get(logKey));
                }
                otherInLog.setKey(logKey)
                        .setValue(val)
                        .setCause("")
                        .setComment("");
                log.put(otherInLog.getKey(), otherInLog);
            }
        }

        // 补充index
        CompareItemSimpleDTO indexInLog = new CompareItemSimpleDTO();
        indexInLog.setKey(LogUtil.INDEX_KEYWORD)
                .setValue(index != null ? index.toString() : "")
                .setCause("")
                .setComment("");
        log.put(LogUtil.INDEX_KEYWORD, indexInLog);

        String targetId = LogUtil.getString(logMap, LogUtil.OLD_TARGETID_KEYWORD);
        String resourceId = LogUtil.getString(logMap, LogUtil.OLD_RESOURCEID_KEYWORD);
        String resourceType = LogUtil.getString(logMap, LogUtil.OLD_RESOURCETYPE_KEYWORD);

        result.setAction(eventCode)
                .setLogServerTime(logServerTime)
                .setTargetId(targetId != null ? targetId : "")
                .setResourceId(resourceId != null ? resourceId : "")
                .setResourceType(resourceType != null ? resourceType : "")
                .setMspm(mspm != null ? mspm : "")
                .setLog(log);

        return result;
    }

    /**
     * 为旧版本日志累计
     *
     * @param eventCode 事件类型
     * @param mspm      mspm值
     */
    private void oldVersionStatisticSumUp(AppStorage appStorage, String eventCode, String mspm) {
        appStorage.getStats().saveMspmStatisticInOldVersion(eventCode, mspm, 1);
    }

    /**
     * 统计未定义的事件类型
     *
     * @param eventCode 事件类型
     */
    private void statisticUndefinedEvent(AppStorage appStorage, String eventCode) {
        appStorage.getStats().saveUndefinedEventStatistics(eventCode, 1);
    }

    /**
     * 将新版本日志转换为页面上需要显示的一条日志记录
     *
     * @param logMap        老版本日志原文
     * @param eventCode     事件类型code
     * @param spm           spm值
     * @param logServerTime 日志获取时间
     * @param index         序号
     * @return 页面上显示所需的日志记录
     */
    private RuleCheckSimpleDTO newLogForNoRule(Map<String, Object> logMap, String spm, String eventCode, String eventName, Long logServerTime, Long index, Boolean logOnly) {
        log.info("新日志记录开始");
        String rootPageOid = LogUtil.getRootPageOid(spm);
        String firstObjOid = LogUtil.getFirstObjOid(spm);
        List<Map<String, CompareItemSimpleDTO>> pListMapsInLog = parseList(LogUtil.getPlist(logMap));
        List<Map<String, CompareItemSimpleDTO>> eListMapsInLog = parseList(LogUtil.getElist(logMap));
        Map<String, CompareItemSimpleDTO> otherMapInLog = Maps.newHashMap();
        if (MapUtils.isNotEmpty(logMap)) {
            for (String logKey : logMap.keySet()) {
                if (LogUtil.PLIST_KEYWORD.equals(logKey) || LogUtil.ELIST_KEYWORD.equals(logKey)) {
                    continue;
                }

                CompareItemSimpleDTO otherInLog = new CompareItemSimpleDTO();
                otherInLog.setKey(logKey)
                        .setValue(String.valueOf(logMap.get(logKey)))
                        .setCause("")
                        .setComment("");
                otherMapInLog.put(otherInLog.getKey(), otherInLog);
            }
        }


        // 补充index
        CompareItemSimpleDTO indexInLog = new CompareItemSimpleDTO();
        indexInLog.setKey(LogUtil.INDEX_KEYWORD)
                .setValue(index != null ? index.toString() : "")
                .setCause("")
                .setComment("");
        otherMapInLog.put(LogUtil.INDEX_KEYWORD, indexInLog);

        Map<String, Object> logCheck = Maps.newHashMap();
        logCheck.put(LogUtil.PLIST_KEYWORD, pListMapsInLog);
        logCheck.put(LogUtil.ELIST_KEYWORD, eListMapsInLog);
        logCheck.putAll(otherMapInLog);

        DetectionIndicatorsSimpleDTO detectionIndicator = new DetectionIndicatorsSimpleDTO();
        detectionIndicator.setPrivateParamCompletion(new IndicatorSimpleDTO(0, 0, 100.00, false))
                .setPrivateParamSuitability(new IndicatorSimpleDTO(0, 0, 100.00, false))
                .setPrivateParamNullRate(new IndicatorSimpleDTO(0, 0, 0.0, false))
                .setPublicParamCompletion(new IndicatorSimpleDTO(0, 0, 100.00, false))
                .setPublicParamSuitability(new IndicatorSimpleDTO(0, 0, 100.00, false))
                .setPublicParamNullRate(new IndicatorSimpleDTO(0, 0, 0.00, false));

        RuleCheckSimpleDTO res = new RuleCheckSimpleDTO();
        Integer checkResult = CheckResultEnum.NO_MATCH_SPM.getResult();
        if (logOnly) {
            checkResult = CheckResultEnum.PASS.getResult();
        }
        log.info("新日志记录结束");
        res.setSpm(spm)
                .setRootPageOid(rootPageOid != null ? rootPageOid : "")
                .setRootPageName("")
                .setFirstObjOid(firstObjOid)
                .setFirstObjName("")
                .setLogServerTime(logServerTime)
                .setEventCode(eventCode)
                .setEventName(eventName)
                .setCheckResult(checkResult)
                .setLog(logCheck)
                .setRule(Maps.newHashMap())
                .setProps(logMap)
                .setDetectionIndicator(detectionIndicator);
//                .setTrackerId(null);
        return res;
    }

    /**
     * 为新版本日志累计
     *
     * @param eventCode 事件类型
     * @param spm       spm值
     */
    private void statisticSumUp(AppStorage appStorage, String eventCode, String spm) {
        appStorage.getStats().saveStatistic(eventCode, spm, 1);
    }

    /**
     * 统计 通过/不通过 的数量
     *
     * @param spm         超级位置模型
     * @param eventCode   事件类型 code
     * @param checkResult 检查结果
     */
    private void statisticEventCheckResult(AppStorage appStorage, String spm, String eventCode, Integer checkResult) {
        appStorage.getStats().saveEventCheckResultStatistics(spm, eventCode, checkResult);
    }

    /**
     * 统计 未匹配spm 的数量
     *
     * @param spm         超级位置模型
     *
     */
    private void statisticUnmatchSpmResult(AppStorage appStorage, String spm) {
        appStorage.getStats().saveUnknownSpmResultStatistics(spm);
    }

    /**
     * 统计旧版本日志的统计结果
     *
     * @param eventCode2NameMap 规则映射到名称的Map
     * @return 旧版本日志的统计结果
     */
    private LogStatisticsSimpleDTO oldVersionStat(AppStorage appStorage, Map<String, String> eventCode2NameMap) {
        Map<String, Map<String, Integer>> eventCode2Spm2NumMap = appStorage.getStats().getLogStatisticInOldVersion();
        LogStatisticsSimpleDTO logStatistic = parseStats(eventCode2Spm2NumMap, eventCode2NameMap);
        return logStatistic;
    }

    /**
     * 统计新版本日志的统计结果
     *
     * @param eventCode2NameMap 规则映射到名称的Map
     * @return 新版本日志的统计结果
     */
    private LogStatisticsSimpleDTO stat(AppStorage appStorage, Map<String, String> eventCode2NameMap) {
        Map<String, Map<String, Integer>> eventCode2Spm2NumMap = appStorage.getStats().getLogStatistic();
        Map<String, Integer> logToCheckFailedNumMap = appStorage.getStats().getCheckFailedNumMap();
        LogStatisticsSimpleDTO logStatistic = parseStats(eventCode2Spm2NumMap, eventCode2NameMap);
        logStatistic.setCheckFailedlogNum(logToCheckFailedNumMap.get(BuryPointLogTypeEnum.INSIGHT.name()));
        return logStatistic;
    }

    /**
     * 获取未定义事件的统计结果
     *
     * @return 未定义事件的统计结果
     */
    private UndefinedEventStatisticsResultDTO getUndefinedEventStatistics(AppStorage appStorage) {
        Map<String, Integer> undefinedEventStatisticsMap = appStorage.getStats().getUndefinedEventStatistics();
        if (MapUtils.isEmpty(undefinedEventStatisticsMap)) {
            return null;
        }

        // 需要返回的对象
        UndefinedEventStatisticsResultDTO undefinedEventStatisticsResultDTO = new UndefinedEventStatisticsResultDTO();
        undefinedEventStatisticsResultDTO.setEventNum(undefinedEventStatisticsMap.size());

        // 未定义的日志总数
        int logNum = 0;
        // 未定义事件的统计详情
        List<UndefinedEventStatisticsResultDTO.UndefinedEventStatisticsItemDTO> statisticDetailList = Lists.newArrayList();

        Iterator<Map.Entry<String, Integer>> iterator = undefinedEventStatisticsMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();

            UndefinedEventStatisticsResultDTO.UndefinedEventStatisticsItemDTO itemDTO =
                    new UndefinedEventStatisticsResultDTO.UndefinedEventStatisticsItemDTO();

            itemDTO.setEventCode(entry.getKey());

            Integer logCount = entry.getValue();
            itemDTO.setLogCount(logCount);

            statisticDetailList.add(itemDTO);

            // 累加日志数量
            logNum += logCount;
        }

        undefinedEventStatisticsResultDTO.setLogNum(logNum);
        undefinedEventStatisticsResultDTO.setDetails(statisticDetailList);
        return undefinedEventStatisticsResultDTO;
    }

    /**
     * 获取校验的统计结果
     *
     * @return 校验的统计结果
     */
    private List<TreeModeStatisticResultDTO> getTreeModeStatisticResult(AppStorage appStorage) {
        Map<String, Map<String, EventCheckResultDTO>> spmToEventToCheckResultMap =
                appStorage.getStats().getEventCheckResultStatistics();
        if (MapUtils.isEmpty(spmToEventToCheckResultMap)) {
            return null;
        }

        // 最终返回的统计结果
        List<TreeModeStatisticResultDTO> treeModeStatisticResultDTOList = Lists.newArrayList();

        // 需要从这个 map 获取 spm 和 eventCode 对应的日志数量
        Map<String, Map<String, Integer>> eventCode2Spm2NumMap = appStorage.getStats().getLogStatistic();

        Set<Map.Entry<String, Map<String, EventCheckResultDTO>>> entrySet = spmToEventToCheckResultMap.entrySet();
        for (Map.Entry<String, Map<String, EventCheckResultDTO>> entry : entrySet) {
            String spm = entry.getKey();
            spm = LogUtil.removePos(spm);
            Map<String, EventCheckResultDTO> eventCheckResultDTOMap = entry.getValue();

            TreeModeStatisticResultDTO resultDTO = new TreeModeStatisticResultDTO();
            resultDTO.setSpm(spm);

            // 每个 spm 对应的统计详情
            List<TreeModeStatisticResultDTO.EventCheckResultItemDTO> details = Lists.newArrayList();

            Set<Map.Entry<String, EventCheckResultDTO>> eventCheckResultEntrySet = eventCheckResultDTOMap.entrySet();
            for (Map.Entry<String, EventCheckResultDTO> eventCheckResultDTOEntry : eventCheckResultEntrySet) {
                String eventCode = eventCheckResultDTOEntry.getKey();
                EventCheckResultDTO eventCheckResultDTO = eventCheckResultDTOEntry.getValue();

                TreeModeStatisticResultDTO.EventCheckResultItemDTO itemDTO =
                        new TreeModeStatisticResultDTO.EventCheckResultItemDTO();
                itemDTO.setEventCode(eventCode);
                itemDTO.setPassSum(eventCheckResultDTO.getPassSum());
                itemDTO.setFailSum(eventCheckResultDTO.getFailSum());

                Map<String, Integer> spmToNumMap = eventCode2Spm2NumMap.get(eventCode);
                if (MapUtils.isNotEmpty(spmToNumMap)) {
                    Integer num = spmToNumMap.get(spm);
                    // 填充 spm 和 eventCode 对应的日志数量
                    itemDTO.setNum(num);
                }

                details.add(itemDTO);
            }

            resultDTO.setDetails(details);
            treeModeStatisticResultDTOList.add(resultDTO);
        }

        return treeModeStatisticResultDTOList;
    }


    private EventStatisticResultDTO getEventStatisticResult(AppStorage appStorage) {
        Map<String, EventCheckResultDTO> eventToCheckResultMap =
                appStorage.getStats().getOnlyEventCheckResultStatistics();
        if (MapUtils.isEmpty(eventToCheckResultMap)) {
            return null;
        }

        // 最终返回的统计结果
        EventStatisticResultDTO eventStatisticResultDTO = new EventStatisticResultDTO();
        List<EventStatisticResultDTO.EventCheckResultItemDTO> details = new ArrayList<>();

        Set<Map.Entry<String, EventCheckResultDTO>> entrySet = eventToCheckResultMap.entrySet();
        for (Map.Entry<String, EventCheckResultDTO> entry : entrySet) {
            String eventCode = entry.getKey();
            EventCheckResultDTO eventCheckResultDTO = entry.getValue();

            EventStatisticResultDTO.EventCheckResultItemDTO resultDTO = new EventStatisticResultDTO.EventCheckResultItemDTO();
            resultDTO.setEventCode(eventCode);
            resultDTO.setPassSum(eventCheckResultDTO.getPassSum());
            resultDTO.setFailSum(eventCheckResultDTO.getFailSum());
            resultDTO.setNum(eventCheckResultDTO.getPassSum() + eventCheckResultDTO.getFailSum());

            details.add(resultDTO);
            }

            eventStatisticResultDTO.setDetails(details);

            return eventStatisticResultDTO;
        }

    private List<UnMatchSpmStatisticResultDTO> getUnknownStatisticResult(AppStorage appStorage, Map<String, String> oid2NameMap) {
        Map<String, Long> unKnownSpmResultMap = appStorage.getStats().getSpmUnknownCheckResultMap();
        if (MapUtils.isEmpty(unKnownSpmResultMap)) {
            return null;
        }

        // 最终返回的统计结果
        List<UnMatchSpmStatisticResultDTO> details = new ArrayList<>();
        Set<Map.Entry<String, Long>> entrySet = unKnownSpmResultMap.entrySet();
        for (Map.Entry<String, Long> entry : entrySet) {
            String spm = entry.getKey();
            Long unKnowNum = entry.getValue();
            String spmNames = LogUtil.transSpm(spm, oid2NameMap);
            UnMatchSpmStatisticResultDTO resultDTO = new UnMatchSpmStatisticResultDTO();
            resultDTO.setSpm(spm);
            resultDTO.setSpmName(spmNames);
            resultDTO.setNum(unKnowNum);

            details.add(resultDTO);
        }

        return details;
    }


}
