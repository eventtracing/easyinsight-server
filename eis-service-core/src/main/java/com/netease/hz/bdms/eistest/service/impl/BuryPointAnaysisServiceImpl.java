package com.netease.hz.bdms.eistest.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.netease.eis.adapters.RealtimeConfigAdapter;
import com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.oldversion.OldVersionLogSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.rulecheck.CompareItemSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.rulecheck.RuleCheckSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.statistic.ErrorMessageSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.realtimetest.RealTimeTestResourceDTO;
import com.netease.hz.bdms.easyinsight.common.enums.BuryPointErrorCategoryEnum;
import com.netease.hz.bdms.easyinsight.common.enums.BuryPointLogTypeEnum;
import com.netease.hz.bdms.easyinsight.common.enums.CheckResultEnum;
import com.netease.hz.bdms.easyinsight.common.enums.ParamValueTypeEnum;
import com.netease.hz.bdms.easyinsight.common.util.JsonUtils;
import com.netease.hz.bdms.easyinsight.common.dto.audit.BloodLink;
import com.netease.hz.bdms.eistest.entity.*;
import com.netease.hz.bdms.easyinsight.service.service.audit.BuryPointRule;
import com.netease.hz.bdms.easyinsight.service.service.util.LogUtil;
import com.netease.hz.bdms.eistest.client.ProcessorRpcAdapter;
import com.netease.hz.bdms.eistest.service.BloodLinkService;
import com.netease.hz.bdms.eistest.service.BuryPointTestInfoCacheService;
import com.netease.hz.bdms.eistest.service.BuryPointAnaysisService;
import com.netease.hz.bdms.eistest.ws.BuryPointValidationServiceImpl;
import com.netease.hz.bdms.eistest.ws.SessionManager;
import com.netease.hz.bdms.eistest.entity.AppPushLogAction;
import com.netease.hz.bdms.eistest.ws.dto.AppStorage;
import com.netease.hz.bdms.eistest.ws.dto.BuryPointStats;
import com.netease.hz.bdms.eistest.ws.dto.PcStorage;
import com.netease.hz.bdms.eistest.ws.session.AppSession;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service("buryPointAnaysisService")
public class BuryPointAnaysisServiceImpl implements BuryPointAnaysisService, InitializingBean {

    @Autowired
    @Qualifier("appSessionManager")
    protected SessionManager appSessionManager;
    @Resource
    private BuryPointValidationServiceImpl buryPointValidationService;
    @Resource
    private BuryPointTestInfoCacheService buryPointTestInfoCacheService;
    @Resource
    private ConversationBasicInfoService conversationBasicInfoService;
    @Resource
    private ESTaskConsumerService esTaskConsumerService;
    @Resource
    private BloodLinkService bloodLinkService;
    @Resource
    private RealtimeConfigAdapter realtimeConfigAdapter;
    @Autowired
    private ProcessorRpcAdapter processorRpcAdapter;

    private static Long errorId = 0L;

    private static final Set<String> logCodes = new HashSet<>();

    @Getter
    public static ArrayBlockingQueue<Map<String, Object>> logQueue = new ArrayBlockingQueue<>(10000);

    @Getter
    public static ArrayBlockingQueue<Map<String, Object>> paramQueue = new ArrayBlockingQueue<>(10000);

    @Override
    public void parseBuryPointResource(String conversation, String message, AppSession as) throws InterruptedException {
        AppPushMessage parsedMessage;
        try {
            parsedMessage = JSON.parseObject(message, AppPushMessage.class);
        } catch (JSONException e) {
            log.error("buryPointLog:[{}] parse occurred error", message, e);
            return;
        }
        String action = parsedMessage.getAction();
        parseBuryPointResource(conversation, as, parsedMessage.getContent(), action);
    }

    @Override
    public void parseBuryPointResource(String conversation, AppSession as, String content, String action) throws InterruptedException {
        Map<String, AppPushLogAction> actionMap = AppPushLogAction.getActionMap();
        AppPushLogAction appPushLogAction = actionMap.get(action);
        AppStorage appStorage = as.getStorage();
        switch (appPushLogAction) {
            case BASIC_INFO: {
                ClientBasicInfo clientBasicInfo = JSON.parseObject(content, ClientBasicInfo.class);
                conversationBasicInfoService.setClientBasicInfo(conversation, clientBasicInfo);
                break;
            }
            case LOG: {
                boolean needLog = !CollectionUtils.isNotEmpty(logCodes);
                String code = as.getCode();
                if (code != null && logCodes.contains(code)) {
                    needLog = true;
                }
                if (needLog) {
                    log.info("receive log, code={}, content={}", code, content);
                }

                BuryPointLog pointLog = JSON.parseObject(content, BuryPointLog.class);
                //填充埋点元数据
                PcStorage pcStorage = new PcStorage();
                pcStorage.setMetaInfo(appStorage.getMetaInfo());
                //解析埋点信息
                BuryPointValidationAndStatistics validationAndStats = buryPointValidationService.validateAndStat(appStorage, pcStorage, pointLog, pcStorage.isLogOnly());
                //缓存
                boolean oldVersion = pointLog.getEt() == null || !pointLog.getEt();
//                buryPointTestInfoCacheService.setInsightLogInfo(as.getCode(), oldVersion, validationAndStats);
                //todo 写入es
                Map<String, Object> buryPointLogMap = parseBuryPointToES(validationAndStats, null, as.getCode(), appPushLogAction);
                logQueue.put(buryPointLogMap);

                BuryPointRule rule = pcStorage.getMetaInfo().getBuryPointRule(validationAndStats.getSpm());
                BuryPointRule eventRule = pcStorage.getMetaInfo().getEventRule();
                List<Map<String, Object>> buryPointParamMapList = parseParamToES(validationAndStats, rule, eventRule, as.getCode());
                buryPointParamMapList.forEach(buryPointParamMap -> {
                    try {
                        paramQueue.put(buryPointParamMap);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                //落库
                RuleCheckSimpleDTO ruleCheck = validationAndStats.getRuleCheck();
                Map<String,Long> eventBuryPointMap = appStorage.getMetaInfo().getEventBuryPointMap();
                buryPointTestInfoCacheService.saveInsightLog(as.getCode(), ruleCheck, eventBuryPointMap);
                if (needLog) {
                    log.info("validate log, code={}, ruleCheck={} eventBuryPointMap={}", code, JSON.toJSONString(ruleCheck), JSON.toJSONString(eventBuryPointMap));
                }
                break;
            }
            case EXCEPTION: {
                BuryPointErrorContent exceptionLog = JSON.parseObject(content, BuryPointErrorContent.class);
                log.info("exception埋点日志：{}",content);
                List<ErrorMessageSimpleDTO> errorMessageSimpleDTOList = statisticalError(appStorage, exceptionLog);
                BuryPointErrorContentExpand buryPointErrorContentExpand = new BuryPointErrorContentExpand();
                buryPointErrorContentExpand.setCode(exceptionLog.getCode());
                String category = BuryPointErrorCategoryEnum.match(buryPointErrorContentExpand.getCode()).getCategory();
                buryPointErrorContentExpand.setCategory(category);
                buryPointErrorContentExpand.setTimestamp(System.currentTimeMillis());
                buryPointErrorContentExpand.setLog(exceptionLog);
                //缓存
                BuryPointLogExceptionStatisticsResult exceptionStatisticsResult =
                        new BuryPointLogExceptionStatisticsResult(action, Long.parseLong(conversation + errorId++), errorMessageSimpleDTOList, buryPointErrorContentExpand);
                //
                Map<String, Object> buryPointLogMap = parseBuryPointToES(null, exceptionStatisticsResult, as.getCode(), AppPushLogAction.EXCEPTION);
                logQueue.put(buryPointLogMap);
//                buryPointTestInfoCacheService.setInsightErrorLog(as.getCode(), exceptionStatisticsResult);
                break;
            }
            default: {
            }

        }
        //
        esTaskConsumerService.submitWriteCkTask(logQueue, paramQueue);
    }

    @Scheduled(cron = "0/1 * * * * ?")
    public void writeCkJob(){
        esTaskConsumerService.submitWriteCkTask(logQueue, paramQueue);
    }

    public List<ErrorMessageSimpleDTO> statisticalError(AppStorage appStorage, BuryPointErrorContent buryPointErrorContent) {
        String exceptionName = AppPushLogAction.EXCEPTION.getName();
        BuryPointStats statsResult = appStorage.getStats();
        statsResult.saveErrorStatistics(exceptionName, buryPointErrorContent.getCode(), 1);

        EnumMap<BuryPointErrorCategoryEnum, Integer> errorCategoryEnumIntegerEnumMap = statsResult
                .getErrorStatistics().get(exceptionName);
        return errorCategoryEnumIntegerEnumMap.entrySet().stream()
                .map(entry -> {
                    BuryPointErrorCategoryEnum errorCategoryEnum = entry.getKey();
                    ErrorMessageSimpleDTO errorMessageSimpleDTO = new ErrorMessageSimpleDTO();
                    errorMessageSimpleDTO.setKey(errorCategoryEnum.getKey());
                    errorMessageSimpleDTO.setCode(errorCategoryEnum.getCode());
                    errorMessageSimpleDTO.setCategory(errorCategoryEnum.getCategory());
                    errorMessageSimpleDTO.setCount(entry.getValue());
                    return errorMessageSimpleDTO;
                }).collect(Collectors.toList());
    }

    @Override
    public Map<String, BuryPointRule> getBuryPointStatistics(RealTimeTestResourceDTO realTimeTestResourceDTO) {
        return null;
    }

    @Override
    public List<BuryPointLogRuleCheckDto> parseBuryPointLog(List<String> buryPointString, int logType, String oid, String evnetId, String spm, String searchStr, String checkType) {

        List<BuryPointLogRuleCheckDto> buryPointLogRuleCheckDtos = new ArrayList<>();
        //解析日志
        for(int i=0; i<buryPointString.size(); i++) {
            String pointLog = buryPointString.get(i);
            BuryPointLogRuleCheckDto buryPointLogRuleCheckDto = new BuryPointLogRuleCheckDto();
            buryPointLogRuleCheckDto.setIndex(i);
            buryPointLogRuleCheckDto.setType(logType);
            if (logType == BuryPointLogTypeEnum.INSIGHT.getCode()) {
                RuleCheckSimpleDTO ruleCheckSimpleDTO = JsonUtils.parseObject(pointLog, RuleCheckSimpleDTO.class);
                buryPointLogRuleCheckDto.setRuleCheck(ruleCheckSimpleDTO);
            }else if(logType == BuryPointLogTypeEnum.OLDVERSION.getCode()){
                OldVersionLogSimpleDTO oldVersionLogSimpleDTO = JsonUtils.parseObject(pointLog, OldVersionLogSimpleDTO.class);
                buryPointLogRuleCheckDto.setOldVersionLog(oldVersionLogSimpleDTO);
            }else if(logType == BuryPointLogTypeEnum.EXCEPTION.getCode()){
                BuryPointErrorContentExpand buryPointErrorContentExpand = JsonUtils.parseObject(pointLog, BuryPointErrorContentExpand.class);
                buryPointLogRuleCheckDto.setExceptionStatisticsResult(buryPointErrorContentExpand);
            }
            //模糊匹配
            if(StringUtils.isBlank(searchStr) || pointLog.contains(searchStr)) {
                buryPointLogRuleCheckDtos.add(buryPointLogRuleCheckDto);
            }
        }



        //捞完日志根据过滤条件过滤
        if(logType == BuryPointLogTypeEnum.INSIGHT.getCode() && (StringUtils.isNotBlank(checkType) || StringUtils.isNotBlank(oid) || StringUtils.isNotBlank(evnetId) || spm != null)){

            //条件过滤
            buryPointLogRuleCheckDtos = buryPointLogRuleCheckDtos.stream().filter(dto -> {

                boolean retFlag = true;
                RuleCheckSimpleDTO ruleCheckSimpleDTO = dto.getRuleCheck();
                if("".equals(spm) || ruleCheckSimpleDTO == null){
                    return false;
                }
                if (StringUtils.isNotBlank(checkType)){
                    retFlag &= !ruleCheckSimpleDTO.getCheckResult().equals(CheckResultEnum.PASS.getResult());
                }
                if (StringUtils.isNotBlank(oid)){
                    retFlag &=  ruleCheckSimpleDTO.getFirstObjOid().equals(oid);
                }
                if (StringUtils.isNotBlank(evnetId)){
                    retFlag &=  ruleCheckSimpleDTO.getEventCode().equals(evnetId);
                }
                if (StringUtils.isNotBlank(spm)){
                    retFlag &=  spm.equals(ruleCheckSimpleDTO.getSpm());
                }
                return retFlag;
            }).collect(Collectors.toList());
        }

        return buryPointLogRuleCheckDtos;
    }

    @Override
    public Map<String, Object> parseBuryPointToES(BuryPointValidationAndStatistics validationAndStats, BuryPointLogExceptionStatisticsResult exceptionStatisticsResult, String code, AppPushLogAction appPushLogAction) {

        Map<String, Object> buryPointLogESMap = new HashMap<>();

        if(appPushLogAction.getName().equals(AppPushLogAction.LOG.getName())) {
            //解析日志
            BuryPointLogRuleCheckDto buryPointLogRuleCheckDto = BuryPointLogRuleCheckDto.builder()
                    .ruleCheck(validationAndStats.getRuleCheck())
                    .oldVersionLog(validationAndStats.getOldVersionLog())
                    .index(validationAndStats.getIndex())
                    .build();
            buryPointLogESMap.put("code", code);
            if (buryPointLogRuleCheckDto.getOldVersionLog() != null) {
                buryPointLogESMap.put("type", BuryPointLogTypeEnum.OLDVERSION.getCode());
                buryPointLogESMap.put("checkType", NumberUtils.INTEGER_ONE);
                buryPointLogESMap.put("eventCode", validationAndStats.getOldVersionLog().getAction());
                buryPointLogESMap.put("spm", "");
                buryPointLogESMap.put("logInfo", JsonUtils.toJson(validationAndStats.getOldVersionLog()));
                buryPointLogESMap.put("serverTime", validationAndStats.getOldVersionLog().getLogServerTime());
                buryPointLogESMap.put("category", "");
            } else if (buryPointLogRuleCheckDto.getRuleCheck() != null) {
                buryPointLogESMap.put("type", BuryPointLogTypeEnum.INSIGHT.getCode());
                buryPointLogESMap.put("checkType", validationAndStats.getRuleCheck().getCheckResult());
                buryPointLogESMap.put("eventCode", validationAndStats.getRuleCheck().getEventCode());
                buryPointLogESMap.put("spm", validationAndStats.getRuleCheck().getSpm() != null ? validationAndStats.getRuleCheck().getSpm() : "");
                buryPointLogESMap.put("logInfo", JsonUtils.toJson(validationAndStats.getRuleCheck()));
                buryPointLogESMap.put("serverTime", validationAndStats.getRuleCheck().getLogServerTime());
                buryPointLogESMap.put("category", "");
            }else {
                buryPointLogESMap.put("type", BuryPointLogTypeEnum.UNDEFINED.getCode());
                buryPointLogESMap.put("checkType", NumberUtils.INTEGER_ONE);
                buryPointLogESMap.put("eventCode", "");
                buryPointLogESMap.put("spm", "");
                buryPointLogESMap.put("logInfo", "");
                buryPointLogESMap.put("serverTime", System.currentTimeMillis());
                buryPointLogESMap.put("category", "");
            }
        }else if(appPushLogAction.getName().equals(AppPushLogAction.EXCEPTION.getName())){
            buryPointLogESMap.put("code", code);
            buryPointLogESMap.put("type", BuryPointLogTypeEnum.EXCEPTION.getCode());
            buryPointLogESMap.put("checkType", NumberUtils.INTEGER_ONE);
            buryPointLogESMap.put("eventCode", exceptionStatisticsResult.getAction());
            buryPointLogESMap.put("spm", "");
            buryPointLogESMap.put("logInfo", JsonUtils.toJson(exceptionStatisticsResult.getExpLogContent()));
            buryPointLogESMap.put("serverTime", exceptionStatisticsResult.getExpLogContent().getTimestamp());
            buryPointLogESMap.put("category", exceptionStatisticsResult.getExpLogContent().getCategory());
        }

        return buryPointLogESMap;
    }

    @Override
    public List<Map<String, Object>> parseParamToES(BuryPointValidationAndStatistics validationAndStats, BuryPointRule rule, BuryPointRule eventRule, String code) {

        List<Map<String, Object>> mapList = new ArrayList<>();
        if(validationAndStats.getRuleCheck() == null){
            return mapList;
        }

        Map<String, Object> logMap = validationAndStats.getRuleCheck().getLog();
        Map<String, List<BloodLink.Param>> eventVerifiers = eventRule.getEventsVerifiers();
        Map<String, BloodLink.Param> eventParamMap = new HashMap<>();
        List<BloodLink.Param> eventPram = eventVerifiers.get(validationAndStats.getRuleCheck().getEventCode());

        String spm = validationAndStats.getRuleCheck().getSpm();
        if(StringUtils.isNotBlank(spm)) {
            if(rule == null){
                return new ArrayList<>();
            }
            //解析日志
            String firstOid = validationAndStats.getRuleCheck().getFirstObjOid();
            LinkedHashMap<String, Map<String, Object>> oid2ParamKey2ValueInPListLog = LogUtil.getRulePlist(logMap);
            LinkedHashMap<String, Map<String, Object>> oid2ParamKey2ValueInEListLog = LogUtil.getRuleElist(logMap);
            oid2ParamKey2ValueInPListLog.putAll(oid2ParamKey2ValueInEListLog);

            LinkedHashMap<String, Map<String, BloodLink.Param>> pageListVerifiers = rule.getPageListVerifiers();
            LinkedHashMap<String, Map<String, BloodLink.Param>> eleListVerifiers = rule.getEleListVerifiers();
            if(eventPram != null) {
                eventParamMap = eventPram.stream().collect(Collectors.toMap(BloodLink.Param::getCode, Function.identity()));
            }

            LinkedHashMap<String, Map<String, BloodLink.Param>> allVerifiers = new LinkedHashMap<String, Map<String, BloodLink.Param>>();
            allVerifiers.putAll(pageListVerifiers);
            allVerifiers.putAll(eleListVerifiers);

            //对象埋点
            for (String oid : oid2ParamKey2ValueInPListLog.keySet()) {
                Map<String, Object> paramCode2ParamInPListRule = oid2ParamKey2ValueInPListLog.get(oid);
                //计算event 和 oid 参数枚举测试分支
                Map<String, Object> paramMap = new HashMap<>();
                if (oid.equals(firstOid)) {
                    Map<String, BloodLink.Param> stringParamMap = allVerifiers.get(oid);
//                    stringParamMap.putAll(eventParamMap);
                    for (String paramKey : stringParamMap.keySet()) {
                        BloodLink.Param paramValue = stringParamMap.get(paramKey);
                        if (paramValue.getValueType().equals(ParamValueTypeEnum.VARIABLE.getType()) || paramKey.equals("_oid")) {
                            continue;
                        }
                        List<String> paramValueList = paramValue.getSelectedValues();
                        for (String value : paramValueList) {
                            paramMap.put(paramKey + value, 1);
                        }
                    }
                }


                for (String key : paramCode2ParamInPListRule.keySet()) {
                    Map<String, Object> buryPointParamESMap = new HashMap<>();
                    CompareItemSimpleDTO compareItemSimpleDTO = (CompareItemSimpleDTO) paramCode2ParamInPListRule.get(key);
                    if (StringUtils.isBlank(compareItemSimpleDTO.getCause()) && !compareItemSimpleDTO.getKey().equals("_oid")) {
                        buryPointParamESMap.put("code", code);
                        buryPointParamESMap.put("spm", spm);
                        buryPointParamESMap.put("oid", oid);
                        buryPointParamESMap.put("eventCode", validationAndStats.getRuleCheck().getEventCode());
                        buryPointParamESMap.put("paramKey", compareItemSimpleDTO.getKey());
                        buryPointParamESMap.put("paramValue", compareItemSimpleDTO.getValue());
                        buryPointParamESMap.put("paramKeyValue", key + compareItemSimpleDTO.getValue());
                        buryPointParamESMap.put("isHit", paramMap.containsKey(key + compareItemSimpleDTO.getValue()) ? 1 : 0);
                        buryPointParamESMap.put("paramKeyValueCount", paramMap.keySet().size());
                        mapList.add(buryPointParamESMap);
                    }
                }
            }
        }else {

            if(eventPram != null) {
                eventParamMap = eventPram.stream().collect(Collectors.toMap(BloodLink.Param::getCode, Function.identity()));
            }
            Map<String, Object> paramMap = new HashMap<>();
            for (String paramKey : eventParamMap.keySet()) {
                BloodLink.Param paramValue = eventParamMap.get(paramKey);
                if (paramValue.getValueType().equals(ParamValueTypeEnum.VARIABLE.getType())) {
                    continue;
                }
                List<String> paramValueList = paramValue.getSelectedValues();
                for (String value : paramValueList) {
                    paramMap.put(paramKey + value, 1);
                }
            }

            for (String key : logMap.keySet()) {
                if(key.equals("_elist") || key.equals("_plist") || key.equals("_eventcode")) continue;
//                String logValue = LogUtil.getString(logMap, key);
                Map<String, Object> buryPointParamESMap = new HashMap<>();
                CompareItemSimpleDTO compareItemSimpleDTO = (CompareItemSimpleDTO) logMap.get(key);
                if (StringUtils.isBlank(compareItemSimpleDTO.getCause()) && !compareItemSimpleDTO.getKey().equals("_oid")) {
                    buryPointParamESMap.put("code", code);
                    buryPointParamESMap.put("spm", "");
                    buryPointParamESMap.put("oid", "");
                    buryPointParamESMap.put("eventCode", validationAndStats.getRuleCheck().getEventCode());
                    buryPointParamESMap.put("paramKey", compareItemSimpleDTO.getKey());
                    buryPointParamESMap.put("paramValue", compareItemSimpleDTO.getValue());
                    buryPointParamESMap.put("paramKeyValue", key + compareItemSimpleDTO.getValue());
                    buryPointParamESMap.put("isHit", paramMap.containsKey(key + compareItemSimpleDTO.getValue()) ? 1 : 0);
                    buryPointParamESMap.put("paramKeyValueCount", paramMap.keySet().size());
                    mapList.add(buryPointParamESMap);

                }

            }
        }
        return mapList;
    }

    @Override
    public List<Map<String, Object>> parseCacheLogToES(List<String> buryPointString, int logType, long code, long index) {

        List<Map<String, Object>> buryPointLogESDtos = new ArrayList<>();
        //解析日志
        for(int i=0; i<buryPointString.size(); i++) {
            String pointLog = buryPointString.get(i);
            Map<String, Object> buryPointLogESMap = new HashMap<String, Object>();
            buryPointLogESMap.put("index", index + i);
            buryPointLogESMap.put("code", code);
            buryPointLogESMap.put("type", logType);
            buryPointLogESMap.put("logInfo", pointLog);
//            if (logType == BuryPointLogTypeEnum.INSIGHT.getCode()) {
//                RuleCheckSimpleDTO ruleCheckSimpleDTO = JsonUtils.parseObject(pointLog, RuleCheckSimpleDTO.class);
//                buryPointLogESDto.setLogInfo(JsonUtils.toJson(ruleCheckSimpleDTO));
//            }else if(logType == BuryPointLogTypeEnum.OLDVERSION.getCode()){
//                OldVersionLogSimpleDTO oldVersionLogSimpleDTO = JsonUtils.parseObject(pointLog, OldVersionLogSimpleDTO.class);
//                buryPointLogESDto.setLogInfo(JsonUtils.toJson(oldVersionLogSimpleDTO));
//            }else if(logType == BuryPointLogTypeEnum.EXCEPTION.getCode()){
//                BuryPointErrorContentExpand buryPointErrorContentExpand = JsonUtils.parseObject(pointLog, BuryPointErrorContentExpand.class);
//                buryPointLogESDto.setLogInfo(JsonUtils.toJson(buryPointErrorContentExpand));
//            }
            buryPointLogESDtos.add(buryPointLogESMap);
        }
        return buryPointLogESDtos;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        realtimeConfigAdapter.listenJSON("logCodes", (s) -> {
            logCodes.clear();
            logCodes.addAll(JsonUtils.parseObject(s, new TypeReference<Set<String>>() {}));
        });
    }
}
