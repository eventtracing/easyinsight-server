package com.netease.hz.bdms.eistest.service.es;


import com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.statistic.*;
import com.netease.hz.bdms.easyinsight.common.enums.BuryPointLogTypeEnum;
import com.netease.hz.bdms.easyinsight.common.vo.logcheck.BranchCoverageDetailVO;
import com.netease.hz.bdms.eistest.entity.BuryPointStatisticsDto;
import com.netease.hz.bdms.eistest.service.BuryPointAnaysisService;
import com.netease.hz.bdms.eistest.service.BuryPointTestInfoCacheService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.collect.Tuple;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ElasticsearchQueryService {

    @Resource
    private EsIndexOperation esIndexOperation;
    @Resource
    private BuryPointTestInfoCacheService buryPointTestInfoCacheService;
    @Resource
    private BuryPointAnaysisService buryPointAnaysisService;
    @Resource
    private EsQueryOperation esQueryOperation;


    public List<Map<String,Object>> queryCode(String indexName, String code){

        List<Map<String,Object>> logInfoList = esQueryOperation.queryCode(indexName, code);

        return logInfoList;
    }

    public List<Map<String,Object>> queryInsightlog(String indexName, String code, int logType, String eventCode, String spm, Integer checkType, String searchStr, int offset){

        List<Map<String,Object>> logInfoList = esQueryOperation.page(indexName, code, logType, eventCode, spm, checkType, searchStr, 0, 10000);

        return logInfoList.subList(0, Math.max(logInfoList.size() - offset, 0));
    }

    public Long queryLogCount(String indexName){

        return esQueryOperation.count(indexName);
    }

    public Long queryLogCountByCodeType(String indexName, String code, int logType){

        return esQueryOperation.countByCodeType(indexName, code, logType);
    }

    public Set<BranchCoverageDetailVO> getBranchCoverage(String spm, String code, String eventCode) {
        if (StringUtils.isBlank(spm) || StringUtils.isBlank(code)) {
            return new HashSet<>();
        }
        return esQueryOperation.queryBranchCoverage(spm, code, eventCode);
    }


    public BuryPointStatisticsDto queryCountStatistic(String indexName, String code, Map<String, String> oidToNameMap, Map<String, String> evToNameMap){

        BuryPointStatisticsDto buryPointStatisticsDto = new BuryPointStatisticsDto();

        List<TreeModeStatisticResultDTO> treeModeStatistic = esQueryOperation.treeModeCount(indexName, code, BuryPointLogTypeEnum.INSIGHT.getCode());
        //?????????????????????????????????
        for(TreeModeStatisticResultDTO resultDTO : treeModeStatistic){
            String spm = resultDTO.getSpm();
            List<TreeModeStatisticResultDTO.EventCheckResultItemDTO> resultItemDTOS = resultDTO.getDetails();
            for(TreeModeStatisticResultDTO.EventCheckResultItemDTO eventDto : resultItemDTOS){
                String eventCode = eventDto.getEventCode();
                Tuple<Integer, Integer> countTuple = esQueryOperation.paramModeCount("insight_esparam*", code, spm, eventCode);
                eventDto.setHitSum(countTuple.v1());
                eventDto.setReqSum(countTuple.v2());
            }
        }
        buryPointStatisticsDto.setTreeModeStatistic(treeModeStatistic);

        LogStatisticsSimpleDTO statistics = esQueryOperation.logModeCount(indexName, code, BuryPointLogTypeEnum.INSIGHT.getCode(), evToNameMap);
        buryPointStatisticsDto.setStatistics(statistics);

        LogStatisticsSimpleDTO oldVersionStatistics = esQueryOperation.logModeCount(indexName, code, BuryPointLogTypeEnum.OLDVERSION.getCode(), evToNameMap);
        buryPointStatisticsDto.setOldVersionStatistics(oldVersionStatistics);

        UndefinedEventStatisticsResultDTO undefinedStatistics = esQueryOperation.undefinedModeCount(indexName, code, BuryPointLogTypeEnum.UNDEFINED.getCode());
        buryPointStatisticsDto.setUndefinedStatistics(undefinedStatistics);

        List<ErrorMessageSimpleDTO> errorStatistic = esQueryOperation.errorModeCount(indexName, code, BuryPointLogTypeEnum.EXCEPTION.getCode());
        buryPointStatisticsDto.setErrorStatistic(errorStatistic);

        List<UnMatchSpmStatisticResultDTO> unMatchSpmStatistic = esQueryOperation.unMatchSpmModeCount(indexName, code, BuryPointLogTypeEnum.INSIGHT.getCode(), oidToNameMap);
        if(unMatchSpmStatistic != null) {
            unMatchSpmStatistic = unMatchSpmStatistic.stream().filter(dto -> StringUtils.isNotBlank(dto.getSpm())).collect(Collectors.toList());
            buryPointStatisticsDto.setUnMatchSpmStatistic(unMatchSpmStatistic);
        }

        EventStatisticResultDTO eventStatistic = esQueryOperation.eventModeCount(indexName, code, BuryPointLogTypeEnum.INSIGHT.getCode());
        //?????????????????????????????????
        List<EventStatisticResultDTO.EventCheckResultItemDTO> eventDetails = eventStatistic.getDetails();
        if(CollectionUtils.isNotEmpty(eventDetails)) {
            for (EventStatisticResultDTO.EventCheckResultItemDTO resultDTO : eventDetails) {
                String eventCode = resultDTO.getEventCode();
                Tuple<Integer, Integer> countTuple = esQueryOperation.paramEventModeCount("insight_esparam*", "", code, eventCode);
                resultDTO.setHitSum(countTuple.v1());
                resultDTO.setReqSum(countTuple.v2());
            }
        }
        buryPointStatisticsDto.setEventStatistic(eventStatistic);

        return buryPointStatisticsDto;
    }


}
