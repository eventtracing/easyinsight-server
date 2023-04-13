package com.netease.hz.bdms.easyinsight.service.service.audit;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.netease.hz.bdms.easyinsight.common.dto.audit.BloodLink;
import com.netease.hz.bdms.easyinsight.common.dto.audit.CheckScopeEnum;
import com.netease.hz.bdms.easyinsight.common.dto.message.EasyInsightLogMessage;
import com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.rulecheck.*;
import com.netease.hz.bdms.easyinsight.common.enums.CheckResultEnum;

import com.netease.hz.bdms.easyinsight.common.enums.LogCheckResultEnum;
import com.netease.hz.bdms.easyinsight.service.service.util.CompareUtil;
import com.netease.hz.bdms.easyinsight.service.service.util.LogUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Data
@Accessors(chain = true)
@Slf4j
public class BuryPointRule {

    //根据血缘链路构造唯一key，例如：nm_user_home/module_user_hometop/module_userrecom/card_user_ver/btn_focus
    private String key;
    private Long trackerId;
    // 事件公参：当前对象的事件公参， key指事件类型code, value指事件公参信息
    private Map<String, List<BloodLink.Param>> eventsVerifiers;
    // 全局公参：当前对象的全局公参， key指参数code， value指参数对象（含参数值信息）
    private Map<String, BloodLink.Param> commonVerifiers;
    // 页面的私参：当前对象及其祖先页面的对象业务私参和对象标准私参， key指的是祖先页面的oid, value是指<参数code， 参数对象（含参数值信息）>
    private LinkedHashMap<String, Map<String, BloodLink.Param>> pageListVerifiers;
    // 元素的私参：当前对象及其祖先页面的对象业务私参和对象标准私参， key指的是祖先元素的oid， value指<参数code， 参数对象（含参数值信息）>
    private LinkedHashMap<String, Map<String, BloodLink.Param>> eleListVerifiers;

    /**
     * 实时测试用
     */
    public RuleCheckSimpleDTO validate(String realTimeType, Map<String, Object> props,
                                       String eventCode,
                                       String eventName, Map<String, String> oid2NameMap, Long logServerTime, Long index) {
        EasyInsightLogMessage buryPointLog = new EasyInsightLogMessage();
        buryPointLog.setProps(props);
        return validate(buryPointLog, eventCode, eventName, oid2NameMap, logServerTime, index, realTimeType, CheckScopeEnum.REALTIME_ONLY);
    }

    /**
     * 稽查用
     */
    public RuleCheckSimpleDTO validate(EasyInsightLogMessage buryPointLog,
                                       String eventCode,
                                       String eventName, Map<String, String> oid2NameMap, Long logServerTime) {
        return validate(buryPointLog, eventCode, eventName, oid2NameMap, logServerTime, null, null, CheckScopeEnum.LOG_STATS_ONLY);
    }

    public RuleCheckSimpleDTO validate(EasyInsightLogMessage buryPointLog,
                                       String eventCode,
                                       String eventName, Map<String, String> oid2NameMap, Long logServerTime, Long index, String realTimeType, CheckScopeEnum checkScope) {
        Map<String, BloodLink.Param> eventVerifiers = getEventVerifier(eventCode);
        String spm = LogUtil.removePos(LogUtil.getSpm(buryPointLog));

        List<Map<String, CompareItemSimpleDTO>> pListMapsInLog = Lists.newArrayList();
        List<Map<String, CompareItemSimpleDTO>> eListMapsInLog = Lists.newArrayList();
        Map<String, CompareItemSimpleDTO> otherMapInLog = Maps.newHashMap();

        List<Map<String, CompareItemSimpleDTO>> pListMapsInRule = Lists.newArrayList();
        List<Map<String, CompareItemSimpleDTO>> eListMapsInRule = Lists.newArrayList();
        Map<String, CompareItemSimpleDTO> otherMapInRule = Maps.newHashMap();
        Map<String, String> logCauseMap = new HashMap<>();
        List<String> unMatchedParamCode = new ArrayList<>();

        LogCheckResultEnum totalCheckResult = LogCheckResultEnum.OK;// 全局的校验结果，若任一参数的校验过程中出现了NOT_PASS, 则该字段为NOT_PASS
        int allPrivateParamInRule = 0;// 规则中的所有私参数目
        int allPublicParamInRule = 0;// 规则中的所有公参数目

        int notEmptyPrivateParamHitKeyInLog = 0;// 日志中命中参数code所有非空私参数目
        int notEmptyPublicParamHitKeyInLog = 0;// 日志中命中参数code所有非空公参数目
        int privateParamOfHitKeyInLog = 0; // 日志中的命中私参的参数code(key)的私参数目
        int privateParamOfHitValueInLog = 0; // 日志中的命中私参的参数候选值(value)的私参数目
        int notEmptyPrivateParamWithoutValueInLog = 0;// 日志中的无值的非空私参数目
        int publicParamOfHitKeyInLog = 0; // 日志中的命中公参的参数code(key)的私参数目
        int publicParamOfHitValueInLog = 0; // 日志中的命中公参的参数候选值(value)的私参数目
        int notEmptyPublicParamWithoutValueInLog = 0;// 日志中的无值的非空私参数目

        // 稽查对错误归类用
        List<FailKeyDTO> failKeys = new ArrayList<>();

        // 考虑pList中的私参:优先以日志为遍历依据，构建日志这边的匹配情况（含相关指标信息）， 其次以规则为遍历依据，构建规则这边的匹配情况
        LinkedHashMap<String, Map<String, Object>> oid2ParamKey2ValueInPListLog = LogUtil
                .getPlist(buryPointLog.getProps()); // key表示oid, value表示<日志中参数key, 日志中参数value>
        boolean oid2ParamKey2ValueInPListLogNotEmpty = MapUtils.isNotEmpty(oid2ParamKey2ValueInPListLog);
        if (realTimeType != null) {
            oid2ParamKey2ValueInPListLogNotEmpty = oid2ParamKey2ValueInPListLogNotEmpty && realTimeType.equals("spm");
        }
        if (oid2ParamKey2ValueInPListLogNotEmpty) {
            for (String pageOidInLog : oid2ParamKey2ValueInPListLog.keySet()) {
                Map<String, CompareItemSimpleDTO> pListInLog = Maps.newHashMap();
                Map<String, Object> paramKey2ValueInPListLog = oid2ParamKey2ValueInPListLog
                        .get(pageOidInLog); // key表示日志中参数key, value表示日志中参数value
                if (pageListVerifiers != null && pageListVerifiers.containsKey(pageOidInLog)) {
                    Map<String, BloodLink.Param> paramCode2ParamInPListRule = pageListVerifiers
                            .get(pageOidInLog); // key表示规则中参数code, value表示参数值
                    for (String key : paramKey2ValueInPListLog.keySet()) {
                        BloodLink.Param paramInRule =
                                MapUtils.isNotEmpty(paramCode2ParamInPListRule) ? paramCode2ParamInPListRule
                                        .get(key) : null;
                        if (paramInRule != null) {
                            // 在本场景下不需要校验的参数，如"客户端实时校验参数"
                            if (!needCheck(checkScope, paramInRule)) {
                                continue;
                            }
                            CheckItemResultFormatSimpleDTO compareItem = CompareUtil
                                    .check(paramInRule, paramKey2ValueInPListLog);
                            if (compareItem != null) {
                                CompareItemSimpleDTO tmpLog = compareItem.getLog();
                                if (tmpLog != null) {
                                    pListInLog.put(tmpLog.getKey(), tmpLog);
                                }
                            }
                        } else {
                            // 相当于日志中包含多余的参数
                            CompareItemSimpleDTO log = new CompareItemSimpleDTO();
                            log.setKey(key)
                                    .setValue(String.valueOf(paramKey2ValueInPListLog.get(key)))
                                    .setCause("")
                                    .setComment("");
                            pListInLog.put(log.getKey(), log);
                            // 记录写入
                            unMatchedParamCode.add(key);
                        }
                    }
                } else {
//                    // 相当于日志中的血缘链路多了一个节点
//                    totalCheckResult = CheckResultEnum.NOT_PASS;
//                    if (MapUtils.isNotEmpty(paramKey2ValueInPListLog)) {
//                        for (String key : paramKey2ValueInPListLog.keySet()) {
//                            CompareItemSimpleDTO log = new CompareItemSimpleDTO();
//                            log.setKey(key)
//                                    .setValue(String.valueOf(paramKey2ValueInPListLog.get(key)))
//                                    .setCause(CheckErrorCauseEnum.REDUNDANT_TRACKER_PARAM_IN_BLOODLINK.getCause())
//                                    .setComment("");
//                            pListInLog.put(log.getKey(), log);
//                        }
//                    }
                }
                //plist根结点，校验根结点refer
                Map<String, Object> paramKey2ValueInRootPageLog = oid2ParamKey2ValueInPListLog.get(LogUtil.getRootPageOid(spm));
                if (!paramKey2ValueInRootPageLog.containsKey("_psrefer")) {
                    CompareItemSimpleDTO log = new CompareItemSimpleDTO();
                    log.setKey("_psrefer")
                            .setValue(String.valueOf(paramKey2ValueInRootPageLog.get("_psrefer")))
                            .setCause("缺失_psrefer！")
                            .setComment("");
                    pListInLog.put(log.getKey(), log);
                }
                if (!paramKey2ValueInRootPageLog.containsKey("_pgrefer")) {
                    CompareItemSimpleDTO log = new CompareItemSimpleDTO();
                    log.setKey("_pgrefer")
                            .setValue(String.valueOf(paramKey2ValueInRootPageLog.get("_pgrefer")))
                            .setCause("缺失_pgrefer！")
                            .setComment("");
                    pListInLog.put(log.getKey(), log);
                }

                if (MapUtils.isNotEmpty(pListInLog)) {
                    pListMapsInLog.add(pListInLog);
                    pListInLog.forEach((key, compareItemSimpleDTO) -> {
                        if (StringUtils.isNotBlank(compareItemSimpleDTO.getCause())) {
                            String logCauseKey = LogUtil.PLIST_KEYWORD + "#" + pageOidInLog + "#" + key;
                            logCauseMap.put(logCauseKey, compareItemSimpleDTO.getCause());
                        }
                    });
                }
            }

        }
        boolean pageListVerifiersNotEmpty = MapUtils.isNotEmpty(pageListVerifiers);
        if (realTimeType != null) {
            pageListVerifiersNotEmpty = pageListVerifiersNotEmpty && realTimeType.equals("spm");
        }
        if (pageListVerifiersNotEmpty) {
            for (String pageOid : pageListVerifiers.keySet()) {
                Map<String, Object> paramKey2ValueInPListLog = oid2ParamKey2ValueInPListLog
                        .get(pageOid); // key表示日志中参数key, value表示规则中参数value
                Map<String, BloodLink.Param> paramCode2ParamInPListRule = pageListVerifiers
                        .get(pageOid); // key表示规则中参数code, value表示规则中参数值
                if (MapUtils.isNotEmpty(paramCode2ParamInPListRule)) {
                    Map<String, CompareItemSimpleDTO> pListInRule = Maps.newHashMap();
                    for (String privateParamCode : paramCode2ParamInPListRule.keySet()) {
                        BloodLink.Param privateParamInPList = paramCode2ParamInPListRule.get(privateParamCode);
                        // 在本场景下不需要校验的参数，如"客户端实时校验参数"
                        if (!needCheck(checkScope, privateParamInPList)) {
                            continue;
                        }
                        allPrivateParamInRule++;
                        // 匹配
                        CheckItemResultFormatSimpleDTO compareItem = CompareUtil
                                .check(privateParamInPList, paramKey2ValueInPListLog);
                        if (compareItem != null) {
                            CompareItemSimpleDTO rule = compareItem.getRule();
                            if (rule != null) {
                                String logCauseKey = LogUtil.PLIST_KEYWORD + "#" + pageOid + "#" + rule.getKey();
                                String causeInLog = logCauseMap.get(logCauseKey);
                                // 如果日志里有错误，那把规则对应也标成错误
                                if (StringUtils.isNotBlank(causeInLog) && StringUtils.isBlank(rule.getCause())) {
                                    rule.setCause(causeInLog);
                                }
                                pListInRule.put(rule.getKey(), rule);
                            }
                            if (CheckResultEnum.NOT_PASS == compareItem.getCheckResult()) {
                                LogCheckResultEnum failedEnum = getFailLogCheckResult(compareItem);
                                addFailKey(pageOid, compareItem, failedEnum, failKeys);
                                totalCheckResult = failedEnum;
                            }
                            if (compareItem.getKeyMatched()) {
                                privateParamOfHitKeyInLog++;
                                if (privateParamInPList.getNotEmpty()) {
                                    notEmptyPrivateParamHitKeyInLog++;
                                    if (compareItem.getValueEmpty()) {
                                        notEmptyPrivateParamWithoutValueInLog++;
                                    }
                                }
                            }
                            if (compareItem.getValueMatched()) {
                                privateParamOfHitValueInLog++;
                            }
                        }
                    }
                    if (MapUtils.isNotEmpty(pListInRule)) {
                        pListMapsInRule.add(pListInRule);
                    }
                } else {
                    // 此时相当于规则中没有对应的参数信息，只有oid链信息
                    Map<String, CompareItemSimpleDTO> pListInRule = Maps.newHashMap();
                    CompareItemSimpleDTO emptyRule = new CompareItemSimpleDTO();
                    emptyRule.setKey(LogUtil.OID_KEYWORD)
                            .setValue(pageOid)
                            .setCause("")
                            .setComment("");
                    pListInRule.put(LogUtil.OID_KEYWORD, emptyRule);
                    pListMapsInRule.add(pListInRule);
                }
            }
        }

        // 考虑eList中的私参:优先以日志为遍历依据，构建日志这边的匹配情况（含相关指标信息）， 其次以规则为遍历依据，构建规则这边的匹配情况
        LinkedHashMap<String, Map<String, Object>> oid2ParamKey2ValueInEListLog = LogUtil
                .getElist(buryPointLog.getProps()); // key表示oid, value表示<日志中参数key, 日志中参数value>
        boolean oid2ParamKey2ValueInEListLogNotEmpty = MapUtils.isNotEmpty(oid2ParamKey2ValueInEListLog);
        if (realTimeType != null) {
            oid2ParamKey2ValueInEListLogNotEmpty = oid2ParamKey2ValueInEListLogNotEmpty && realTimeType.equals("spm");
        }
        if (oid2ParamKey2ValueInEListLogNotEmpty) {
            for (String pageOidInLog : oid2ParamKey2ValueInEListLog.keySet()) {
//        List<CompareItemSimpleDTO> eListInLog = Lists.newArrayList();
                Map<String, CompareItemSimpleDTO> eListInLog = Maps.newHashMap();
                Map<String, Object> paramKey2ValueInEListLog = oid2ParamKey2ValueInEListLog
                        .get(pageOidInLog); // key表示日志中参数key, value表示日志中参数value
//        if (MapUtils.isNotEmpty(paramKey2ValueInEListLog)) {
                if (eleListVerifiers != null && eleListVerifiers.containsKey(pageOidInLog)) {
                    Map<String, BloodLink.Param> paramCode2ParamInEListRule = eleListVerifiers
                            .get(pageOidInLog); // key表示规则中参数code, value表示参数值
                    for (String key : paramKey2ValueInEListLog.keySet()) {
                        BloodLink.Param paramInRule =
                                MapUtils.isNotEmpty(paramCode2ParamInEListRule) ? paramCode2ParamInEListRule
                                        .get(key) : null;
                        if (paramInRule != null) {
                            // 在本场景下不需要校验的参数，如"客户端实时校验参数"
                            if (!needCheck(checkScope, paramInRule)) {
                                continue;
                            }
                            CheckItemResultFormatSimpleDTO compareItem = CompareUtil
                                    .check(paramInRule, paramKey2ValueInEListLog);
                            if (compareItem != null) {
                                CompareItemSimpleDTO tmpLog = compareItem.getLog();
                                if (tmpLog != null) {
                                    eListInLog.put(tmpLog.getKey(), tmpLog);
                                }
                            }
                        } else {
                            // 相当于日志中包含多余的参数
                            CompareItemSimpleDTO log = new CompareItemSimpleDTO();
                            log.setKey(key)
                                    .setValue(String.valueOf(paramKey2ValueInEListLog.get(key)))
                                    .setCause("")
                                    .setComment("");
                            eListInLog.put(log.getKey(), log);
                            // 记录写入
                            unMatchedParamCode.add(key);
                        }
                    }
                } else {
//                    // 相当于日志中的血缘链路多了一个节点
//                    totalCheckResult = CheckResultEnum.NOT_PASS;
//                    if (MapUtils.isNotEmpty(paramKey2ValueInEListLog)) {
//                        for (String key : paramKey2ValueInEListLog.keySet()) {
//                            CompareItemSimpleDTO log = new CompareItemSimpleDTO();
//                            log.setKey(key)
//                                    .setValue(String.valueOf(paramKey2ValueInEListLog.get(key)))
//                                    .setCause(CheckErrorCauseEnum.REDUNDANT_TRACKER_PARAM_IN_BLOODLINK.getCause())
//                                    .setComment("");
//                            eListInLog.put(log.getKey(), log);
//                        }
//                    }
                }
                if (MapUtils.isNotEmpty(eListInLog)) {
                    eListMapsInLog.add(eListInLog);
                    eListInLog.forEach((key, compareItemSimpleDTO) -> {
                        if (StringUtils.isNotBlank(compareItemSimpleDTO.getCause())) {
                            String logCauseKey = LogUtil.ELIST_KEYWORD + "#" + pageOidInLog + "#" + key;
                            logCauseMap.put(logCauseKey, compareItemSimpleDTO.getCause());
                        }
                    });
                }
            }
//      }
        }
        boolean eleListVerifiersNotEmpty = MapUtils.isNotEmpty(eleListVerifiers);
        if (realTimeType != null) {
            eleListVerifiersNotEmpty = eleListVerifiersNotEmpty && realTimeType.equals("spm");
        }
        if (eleListVerifiersNotEmpty) {
            for (String eleOid : eleListVerifiers.keySet()) {
                Map<String, Object> paramKey2ValueInEListLog = oid2ParamKey2ValueInEListLog
                        .get(eleOid); // key表示日志中参数key, value表示规则中参数value
                Map<String, BloodLink.Param> paramCode2ParamInEListRule = eleListVerifiers
                        .get(eleOid); // key表示规则中参数code, value表示规则中参数值
                if (MapUtils.isNotEmpty(paramCode2ParamInEListRule)) {
//          List<CompareItemSimpleDTO> eListInRule = Lists.newArrayList();
                    Map<String, CompareItemSimpleDTO> eListInRule = Maps.newHashMap();
                    for (String privateParamCode : paramCode2ParamInEListRule.keySet()) {
                        BloodLink.Param privateParamInEList = paramCode2ParamInEListRule.get(privateParamCode);
                        // 在本场景下不需要校验的参数，如"客户端实时校验参数"
                        if (!needCheck(checkScope, privateParamInEList)) {
                            continue;
                        }
                        allPrivateParamInRule++;
                        // 匹配
                        CheckItemResultFormatSimpleDTO compareItem = CompareUtil
                                .check(privateParamInEList, paramKey2ValueInEListLog);
                        if (compareItem != null) {
                            CompareItemSimpleDTO rule = compareItem.getRule();
                            if (rule != null) {
                                String logCauseKey = LogUtil.ELIST_KEYWORD + "#" + eleOid + "#" + rule.getKey();
                                String causeInLog = logCauseMap.get(logCauseKey);
                                // 如果日志里有错误，那把规则对应也标成错误
                                if (StringUtils.isNotBlank(causeInLog) && StringUtils.isBlank(rule.getCause())) {
                                    rule.setCause(causeInLog);
                                }
                                eListInRule.put(rule.getKey(), rule);
                            }
                            if (CheckResultEnum.NOT_PASS == compareItem.getCheckResult()) {
                                LogCheckResultEnum failedEnum = getFailLogCheckResult(compareItem);
                                addFailKey(eleOid, compareItem, failedEnum, failKeys);
                                totalCheckResult = failedEnum;
                            }
                            if (compareItem.getKeyMatched()) {
                                privateParamOfHitKeyInLog++;
                                if (privateParamInEList.getNotEmpty()) {
                                    notEmptyPrivateParamHitKeyInLog++;
                                    if (compareItem.getValueEmpty()) {
                                        notEmptyPrivateParamWithoutValueInLog++;
                                    }
                                }
                            }
                            if (compareItem.getValueMatched()) {
                                privateParamOfHitValueInLog++;
                            }
                        }
                    }
                    if (MapUtils.isNotEmpty(eListInRule)) {
                        eListMapsInRule.add(eListInRule);
                    }
                } else {
                    // 此时相当于规则中没有对应的参数信息，只有oid链信息
                    Map<String, CompareItemSimpleDTO> eListInRule = Maps.newHashMap();
                    CompareItemSimpleDTO emptyRule = new CompareItemSimpleDTO();
                    emptyRule.setKey(LogUtil.OID_KEYWORD)
                            .setValue(eleOid)
                            .setCause("")
                            .setComment("");
                    eListInRule.put(LogUtil.OID_KEYWORD, emptyRule);
                    eListMapsInRule.add(eListInRule);
                }
            }
        }

        // 考虑事件公参:以规则中的事件公参为排序依据
        if (MapUtils.isNotEmpty(eventVerifiers)) {
            for (String eventParamCode : eventVerifiers.keySet()) {
                BloodLink.Param eventPublicParam = eventVerifiers.get(eventParamCode);
                // 在本场景下不需要校验的参数，如"客户端实时校验参数"
                if (!needCheck(checkScope, eventPublicParam)) {
                    continue;
                }
                allPublicParamInRule++;
                // 匹配
                CheckItemResultFormatSimpleDTO compareItem = CompareUtil
                        .check(eventPublicParam, buryPointLog.getProps());
                if (null != compareItem) {
                    CompareItemSimpleDTO tmpLog = compareItem.getLog();
                    CompareItemSimpleDTO tmpRule = compareItem.getRule();
                    if (tmpLog != null) {
                        otherMapInLog.put(tmpLog.getKey(), tmpLog);
                    }
                    if (tmpRule != null) {
                        otherMapInRule.put(tmpRule.getKey(), tmpRule);
                    }
                    if (CheckResultEnum.NOT_PASS == compareItem.getCheckResult()) {
                        LogCheckResultEnum failedEnum = getFailLogCheckResult(compareItem);
                        addFailKey("事件公参", compareItem, failedEnum, failKeys);
                        totalCheckResult = failedEnum;
                    }
                    if (compareItem.getKeyMatched()) {
                        publicParamOfHitKeyInLog++;
                        if (eventPublicParam.getNotEmpty()) {
                            notEmptyPublicParamHitKeyInLog++;
                            if (compareItem.getValueEmpty()) {
                                notEmptyPublicParamWithoutValueInLog++;
                            }
                        }
                    }
                    if (compareItem.getValueMatched()) {
                        publicParamOfHitValueInLog++;
                    }
                }
            }
        }

        // 考虑全局公参:以规则中的全局公参为排序依据
        if (MapUtils.isNotEmpty(commonVerifiers)) {
            for (String globalParamCode : commonVerifiers.keySet()) {
                BloodLink.Param globalPublicParam = commonVerifiers.get(globalParamCode);
                // 在本场景下不需要校验的参数，如"客户端实时校验参数"
                if (!needCheck(checkScope, globalPublicParam)) {
                    continue;
                }
                allPublicParamInRule++;
                // 匹配
                CheckItemResultFormatSimpleDTO compareItem = CompareUtil
                        .check(globalPublicParam, buryPointLog.getProps());
                if (null != compareItem) {
                    CompareItemSimpleDTO tmpLog = compareItem.getLog();
                    CompareItemSimpleDTO tmpRule = compareItem.getRule();
                    if (tmpLog != null) {
                        otherMapInLog.put(tmpLog.getKey(), tmpLog);
                    }
                    if (tmpRule != null) {
                        otherMapInRule.put(tmpRule.getKey(), tmpRule);
                    }
                    if (CheckResultEnum.NOT_PASS == compareItem.getCheckResult()) {
                        LogCheckResultEnum failedEnum = getFailLogCheckResult(compareItem);
                        addFailKey("全局公参", compareItem, failedEnum, failKeys);
                        totalCheckResult = failedEnum;
                    }

                    if (compareItem.getKeyMatched()) {
                        publicParamOfHitKeyInLog++;
                        if (globalPublicParam.getNotEmpty()) {
                            notEmptyPublicParamHitKeyInLog++;
                            if (compareItem.getValueEmpty()) {
                                notEmptyPublicParamWithoutValueInLog++;
                            }
                        }
                    }
                    if (compareItem.getValueMatched()) {
                        publicParamOfHitValueInLog++;
                    }
                }
            }
        }

        // 处理日志中未被匹配的key：非plist,elist, 全局公参，事件公参
        if (MapUtils.isNotEmpty(buryPointLog.getProps())) {
            for (String logKey : buryPointLog.getProps().keySet()) {
                if (commonVerifiers.containsKey(logKey) || eventVerifiers.containsKey(logKey) ||
                        LogUtil.PLIST_KEYWORD.equals(logKey) || LogUtil.ELIST_KEYWORD.equals(logKey)) {
                    continue;
                }

                CompareItemSimpleDTO otherInLog = new CompareItemSimpleDTO();
                otherInLog.setKey(logKey)
                        .setValue(String.valueOf(buryPointLog.getProps().get(logKey)))
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

        Map<String, Object> ruleCheck = Maps.newHashMap();
        ruleCheck.put(LogUtil.PLIST_KEYWORD, pListMapsInRule);
        ruleCheck.put(LogUtil.ELIST_KEYWORD, eListMapsInRule);
        ruleCheck.put(LogUtil.EVENTCODE_KEYWORD, eventCode);
        ruleCheck.putAll(otherMapInRule);

        DetectionIndicatorsSimpleDTO detectionIndicator = new DetectionIndicatorsSimpleDTO();

        detectionIndicator.setPrivateParamCompletion(
                new IndicatorSimpleDTO(privateParamOfHitKeyInLog, allPrivateParamInRule,
                        allPrivateParamInRule == 0 ? 100
                                : 100 * (privateParamOfHitKeyInLog / (double) allPrivateParamInRule),
                        privateParamOfHitKeyInLog < allPrivateParamInRule))
                .setPrivateParamSuitability(
                        new IndicatorSimpleDTO(privateParamOfHitValueInLog, privateParamOfHitKeyInLog,
                                privateParamOfHitKeyInLog == 0 ? 100
                                        : 100 * (privateParamOfHitValueInLog / (double) privateParamOfHitKeyInLog),
                                privateParamOfHitValueInLog < privateParamOfHitKeyInLog))
                .setPrivateParamNullRate(new IndicatorSimpleDTO(notEmptyPrivateParamWithoutValueInLog,
                        notEmptyPrivateParamHitKeyInLog, notEmptyPrivateParamHitKeyInLog == 0 ? 0
                        : 100 * (notEmptyPrivateParamWithoutValueInLog
                        / (double) notEmptyPrivateParamHitKeyInLog),
                        notEmptyPrivateParamHitKeyInLog != 0 && notEmptyPrivateParamWithoutValueInLog != 0))
                .setPublicParamCompletion(
                        new IndicatorSimpleDTO(publicParamOfHitKeyInLog, allPublicParamInRule,
                                allPublicParamInRule == 0 ? 100
                                        : 100 * (publicParamOfHitKeyInLog / (double) allPublicParamInRule),
                                publicParamOfHitKeyInLog < allPublicParamInRule))
                .setPublicParamSuitability(
                        new IndicatorSimpleDTO(publicParamOfHitValueInLog, publicParamOfHitKeyInLog,
                                publicParamOfHitKeyInLog == 0 ? 100
                                        : 100 * (publicParamOfHitValueInLog / (double) publicParamOfHitKeyInLog),
                                publicParamOfHitValueInLog < publicParamOfHitKeyInLog))
                .setPublicParamNullRate(new IndicatorSimpleDTO(notEmptyPublicParamWithoutValueInLog,
                        notEmptyPublicParamHitKeyInLog, notEmptyPublicParamHitKeyInLog == 0 ? 0
                        : 100 * (notEmptyPublicParamWithoutValueInLog
                        / (double) notEmptyPublicParamHitKeyInLog),
                        notEmptyPublicParamHitKeyInLog != 0 && notEmptyPublicParamWithoutValueInLog != 0));

        RuleCheckSimpleDTO ruleCheckSimpleDTO = new RuleCheckSimpleDTO();
        String rootPageOid = LogUtil.getRootPageOid(spm);
        String firstObjOid = LogUtil.getFirstObjOid(spm);
        String rootPageName = StringUtils.isNotBlank(rootPageOid) && oid2NameMap != null && oid2NameMap
                .containsKey(rootPageOid) ?
                oid2NameMap.get(rootPageOid) : "";
        String firstObjName = StringUtils.isNotBlank(firstObjOid) && oid2NameMap != null && oid2NameMap
                .containsKey(firstObjOid) ?
                oid2NameMap.get(firstObjOid) : "";
        ruleCheckSimpleDTO.setSpm(spm)
                .setRootPageOid(rootPageOid != null ? rootPageOid : "")
                .setRootPageName(rootPageName)
                .setFirstObjOid(firstObjOid)
                .setFirstObjName(firstObjName)
                .setLogServerTime(logServerTime)
                .setEventCode(eventCode)
                .setEventName(eventName)
                .setCheckResult(totalCheckResult.getCheckResultEnum().getResult())
                .setLog(logCheck)
                .setProps(buryPointLog.getProps())
                .setRule(ruleCheck)
                .setDetectionIndicator(detectionIndicator)
                .setTrackerId(trackerId)
                .setUnMatchedParamCode(unMatchedParamCode);
        // 组装失败分类key
        if (CollectionUtils.isNotEmpty(failKeys)) {
            List<String> failKeyStrings = failKeys.stream().distinct().map(FailKeyDTO::toFailKey).collect(Collectors.toList());
            String failKey = StringUtils.join(failKeyStrings, ",");
            ruleCheckSimpleDTO.setFailKey(failKey);
        } else {
            ruleCheckSimpleDTO.setFailKey("");
        }
        return ruleCheckSimpleDTO;
    }

    /**
     * 失败时的LogCheckResultEnum判断
     */
    private LogCheckResultEnum getFailLogCheckResult(CheckItemResultFormatSimpleDTO compareItem) {
        if (Boolean.TRUE.equals(compareItem.getValueEmpty())) {
            return LogCheckResultEnum.PARAM_MISSING;
        }
        return LogCheckResultEnum.PARAM_VALUE_INVALID;
    }

    private void addFailKey(String prefix, CheckItemResultFormatSimpleDTO compareItem, LogCheckResultEnum logCheckResult, List<FailKeyDTO> list) {
        if (compareItem == null) {
            return;
        }
        if (logCheckResult == LogCheckResultEnum.OK) {
            return;
        }
        FailKeyDTO failKeyDTO = new FailKeyDTO().setPrefix(prefix).setKey(compareItem.getRule().getKey()).setType(logCheckResult);
        list.add(failKeyDTO);
    }

    private Map<String, BloodLink.Param> getEventVerifier(String eventCode) {
        Map<String, BloodLink.Param> eventVerifiers = Maps.newHashMap();
        if (StringUtils.isNotBlank(eventCode)) {
            List<BloodLink.Param> eventVerifierList = eventsVerifiers.get(eventCode);
            if (CollectionUtils.isNotEmpty(eventVerifierList)) {
                for (BloodLink.Param eventVerifier : eventVerifierList) {
                    eventVerifiers.put(eventVerifier.getCode(), eventVerifier);
                }
            }
        }
        return eventVerifiers;
    }

    /**
     * 这个校验规则是否要生效
     * @param currentScene
     * @param param
     * @return
     */
    private boolean needCheck(CheckScopeEnum currentScene, BloodLink.Param param) {
        if (param == null) {
            return true;
        }
        CheckScopeEnum ruleScope = param.getCheckScopeEnum();
        if (ruleScope == null || ruleScope == CheckScopeEnum.ALL) {
            return true;
        }
        if (currentScene == CheckScopeEnum.LOG_STATS_ONLY) {
            return CheckScopeEnum.LOG_STATS_ONLY.equals(ruleScope);
        }
        if (currentScene == CheckScopeEnum.REALTIME_ONLY) {
            return CheckScopeEnum.REALTIME_ONLY.equals(ruleScope);
        }
        return false;
    }
}
