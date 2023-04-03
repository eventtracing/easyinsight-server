package com.netease.hz.bdms.eistest.service.es;

import com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.statistic.*;
import com.netease.hz.bdms.easyinsight.common.enums.CheckResultEnum;
import com.netease.hz.bdms.easyinsight.common.vo.logcheck.BranchCoverageDetailVO;
import com.netease.hz.bdms.easyinsight.service.service.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeAggregation;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeValuesSourceBuilder;
import org.elasticsearch.search.aggregations.bucket.composite.TermsValuesSourceBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Service
public class EsQueryOperation {

    @Resource
    private RestHighLevelClient client ;

    private final RequestOptions options = RequestOptions.DEFAULT;

    /**
     * 查询总数
     */
    public Long count (String indexName){

        // 指定创建时间
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queryBuilder);
        CountRequest countRequest = new CountRequest(indexName);
        countRequest.source(sourceBuilder);

        try {
            CountResponse countResponse = client.count(countRequest, options);
            return countResponse.getCount();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("查询count失败, index={}", indexName, e);
        }
        return 0L;
    }

    /**
     * 查询总数
     */
    public Long countByCodeType(String indexName, String code, int logType){

        // 指定创建时间
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        if(StringUtils.isNotBlank(code)) {
            queryBuilder.must(QueryBuilders.termQuery("code", code));
        }
        queryBuilder.must(QueryBuilders.termQuery("type", logType));
        sourceBuilder.query(queryBuilder);
        CountRequest countRequest = new CountRequest(indexName);
        countRequest.source(sourceBuilder);

        try {
            CountResponse countResponse = client.count(countRequest, options);
            return countResponse.getCount();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("查询count失败, index={}", indexName, e);
        }
        return 0L;
    }

    /**
     * 查询树模式日志统计
     */
    public List<TreeModeStatisticResultDTO> treeModeCount(String indexName, String code, int logType){

        // 指定创建时间
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        if(StringUtils.isNotBlank(code)) {
            queryBuilder.must(QueryBuilders.termQuery("code", code));
        }
        queryBuilder.must(QueryBuilders.termQuery("type", logType));
        TermsAggregationBuilder aggregationByType = AggregationBuilders.terms("agg_type").field("type");
        TermsAggregationBuilder aggregationBySpm = AggregationBuilders.terms("agg_spm").field("spm").size(200);
        TermsAggregationBuilder aggregationByEvent = AggregationBuilders.terms("agg_event").field("eventCode");
        TermsAggregationBuilder aggregationByResult = AggregationBuilders.terms("agg_checkType").field("checkType");
        aggregationByType.subAggregation(aggregationBySpm);
        aggregationBySpm.subAggregation(aggregationByEvent);
        aggregationByEvent.subAggregation(aggregationByResult);
        sourceBuilder.aggregation(aggregationByType);
        sourceBuilder.query(queryBuilder);

        SearchRequest searchRequest = new SearchRequest(indexName);
        searchRequest.source(sourceBuilder);

        try {

            SearchResponse searchResp = client.search(searchRequest, options);

            Aggregations aggregations = searchResp.getAggregations();
            Terms typeAggregation = aggregations.get("agg_type");
            for (Terms.Bucket typebucket : typeAggregation.getBuckets()) {
                Terms spmAggregation = typebucket.getAggregations().get("agg_spm");
                List<TreeModeStatisticResultDTO> treeModeStatistic = new ArrayList<>();
                for (Terms.Bucket bucket : spmAggregation.getBuckets()) {
                    TreeModeStatisticResultDTO resultDTO = new TreeModeStatisticResultDTO();
                    String spmByBucket = bucket.getKey().toString();
                    resultDTO.setSpm(spmByBucket);
                    List<TreeModeStatisticResultDTO.EventCheckResultItemDTO> details = new ArrayList<>();
                    Terms eventAggregation = bucket.getAggregations().get("agg_event");
                    for (Terms.Bucket bucketByEV : eventAggregation.getBuckets()) {
                        TreeModeStatisticResultDTO.EventCheckResultItemDTO resultItemDTO = new TreeModeStatisticResultDTO.EventCheckResultItemDTO();
                        String eventByBucket = bucketByEV.getKey().toString();
                        resultItemDTO.setEventCode(eventByBucket);
                        long logCount = bucketByEV.getDocCount();
                        resultItemDTO.setNum((int) logCount);
                        Terms checkAggregation = bucketByEV.getAggregations().get("agg_checkType");
                        for (Terms.Bucket bucketByType : checkAggregation.getBuckets()) {
                            String typeByBucket = bucketByType.getKey().toString();
                            long typeCount = bucketByType.getDocCount();
                            if (Integer.parseInt(typeByBucket) == CheckResultEnum.PASS.getResult()) {
                                resultItemDTO.setPassSum((int) typeCount);
                            } else {
                                resultItemDTO.setFailSum((int) typeCount);
                            }
                        }
                        details.add(resultItemDTO);
                    }
                    resultDTO.setDetails(details);
                    treeModeStatistic.add(resultDTO);
                }

                return treeModeStatistic;
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("查询失败, index={}", indexName, e);
        }
        return new ArrayList<>() ;
    }

    /**
     * 查询时序模式日志统计
     */
    public LogStatisticsSimpleDTO logModeCount(String indexName, String code, int logType, Map<String, String> evToNameMap){

        // 指定创建时间
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        if(StringUtils.isNotBlank(code)) {
            queryBuilder.must(QueryBuilders.termQuery("code", code));
        }
        queryBuilder.must(QueryBuilders.termQuery("type", logType));
//        queryBuilder.must(QueryBuilders.rangeQuery("index").gte(1));
        TermsAggregationBuilder aggregationByType = AggregationBuilders.terms("agg_type").field("type");
        TermsAggregationBuilder aggregationByEvent = AggregationBuilders.terms("agg_event").field("eventCode");
        TermsAggregationBuilder aggregationBySpm = AggregationBuilders.terms("agg_spm").field("spm").size(200);
        aggregationByType.subAggregation(aggregationByEvent);
        aggregationByEvent.subAggregation(aggregationBySpm);
        sourceBuilder.aggregation(aggregationByType);
        sourceBuilder.query(queryBuilder);

        SearchRequest searchRequest = new SearchRequest(indexName);
        searchRequest.source(sourceBuilder);

        try {

            SearchResponse searchResp = client.search(searchRequest, options);

            Aggregations aggregations = searchResp.getAggregations();
            Terms typeAggregation = aggregations.get("agg_type");
            LogStatisticsSimpleDTO logStatistic = new LogStatisticsSimpleDTO();
            for (Terms.Bucket bucket : typeAggregation.getBuckets()) {
                logStatistic.setLogNum((int)bucket.getDocCount());
                List<LogEventStatisticSimpleDTO> eventStatistics = new ArrayList<>();
                Terms eventAggregation = bucket.getAggregations().get("agg_event");
                long spmCountByType = 0;
                for(Terms.Bucket bucketByEV : eventAggregation.getBuckets()){
                    LogEventStatisticSimpleDTO resultItemDTO = new LogEventStatisticSimpleDTO();
                    String eventByBucket = bucketByEV.getKey().toString();
                    resultItemDTO.setEventCode(eventByBucket);
                    long logCount = bucketByEV.getDocCount();
                    // 查询event name
                    resultItemDTO.setEventName(evToNameMap.get(eventByBucket));
                    resultItemDTO.setLogNum((int)logCount);
                    Terms spmAggregation = bucketByEV.getAggregations().get("agg_spm");
                    List<LogSpmStatisticSimpleDTO> spmStatistics = new ArrayList<>();
                    long spmCountByEvent = 0;
                    for(Terms.Bucket bucketByType : spmAggregation.getBuckets()){
                        LogSpmStatisticSimpleDTO simpleDTO = new LogSpmStatisticSimpleDTO();
                        String spmByBucket = bucketByType.getKey().toString();
                        long spmCount = bucketByType.getDocCount();
                        simpleDTO.setSpm(spmByBucket);
                        simpleDTO.setNum((int)spmCount);
                        spmCountByEvent += spmCount;
                        spmStatistics.add(simpleDTO);
                    }
                    spmCountByType += spmCountByEvent;
                    resultItemDTO.setObjTrackerNum((int)spmCountByEvent);
                    resultItemDTO.setSpmStatistics(spmStatistics);
                    eventStatistics.add(resultItemDTO);
                }
                logStatistic.setObjTrackerNum((int)spmCountByType);
                logStatistic.setEventStatistics(eventStatistics);
            }

            return logStatistic;

        } catch (Exception e) {
            e.printStackTrace();
            log.error("查询失败, index={}", indexName, e);
        }
        return null ;
    }

    /**
     * 查询未定义区日志统计
     */
    public UndefinedEventStatisticsResultDTO undefinedModeCount(String indexName, String code, int logType){

        // 指定创建时间
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        if(StringUtils.isNotBlank(code)) {
            queryBuilder.must(QueryBuilders.termQuery("code", code));
        }
        queryBuilder.must(QueryBuilders.termQuery("type", logType));
        TermsAggregationBuilder aggregationByEvent = AggregationBuilders.terms("agg_event").field("eventCode");
        sourceBuilder.aggregation(aggregationByEvent);
        sourceBuilder.query(queryBuilder);

        SearchRequest searchRequest = new SearchRequest(indexName);
        searchRequest.source(sourceBuilder);

        try {

            SearchResponse searchResp = client.search(searchRequest, options);

            Aggregations aggregations = searchResp.getAggregations();
            Terms eventAggregation = aggregations.get("agg_event");
            UndefinedEventStatisticsResultDTO undefinedResultDTO = new UndefinedEventStatisticsResultDTO();
            int eventNum = 0;
            int logNum = 0;
            List<UndefinedEventStatisticsResultDTO.UndefinedEventStatisticsItemDTO> eventStatistics = new ArrayList<>();
            for (Terms.Bucket bucket : eventAggregation.getBuckets()) {
                UndefinedEventStatisticsResultDTO.UndefinedEventStatisticsItemDTO undefinedEventStatisticsItemDTO = new UndefinedEventStatisticsResultDTO.UndefinedEventStatisticsItemDTO();
                String eventByBucket = bucket.getKey().toString();
                long logCount = bucket.getDocCount();
                eventNum++;
                logNum += logCount;
                undefinedEventStatisticsItemDTO.setEventCode(eventByBucket);
                undefinedEventStatisticsItemDTO.setLogCount((int) logCount);
                eventStatistics.add(undefinedEventStatisticsItemDTO);
            }
            undefinedResultDTO.setDetails(eventStatistics);
            undefinedResultDTO.setEventNum(eventNum);
            undefinedResultDTO.setLogNum(logNum);

            return undefinedResultDTO;

        } catch (Exception e) {
            e.printStackTrace();
            log.error("查询失败, index={}", indexName, e);
        }
        return null ;
    }

    /**
     * 查询错误日志统计
     */
    public List<ErrorMessageSimpleDTO> errorModeCount(String indexName, String code, int logType){

        // 指定创建时间
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        if(StringUtils.isNotBlank(code)) {
            queryBuilder.must(QueryBuilders.termQuery("code", code));
        }
        queryBuilder.must(QueryBuilders.termQuery("type", logType));
        TermsAggregationBuilder aggregationByCategory = AggregationBuilders.terms("agg_category").field("category");

        sourceBuilder.aggregation(aggregationByCategory);
        sourceBuilder.query(queryBuilder);

        SearchRequest searchRequest = new SearchRequest(indexName);
        searchRequest.source(sourceBuilder);

        try {
            SearchResponse searchResp = client.search(searchRequest, options);

            Aggregations aggregations = searchResp.getAggregations();
            Terms categoryAggregation = aggregations.get("agg_category");
            List<ErrorMessageSimpleDTO> errorStatistics = new ArrayList<>();
            for (Terms.Bucket bucket : categoryAggregation.getBuckets()) {
                ErrorMessageSimpleDTO errorMessageSimpleDTO = new ErrorMessageSimpleDTO();
                String categoryByBucket = bucket.getKey().toString();
                long logCount = bucket.getDocCount();
                errorMessageSimpleDTO.setCategory(categoryByBucket);
                errorMessageSimpleDTO.setCount((int) logCount);
                errorStatistics.add(errorMessageSimpleDTO);
            }

            return errorStatistics;

        } catch (Exception e) {
            e.printStackTrace();
            log.error("查询失败, index={}", indexName, e);
        }
        return null ;
    }

    /**
     * 查询未匹配spm日志统计
     */
    public List<UnMatchSpmStatisticResultDTO> unMatchSpmModeCount(String indexName, String code, int logType, Map<String, String> oidToNameMap){

        // 指定创建时间
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        if(StringUtils.isNotBlank(code)) {
            queryBuilder.must(QueryBuilders.termQuery("code", code));
        }
        queryBuilder.must(QueryBuilders.termQuery("type", logType));
        TermsAggregationBuilder aggregationBySpm = AggregationBuilders.terms("agg_spm").field("spm").size(200);

        sourceBuilder.aggregation(aggregationBySpm);
        sourceBuilder.query(queryBuilder);

        SearchRequest searchRequest = new SearchRequest(indexName);
        searchRequest.source(sourceBuilder);

        try {
            SearchResponse searchResp = client.search(searchRequest, options);

            Aggregations aggregations = searchResp.getAggregations();
            Terms categoryAggregation = aggregations.get("agg_spm");
            List<UnMatchSpmStatisticResultDTO> unMatchSpmStatistics = new ArrayList<>();
            for (Terms.Bucket bucket : categoryAggregation.getBuckets()) {
                UnMatchSpmStatisticResultDTO unMatchSpmStatisticResultDTO = new UnMatchSpmStatisticResultDTO();
                String categoryByBucket = bucket.getKey().toString();
                long logCount = bucket.getDocCount();
                unMatchSpmStatisticResultDTO.setSpm(categoryByBucket);
                unMatchSpmStatisticResultDTO.setSpmName(LogUtil.transSpm(categoryByBucket, oidToNameMap));
                unMatchSpmStatisticResultDTO.setNum(logCount);
                unMatchSpmStatistics.add(unMatchSpmStatisticResultDTO);
            }

            return unMatchSpmStatistics;

        } catch (Exception e) {
            e.printStackTrace();
            log.error("查询失败, index={}", indexName, e);
        }
        return null ;
    }


    /**
     * 查询纯事件日志统计
     */
    public EventStatisticResultDTO eventModeCount(String indexName, String code, int logType){

        // 指定创建时间
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        if(StringUtils.isNotBlank(code)) {
            queryBuilder.must(QueryBuilders.termQuery("code", code));
        }
        queryBuilder.must(QueryBuilders.termQuery("spm", ""));
//        queryBuilder.must(QueryBuilders.existsQuery("spm"));
//        queryBuilder.mustNot(QueryBuilders.wildcardQuery("spm","*"));
        queryBuilder.must(QueryBuilders.termQuery("type", logType));
        TermsAggregationBuilder aggregationByEvent = AggregationBuilders.terms("agg_event").field("eventCode");
        TermsAggregationBuilder aggregationByType = AggregationBuilders.terms("agg_checkType").field("checkType");

        aggregationByEvent.subAggregation(aggregationByType);
        sourceBuilder.aggregation(aggregationByEvent);
        sourceBuilder.query(queryBuilder);

        SearchRequest searchRequest = new SearchRequest(indexName);
        searchRequest.source(sourceBuilder);

        try {
            SearchResponse searchResp = client.search(searchRequest, options);

            Aggregations aggregations = searchResp.getAggregations();
            Terms eventAggregation = aggregations.get("agg_event");
            EventStatisticResultDTO eventStatisticResultDTO = new EventStatisticResultDTO();
            List<EventStatisticResultDTO.EventCheckResultItemDTO> eventCheckResultItemDTOS = new ArrayList<>();
            for (Terms.Bucket bucket : eventAggregation.getBuckets()) {
                EventStatisticResultDTO.EventCheckResultItemDTO resultItemDTO = new EventStatisticResultDTO.EventCheckResultItemDTO();
                String eventByBucket = bucket.getKey().toString();
                long logCount = bucket.getDocCount();
                resultItemDTO.setEventCode(eventByBucket);
                resultItemDTO.setNum((int) logCount);
                Terms typeAggregations = bucket.getAggregations().get("agg_checkType");
                for (Terms.Bucket bucketByType : typeAggregations.getBuckets()) {
                    String typeByBucket = bucketByType.getKey().toString();
                    long typeCount = bucketByType.getDocCount();
                    if(Integer.parseInt(typeByBucket) == CheckResultEnum.PASS.getResult()) {
                        resultItemDTO.setPassSum((int) typeCount);
                    }else {
                        resultItemDTO.setFailSum((int) typeCount);
                    }
                }
                eventCheckResultItemDTOS.add(resultItemDTO);
                eventStatisticResultDTO.setDetails(eventCheckResultItemDTOS);
            }

            return eventStatisticResultDTO;

        } catch (Exception e) {
            e.printStackTrace();
            log.error("查询失败, index={}", indexName, e);
        }
        return null ;
    }


    /**
     * 分页查询
     */
    public List<Map<String,Object>> page (String indexName, String code, int logType, String eventCode, String spm, Integer checkType, String searchStr, int offset, int size) {

        // 查询条件,指定时间并过滤指定字段值
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        if(StringUtils.isNotBlank(searchStr)) {
            queryBuilder.must(QueryBuilders.matchPhraseQuery("logInfo", searchStr));
        }
        if(StringUtils.isNotBlank(code)) {
            queryBuilder.must(QueryBuilders.termQuery("code", code));
        }
        if(StringUtils.isNotBlank(eventCode)) {
            queryBuilder.must(QueryBuilders.termQuery("eventCode", eventCode));
        }
        if(StringUtils.isNotBlank(spm)) {
            queryBuilder.must(QueryBuilders.termQuery("spm", spm));
        }
        if(checkType != null) {
            queryBuilder.must(QueryBuilders.termQuery("checkType", checkType));
        }
        queryBuilder.must(QueryBuilders.termQuery("type", logType));

        sourceBuilder.query(queryBuilder);
        sourceBuilder.from(offset);
        sourceBuilder.size(size);
        sourceBuilder.sort("serverTime", SortOrder.DESC);

        SearchRequest searchRequest = new SearchRequest(indexName);
        searchRequest.source(sourceBuilder);

        try {

            SearchResponse searchResp = client.search(searchRequest, options);
            List<Map<String,Object>> data = new ArrayList<>() ;
            SearchHit[] searchHitArr = searchResp.getHits().getHits();
            for (SearchHit searchHit:searchHitArr){
                Map<String,Object> temp = searchHit.getSourceAsMap();
                temp.put("id",searchHit.getId()) ;
                data.add(temp);
            }
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("分页查询失败, index={}", indexName, e);
        }
        return null ;
    }

    /**
     * 分页查询
     */
    public List<Map<String,Object>> queryCode (String indexName, String code) {

        // 查询条件,指定时间并过滤指定字段值
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();

        if(StringUtils.isNotBlank(code)) {
            queryBuilder.must(QueryBuilders.termQuery("code", code));
        }
        queryBuilder.must(QueryBuilders.termQuery("spm", "eventspm"));


        sourceBuilder.query(queryBuilder);
        sourceBuilder.from(0);
        sourceBuilder.size(10000);
        sourceBuilder.sort("serverTime", SortOrder.DESC);

        SearchRequest searchRequest = new SearchRequest(indexName);
        searchRequest.source(sourceBuilder);

        try {

            SearchResponse searchResp = client.search(searchRequest, options);
            List<Map<String,Object>> data = new ArrayList<>() ;
            SearchHit[] searchHitArr = searchResp.getHits().getHits();
            for (SearchHit searchHit:searchHitArr){
                Map<String,Object> temp = searchHit.getSourceAsMap();
                temp.put("id",searchHit.getId()) ;
                data.add(temp);
            }
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("分页查询失败, index={}", indexName, e);
        }
        return null ;
    }

    /**
     * 查询spm参数覆盖率统计
     */
    public Tuple<Integer, Integer> paramModeCount(String indexName, String code, String spm, String eventCode){

        // 指定创建时间
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        if(StringUtils.isNotBlank(code)) {
            queryBuilder.must(QueryBuilders.termQuery("code", code));
        }
        if(StringUtils.isNotBlank(spm)) {
            queryBuilder.must(QueryBuilders.termQuery("spm", spm));
        }
        if(StringUtils.isNotBlank(code)) {
            queryBuilder.must(QueryBuilders.termQuery("eventCode", eventCode));
        }

        queryBuilder.must(QueryBuilders.termQuery("isHit", 1));

        TermsAggregationBuilder aggregationByparam = AggregationBuilders.terms("agg_paramKeyValue").field("paramKeyValue");

        sourceBuilder.aggregation(aggregationByparam);
        sourceBuilder.query(queryBuilder);

        SearchRequest searchRequest = new SearchRequest(indexName);
        searchRequest.source(sourceBuilder);

        Set<String> paramSet = new HashSet<>();

        try {
            SearchResponse searchResp = client.search(searchRequest, options);

            SearchHit[] searchHitArr = searchResp.getHits().getHits();
            int paramKeyValueCount = 0;
            if(searchHitArr.length > 0){
                SearchHit searchHit = searchHitArr[0];
                Map<String,Object> temp = searchHit.getSourceAsMap();
                paramKeyValueCount = (int) temp.get("paramKeyValueCount");
            }

            Aggregations aggregations = searchResp.getAggregations();
            Terms paramAggregation = aggregations.get("agg_paramKeyValue");
            for (Terms.Bucket bucket : paramAggregation.getBuckets()) {
                String paramByBucket = bucket.getKey().toString();
                paramSet.add(paramByBucket);
            }

            if(paramKeyValueCount > 0) {
                return new Tuple<>(paramSet.size(), paramKeyValueCount);
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error("查询失败, index={}", indexName, e);
        }
        return new Tuple<>(1, 1) ;
    }

    /**
     * 查询event参数覆盖率统计
     */
    public Tuple<Integer, Integer> paramEventModeCount(String indexName, String spm, String code, String eventCode){

        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        if(StringUtils.isNotBlank(code)) {
            queryBuilder.must(QueryBuilders.termQuery("code", code));
        }
        if(StringUtils.isNotBlank(code)) {
            queryBuilder.must(QueryBuilders.termQuery("eventCode", eventCode));
        }
        queryBuilder.must(QueryBuilders.termQuery("isHit", 1));


        queryBuilder.must(QueryBuilders.termQuery("spm", spm));

        TermsAggregationBuilder aggregationByparam = AggregationBuilders.terms("agg_paramKeyValue").field("paramKeyValue");

        sourceBuilder.aggregation(aggregationByparam);
        sourceBuilder.query(queryBuilder);

        SearchRequest searchRequest = new SearchRequest(indexName);
        searchRequest.source(sourceBuilder);

        Set<String> paramSet = new HashSet<>();

        try {
            SearchResponse searchResp = client.search(searchRequest, options);

            SearchHit[] searchHitArr = searchResp.getHits().getHits();
            int paramKeyValueCount = 0;
            if(searchHitArr.length > 0){
                SearchHit searchHit = searchHitArr[0];
                Map<String,Object> temp = searchHit.getSourceAsMap();
                paramKeyValueCount = (int) temp.get("paramKeyValueCount");
            }

            Aggregations aggregations = searchResp.getAggregations();
            Terms paramAggregation = aggregations.get("agg_paramKeyValue");
            for (Terms.Bucket bucket : paramAggregation.getBuckets()) {
                String paramByBucket = bucket.getKey().toString();
                paramSet.add(paramByBucket);
            }

            if(paramKeyValueCount > 0) {
                return new Tuple<>(paramSet.size(), paramKeyValueCount);
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error("查询失败, index={}", indexName, e);
        }
        return new Tuple<>(1, 1) ;
    }

    /**
     * 查询参数覆盖情况
     */
    public Set<BranchCoverageDetailVO> queryBranchCoverage(String spm, String code, String eventCode){

        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        if(StringUtils.isNotBlank(code)) {
            queryBuilder.must(QueryBuilders.termQuery("code", code));
        }
        if(StringUtils.isNotBlank(eventCode)) {
            queryBuilder.must(QueryBuilders.termQuery("eventCode", eventCode));
        }
        if(StringUtils.isNotBlank(spm)) {
            queryBuilder.must(QueryBuilders.termQuery("spm", spm));
        }
        queryBuilder.must(QueryBuilders.termQuery("isHit", 1)); // isHit = 1 时才是常量，才是待测分支

        List<CompositeValuesSourceBuilder<?>> sources = new ArrayList<>();
        sources.add(new TermsValuesSourceBuilder("spmAgg")
                .field("spm"));
        sources.add(new TermsValuesSourceBuilder("eventCodeAgg")
                .field("eventCode"));
        sources.add(new TermsValuesSourceBuilder("paramKeyAgg")
                .field("paramKey"));
        sources.add(new TermsValuesSourceBuilder("paramValueAgg")
                .field("paramValue"));
        CompositeAggregationBuilder compositeAggregationBuilder = new CompositeAggregationBuilder(
                "branchCoverageAgg", sources)
                .size(10000);
        sourceBuilder.aggregation(compositeAggregationBuilder);
        sourceBuilder.query(queryBuilder);

        SearchRequest searchRequest = new SearchRequest("insight_esparam*");
        searchRequest.source(sourceBuilder);

        Set<BranchCoverageDetailVO> result = new HashSet<>();

        try {
            SearchResponse searchResp = client.search(searchRequest, options);
            Aggregations aggregations = searchResp.getAggregations();
            SearchHits hits = searchResp.getHits();
            CompositeAggregation compositeAggregation = aggregations.get("branchCoverageAgg");
            for (CompositeAggregation.Bucket bucket : compositeAggregation.getBuckets()) {
                BranchCoverageDetailVO branchCoverageDetailVO = new BranchCoverageDetailVO();
                Map<String, Object> keyMap = bucket.getKey();
                branchCoverageDetailVO.setSpm(getValue(keyMap, "spmAgg"));
                branchCoverageDetailVO.setEventCode(getValue(keyMap, "eventCodeAgg"));
                branchCoverageDetailVO.setParamCode(getValue(keyMap, "paramKeyAgg"));
                branchCoverageDetailVO.setParamValue(getValue(keyMap, "paramValueAgg"));
                result.add(branchCoverageDetailVO);
            }
            return result;
        } catch (Exception e) {
            log.error("queryBranchCoverage failed, index={}", "insight_esparam*", e);
            throw new RuntimeException("查询覆盖分支情况失败", e);
        }
    }

    private static String getValue(Map<String, Object> map, String key) {
        if (map == null) {
            return "";
        }
        Object o = map.get(key);
        if (o == null) {
            return "";
        }
        return (String) o;
    }
}
