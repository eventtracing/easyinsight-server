package com.netease.hz.bdms.eistest.ws.dto;

import com.netease.hz.bdms.easyinsight.common.dto.param.parambind.ParamBindItemDTO;
import com.netease.hz.bdms.easyinsight.common.dto.realtimetest.RealTimeTestResourceDTO;
import com.netease.hz.bdms.easyinsight.common.enums.ObjTypeEnum;
import com.netease.hz.bdms.easyinsight.common.exception.RealTimeTestException;
import com.netease.hz.bdms.easyinsight.common.util.JsonUtils;
import com.netease.hz.bdms.easyinsight.common.vo.logcheck.BranchCoverageDetailVO;
import com.netease.hz.bdms.easyinsight.service.service.audit.BuryPointRule;
import com.netease.hz.bdms.easyinsight.service.service.util.LogUtil;
import com.netease.hz.bdms.eistest.entity.BloodLinkQuery;
import com.netease.hz.bdms.eistest.service.BloodLinkService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理埋点规则相关元数据
 */
@Slf4j
@Data
public class BuryPointMetaInfo implements Serializable {

    private static final long serialVersionUID = -4933595439936756013L;

    private static String kafkaLogParamWhite = "s_cid_er";

    /**
     * 事件埋点规则
     */
    private BuryPointRule eventRule = new BuryPointRule();
    /**
     * 埋点规则 map,
     * key = SPM,
     * value = <根据血缘链路对象的oid构造唯一key(spm), 埋点规则>
     */
    private Map<String, BuryPointRule> conversationRuleMap = new ConcurrentHashMap<>();

    /**
     * 事件类型 map,
     * key = 会话ID,
     * value = <事件code, 事件name>
     */
    private Map<String, String> conversationEventCodeToNameMap = new ConcurrentHashMap<>();

    /**
     * 对象oid与名字的 Map,
     * key = 会话ID,
     * value = <对象oid, 对象name>
     */
    private Map<String, String> conversationObjectMap = new ConcurrentHashMap<>();

    /**
     * 路由路径的 Map,
     * key = 会话ID,
     * value = <路由路径, oid>
     */
    private Map<String, String> conversationRoutePathToOidMap = new ConcurrentHashMap<>();

    //事件的code -> pointId map
    Map<String,Long> eventBuryPointMap = new ConcurrentHashMap<>();

    Map<String, List<RealTimeTestResourceDTO.Event>> oidBindEventMap = new HashMap<>();

    Map<String, RealTimeTestResourceDTO.Linage> linageMap = new HashMap<>();

    /**
     * 所有浮层的oid
     */
    private Set<String> popovers = new HashSet<>();

    //增量spm信息
    private Set<String> updateSpmInfo = new HashSet<>();

    /**
     * 手动标记为无需测试的分支
     */
    private List<BranchCoverageDetailVO> branchCoverageIgnores = new ArrayList<>(0);

    private BloodLinkService bloodLinkService;

    public BuryPointMetaInfo(BloodLinkService bloodLinkService) {
        this.bloodLinkService = bloodLinkService;
    }

    public RealTimeTestResourceDTO initBuryPointRule(Long taskId, Long terminalId,Long domainId, Long appId) {
        BloodLinkQuery bloodLinkQuery = new BloodLinkQuery();
        bloodLinkQuery.setTaskId(taskId);
        bloodLinkQuery.setTerminalId(terminalId);
        bloodLinkQuery.setDomainId(domainId);
        bloodLinkQuery.setAppId(appId);
        RealTimeTestResourceDTO realTimeTestResourceDTO = bloodLinkService.getBuryPointResource(bloodLinkQuery);
        log.info("get bury point resource from remote successfully");

        if (CollectionUtils.isEmpty(realTimeTestResourceDTO.getObjMetas())) {
            log.info("get empty bury point resource from remote,  taskId:{}, domainId:{}, appId:{}",
                    taskId, domainId, appId);
            throw new RealTimeTestException("no bury point resource found");
        }

        eventRule = bloodLinkService.generateEventRule(realTimeTestResourceDTO);

        Map<String, String> allEventNameMap = Optional.ofNullable(realTimeTestResourceDTO.getAllEventNameMap()).orElse(new HashMap<>());
        conversationEventCodeToNameMap.putAll(allEventNameMap);

        Map<String, String> oidToNameMap = Optional.ofNullable(realTimeTestResourceDTO.getAllObjNameMap()).orElse(new HashMap<>());
        conversationObjectMap.putAll(oidToNameMap);

        Map<String,Long> eventToBuryPointMap = Optional.ofNullable(realTimeTestResourceDTO.getEventBuryPointMap()).orElse(new HashMap<>());
        eventBuryPointMap.putAll(eventToBuryPointMap);

        Map<String, RealTimeTestResourceDTO.Linage> linageRuleMap = Optional.ofNullable(realTimeTestResourceDTO.getLinageMap()).orElse(new HashMap<>());
        linageMap.putAll(linageRuleMap);

        Set<String> updateSpmSet = Optional.ofNullable(realTimeTestResourceDTO.getUpdateSpmInfo()).orElse(new HashSet<>());
        updateSpmInfo.addAll(updateSpmSet);

        //已废弃
        Map<String, String> routePath2OidMap = new HashMap<>();

        Map<String, List<RealTimeTestResourceDTO.Event>> oidPath2EventMap = new HashMap<>();
        for (RealTimeTestResourceDTO.ObjMeta objMeta : realTimeTestResourceDTO.getObjMetas()) {
            if(objMeta.getObjType().equals(ObjTypeEnum.POPOVER.getType())){
                popovers.add(objMeta.getOid());
            }
            List<ParamBindItemDTO> privateParams = objMeta.getPrivateParams();
            List<String> filterParam = Arrays.asList(kafkaLogParamWhite.split(","));
            privateParams.removeIf(paramBindItemDTO -> filterParam.contains(paramBindItemDTO.getCode()));
            oidPath2EventMap.put(objMeta.getOid(), objMeta.getEvents());
        }

        Map<String, BuryPointRule> generatedRuleMap = bloodLinkService.generateBuryPointRule(realTimeTestResourceDTO);
        conversationRuleMap.putAll(generatedRuleMap);

        oidBindEventMap.putAll(oidPath2EventMap);
        return realTimeTestResourceDTO;
    }

    public void updateIgnoreBranches(String coversationId) {
        branchCoverageIgnores = bloodLinkService.getBranchCoverageIgnoreList(coversationId);
    }

    public Map<String, String> getEventCodeToNameMap() {
        return conversationEventCodeToNameMap;
    }

    public Map<String, String> getOid2NameMap() {
        return conversationObjectMap;
    }

    /**
     * 根据spm(去除pos)获取埋点规则
     *
     * @param spmWithNoPos spm(去除pos)， 可能是由路由路径构成的竖线分割的串
     * @return 埋点规则
     */
    public BuryPointRule getBuryPointRule( String spmWithNoPos) {
        log.info("获取规则开始");
        if (StringUtils.isBlank(spmWithNoPos)) {
            return null;
        }

        if (MapUtils.isEmpty(conversationRuleMap)) {
            log.error("no bury point rule found");
            return null;
        }
//        //去除浮层的父对象路径
//        spmWithNoPos = removePopParentsOfLog(spmWithNoPos);
        String[] keys = spmWithNoPos.split(LogUtil.SPM_SEPERATOR_KEYWORD);
        StringBuilder oidKeyBuilder = new StringBuilder();

        if (MapUtils.isEmpty(conversationRoutePathToOidMap)) {
            // 若不存在路由路径与oid的映射，则直接使用spm
            oidKeyBuilder.append(spmWithNoPos);

        } else {
            // 若存在路由路径与oid的映射，则尝试将spm（去除pos)中的路由路径翻译成oid，再查找规则
            for (String tmpKey : keys) {
                String oid = conversationRoutePathToOidMap.get(tmpKey);
                if (StringUtils.isBlank(oid)) {
                    oid = tmpKey;
                }
                oidKeyBuilder.append(oid).append("|");
            }
            if (oidKeyBuilder.length() > 0) {
                oidKeyBuilder.deleteCharAt(oidKeyBuilder.length() - 1);
            }
        }

        String oidKey = oidKeyBuilder.toString();
        log.info("获取规则结束");

        BuryPointRule result = conversationRuleMap.get(oidKey);
        if (result != null) {
            return result;
        }

        // 如果最上层是浮层，可按前缀匹配
        if (popovers.contains(keys[keys.length - 1])) {
            return matchByPrefix(spmWithNoPos);
        }
        return null;
    }

    private BuryPointRule matchByPrefix(String spm) {
        if (conversationRuleMap == null || StringUtils.isEmpty(spm)) {
            return null;
        }
        Set<String> allSpms = conversationRuleMap.keySet();
        String spmPrefixMatched = allSpms.stream().filter(s -> s.startsWith(spm)).findFirst().orElse(null);
        if (spmPrefixMatched == null) {
            return null;
        }
        BuryPointRule buryPointRule = conversationRuleMap.get(spmPrefixMatched);
        if (buryPointRule == null) {
            return null;
        }
        // 加工BuryPointRule，只保留spm内oid有关数据
        Set<String> oidsOfSpm = new HashSet<>(Arrays.asList(spm.split(LogUtil.SPM_SEPERATOR_KEYWORD)));
        BuryPointRule result = JsonUtils.parseObject(JsonUtils.toJson(buryPointRule), BuryPointRule.class);
        result.setKey(spm);
        // 剔除不相关oid
        Set<String> pageListVerifierKeys = new HashSet<>(result.getPageListVerifiers().keySet());
        pageListVerifierKeys.forEach(k -> {
            if (!oidsOfSpm.contains(k)) {
                result.getPageListVerifiers().remove(k);
            }
        });
        // 剔除不相关oid
        Set<String> elementListVerifierKeys = new HashSet<>(result.getEleListVerifiers().keySet());
        elementListVerifierKeys.forEach(k -> {
            if (!oidsOfSpm.contains(k)) {
                result.getEleListVerifiers().remove(k);
            }
        });
        conversationRuleMap.put(spm, result);
        return result;
    }

    /**
     * 去除浮层的父对象路径
     * @param originSpm
     * @return
     */
    public String removePopParentsOfLog(String originSpm){
        log.info("去除浮层开始，originspm={}",originSpm);
        List<String> oidSeq = Arrays.asList(originSpm.split("\\|"));
        String popoverOid = "";
        for (String oid : oidSeq) {
            if(popovers.contains(oid)){
                popoverOid = oid;
                break;
            }
        }
        if(StringUtils.isEmpty(popoverOid)){
            log.info("去除浮层结束");
            return originSpm;
        }
        String spmWithoutPopParents = StringUtils.substringBefore(originSpm, popoverOid);
        spmWithoutPopParents = spmWithoutPopParents + popoverOid;
        log.info("去除浮层结束");
        return spmWithoutPopParents;
    }
}
