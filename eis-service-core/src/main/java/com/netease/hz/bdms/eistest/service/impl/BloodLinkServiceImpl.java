package com.netease.hz.bdms.eistest.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.netease.eis.adapters.CacheAdapter;
import com.netease.eis.adapters.RealtimeConfigAdapter;
import com.netease.hz.bdms.easyinsight.common.dto.obj.param.ParamWithValueItemDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.ParamValueSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.parambind.ParamBindItemDTO;
import com.netease.hz.bdms.easyinsight.common.dto.realtimetest.RealTimeTestResourceDTO;
import com.netease.hz.bdms.easyinsight.common.enums.ObjTypeEnum;
import com.netease.hz.bdms.easyinsight.common.enums.ParamTypeEnum;
import com.netease.hz.bdms.easyinsight.common.enums.ParamValueTypeEnum;
import com.netease.hz.bdms.easyinsight.common.exception.CommonException;
import com.netease.hz.bdms.easyinsight.common.util.CacheUtils;
import com.netease.hz.bdms.easyinsight.common.util.JsonUtils;
import com.netease.hz.bdms.easyinsight.common.vo.logcheck.BranchCoverageDetailVO;
import com.netease.hz.bdms.easyinsight.common.dto.audit.BloodLink;
import com.netease.hz.bdms.easyinsight.service.service.audit.BuryPointRule;
import com.netease.hz.bdms.easyinsight.common.dto.audit.CheckScopeEnum;
import com.netease.hz.bdms.eistest.client.ProcessorRpcAdapter;
import com.netease.hz.bdms.eistest.entity.BloodLinkQuery;
import com.netease.hz.bdms.eistest.service.BloodLinkService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Service("bloodLinkService")
public class BloodLinkServiceImpl implements BloodLinkService, InitializingBean {

    @Resource
    private RealtimeConfigAdapter realtimeConfigAdapter;

    @Resource
    private ProcessorRpcAdapter processorRpcAdapter;

    @Resource
    private CacheAdapter cacheAdapter;

    private static final String CACHE_KEY = "getBuryPointResource_";
    private static final int CACHE_TTL = 600;

    private static Map<String, String> paramCheckScopeMap = new HashMap<>();

    private static String getCacheKey(BloodLinkQuery query) {
        return CACHE_KEY + query.getTaskId() + "_" + query.getTerminalId() + "_" + query.getDomainId() + "_" + query.getAppId();
    }

    @Override
    public RealTimeTestResourceDTO getBuryPointResource(BloodLinkQuery query) {
        if (query == null) {
            throw new CommonException("query is null");
        }
        return CacheUtils.getAndSetIfAbsent(() -> getCacheKey(query),
                () -> processorRpcAdapter.getResource(query.getTaskId(), query.getTerminalId(),query.getDomainId(), query.getAppId()),
                (k) -> cacheAdapter.get(k),
                (k, v) -> cacheAdapter.setWithExpireTime(k, v, CACHE_TTL),
                RealTimeTestResourceDTO.class);
    }

    @Override
    public List<BranchCoverageDetailVO> getBranchCoverageIgnoreList(String conversationId) {
        if (conversationId == null) {
            return new ArrayList<>(0);
        }
        return processorRpcAdapter.updateNoNeedCoverBranches(conversationId);
    }

    @Override
    public Map<String, BuryPointRule> generateBuryPointRule(RealTimeTestResourceDTO realTimeTestResourceDTO) {
        try {
            Map<String, BuryPointRule> resMap = new HashMap<>();
            Map<String, RealTimeTestResourceDTO.Linage> linageMap = realTimeTestResourceDTO.getLinageMap();
            List<RealTimeTestResourceDTO.ObjMeta> objMetas = realTimeTestResourceDTO.getObjMetas();
            List<ParamWithValueItemDTO> eventPublicParams = realTimeTestResourceDTO.getEventPublicParams();
            List<ParamWithValueItemDTO> globalPublicParams = realTimeTestResourceDTO.getGlobalPublicParams();
            Map<String, RealTimeTestResourceDTO.ObjMeta> objTrackerMap = new HashMap<>();
            for (RealTimeTestResourceDTO.ObjMeta objMeta : objMetas) {
                objTrackerMap.put(objMeta.getOid(), objMeta);
            }
            /**
             * 每个spm对应一个规则，同时一个spm对应一个目标oid，一个oid对应一个objTracker
             */
            for (Map.Entry<String, RealTimeTestResourceDTO.Linage> entry : linageMap.entrySet()) {
                BuryPointRule buryPointRule = new BuryPointRule();
                String spm = entry.getKey();
                RealTimeTestResourceDTO.Linage linage = entry.getValue();
                List<String> lineageNodes = linage.getLinageNodes();
                String oid = linage.getOid();
                RealTimeTestResourceDTO.ObjMeta objMeta = objTrackerMap.get(oid);
                if(objMeta == null){
                    log.error("元数据对象不存在！{}", oid);
                }
                LinkedHashMap<String, Map<String, BloodLink.Param>> pList = getVerifiersForPListOrEList(lineageNodes, objTrackerMap, ObjTypeEnum.PAGE);
                LinkedHashMap<String, Map<String, BloodLink.Param>> eList = getVerifiersForPListOrEList(lineageNodes, objTrackerMap, ObjTypeEnum.ELEMENT);
                // 浮层也算到plist
                LinkedHashMap<String, Map<String, BloodLink.Param>> popovers = getVerifiersForPListOrEList(lineageNodes, objTrackerMap, ObjTypeEnum.POPOVER);
                if (MapUtils.isNotEmpty(popovers)) {
                    pList.putAll(popovers);
                }

                Map<String, List<BloodLink.Param>> eventCode2EventParamMap = getEventsVerifiers(objMeta, eventPublicParams);
                Map<String, BloodLink.Param> paramCode2CommonParamMap = getCommonVerifiers(globalPublicParams);
                buryPointRule.setKey(spm);
                buryPointRule.setTrackerId(linage.getTrackerId());
                buryPointRule.setPageListVerifiers(pList);
                buryPointRule.setEleListVerifiers(eList);
                buryPointRule.setEventsVerifiers(eventCode2EventParamMap);
                buryPointRule.setCommonVerifiers(paramCode2CommonParamMap);

                // 实时测试专用规则
                // _valid_page_type
//                RealTimeTestResourceDTO.ObjMeta objMetaOfCurrent = objTrackerMap.get(oid);
//                if (objMeta != null) {
//                    if (ObjTypeEnum.PAGE.getType().equals(objMetaOfCurrent.getObjType())
//                            && linage.getLinageNodes() != null
//                            && linage.getLinageNodes().size() == 2
//                            && linage.getLinageNodes().get(1).equals("page_rn")) {
//                        Map<String, BloodLink.Param> checkParamMap = buryPointRule.getPageListVerifiers().computeIfAbsent(oid, k -> new HashMap<>());
//                        BloodLink.Param validPageTypeParam = validPageTypeParam();
//                        checkParamMap.put(validPageTypeParam.getCode(), validPageTypeParam);
//                    }
//                    // _valid_logical_mount
//                    if (ObjTypeEnum.POPOVER.getType().equals(objMetaOfCurrent.getObjType())) {
//                        Map<String, BloodLink.Param> checkParamMap = buryPointRule.getPageListVerifiers().computeIfAbsent(oid, k -> new HashMap<>());
//                        BloodLink.Param validLogicalMountParam = validLogicalMountParam();
//                        checkParamMap.put(validLogicalMountParam.getCode(), validLogicalMountParam());
//                    }
//                }

                resMap.put(spm, buryPointRule);
            }
            return resMap;
        } catch (Exception e) {
            log.error("generateBuryPointRule failed", e);
            throw e;
        }
    }

    private static BloodLink.Param validPageTypeParam() {
        BloodLink.Param param = new BloodLink.Param();
        param.setCode("_valid_page_type");
        param.setName("客户端校验参数：_valid_page_type");
        param.setNotEmpty(true);
        param.setMust(true);
        param.setParamType(ParamTypeEnum.OBJ_BUSINESS_PRIVATE_PARAM.getType());
        param.setValueType(ParamValueTypeEnum.CONSTANT.getType());
        param.setSelectedValues(Collections.singletonList("rootPage"));
        param.setDescription("page_rn名下，第一层的page节点，全部都应该是 `rootpage` 节点。RN侧开发，需要给改page节点设置为 rootpage");
        param.setCheckScopeEnum(CheckScopeEnum.fromName("_valid_page_type"));   // 这里就是CheckScopeEnum.REALTIME_ONLY
        return param;
    }

    private static BloodLink.Param validLogicalMountParam() {
        BloodLink.Param param = new BloodLink.Param();
        param.setCode("_valid_logical_mount");
        param.setName("客户端校验参数：_valid_logical_mount");
        param.setNotEmpty(true);
        param.setMust(true);
        param.setParamType(ParamTypeEnum.OBJ_BUSINESS_PRIVATE_PARAM.getType());
        param.setValueType(ParamValueTypeEnum.CONSTANT.getType());
        param.setSelectedValues(Collections.singletonList("auto"));
        param.setDescription("浮层节点，都应该设置为自动挂载(auto_mount)将自己自动挂载到当前根节点名下");
        param.setCheckScopeEnum(CheckScopeEnum.fromName("_valid_logical_mount"));   // 这里就是CheckScopeEnum.REALTIME_ONLY
        return param;
    }

    @Override
    public BuryPointRule generateEventRule(RealTimeTestResourceDTO realTimeTestResourceDTO) {

        List<ParamWithValueItemDTO> eventPublicParams = realTimeTestResourceDTO.getEventPublicParams();
        List<ParamWithValueItemDTO> globalPublicParams = realTimeTestResourceDTO.getGlobalPublicParams();
        Map<Long,String> allEventCodeMap = realTimeTestResourceDTO.getAllEventCodeMap();
        Map<Long,Long> allEventVersionMap = realTimeTestResourceDTO.getAllEventVersionMap();
        BuryPointRule buryPointRule = new BuryPointRule();
        Map<String, List<BloodLink.Param>> eventCode2EventParamMap = getAllEventsVerifiers(eventPublicParams, allEventCodeMap, allEventVersionMap);
        Map<String, BloodLink.Param> paramCode2CommonParamMap = getCommonVerifiers(globalPublicParams);
        buryPointRule.setEventsVerifiers(eventCode2EventParamMap);
        buryPointRule.setCommonVerifiers(paramCode2CommonParamMap);

        return buryPointRule;
    }

    private LinkedHashMap<String, Map<String, BloodLink.Param>> getVerifiersForPListOrEList(List<String> lineageNodes
            , Map<String, RealTimeTestResourceDTO.ObjMeta> objTrackerMap, ObjTypeEnum objTypeEnum) {
        LinkedHashMap<String, Map<String, BloodLink.Param>> resMap = new LinkedHashMap<>();
        for (String oid : lineageNodes) {
            RealTimeTestResourceDTO.ObjMeta objMeta = objTrackerMap.get(oid);
            if (objMeta != null && objTypeEnum.getType().equals(objMeta.getObjType())) {
                Map<String, BloodLink.Param> paramMap = new HashMap<>();
                List<ParamBindItemDTO> privateParams = objMeta.getPrivateParams();
                if (CollectionUtils.isEmpty(privateParams)) {
                    resMap.put(oid, paramMap);
                    continue;
                }
                for (ParamBindItemDTO privateParam : privateParams) {
                    BloodLink.Param param = new BloodLink.Param();
                    param.setCode(privateParam.getCode());
                    param.setName(privateParam.getName());
                    param.setDescription(privateParam.getDescription());
                    param.setMust(privateParam.getMust());
                    param.setNotEmpty(privateParam.getNotEmpty());
                    param.setParamType(privateParam.getParamType());
                    param.setValueType(privateParam.getValueType());
                    List<ParamValueSimpleDTO> allValues = privateParam.getValues();
                    List<Long> selectedValueIds = privateParam.getSelectedValues();
                    if (!CollectionUtils.isEmpty(allValues) && !CollectionUtils.isEmpty(selectedValueIds)) {
                        List<String> selectedValues = new ArrayList<>();
                        for (ParamValueSimpleDTO value : allValues) {
                            if (selectedValueIds.contains(value.getId())) {
                                selectedValues.add(value.getCode());
                            }
                        }
                        param.setSelectedValues(selectedValues);
                    } else {
                        param.setSelectedValues(new ArrayList<>());
                    }
                    param.setCheckScopeEnum(CheckScopeEnum.fromName(paramCheckScopeMap.get(param.getCode())));
                    paramMap.put(param.getCode(), param);
                }
                resMap.put(oid, paramMap);
            }
        }
        return resMap;
    }

    private Map<String, List<BloodLink.Param>> getEventsVerifiers(RealTimeTestResourceDTO.ObjMeta objMeta
            , List<ParamWithValueItemDTO> eventPublicParamsOfDto) {
        Map<String, List<BloodLink.Param>> resMap = new HashMap<>();
        if(objMeta == null){
            return resMap;
        }
        List<RealTimeTestResourceDTO.Event> eventsOfTracker = objMeta.getEvents();
        Multimap<String, BloodLink.Param> eventParamsMultiMap = HashMultimap.create();
        if (CollectionUtils.isEmpty(eventsOfTracker)) {
            return resMap;
        }
        //遍历当前对象绑定的事件，获得事件与事件公参的映射
        for (RealTimeTestResourceDTO.Event event : eventsOfTracker) {
            String eventCode = event.getCode();
            Long eventId = event.getId();
            Long eventParamVersion = event.getParamVersion();
            for (ParamWithValueItemDTO paramWithValueItemDTO : eventPublicParamsOfDto) {
                if (paramWithValueItemDTO.getEntityId().equals(eventId) && paramWithValueItemDTO.getVersionId().equals(eventParamVersion)) {
                    BloodLink.Param param = new BloodLink.Param();
                    param.setCode(paramWithValueItemDTO.getCode());
                    param.setName(paramWithValueItemDTO.getName());
                    param.setValueType(paramWithValueItemDTO.getValueType());
                    param.setSelectedValues(paramWithValueItemDTO.getValues());
                    param.setParamType(paramWithValueItemDTO.getParamType());
                    param.setDescription(paramWithValueItemDTO.getDescription());
                    param.setMust(paramWithValueItemDTO.getMust());
                    param.setNotEmpty(paramWithValueItemDTO.getNotEmpty());
                    eventParamsMultiMap.put(eventCode, param);
                }
            }
        }
        Map<String, Collection<BloodLink.Param>> tempMap = eventParamsMultiMap.asMap();
        for (Map.Entry<String, Collection<BloodLink.Param>> entry : tempMap.entrySet()) {
            resMap.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return resMap;
    }


    private Map<String, List<BloodLink.Param>> getAllEventsVerifiers(List<ParamWithValueItemDTO> eventPublicParamsOfDto, Map<Long,String> allEventCodeMap, Map<Long,Long> allEventVersionMap) {
        Map<String, List<BloodLink.Param>> resMap = new HashMap<>();
        Multimap<String, BloodLink.Param> eventParamsMultiMap = HashMultimap.create();
        //遍历当前对象绑定的事件，获得事件与事件公参的映射
        for (ParamWithValueItemDTO paramWithValueItemDTO : eventPublicParamsOfDto) {
            Long versionId = allEventVersionMap.get(paramWithValueItemDTO.getEntityId());
            if(versionId != null && versionId.equals(paramWithValueItemDTO.getVersionId())) {
                BloodLink.Param param = new BloodLink.Param();
                param.setCode(paramWithValueItemDTO.getCode());
                param.setName(paramWithValueItemDTO.getName());
                param.setValueType(paramWithValueItemDTO.getValueType());
                param.setSelectedValues(paramWithValueItemDTO.getValues());
                param.setParamType(paramWithValueItemDTO.getParamType());
                param.setDescription(paramWithValueItemDTO.getDescription());
                param.setMust(paramWithValueItemDTO.getMust());
                param.setNotEmpty(paramWithValueItemDTO.getNotEmpty());
                eventParamsMultiMap.put(allEventCodeMap.get(paramWithValueItemDTO.getEntityId()), param);
            }
        }

        Map<String, Collection<BloodLink.Param>> tempMap = eventParamsMultiMap.asMap();
        for (Map.Entry<String, Collection<BloodLink.Param>> entry : tempMap.entrySet()) {
            resMap.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return resMap;
    }

    private Map<String, BloodLink.Param> getCommonVerifiers(List<ParamWithValueItemDTO> globalPublicParams) {
        Map<String, BloodLink.Param> resMap = new HashMap<>();
        if (CollectionUtils.isEmpty(globalPublicParams)) {
            return resMap;
        }
        for (ParamWithValueItemDTO globalPublicParam : globalPublicParams) {
            BloodLink.Param param = new BloodLink.Param();
            param.setCode(globalPublicParam.getCode());
            param.setName(globalPublicParam.getName());
            param.setValueType(globalPublicParam.getValueType());
            param.setSelectedValues(globalPublicParam.getValues());
            param.setParamType(globalPublicParam.getParamType());
            param.setDescription(globalPublicParam.getDescription());
            param.setMust(globalPublicParam.getMust());
            param.setNotEmpty(globalPublicParam.getNotEmpty());
            resMap.put(param.getCode(), param);
        }
        return resMap;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        realtimeConfigAdapter.listenJSON("paramCheckScopeMap", (s) -> paramCheckScopeMap = JsonUtils.parseObject(s, new TypeReference<Map<String, String>>() {}));
    }
}
