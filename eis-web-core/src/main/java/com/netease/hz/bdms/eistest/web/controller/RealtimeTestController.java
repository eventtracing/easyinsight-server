package com.netease.hz.bdms.eistest.web.controller;


import com.netease.eis.adapters.CacheAdapter;
import com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.statistic.ReqTestInfoDTO;
import com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.statistic.TreeModeStatisticResultDTO;
import com.netease.hz.bdms.easyinsight.common.dto.realtimetest.RealTimeTestResourceDTO;
import com.netease.hz.bdms.easyinsight.common.dto.realtimetest.ServerLogParam;
import com.netease.hz.bdms.easyinsight.common.enums.BuryPointLogTypeEnum;
import com.netease.hz.bdms.easyinsight.common.enums.ParamValueTypeEnum;
import com.netease.hz.bdms.easyinsight.common.enums.RealTestResultEnum;
import com.netease.hz.bdms.easyinsight.common.http.HttpResult;
import com.netease.hz.bdms.easyinsight.common.param.auth.TestStatisticInfoParam;
import com.netease.hz.bdms.easyinsight.common.param.auth.UserBaseInfoParam;
import com.netease.hz.bdms.easyinsight.common.util.CommonUtil;
import com.netease.hz.bdms.easyinsight.common.util.JsonUtils;
import com.netease.hz.bdms.easyinsight.common.vo.logcheck.BranchCoverageDetailVO;
import com.netease.hz.bdms.easyinsight.common.dto.audit.BloodLink;
import com.netease.hz.bdms.easyinsight.service.service.audit.BuryPointRule;
import com.netease.hz.bdms.easyinsight.service.service.util.LogUtil;
import com.netease.hz.bdms.eistest.cache.ConversationMetaCache;
import com.netease.hz.bdms.eistest.entity.*;
import com.netease.hz.bdms.eistest.service.es.ElasticsearchQueryService;
import com.netease.hz.bdms.eistest.cache.BuryPointProcessorKey;
import com.netease.hz.bdms.eistest.service.BloodLinkService;
import com.netease.hz.bdms.eistest.service.BuryPointAnaysisService;
import com.netease.hz.bdms.eistest.service.BuryPointTestInfoCacheService;
import com.netease.hz.bdms.easyinsight.common.vo.eistest.DoubleListFallsPageResultVO;
import com.netease.hz.bdms.easyinsight.common.vo.eistest.FallsPage;
import com.netease.hz.bdms.easyinsight.common.vo.eistest.FallsPageRequest;
import com.netease.hz.bdms.easyinsight.common.vo.eistest.FallsPageResult;
import com.netease.hz.bdms.eistest.ws.SessionManager;
import com.netease.hz.bdms.eistest.ws.dto.BuryPointMetaInfo;
import com.netease.hz.bdms.eistest.ws.session.AppSession;
import com.netease.hz.bdms.eistest.ws.session.WsSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.CloseStatus;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/processor/realtime/test")
@Slf4j
public class RealtimeTestController {

    @Autowired
    @Qualifier("appSessionManager")
    protected SessionManager appSessionManager;
    @Autowired
    private BloodLinkService bloodLinkService;
    @Resource
    private CacheAdapter cacheAdapter;
    @Resource
    private BuryPointAnaysisService buryPointAnaysisService;
    @Resource
    private BuryPointTestInfoCacheService buryPointTestInfoCacheService;
    @Resource
    private ElasticsearchQueryService elasticsearchQueryService;
    @Resource
    private ConversationMetaCache conversationMetaCache;

    /**
     * @return {@link BuryPointMetaInfoDto}
     */
    @GetMapping("/get/metainfo")
    public HttpResult<BuryPointMetaInfoDto> getStatistics(@RequestParam(name = "code") String code){

        String key = BuryPointProcessorKey.getBuryPointMetaKey(code);
        String metaInfoString = cacheAdapter.get(key);
        BuryPointMetaInfoDto buryPointMetaInfo = JsonUtils.parseObject(metaInfoString, BuryPointMetaInfoDto.class);
        return HttpResult.success(buryPointMetaInfo);
    }

    @GetMapping("/upload/end")
    public HttpResult<Boolean> endConversation(@RequestParam(name = "code") String code){

        try {
            WsSession appSession = appSessionManager.getSessionByCode(code);
            if (appSession != null) {
                appSession.getRaw().close(CloseStatus.GOING_AWAY);
                appSessionManager.removeSession(code);
            }
        }catch (Exception e){
            log.error("/upload/end failed", e);
            return HttpResult.success(false);
        }
        return HttpResult.success(true);
    }

    /**
     * 上报用户信息
     * @return {@link Boolean}
     */
    @PostMapping("/set/userInfo")
    public HttpResult<Boolean> setUserInfo(@RequestBody @Validated UserBaseInfoParam param){
        String key = BuryPointProcessorKey.getBuryPointUserKey(param.getConversation());
        String s = cacheAdapter.get(key);
        // 如果已上报，则无需覆盖
        if (StringUtils.isBlank(s)) {
            cacheAdapter.setWithExpireTime(key, JsonUtils.toJson(param), 86400 * 30); // 30d
        }
        return HttpResult.success(true);
    }

    /**
     * 上报服务端日志
     */
    @PostMapping("/upload/serverlog")
    public HttpResult<String> uploadServerLog(@RequestBody ServerLogParam param) {
        log.info("/upload/serverlog param={}", JsonUtils.toJson(param));
        if (param.getData() == null) {
            param.setData(new HashMap<>());
        }
        param.getData().put("_action", param.getAction());
        String data = JsonUtils.toJson(param.getData());
        String conversation = String.valueOf(param.getCode());
        BuryPointMetaInfo buryPointMetaInfo = conversationMetaCache.get(conversation);
        if (buryPointMetaInfo == null) {
            return HttpResult.success("会话ID不存在 " + conversation);
        }
        // TODO
        BuryPointLog buryPointLog = new BuryPointLog();
        buryPointLog.setAction(param.getAction());
        buryPointLog.setContent(data);
        buryPointLog.setEt(true);
        buryPointLog.setIndex(-1L);
        buryPointLog.setLogTime(System.currentTimeMillis());
        buryPointLog.setLogtype("ua");
        buryPointLog.setOs("server");
        WsSession appSession = appSessionManager.getSessionByCode(conversation);
        if(appSession == null){
            log.error("会话不存在，请检查客户端连接状态。code={}", conversation);
        }
        try {
            buryPointAnaysisService.parseBuryPointResource(conversation, (AppSession) appSession, JsonUtils.toJson(buryPointLog), AppPushLogAction.LOG.getName());
        } catch (Exception e) {
            log.error("parseBuryPointResource failed! code {} data {}", conversation, data, e);
        }
        return HttpResult.success(null);
    }

    /**
     * 上报统计信息快照
     * @return {@link Boolean}
     */
    @PostMapping("/set/snapshot")
    public HttpResult<Boolean> setStaSnapshot(@RequestBody @Validated TestStatisticInfoParam param){
        boolean ret =buryPointTestInfoCacheService.saveTestRecordInfo(param);
        return HttpResult.success(ret);
    }

    /**
     * 轮询埋点数据统计结果
     * @param code
     * @param indexName 统计类型
     * @return {@link BuryPointStatisticsDto}
     */
    @GetMapping("/statistics")
    public HttpResult<BuryPointStatisticsDto> getStatistics(@RequestParam(name = "code") String code,
                                                            @RequestParam(name = "taskId", required = false) Long taskId,
                                                            @RequestParam(name = "terminalId", required = false) Long terminalId,
                                                            @RequestParam(name = "domainId", required = false) Long domainId,
                                                            @RequestParam(name = "appId", required = false) Long appId,
                                                            @RequestParam(name = "indexName", required = false) String indexName){


        indexName = "insight_eslog*";
//        BuryPointStatisticsDto buryPointStatistics = buryPointTestInfoCacheService.getBuryPointStatistics(code);
        AppSession appSession = (AppSession) appSessionManager.getSessionByCode(code);
        BuryPointMetaInfo buryPointMetaInfo = new BuryPointMetaInfo(bloodLinkService);
        if(appSession == null || appSession.getStorage() == null || appSession.getStorage().getMetaInfo() == null){
            buryPointMetaInfo.initBuryPointRule(taskId, terminalId, domainId, appId);
        }else {
            buryPointMetaInfo = appSession.getStorage().getMetaInfo();
        }
        // 无需测试分支
        buryPointMetaInfo.updateIgnoreBranches(code);
        List<BranchCoverageDetailVO> branchCoverageIgnores = buryPointMetaInfo.getBranchCoverageIgnores();
        Map<String, List<BranchCoverageDetailVO>> branchIgnoreGroupBySpm = branchCoverageIgnores.stream().collect(Collectors.groupingBy(BranchCoverageDetailVO::getSpm));
//        BuryPointMetaInfo buryPointMetaInfo = new BuryPointMetaInfo(bloodLinkService);
//        BuryPointMetaInfo buryPointMetaInfo = appStorage.getMetaInfo();
//        RealTimeTestResourceDTO realTimeTestResourceDTO = buryPointMetaInfo.initBuryPointRule(taskId, terminalId, domainId, appId);
        Map<String, String> oidToNameMap = Optional.ofNullable(buryPointMetaInfo.getConversationObjectMap()).orElse(new HashMap<>());
        Map<String, String> evToNameMap = Optional.ofNullable(buryPointMetaInfo.getConversationEventCodeToNameMap()).orElse(new HashMap<>());
        BuryPointStatisticsDto buryPointStatistics = elasticsearchQueryService.queryCountStatistic(indexName, code, oidToNameMap, evToNameMap, buryPointMetaInfo.getOidBindEventMap());

        if(buryPointStatistics != null) {
            buryPointStatistics.setAppStatus(appSession != null && appSession.getStorage() != null && appSession.getStorage().getMetaInfo() != null);
//            //组装待更新日志数量
////            Triple<Long, Long, Long> updatePointCountTriple = buryPointTestInfoCacheService.getInsightLogUpdateCount(code);
            Long firstCount = elasticsearchQueryService.queryLogCountByCodeType(indexName, code, BuryPointLogTypeEnum.INSIGHT.getCode());
            Long secondCount = elasticsearchQueryService.queryLogCountByCodeType(indexName, code, BuryPointLogTypeEnum.OLDVERSION.getCode());
            Long thirdCount = elasticsearchQueryService.queryLogCountByCodeType(indexName, code, BuryPointLogTypeEnum.EXCEPTION.getCode());
            Triple<Long, Long, Long> webCount = buryPointTestInfoCacheService.getLogWebCount(code);
            Triple<Long, Long, Long> updatePointCountTriple = Triple.of(firstCount - webCount.getLeft(), secondCount - webCount.getMiddle(), thirdCount - webCount.getRight());
            buryPointStatistics.setLogUpdateNum(updatePointCountTriple.getLeft());
            buryPointStatistics.setOldLogUpdateNum(updatePointCountTriple.getMiddle());
            buryPointStatistics.setExceptionLogNum(updatePointCountTriple.getRight());

            //获取spm覆盖分支数
            ReqTestInfoDTO reqTestInfoStatistic = new ReqTestInfoDTO();
            Map<String, RealTimeTestResourceDTO.Linage> linageMap = buryPointMetaInfo.getLinageMap();
            Map<String, List<RealTimeTestResourceDTO.Event>> oidPath2EventMap = buryPointMetaInfo.getOidBindEventMap();
            Map<String, Integer> paramMap = new HashMap<>();
            List<TreeModeStatisticResultDTO> treeModeStatisticResultDTOS = buryPointStatistics.getTreeModeStatistic();
            treeModeStatisticResultDTOS = treeModeStatisticResultDTOS.stream().filter(dto -> CollectionUtils.isNotEmpty(dto.getDetails())).collect(Collectors.toList());
            List<String> nowSpms = treeModeStatisticResultDTOS.stream().map(TreeModeStatisticResultDTO::getSpm).collect(Collectors.toList());
            Set<String> eventCodeSet = new HashSet<>();
            for(String spm : buryPointMetaInfo.getUpdateSpmInfo()) {
                if(nowSpms.contains(spm)) continue;
                TreeModeStatisticResultDTO currTreeModeStaResult = new TreeModeStatisticResultDTO();
                currTreeModeStaResult.setSpm(spm);
                currTreeModeStaResult.setResult(RealTestResultEnum.NOT_PASS.getResult());
                BuryPointRule rule = buryPointMetaInfo.getBuryPointRule(spm);
                if(rule == null){
                    log.error("code:" + code + " spm:" + spm + " taskId:" + taskId + "规则获取失败，请查看埋点设计");
                    continue;
                }
                RealTimeTestResourceDTO.Linage linage = linageMap.get(spm);
                String oid = linage.getOid();
                LinkedHashMap<String, Map<String, BloodLink.Param>> pageListVerifiers = rule.getPageListVerifiers();
                LinkedHashMap<String, Map<String, BloodLink.Param>> eleListVerifiers = rule.getEleListVerifiers();
                LinkedHashMap<String, Map<String, BloodLink.Param>> allVerifiers = new LinkedHashMap<String, Map<String, BloodLink.Param>>();
                allVerifiers.putAll(pageListVerifiers);
                allVerifiers.putAll(eleListVerifiers);
                Map<String, BloodLink.Param> stringParamMap = allVerifiers.get(oid);
//                stringParamMap.putAll(eventParamMap);
                int spmCount = 0;
                if(stringParamMap != null && stringParamMap.size() > NumberUtils.LONG_ZERO) {
                    for (String paramKey : stringParamMap.keySet()) {
                        BloodLink.Param paramValue = stringParamMap.get(paramKey);
                        if (paramValue.getValueType().equals(ParamValueTypeEnum.VARIABLE.getType()) || paramKey.equals("_oid")) {
                            continue;
                        }
                        List<String> paramValueList = paramValue.getSelectedValues();
                        spmCount += paramValueList.size();
                    }
                }
                paramMap.put(spm, spmCount > NumberUtils.INTEGER_ZERO ? spmCount : 1);
                List<RealTimeTestResourceDTO.Event> events = oidPath2EventMap.get(oid);
                if(CollectionUtils.isNotEmpty(events)) {
                    List<TreeModeStatisticResultDTO.EventCheckResultItemDTO> details = new ArrayList<>();
                    for (RealTimeTestResourceDTO.Event event : events) {
                        TreeModeStatisticResultDTO.EventCheckResultItemDTO eventCheckResultItemDTO = new TreeModeStatisticResultDTO.EventCheckResultItemDTO();
                        eventCheckResultItemDTO.setReqSum(paramMap.get(spm));
                        eventCheckResultItemDTO.setEventCode(event.getCode());
                        details.add(eventCheckResultItemDTO);
                        eventCodeSet.add(event.getCode());
                    }
                    currTreeModeStaResult.setDetails(details);
                    treeModeStatisticResultDTOS.add(currTreeModeStaResult);
                }
            }

            int reqBranchCount = 0;
            int paramCount = 0;
            for(TreeModeStatisticResultDTO treeModeStatisticResultDTO : treeModeStatisticResultDTOS){
                if(buryPointMetaInfo.getUpdateSpmInfo().contains(treeModeStatisticResultDTO.getSpm())) {
                    List<TreeModeStatisticResultDTO.EventCheckResultItemDTO> eventCheckResultItemDTOS = treeModeStatisticResultDTO.getDetails();
                    if (CollectionUtils.isNotEmpty(eventCheckResultItemDTOS)) {
                        paramCount += eventCheckResultItemDTOS.get(0).getReqSum();
                        for (TreeModeStatisticResultDTO.EventCheckResultItemDTO eventCheckResultItemDTO : eventCheckResultItemDTOS) {
                            reqBranchCount += eventCheckResultItemDTO.getReqSum();
                        }
                    }
                }
            }
            buryPointStatistics.setTreeModeStatistic(treeModeStatisticResultDTOS);

            reqTestInfoStatistic.setSpmNum(buryPointMetaInfo.getUpdateSpmInfo().size());
            reqTestInfoStatistic.setActionNum(eventCodeSet.size());
            reqTestInfoStatistic.setBranchNum(reqBranchCount);
            reqTestInfoStatistic.setParamNum(paramCount);
            buryPointStatistics.setReqTestInfoStatistic(reqTestInfoStatistic);

            //组装即使摘要
            String key = BuryPointProcessorKey.getUserTestRecordStaKey(code);
            String snapshotInfoString = cacheAdapter.get(key);
            TestStatisticInfoParam testStatisticInfoParam = JsonUtils.parseObject(snapshotInfoString, TestStatisticInfoParam.class);
            buryPointStatistics.setTestStatisticInfoParam(testStatisticInfoParam);
        }else {
            buryPointStatistics = new BuryPointStatisticsDto();
        }

        // 手动标记无需覆盖的分支数从待测分支中去除
        if (CollectionUtils.isNotEmpty(branchCoverageIgnores) && CollectionUtils.isNotEmpty(buryPointStatistics.getTreeModeStatistic())) {
            buryPointStatistics.getTreeModeStatistic().forEach(treeNode -> {
                if (StringUtils.isBlank(treeNode.getSpm()) || CollectionUtils.isEmpty(treeNode.getDetails())) {
                    return;
                }
                List<BranchCoverageDetailVO> branchCoverageIgnoreOfThisSpm = branchIgnoreGroupBySpm.get(treeNode.getSpm());
                if (CollectionUtils.isEmpty(branchCoverageIgnoreOfThisSpm)) {
                   return;
                }
                for (TreeModeStatisticResultDTO.EventCheckResultItemDTO detail : treeNode.getDetails()) {
                    if (detail.getReqSum() == null || detail.getReqSum() < 1) {
                        continue;
                    }
                    int ignoreCount = 0;
                    String eventCode = detail.getEventCode();
                    for (BranchCoverageDetailVO ignore : branchCoverageIgnoreOfThisSpm) {
                        if (StringUtils.equals(ignore.getEventCode(), eventCode)) {
                            ignoreCount++;
                        }
                    }
                    // 待测分支数扣除，不得减至detail.getHitSum()以下
                    int correctReqSum = Math.max(detail.getHitSum(), detail.getReqSum() - ignoreCount);
                    detail.setReqSum(correctReqSum);
                }
            });
        }

        return HttpResult.success(buryPointStatistics);
    }

    /**
     * 更新埋点日志统计结果
     * @param oid
     * @param evnetId
     * @param code
     * @param logType 日志类型
     * @param updateFlag 是否更新
     * @param pageRequest 分页请求参数
     * @param userOperate 用户操作 1 -清空日志，2-展示全部
     * @return {@link FallsPageResult<BuryPointLogRuleCheckDto>}
     */
    @RequestMapping("/logInfo")
    public HttpResult<FallsPageResult<BuryPointLogRuleCheckDto>> getLogUpdateInfo(@RequestParam(name = "oid", required = false) String oid,
                                                                      @RequestParam(name = "eventId", required = false) String evnetId,
                                                                      @RequestParam(name = "spm", required = false) String spm,
                                                                      @RequestParam(name = "checkType", required = false) String checkType,
                                                                      @RequestParam(name = "code") String code,
                                                                      @RequestParam(name = "logType") int logType,
                                                                      @RequestParam(name = "updateFlag") boolean updateFlag,
                                                                      @RequestParam(name = "searchStr", required = false) String searchStr,
                                                                      @RequestParam(name = "userOperate", required = false) Integer userOperate,
                                                                      @RequestParam(name = "pageRequest")String pageRequest) throws Exception {


        //参数校验
        FallsPageRequest fallsPageRequest = JsonUtils.parseObject(pageRequest, FallsPageRequest.class);
        if (fallsPageRequest == null || fallsPageRequest.getSize() == null || fallsPageRequest.getSize() < NumberUtils.INTEGER_ONE) {
            throw new Exception("参数异常");
        }

        //检查用户操作项
        if(userOperate != null && userOperate > NumberUtils.INTEGER_ZERO){
            buryPointTestInfoCacheService.setInsightUserLogCount(code, logType, userOperate);
        }

        long userIndex = buryPointTestInfoCacheService.getInsightUserLogCount(code, logType);

        //根据分页参数捞日志
        List<String> buryPointLogString= buryPointTestInfoCacheService.getInsightLogByPage(code,logType, userIndex);
        if(CollectionUtils.isEmpty(buryPointLogString) || NumberUtils.INTEGER_ONE.equals(userOperate)){
            FallsPageResult<BuryPointLogRuleCheckDto> result = new FallsPageResult<>();
            FallsPage page = new FallsPage();
            page.setTotal(0);
            result.setPage(page);
            return HttpResult.success(result);
        }

        //根据类型解析日志
        spm = LogUtil.removePos(spm);
        List<BuryPointLogRuleCheckDto> buryPointLogList = buryPointAnaysisService.parseBuryPointLog(buryPointLogString, logType, oid, evnetId, spm, searchStr, checkType);
        int startIndex = 0;
        int endIndex = buryPointLogList.size();
        if(!updateFlag && StringUtils.isBlank(searchStr)){
            endIndex = fallsPageRequest.getTotal();
        }
        buryPointLogList = buryPointLogList.subList(startIndex, endIndex);


        //记录web端当前日志数量
        if(updateFlag){
            buryPointTestInfoCacheService.setInsightLogWebCount(code, logType);
        }

        FallsPageResult<BuryPointLogRuleCheckDto> result = new FallsPageResult<>();

        int startRecord = fallsPageRequest.getSize()*(fallsPageRequest.getPage()-1);
        result.setRecords(buryPointLogList.subList(startRecord, Math.min(startRecord + fallsPageRequest.getSize(), buryPointLogList.size())));
        FallsPage page = new FallsPage();
        page.setTotal(updateFlag || StringUtils.isNotBlank(searchStr) ? buryPointLogList.size():fallsPageRequest.getTotal());
        page.setSize(fallsPageRequest.getSize());
        page.setPage(fallsPageRequest.getPage());
        result.setPage(page);

        return HttpResult.success(result);
    }


    @RequestMapping("/query/data")
    public HttpResult<DoubleListFallsPageResultVO<Map<String,Object>, String, BranchCoverageDetailVO>> query(@RequestParam(name = "indexName", required = false) String indexName,
                                                                                                             @RequestParam(name = "eventId", required = false) String eventCode,
                                                                                                             @RequestParam(name = "spm", required = false) String spm,
                                                                                                             @RequestParam(name = "checkType", required = false) Integer checkType,
                                                                                                             @RequestParam(name = "code") String code,
                                                                                                             @RequestParam(name = "logType") int logType,
                                                                                                             @RequestParam(name = "updateFlag") boolean updateFlag,
                                                                                                             @RequestParam(name = "searchStr", required = false) String searchStr,
                                                                                                             @RequestParam(name = "userOperate", required = false) Integer userOperate,
                                                                                                             @RequestParam(name = "appId", required = false) Long appId,
                                                                                                             @RequestParam(name = "terminalId", required = false) Long terminalId,
                                                                                                             @RequestParam(name = "domainId", required = false) Long domainId,
                                                                                                             @RequestParam(name = "taskId", required = false) Long taskId,
                                                                                                             @RequestParam(name = "pageRequest")String pageRequest) throws Exception {

        //参数校验
        indexName = "insight_eslog*";
        FallsPageRequest fallsPageRequest = JsonUtils.parseObject(pageRequest, FallsPageRequest.class);
        if (fallsPageRequest == null || fallsPageRequest.getSize() == null || fallsPageRequest.getSize() < NumberUtils.INTEGER_ONE) {
            throw new Exception("参数异常");
        }

        int offset = (fallsPageRequest.getPage() - 1) * fallsPageRequest.getSize();
        int size = fallsPageRequest.getSize();

        // 待覆盖分支
        Set<BranchCoverageDetailVO> shouldCoverBranches = getShouldCoverBranchCoverage(code, spm, eventCode, taskId, terminalId, domainId, appId);
        List<BranchCoverageDetailVO> needCover = new ArrayList<>(shouldCoverBranches);
        List<BranchCoverageDetailVO> covered = new ArrayList<>();

        // 已覆盖分支
        if (CollectionUtils.isNotEmpty(shouldCoverBranches)) {
            Set<BranchCoverageDetailVO> branchCoverage = elasticsearchQueryService.getBranchCoverage(spm, code, eventCode);
            covered.addAll(branchCoverage);
            shouldCoverBranches.removeIf(branchCoverage::contains);
        }

        //检查用户操作项
        if(userOperate != null && userOperate > NumberUtils.INTEGER_ZERO){
            buryPointTestInfoCacheService.setInsightUserLogCount(code, logType, userOperate);
        }
        long userIndex = buryPointTestInfoCacheService.getInsightUserLogCount(code, logType);

        spm = LogUtil.removePos(spm);
        List<Map<String,Object>> list = elasticsearchQueryService.queryInsightlog(indexName, code, logType, eventCode, spm, checkType, searchStr, (int)userIndex);
        if(CollectionUtils.isEmpty(list) || NumberUtils.INTEGER_ONE.equals(userOperate)){
            DoubleListFallsPageResultVO<Map<String,Object>, String, BranchCoverageDetailVO> result = new DoubleListFallsPageResultVO<>();
            FallsPage page = new FallsPage();
            page.setTotal(0);
            result.setPage(page);
            result.setExtraRecords(getBranchCoverageDescs(shouldCoverBranches));
            result.setNeedCover(getBranchCoverageDescs(needCover));
            result.setShouldCoverData(new ArrayList<>(shouldCoverBranches));
            result.setCovered(getBranchCoverageDescs(covered));
            return HttpResult.success(result);
        }


        int endIndex = list.size();
        if(!updateFlag && StringUtils.isBlank(searchStr)){
            endIndex = fallsPageRequest.getTotal();
        }
        list = list.subList(0, endIndex);

        //记录web端当前日志数量
        if(updateFlag){
            Long count = elasticsearchQueryService.queryLogCountByCodeType(indexName, code, logType);
            buryPointTestInfoCacheService.setLogWebCount(code, logType, count);
        }

        DoubleListFallsPageResultVO<Map<String,Object>, String, BranchCoverageDetailVO> result = new DoubleListFallsPageResultVO<>();
        result.setRecords(list.subList(offset, Math.min(offset + size, list.size())));
        result.setExtraRecords(getBranchCoverageDescs(shouldCoverBranches));
        result.setNeedCover(getBranchCoverageDescs(needCover));
        result.setShouldCoverData(new ArrayList<>(shouldCoverBranches));
        result.setCovered(getBranchCoverageDescs(covered));
        FallsPage page = new FallsPage();
        page.setTotal(updateFlag || StringUtils.isNotBlank(searchStr) ? list.size():fallsPageRequest.getTotal());
        page.setSize(fallsPageRequest.getSize());
        page.setPage(fallsPageRequest.getPage());
        result.setPage(page);

        return HttpResult.success(result);
    }

    private BuryPointMetaInfo getBuryPointMetaInfo(String code, Long taskId, Long terminalId, Long domainId, Long appId) {
        AppSession appSession = (AppSession) appSessionManager.getSessionByCode(code);
        if (appSession != null) {
            return appSession.getStorage().getMetaInfo();
        }

        if (taskId != null && terminalId != null && domainId != null && appId != null) {
            BuryPointMetaInfo buryPointMetaInfo = new BuryPointMetaInfo(bloodLinkService);
            buryPointMetaInfo.initBuryPointRule(taskId, terminalId, domainId, appId);
            return buryPointMetaInfo;
        }
        return null;
    }

    private Set<BranchCoverageDetailVO> getShouldCoverBranchCoverage(String code, String requestSpm, String eventCode, Long taskId, Long terminalId, Long domainId, Long appId) {
        if (StringUtils.isBlank(requestSpm) || StringUtils.isBlank(code)) {
            return new HashSet<>();
        }
        BuryPointMetaInfo buryPointMetaInfo = getBuryPointMetaInfo(code, taskId, terminalId, domainId, appId);
        if (buryPointMetaInfo == null) {
            return new HashSet<>();
        }
        BuryPointRule buryPointRule = buryPointMetaInfo.getConversationRuleMap().get(requestSpm);
        if (buryPointRule == null) {
            return new HashSet<>();
        }
        buryPointMetaInfo.updateIgnoreBranches(code);
        Set<BranchCoverageDetailVO> branchCoverageIgnores = new HashSet<>(buryPointMetaInfo.getBranchCoverageIgnores());
        List<String> oids = CommonUtil.transSpmToOidList(requestSpm);
        String currentOid = oids.get(0);
        List<RealTimeTestResourceDTO.Event> events = buryPointMetaInfo.getOidBindEventMap().get(currentOid);
        if (CollectionUtils.isEmpty(events)) {
            return new HashSet<>();
        }
        Set<String> eventCodes = events.stream().map(RealTimeTestResourceDTO.Event::getCode).collect(Collectors.toSet());
        if (StringUtils.isNotBlank(eventCode)) {
            eventCodes.removeIf(o -> !eventCode.equals(o));
        }
        Set<BranchCoverageDetailVO> result = new HashSet<>();
        for (String ruleEventCode : eventCodes) {
            List<BloodLink.Param> allParams = new ArrayList<>();
            Map<String, BloodLink.Param> paramCodeToParamMap;
            paramCodeToParamMap = buryPointRule.getPageListVerifiers().get(currentOid);
            if (paramCodeToParamMap != null) {
                allParams.addAll(paramCodeToParamMap.values());
            }
            paramCodeToParamMap = buryPointRule.getEleListVerifiers().get(currentOid);
            if (paramCodeToParamMap != null) {
                allParams.addAll(paramCodeToParamMap.values());
            }
            allParams.forEach(p -> {
                if (p == null || !ParamValueTypeEnum.CONSTANT.getType().equals(p.getValueType()) || CollectionUtils.isEmpty(p.getSelectedValues()) || "_oid".equals(p.getCode())) {
                    return;
                }
                for (String selectedValue : p.getSelectedValues()) {
                    BranchCoverageDetailVO branchCoverageDetailVO = new BranchCoverageDetailVO();
                    branchCoverageDetailVO.setSpm(requestSpm);
                    branchCoverageDetailVO.setEventCode(ruleEventCode);
                    branchCoverageDetailVO.setParamCode(p.getCode());
                    branchCoverageDetailVO.setParamValue(selectedValue);
                    // 手动标记不无需覆盖的跳过
                    if (branchCoverageIgnores.contains(branchCoverageDetailVO)) {
                        continue;
                    }
                    result.add(branchCoverageDetailVO);
                }
            });
        }
        return result;
    }

    public static List<String> getBranchCoverageDescs(Collection<BranchCoverageDetailVO> branchCoverageDetailVOS) {
        return branchCoverageDetailVOS.stream().map(o -> o.getSpm() + ", " + o.getEventCode() + ", 参数：" + o.getParamCode() + ", 取值：" + o.getParamValue()).collect(Collectors.toList());
    }
}
