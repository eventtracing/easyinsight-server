package com.netease.hz.bdms.eistest.entity;

import com.netease.hz.bdms.easyinsight.service.service.audit.BuryPointRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理埋点规则相关元数据
 */
@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BuryPointMetaInfoDto implements Serializable {

    private static final long serialVersionUID = 8145954685360375756L;
    /**
     * 事件埋点规则
     */
    private BuryPointRule eventRule = new BuryPointRule();
    /**
     * 埋点规则 map,
     * key = SPM,
     * value = <根据血缘链路对象的oid构造唯一key(spm), 埋点规则>
     */
    public Map<String, BuryPointRule> conversationRuleMap = new ConcurrentHashMap<>();

    /**
     * 事件类型 map,
     * key = 会话ID,
     * value = <事件code, 事件name>
     */
    public Map<String, String> conversationEventCodeToNameMap = new ConcurrentHashMap<>();

    /**
     * 对象oid与名字的 Map,
     * key = 会话ID,
     * value = <对象oid, 对象name>
     */
    public Map<String, String> conversationObjectMap = new ConcurrentHashMap<>();

    /**
     * 路由路径的 Map,
     * key = 会话ID,
     * value = <路由路径, oid>
     */
    public Map<String, String> conversationRoutePathToOidMap = new ConcurrentHashMap<>();

    /**
     * 所有浮层的oid
     */
    public Set<String> popovers = new HashSet<>();

}
