package com.netease.hz.bdms.eistest.ws.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.rulecheck.RuleCheckSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.statistic.ErrorMessageSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.enums.BuryPointErrorCategoryEnum;
import com.netease.hz.bdms.eistest.entity.*;
import com.netease.hz.bdms.eistest.ws.BuryPointValidationServiceImpl;
import com.netease.hz.bdms.eistest.entity.AppPushLogAction;
import com.netease.hz.bdms.eistest.ws.dto.AppStorage;
import com.netease.hz.bdms.eistest.ws.dto.BuryPointStats;
import com.netease.hz.bdms.eistest.ws.dto.EvictingBlockingQueue;
import com.netease.hz.bdms.eistest.ws.dto.PcStorage;
import com.netease.hz.bdms.eistest.ws.session.AppSession;
import com.netease.hz.bdms.eistest.ws.session.PcSession;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author sguo
 */
@Slf4j
public class LogConsumer implements Runnable {

    private final String code;
    private final AppSession as;
    private final PcSession ps;
    private BuryPointValidationServiceImpl buryPointValidationService;

    public LogConsumer(String code, AppSession as, PcSession ps, BuryPointValidationServiceImpl buryPointValidationService) {
        this.code = code;
        this.as = as;
        this.ps = ps;
        this.buryPointValidationService = buryPointValidationService;
    }

    @Override
    public void run() {
        log.info("log consumer thread started for code {}", code);
        AppStorage appStorage = as.getStorage();
        PcStorage pcStorage = ps.getStorage();
        EvictingBlockingQueue<String> queue = appStorage.getQueue();
        while (!Thread.interrupted()) {
            String buryPointLog;
            try {
                log.info("code={}，准备从队列获取",code);
                buryPointLog = queue.take();
                log.info("code={},成功从队列获取",code);
            } catch (InterruptedException e) {
                log.error("消费异常");
                break;
            }

            AppPushMessage parsedMessage;
            try {
                parsedMessage = JSON.parseObject(buryPointLog, AppPushMessage.class);
            } catch (JSONException e) {
                log.error("buryPointLog:[{}] parse occurred error", buryPointLog, e);
                continue;
            }

            BuryPointLogStatisticsResult result = new BuryPointLogStatisticsResult(null, null, null, null);

            Map<String, AppPushLogAction> actionMap = AppPushLogAction.getActionMap();
            String action = parsedMessage.getAction();
            AppPushLogAction appPushLogAction = actionMap.get(action);
            switch (appPushLogAction) {
                case BASIC_INFO: {
//                    ClientBasicInfo clientBasicInfo = JSON.parseObject(parsedMessage.getContent(), ClientBasicInfo.class);
//                    appStorage.setClientBasicInfo(clientBasicInfo);
                    break;
                }
                case LOG: {
                    BuryPointLog pointLog = JSON.parseObject(parsedMessage.getContent(), BuryPointLog.class);
                    BuryPointValidationAndStatistics validationAndStats = buryPointValidationService.validateAndStat(appStorage, pcStorage, pointLog, pcStorage.isLogOnly());
                    if (validationAndStats != null) {

                        RuleCheckSimpleDTO ruleCheck = validationAndStats.getRuleCheck();
                        if (ruleCheck != null) {
                            String eventCode = ruleCheck.getEventCode();
//                            if (StringUtils.isNotBlank(eventCode) && eventCode.startsWith("ET")) {
//                                break;
//                            }
                        }

                        validationAndStats.setAction(action);

                        result.setLogType(AppPushLogAction.LOG.getName());
                        result.setLogStats(validationAndStats);
                        result.setExpStats(null);
//                        if(result.getLogStats() != null){
//                            log.info("send data:{}",result.getLogStats().getIndex());
//                        }
                        ps.sendData(JSON.toJSONString(result));
                    }
                    break;
                }
                case EXCEPTION: {
                    BuryPointErrorContent exceptionLog = JSON.parseObject(parsedMessage.getContent(), BuryPointErrorContent.class);
                    log.info("exception埋点日志：{}",parsedMessage.getContent());
                    List<ErrorMessageSimpleDTO> errorMessageSimpleDTOList = statisticalError(appStorage, exceptionLog);

                    BuryPointErrorContentExpand buryPointErrorContentExpand = new BuryPointErrorContentExpand();
                    buryPointErrorContentExpand.setCode(exceptionLog.getCode());
                    String category = BuryPointErrorCategoryEnum.match(buryPointErrorContentExpand.getCode()).getCategory();
                    buryPointErrorContentExpand.setCategory(category);
                    buryPointErrorContentExpand.setTimestamp(System.currentTimeMillis());
                    buryPointErrorContentExpand.setLog(exceptionLog);

                    BuryPointLogExceptionStatisticsResult exceptionStatisticsResult =
                            new BuryPointLogExceptionStatisticsResult(action, System.currentTimeMillis(), errorMessageSimpleDTOList, buryPointErrorContentExpand);

                    result.setLogType(AppPushLogAction.EXCEPTION.getName());
                    result.setExpStats(exceptionStatisticsResult);
                    result.setLogStats(null);
                    log.info("exception统计日志:{}",JSON.toJSONString(result));
                    ps.sendData(JSON.toJSONString(result));
                    break;
                }
                default: {
                    // do nothing
                }
            }
        }
        log.info("log consumer thread exist {}", code);
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
}
