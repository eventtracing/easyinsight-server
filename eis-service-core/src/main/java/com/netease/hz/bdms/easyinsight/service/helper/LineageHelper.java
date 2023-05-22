package com.netease.hz.bdms.easyinsight.service.helper;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.netease.hz.bdms.easyinsight.common.bo.lineage.*;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.exception.CommonException;
import com.netease.hz.bdms.easyinsight.common.exception.ObjException;
import com.netease.hz.bdms.easyinsight.common.util.CommonUtil;
import com.netease.hz.bdms.easyinsight.dao.model.EisObjAllRelationRelease;
import com.netease.hz.bdms.easyinsight.dao.model.EisReqObjRelation;
import com.netease.hz.bdms.easyinsight.dao.model.EisTerminalReleaseHistory;
import com.netease.hz.bdms.easyinsight.dao.model.ObjectBasic;
import com.netease.hz.bdms.easyinsight.service.service.ObjectBasicService;
import com.netease.hz.bdms.easyinsight.service.service.impl.AppRelationService;
import com.netease.hz.bdms.easyinsight.service.service.obj.ObjRelationReleaseService;
import com.netease.hz.bdms.easyinsight.service.service.obj.ReqObjRelationService;
import com.netease.hz.bdms.easyinsight.service.service.terminalrelease.TerminalReleaseService;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class LineageHelper {

    @Resource
    private ReqObjRelationService reqObjRelationService;

    @Resource
    private ObjRelationReleaseService objRelationReleaseService;

    @Resource
    private AppRelationService appRelationService;

    @Resource
    private ObjectBasicService objectBasicService;

    @Resource
    private TerminalReleaseService terminalReleaseService;

    /**
     * 合并需求血缘
     *
     * @param reqRelation
     * @param combineRelation
     * @return
     */
    public Map<Long,Set<Long>> combineReqRelation(Map<Long,Set<Long>> reqRelation, Map<Long,Set<Long>> combineRelation){
        //同一个端版本号关联的任务只能在一个需求组下，因此这些流程的需求组是同一个
        Map<Long,Set<Long>> objToParentsCombine = new HashMap<>();
        for (Long objId : reqRelation.keySet()) {
            Set<Long> parentsOfCombine = objToParentsCombine.computeIfAbsent(objId,k->new HashSet<>());
            if(combineRelation.containsKey(objId)){
                //基线里也存在当前objId，则说明当前对象是变更操作，所以取父对象并集
                Set<Long> parentsOfProcess = reqRelation.get(objId);
                Set<Long> parentsOfBase = combineRelation.get(objId);
                parentsOfCombine.addAll(parentsOfProcess);
                parentsOfCombine.addAll(parentsOfBase);
            }else {
                //基线不存在当前objId,则说明是新增操作，直接取待上线流程中的父对象集合
                Set<Long> parentsOfProcess = reqRelation.get(objId);
                parentsOfCombine.addAll(parentsOfProcess);
            }
        }
        //获取存在基线内、但不在需求下的对象，直接复制父血缘
        for (Long objId : combineRelation.keySet()) {
            Set<Long> parentsOfCombine = objToParentsCombine.computeIfAbsent(objId,k->new HashSet<>());
            if(!reqRelation.containsKey(objId)){
                parentsOfCombine.addAll(combineRelation.get(objId));
            }
        }
        return objToParentsCombine;
    }

    /**
     * 构造需求血缘图，通过base + diff构建血缘图
     *
     * @param baseReleaseId
     * @param terminalId
     * @param reqRelation
     * @return
     */
    public LinageGraph genReqLinageGraph(Long baseReleaseId, Long terminalId, Map<Long,Set<Long>> reqRelation){
        LinageGraph graph = doGenReqLinageGraph(baseReleaseId, terminalId, reqRelation);
        return mergeBridges(graph, EtContext.get(ContextConstant.APP_ID), terminalId, baseReleaseId);
    }

    /**
     * 补充桥梁父血缘
     * @return
     */
    public LinageGraph mergeBridges(LinageGraph originGraph, Long currentAppId, Long currentTerminalId, Long baseReleaseId) {
        Set<Long> allObjIds = originGraph.getAllObjIds();
        if (CollectionUtils.isEmpty(allObjIds)) {
            return originGraph;
        }
        List<ObjectBasic> parentBridges = appRelationService.getParentBridgeCandidatesByReleaseId(currentAppId, currentTerminalId, baseReleaseId);
        Map<Long, List<Long>> bridgeUpTerminalIdMapping = appRelationService.getBridgeUpTerminalIdMapping(parentBridges, currentAppId, currentTerminalId);
        Map<Long, LinageGraph> bridgeTerminalLinageGraphMap = getBridgeTerminalLinageGraphMap(bridgeUpTerminalIdMapping.values().stream().flatMap(o -> o == null ? Stream.empty() : o.stream()).collect(Collectors.toSet()), baseReleaseId);
        // 为每个桥梁对象把其所挂载父亲血缘算出来
        Map<Long, LinageGraph> graphOfEachBridge = new HashMap<>();
        bridgeUpTerminalIdMapping.forEach((bridgeObjId, bridgeUpTerminalIds) -> {
            bridgeUpTerminalIds.forEach(terminalId -> {
                LinageGraph linageGraph = bridgeTerminalLinageGraphMap.get(terminalId);
                if (linageGraph != null) {
                    graphOfEachBridge.put(bridgeObjId, linageGraph);
                }
            });
        });
        return doMergeGraph(originGraph, graphOfEachBridge);
    }

    /**
     * 构造需求血缘图，通过base + diff构建血缘图
     *
     * @param baseReleaseId
     * @param terminalId
     * @param reqRelation
     * @return
     */
    private LinageGraph doGenReqLinageGraph(Long baseReleaseId, Long terminalId, Map<Long,Set<Long>> reqRelation){
        LinageGraph graph = new LinageGraph();
        Set<Long> allObjIds = new HashSet<>();
        Map<Long, Set<Long>> parentsMap = new HashMap<>();
        List<EisObjAllRelationRelease> releasedRelations = objRelationReleaseService
                .getAllRelationsByReleaseId(baseReleaseId);
        Map<Long,Set<Long>> releasedParentsRelationsMap = new HashMap<>();
        /**
         * 对象id为血缘关系map的key，必须按照先放入base的血缘，再放入需求中的血缘这个顺序，这样才能保证diff的部分覆盖base的部分
         */
        //先把基线中的血缘关系放入map
        for (EisObjAllRelationRelease releasedRelation : releasedRelations) {
            allObjIds.add(releasedRelation.getObjId());
            if(releasedRelation.getParentObjId() != null){
                allObjIds.add(releasedRelation.getParentObjId());
            }
            Set<Long> parentsOfCurrent = releasedParentsRelationsMap
                    .computeIfAbsent(releasedRelation.getObjId(),k -> new HashSet<>());

            parentsOfCurrent.add(releasedRelation.getParentObjId());

        }
        //把需求中的血缘放入map，如过objId已存在则直接覆盖。
        for (Long objId : reqRelation.keySet()) {
            allObjIds.add(objId);
            if(reqRelation.get(objId) != null){
                allObjIds.addAll(reqRelation.get(objId));
            }
        }
        parentsMap.putAll(releasedParentsRelationsMap);
        parentsMap.putAll(reqRelation);
        graph.setAllObjIds(allObjIds);
        graph.setParentsMap(parentsMap);
        graph.setChildrenMap(getChildrenMap(parentsMap));
        return graph;
    }

    /**
     * 补充桥梁以上的父空间/父端血缘
     * @param spmsOfCurrentObjInReqLineage 当前空间血缘
     * @param bridgeUpTerminalIdMapping 待补充的桥梁对象
     * @param parentTerminalLinageGraphMap 父空间完整血缘
     * @return
     */
    public List<List<Long>> updateBridgeParent(List<List<Long>> spmsOfCurrentObjInReqLineage, Map<Long, List<Long>> bridgeUpTerminalIdMapping, Map<Long, LinageGraph> parentTerminalLinageGraphMap) {
        if (MapUtils.isEmpty(parentTerminalLinageGraphMap) || MapUtils.isEmpty(bridgeUpTerminalIdMapping)) {
            return spmsOfCurrentObjInReqLineage;
        }
        List<List<Long>> result = new ArrayList<>();
        spmsOfCurrentObjInReqLineage.forEach(objIdsOfSpm -> {
            if (CollectionUtils.isEmpty(objIdsOfSpm) || objIdsOfSpm.size() == 1) {
                result.add(objIdsOfSpm);
                return;
            }
            Long rootObjId = objIdsOfSpm.get(objIdsOfSpm.size() - 1); // 最上层的objId
            List<Long> bridgeUpTerminalIds = bridgeUpTerminalIdMapping.get(rootObjId);
            if (CollectionUtils.isEmpty(bridgeUpTerminalIds)) {
                result.add(objIdsOfSpm);
                return;
            }
            // rootObjId是桥梁，应当拼接桥梁的spm
            boolean bridgeResolved = false;
            for (Long bridgeUpTerminalId : bridgeUpTerminalIds) {
                LinageGraph bridgeLinageGraph = parentTerminalLinageGraphMap.get(bridgeUpTerminalId);
                if (bridgeLinageGraph == null) {
                    continue;
                }
                // 桥梁在父空间的spm列表
                List<List<Long>> bridgeSpmsOfParentSpace = getObjIdSpms(bridgeLinageGraph, rootObjId);
                if (CollectionUtils.isNotEmpty(bridgeSpmsOfParentSpace)) {
                    bridgeSpmsOfParentSpace.forEach(bridgeSpmOfParentSpace -> {
                        // 拼接父空间bridge spm
                        List<Long> fullSpm = new ArrayList<>(objIdsOfSpm);
                        if (bridgeSpmOfParentSpace.size() > 1) {
                            fullSpm.addAll(bridgeSpmOfParentSpace.subList(1, bridgeSpmOfParentSpace.size()));
                        }
                        result.add(fullSpm);
                    });
                    // 任意一端上线即可，不考虑多端血缘一致性
                    bridgeResolved = true;
                    break;
                }
            }
            // 没有有效桥梁
            if (!bridgeResolved) {
                throw new CommonException("桥梁无效，挂载的端均未上线");
            }
        });
        return result;
    }

    /**
     * @param bridgeTerminalIds 桥梁向上桥接的端id列表
     * @return key terminalId，value：parentAppId下，与terminalId同名端的血缘图
     */
    public Map<Long, LinageGraph> getBridgeTerminalLinageGraphMap(Set<Long> bridgeTerminalIds, Long baseReleaseId) {
        if (baseReleaseId == null || CollectionUtils.isEmpty(bridgeTerminalIds)) {
            return new HashMap<>();
        }
        Map<Long, EisTerminalReleaseHistory> parentTerminalReleaseMap = appRelationService.getParentTerminalReleaseMap(baseReleaseId);
        if (MapUtils.isEmpty(parentTerminalReleaseMap)) {
            return new HashMap<>();
        }
        Map<Long, LinageGraph> result = new HashMap<>();
        bridgeTerminalIds.forEach(bridgeTerminalId -> {
            EisTerminalReleaseHistory parentTerminalRelease = parentTerminalReleaseMap.get(bridgeTerminalId);
            if (parentTerminalRelease == null) {
                return;
            }
            // 不支持桥梁嵌套桥梁，因此计算桥梁以上的血缘时，不再嵌套计算上面的桥梁
            LinageGraph linageGraph = genReleasedLinageGraphNoBridge(parentTerminalRelease.getId());
            if (linageGraph == null) {
                return;
            }
            result.put(bridgeTerminalId, linageGraph);
        });
        return result;
    }

    /**
     * 构造需求血缘图，通过base + diff构建血缘图
     *
     * @param baseReleaseId
     * @param terminalId
     * @param reqPoolId
     * @return
     */
    private LinageGraph doGenReqLinageGraph(Long baseReleaseId, Long terminalId, Long reqPoolId){
        LinageGraph graph = new LinageGraph();
        Set<Long> allObjIds = new HashSet<>();
        Map<Long, Set<Long>> parentsMap = new HashMap<>();
        List<EisObjAllRelationRelease> releasedRelations = objRelationReleaseService
                .getAllRelationsByReleaseId(baseReleaseId);
        List<EisReqObjRelation> relationsOfReq = reqObjRelationService.getByReqIdAndTerminalId(reqPoolId, terminalId);
        Map<Long,Set<Long>> releasedParentsRelationsMap = new HashMap<>();
        Map<Long,Set<Long>> reqObjParentsRelationsMap = new HashMap<>();
        /**
         * 对象id为血缘关系map的key，必须按照先放入base的血缘，再放入需求中的血缘这个顺序，这样才能保证diff的部分覆盖base的部分
         */
        //先把基线中的血缘关系放入map
        for (EisObjAllRelationRelease releasedRelation : releasedRelations) {
            allObjIds.add(releasedRelation.getObjId());
            if(releasedRelation.getParentObjId() != null){
                allObjIds.add(releasedRelation.getParentObjId());
            }
            Set<Long> parentsOfCurrent = releasedParentsRelationsMap
                    .computeIfAbsent(releasedRelation.getObjId(),k -> new HashSet<>());
            parentsOfCurrent.add(releasedRelation.getParentObjId());

        }
        //把需求中的血缘放入map，如过objId已存在则直接覆盖。
        for (EisReqObjRelation reqObjRelation : relationsOfReq) {
            allObjIds.add(reqObjRelation.getObjId());
            if(reqObjRelation.getParentObjId() != null){
                allObjIds.add(reqObjRelation.getParentObjId());
            }
            Set<Long> parentsOfCurrent = reqObjParentsRelationsMap
                    .computeIfAbsent(reqObjRelation.getObjId(),k -> new HashSet<>());

            parentsOfCurrent.add(reqObjRelation.getParentObjId());

        }
        parentsMap.putAll(releasedParentsRelationsMap);
        parentsMap.putAll(reqObjParentsRelationsMap);
        graph.setAllObjIds(allObjIds);
        graph.setParentsMap(parentsMap);
        graph.setChildrenMap(getChildrenMap(parentsMap));
        return graph;
    }

    /**
     * 构造需求血缘图，通过base + diff构建血缘图
     *
     * @param baseReleaseId
     * @param terminalId
     * @param reqPoolId
     * @return
     */
    public LinageGraph genReqLinageGraph(Long baseReleaseId, Long terminalId, Long reqPoolId){
        LinageGraph graph = doGenReqLinageGraph(baseReleaseId, terminalId, reqPoolId);
        return mergeBridges(graph, EtContext.get(ContextConstant.APP_ID), terminalId, baseReleaseId);
    }

    public LinageGraph genReleasedLinageGraph(Long baseReleaseId) {
        if (baseReleaseId == 0L) {
            LinageGraph res = new LinageGraph();
            res.setAllObjIds(Sets.newHashSet());
            res.setParentsMap(Maps.newHashMap());
            res.setChildrenMap(Maps.newHashMap());
            return res;
        }
        EisTerminalReleaseHistory terminalReleaseHistory = terminalReleaseService.getById(baseReleaseId);
        if (terminalReleaseHistory == null) {
           throw new CommonException("baseReleaseId 无效" + baseReleaseId);
        }
        LinageGraph base = doGenReleasedLinageGraph(baseReleaseId);
        return mergeBridges(base, EtContext.get(ContextConstant.APP_ID), terminalReleaseHistory.getTerminalId(), baseReleaseId);
    }

    /**
     * 计算血缘图，不包括桥梁
     */
    public LinageGraph genReleasedLinageGraphNoBridge(Long baseReleaseId) {
        if (baseReleaseId == 0L) {
            LinageGraph res = new LinageGraph();
            res.setAllObjIds(Sets.newHashSet());
            res.setParentsMap(Maps.newHashMap());
            res.setChildrenMap(Maps.newHashMap());
            return res;
        }
        EisTerminalReleaseHistory terminalReleaseHistory = terminalReleaseService.getById(baseReleaseId);
        if (terminalReleaseHistory == null) {
            throw new CommonException("baseReleaseId 无效" + baseReleaseId);
        }
        return doGenReleasedLinageGraph(baseReleaseId);
    }

    private static LinageGraph doMergeGraph(LinageGraph base, Map<Long, LinageGraph> graphOfEachBridge) {
        if (MapUtils.isEmpty(graphOfEachBridge)) {
            return base;
        }
        Set<Long> noParentsObjIds = new HashSet<>();
        base.getAllObjIds().forEach(objId -> {
            if (base.getParentsMap().get(objId) == null) {
                noParentsObjIds.add(objId);
            }
        });
        // 无父对象的对象，可能是桥梁，在父空间血缘树里
        noParentsObjIds.forEach(noParentsObjId -> {
            LinageGraph graphOfBridge = graphOfEachBridge.get(noParentsObjId);
            if (graphOfBridge != null) {
                doMerge(noParentsObjId, base, graphOfBridge);
            }
        });
        // 更新childrenMap
        base.setChildrenMap(getChildrenMap(base.getParentsMap()));
        return base;
    }

    private static void doMerge(Long objId, LinageGraph base, LinageGraph parent) {
        Set<Long> parentsInParentGraph = parent.getParentsMap().get(objId);
        if (CollectionUtils.isEmpty(parentsInParentGraph)) {
            return;
        }
        Set<Long> parentsInBaseGraph = base.getParentsMap().computeIfAbsent(objId, k -> new HashSet<>());
        base.getAllObjIds().addAll(parentsInParentGraph);
        parentsInBaseGraph.addAll(parentsInParentGraph);
        for (Long parentObjId : parentsInParentGraph) {
            doMerge(parentObjId, base, parent);
        }
    }

    /**
     * 构造已上线的血缘图
     * @param baseReleaseId
     * @return
     */
    private LinageGraph doGenReleasedLinageGraph(Long baseReleaseId){
        LinageGraph graph = new LinageGraph();
        Set<Long> allObjIds = new HashSet<>();
        Map<Long, Set<Long>> parentsMap = new HashMap<>();
        List<EisObjAllRelationRelease> releasedRelations = objRelationReleaseService
                .getAllRelationsByReleaseId(baseReleaseId);
        Map<Long,Set<Long>> releasedParentsRelationsMap = new HashMap<>();
        for (EisObjAllRelationRelease releasedRelation : releasedRelations) {
            allObjIds.add(releasedRelation.getObjId());
            if(releasedRelation.getParentObjId() != null){
                allObjIds.add(releasedRelation.getParentObjId());
            }
            Set<Long> parentsOfCurrent = releasedParentsRelationsMap
                    .computeIfAbsent(releasedRelation.getObjId(),k -> new HashSet<>());
            if(releasedRelation.getParentObjId() != null){
                parentsOfCurrent.add(releasedRelation.getParentObjId());
            }
        }
        parentsMap.putAll(releasedParentsRelationsMap);
        graph.setAllObjIds(allObjIds);
        graph.setParentsMap(parentsMap);
        graph.setChildrenMap(getChildrenMap(parentsMap));
        return graph;
    }

    public static Map<Long, Set<Long>> getChildrenMap(Map<Long, Set<Long>> parentsMap) {
        Map<Long, Set<Long>> childrenMap = new HashMap<>();
        for (Long objId : parentsMap.keySet()) {
            Set<Long> parentObjIds = parentsMap.get(objId);
            if (CollectionUtils.isEmpty(parentObjIds) || (parentObjIds.size() == 1 && parentObjIds.contains(null))) {
                // 根节点
                childrenMap.putIfAbsent(objId, Sets.newHashSet());
            } else {
                // 非根节点
                parentObjIds.forEach(parentObjId -> {
                    if (parentObjId != null) {
                        Set<Long> currentChildrenSet = childrenMap.computeIfAbsent(parentObjId, k -> new HashSet<>());
                        currentChildrenSet.add(objId);
                    }
                });
            }
        }
        return childrenMap;
    }

    /**
     * 根据血缘图，构建血缘层级关系森林结构
     * @param linageGraph
     * @return
     */
    public LineageForest buildForestByGraph(LinageGraph linageGraph){
        Set<Long> rootObjIds = new HashSet<>();
        Set<Long> allObjIds = linageGraph.getAllObjIds();
        Map<Long,Set<Long>> childrenMap = linageGraph.getChildrenMap();
        Map<Long, Set<Long>> parentsMap = linageGraph.getParentsMap();
        for (Long objId : allObjIds) {
            if(CollectionUtils.isEmpty(parentsMap.get(objId)) || parentsMap.get(objId).contains(null)){
                rootObjIds.add(objId);
            }
        }
        List<TreeNode> roots = new ArrayList<>();
        for (Long rootObjId : rootObjIds) {
            TreeNode rootTreeNode = new TreeNode(rootObjId);
            rootTreeNode.setSpmByObjId(rootObjId.toString());
            Set<Long> children = childrenMap.get(rootObjId);
            LinkedList<TreeNode> queue = new LinkedList<>();
            roots.add(rootTreeNode);
            if(CollectionUtils.isEmpty(children)){
                rootTreeNode.setChildren(new ArrayList<>());
                continue;
            }
            List<TreeNode> rootChildren = new ArrayList<>();
            for (Long child : children) {
                TreeNode childNode = new TreeNode(child);
                String spm = child + "|" + rootTreeNode.getSpmByObjId();
                childNode.setSpmByObjId(spm);
                rootChildren.add(childNode);
                queue.offer(childNode);
            }
            rootTreeNode.setChildren(rootChildren);
            while(!queue.isEmpty()){
                TreeNode currentTreeNode = queue.poll();
                Long objId = currentTreeNode.getObjId();
                Set<Long> currentChildren = childrenMap.get(objId);
                List<TreeNode> currenChildrenNode = new ArrayList<>();
                if(!CollectionUtils.isEmpty(currentChildren)){
                    for (Long currentChild : currentChildren) {
                        TreeNode childNode = new TreeNode(currentChild);
                        String spmByObjId = currentChild + "|" + currentTreeNode.getSpmByObjId();
                        childNode.setSpmByObjId(spmByObjId);
                        currenChildrenNode.add(childNode);
                        queue.offer(childNode);
                    }
                }
                currentTreeNode.setChildren(currenChildrenNode);
            }
        }
        LineageForest lineageForest = new LineageForest();
        lineageForest.setRoots(roots);
        return lineageForest;
    }

    /**
     * 根据一批spm构建血缘森林结构
     * @param spmAsObjIdLists
     * @return
     */
    public LineageForest buildForestBySpms(List<List<Long>> spmAsObjIdLists){
        Map<Long,Set<Long>> parentsMap = new HashMap<>();
        Map<Long,Set<Long>> childrenMap = new HashMap<>();
        Set<Long> allObjIds = new HashSet<>();
        for (List<Long> spmAsObjIdList : spmAsObjIdLists) {
            allObjIds.addAll(spmAsObjIdList);
            if(spmAsObjIdList.size() == 1){
                Set<Long> parents = parentsMap.computeIfAbsent(spmAsObjIdList.get(0), k -> new HashSet<>());
                parents.add(null);
            }else {
                for (int i = 0; i < spmAsObjIdList.size() - 1; i++) {
                    Set<Long> parents = parentsMap.computeIfAbsent(spmAsObjIdList.get(i), k -> new HashSet<>());
                    parents.add(spmAsObjIdList.get(i + 1));
                }
            }
        }
        for (Long objId : parentsMap.keySet()) {
            Set<Long> parentObjIds = parentsMap.get(objId);
            for (Long parentObjId : parentObjIds) {
                if(parentObjId != null) {
                    Set<Long> currentChildrenSet = childrenMap.computeIfAbsent(parentObjId, k -> new HashSet<>());
                    currentChildrenSet.add(objId);
                }
            }
        }
        LinageGraph linageGraph = new LinageGraph();
        linageGraph.setAllObjIds(allObjIds);
        linageGraph.setParentsMap(parentsMap);
        linageGraph.setChildrenMap(childrenMap);
        return buildForestByGraph(linageGraph);
    }

    /**
     * 获取某一血缘图下的某个对象的所有到根节点的路径，以objId列表形式组织
     * @param graph
     * @param objId
     * @return
     */
    public List<List<Long>> getObjIdSpms(LinageGraph graph,Long objId){
        Map<Long,Set<Long>> parentsMap = graph.getParentsMap();
        List<List<Long>> allSpms = new ArrayList<>();
        if(CollectionUtils.isEmpty(parentsMap.get(objId))){
            allSpms.add(Lists.newArrayList(objId));
            return allSpms;
        }
        getObjIdSpmCore(parentsMap,allSpms,objId,Lists.newArrayList(objId));
        return allSpms;
    }

//    /**
//     * 获取两个ObjId之间的所有路径
//     * @param fromId
//     * @param toId
//     * @param graph
//     * @return
//     */
//    public List<List<Long>> getObjIdPathBetweenTwoObj(Long fromId,Long toId,LinageGraph graph){
//        List<List<Long>> spmsOfFromObj = getObjIdSpms(graph,fromId);
//        List<List<Long>> result = new ArrayList<>();
//        for (List<Long> spm : spmsOfFromObj) {
//            if(spm.contains(toId)){
//                int indexOfToObjId = spm.indexOf(toId);
//                result.add(spm.subList(0,indexOfToObjId+1));
//            }
//        }
//        return result;
//    }

    public void getObjIdSpmCore(Map<Long, Set<Long>> parentsMap, List<List<Long>> allSpms,
                                Long objId, List<Long> curentSpm){
        Set<Long> parents = parentsMap.get(objId);
        if(CollectionUtils.isEmpty(parents) || (parents.size() == 1 && parents.contains(null))){
            allSpms.add(new ArrayList<>(curentSpm));
            return;
        }
        for(Long parentObjId:parents){
            if(parentObjId == null){
                allSpms.add(new ArrayList<>(curentSpm));
                continue;
            }
            curentSpm.add(parentObjId);
            getObjIdSpmCore(parentsMap,allSpms,parentObjId,curentSpm);
            curentSpm.remove(parentObjId);
        }
    }


    // ==============================================对象管理模块============================================
    /**
     * 获取需求组的全量血缘（非终态血缘，血缘关系只增不删）
     *
     * @param baseReleasedId 基线发布版本
     * @param terminalId 端ID
     * @param reqPoolId 需求组ID
     * @return
     */
    public TotalLineageGraph getTotalLineageGraph(
            Long baseReleasedId, Long terminalId, Long reqPoolId){
        // 1. 信息查询
        List<EisObjAllRelationRelease> releasedRelationList = objRelationReleaseService
                .getAllRelationsByReleaseId(baseReleasedId);
        List<EisReqObjRelation> reqPoolRelationList = reqObjRelationService
                .getByReqIdAndTerminalId(reqPoolId, terminalId);

        // 2. 完整血缘图构建
        // 基础血缘
        Map<Long, Set<Long>> baseParentsMap = Maps.newHashMap();
        for (EisObjAllRelationRelease baseRelation : releasedRelationList) {
            Long currObjId = baseRelation.getObjId();
            Long currParentObjId = baseRelation.getParentObjId();
            Set<Long> parentObjIdSet = baseParentsMap
                    .computeIfAbsent(currObjId, k -> Sets.newHashSet());

            parentObjIdSet.add(currParentObjId);

        }
        // 变更血缘
        Map<Long, Set<Long>> diffParentsMap = Maps.newHashMap();
        for (EisReqObjRelation diffRelation : reqPoolRelationList) {
            Long currObjId = diffRelation.getObjId();
            Long currParentObjId = diffRelation.getParentObjId();
            Set<Long> parentObjIdSet = diffParentsMap
                    .computeIfAbsent(currObjId, k -> Sets.newHashSet());

            parentObjIdSet.add(currParentObjId);

        }
        // 血缘合并至baseParentsMap, 合并时【只增不删】, 并记录增删信息
        Map<Long, Set<Long>> addedRelationMap = Maps.newHashMap();  // 新增的父子血缘
        Map<Long, Set<Long>> deletedRelationMap = Maps.newHashMap();  // 删除的父子血缘
        for (Long currObjId : diffParentsMap.keySet()) {
            Set<Long> diffParentObjIdSet = diffParentsMap.get(currObjId);
            if(baseParentsMap.containsKey(currObjId)){
                // 变更对象情形
                Set<Long> baseParentObjIdSet = baseParentsMap.get(currObjId);
                // 新增血缘
                Set<Long> addedParentObjIdSet = diffParentObjIdSet.stream()
                        .filter(k -> !baseParentObjIdSet.contains(k))
                        .collect(Collectors.toSet());
                baseParentObjIdSet.addAll(addedParentObjIdSet); // 新增合并
                addedParentObjIdSet.forEach(parentId -> {
                    Set<Long> childObjIdSet = addedRelationMap
                            .computeIfAbsent(parentId, k -> Sets.newHashSet());
                    childObjIdSet.add(currObjId);
                });

                // 删除血缘
                Set<Long> deletedParentObjIdSet = baseParentObjIdSet.stream()
                        .filter(k -> !diffParentObjIdSet.contains(k))
                        .collect(Collectors.toSet());
                deletedParentObjIdSet.forEach(parentId -> {
                    Set<Long> childObjIdSet = deletedRelationMap
                            .computeIfAbsent(parentId, k -> Sets.newHashSet());
                    childObjIdSet.add(currObjId);
                });

            }else {
                // 新增对象情形
                diffParentObjIdSet.forEach(parentId -> {
                    Set<Long> childrenObjIdSet = addedRelationMap
                            .computeIfAbsent(parentId, k -> Sets.newHashSet());
                    childrenObjIdSet.add(currObjId);
                });
                baseParentsMap.put(currObjId, diffParentObjIdSet);  // 合并新增
            }
        }

        // 构建父子关系集合
        Map<Long, Set<Long>> childrenMap = Maps.newHashMap();
        Set<Long> allObjIds = Sets.newHashSet();
        for (Long currObjId : baseParentsMap.keySet()) {
            Set<Long> currParentIdSet = baseParentsMap.get(currObjId);
            allObjIds.add(currObjId);
            allObjIds.addAll(currParentIdSet);
            if(CollectionUtils.isEmpty(currParentIdSet)){
                // 根节点
                childrenMap.putIfAbsent(currObjId, Sets.newHashSet());
            }else {
                // 非根节点
                currParentIdSet.forEach(parentId -> {
                    Set<Long> currChildrenIdSet = childrenMap
                            .computeIfAbsent(parentId, k -> Sets.newHashSet());
                    currChildrenIdSet.add(currObjId);
                });
            }
        }

        // 3. 信息组合
        TotalLineageGraph totalLineageGraph = new TotalLineageGraph();
        totalLineageGraph.setAllObjIds(allObjIds);
        totalLineageGraph.setChildrenMap(childrenMap);
        totalLineageGraph.setParentsMap(baseParentsMap);
        totalLineageGraph.setAddedRelationMap(addedRelationMap);
        totalLineageGraph.setDeletedRelationMap(deletedRelationMap);

        // 基线父空间桥梁血缘补充
        allObjIds.addAll(addedRelationMap.keySet());
        // 合并父亲中需要的部分到base里
        return (TotalLineageGraph) mergeBridges(totalLineageGraph, EtContext.get(ContextConstant.APP_ID), terminalId, baseReleasedId);
    }

    /**
     * 给定血缘图，构建指定对象的血缘树
     *
     * @param graph 血缘关系
     * @param objId 对象ID
     * @return
     */
    public List<Node> getObjLineageTree(LinageGraph graph, Long objId){
        Preconditions.checkArgument(null != graph, "血缘图不能为空");
        Preconditions.checkArgument(null != objId, "对象ID不能为空");
        // 1. 构建当前节点的父血缘、子血缘
        Map<Long, Set<Long>> parentsMap = graph.getParentsMap();
        Map<Long, Set<Long>> childrenMap = graph.getChildrenMap();
        List<Node> superTrees = this.buildSuperTree(parentsMap, Sets.newHashSet(objId), null);
        Node subTree = this.buildSubTree(childrenMap, objId);

        // 2. 血缘树拼接 (将subTree拼接到所有superTree的各个叶子节点上)
        Queue<Node> nodeQueue = Lists.newLinkedList();
        superTrees.forEach(nodeQueue::offer);
        while (!nodeQueue.isEmpty()) {
            Node currNode = nodeQueue.poll();
            // 获取当前节点的子节点
            List<Node> currChildNodes = currNode.getChildren();
            if (CollectionUtils.isEmpty(currChildNodes)) {
                // 若当前节点为叶节点，执行拼接
                currNode.setChildren(subTree.getChildren());
            } else {
                // 若当前节点为非叶节点，所有子节点入队
                currChildNodes.forEach(nodeQueue::offer);
            }
        }

        for (Node node : superTrees){
            if(node.getObjId() == null && node.getChildren().size() == 1){
                Node childrenNode = node.getChildren().get(0);
                node.setChildren(childrenNode.getChildren());
                node.setObjId(childrenNode.getObjId());
                node.setType(childrenNode.getType());
            }
        }

        return superTrees;
    }


    /**
     * 给定全量血缘图以及筛选出的对象集合，获取所有对象列表树
     *
     * @param graph 血缘图
     * @param selectedObjIds
     * @return
     */
    public List<Node> getObjTree(LinageGraph graph, Set<Long> selectedObjIds){
        Preconditions.checkArgument(null != graph, "血缘图不能为空");
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(selectedObjIds), "候选对象集不能为空");

        Map<Long, Set<Long>> parentsMap = graph.getParentsMap();
        return this.buildSuperTree(parentsMap, selectedObjIds, null);
    }

    /**
     * 给定全量血缘图以及筛选出的对象集合，获取对象子树树
     *
     * @param graph 血缘图
     * @param selectedObjIds
     * @return
     */
    public List<Node> getSonTree(LinageGraph graph, Set<Long> selectedObjIds, Long targetObjId){
        Preconditions.checkArgument(null != graph, "血缘图不能为空");
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(selectedObjIds), "候选对象集不能为空");

        Map<Long, Set<Long>> parentsMap = graph.getParentsMap();
        return this.buildSuperTree(parentsMap, selectedObjIds, targetObjId);
    }

    /**
     * 按SPM过滤树
     * @param originTree
     * @param spmByObjId
     * @return
     */
    public List<Node> filterObjTreeBySpm(List<Node> originTree, String spmByObjId) {
        if (StringUtils.isBlank(spmByObjId)) {
            return new ArrayList<>(0);
        }
        List<Long> spmObjIds = CommonUtil.transSpmToObjIdList(spmByObjId);
        List<Node> result = new ArrayList<>();
        // 第一层没有父亲，特殊处理
        originTree.forEach(rootNode -> {
            if (CollectionUtils.isEmpty(spmObjIds)) {
                result.add(rootNode);
                return;
            }
            if (CollectionUtils.isEmpty(rootNode.getChildren())) {
                return;
            }
            Pair<Long, List<Long>> p = extractLast(spmObjIds);
            Long currentObjIdToMatch = p.getKey();
            List<Long> spmObjIds4Children = p.getValue();
            boolean currentMatch = rootNode.getObjId().equals(currentObjIdToMatch);
            // 本层就不匹配，直接跳过
            if (!currentMatch) {
                return;
            }
            // 本层匹配，需要判断儿子是否匹配
            boolean match = matchAndFilterChildrenBySpm(rootNode, spmObjIds4Children);
            if (match) {
                result.add(rootNode);
            }
        });
        return result;
    }

    /**
     * 返回是否匹配，并且过滤掉不满足SPM的儿子
     */
    private boolean matchAndFilterChildrenBySpm(Node parentNode, List<Long> spmObjIds) {
        // 已不需要匹配
        if (CollectionUtils.isEmpty(spmObjIds)) {
            return true;
        }

        List<Node> children = parentNode.getChildren();
        if (CollectionUtils.isEmpty(children)) {
            // spmObjIds不为空，说明spm还有部分要匹配，而这里又没有子节点了，不满足SPM
            return false;
        }

        // 匹配所有children
        Pair<Long, List<Long>> p = extractLast(spmObjIds);
        Long currentObjIdToMatch = p.getKey();
        List<Long> spmObjIds4Children = p.getValue();

        List<Node> matchedChildren = new ArrayList<>();
        for (Node child : children) {
            // 儿子是否匹配SPM
            boolean childMatch = child.getObjId().equals(currentObjIdToMatch);
            if (!childMatch) {
                continue;
            }
            // 递归儿子去匹配孙子
            childMatch = matchAndFilterChildrenBySpm(child, spmObjIds4Children);
            if (childMatch) {
                matchedChildren.add(child);
            }
        }

        // 保留匹配的儿子列表
        parentNode.setChildren(matchedChildren);
        return CollectionUtils.isNotEmpty(matchedChildren);
    }

    /**
     * 将列表拆分为最后一个元素 + 剩余子列表
     */
    private Pair<Long, List<Long>> extractLast(List<Long> spmObjIds) {
        if (CollectionUtils.isEmpty(spmObjIds)) {
            throw new CommonException("");
        }
        return new Pair<>(spmObjIds.get(spmObjIds.size() - 1), new ArrayList<>(spmObjIds.subList(0, spmObjIds.size() - 1)));
    }

    /**
     * 按对象名、oid过滤树
     * @param originTree
     * @param objSearch
     * @param objectBasicMap
     * @return
     */
    public List<Node> filterObjTreeByObject(List<Node> originTree, String objSearch, Map<Long, ObjectBasic> objectBasicMap) {
        if (StringUtils.isBlank(objSearch)) {
            return originTree;
        }
        if (objectBasicMap == null) {
            return new ArrayList<>(0);
        }
        if (CollectionUtils.isEmpty(originTree)) {
            return new ArrayList<>(0);
        }
        List<Node> result = new ArrayList<>();
        for (Node rootNode : originTree) {
            Long objId = rootNode.getObjId();
            if (objId == null) {
                continue;
            }
            ObjectBasic objectBasic = objectBasicMap.get(objId);
            if (objectBasic == null) {
                continue;
            }
            // 自己匹配
            if (objectBasic.getName().contains(objSearch) || objectBasic.getOid().contains(objSearch)) {
                rootNode.setChildren(new ArrayList<>(0));
                result.add(rootNode);
                continue;
            }

            // 匹配儿子
            boolean match = matchAndFilterChildrenByObject(rootNode, objSearch, objectBasicMap);
            if (match) {
                result.add(rootNode);
            }

        }
        return result;
    }

    private boolean matchAndFilterChildrenByObject(Node parentNode, String objSearch, Map<Long, ObjectBasic> objectBasicMap) {
        List<Node> children = parentNode.getChildren();
        if (CollectionUtils.isEmpty(children)) {
            return false;
        }
        List<Node> matchedChildren = new ArrayList<>();
        for (Node child : children) {
            Long objId = child.getObjId();
            if (objId == null) {
                continue;
            }
            ObjectBasic objectBasic = objectBasicMap.get(objId);
            if (objectBasic == null) {
                continue;
            }
            // 当前匹配了
            if (objectBasic.getName().contains(objSearch) || objectBasic.getOid().contains(objSearch)) {
                child.setChildren(new ArrayList<>(0));
                matchedChildren.add(child);
                continue;
            }
            // 当前没匹配，进行递归，如果孙子以下匹配，那还是要算儿子匹配的
            boolean childMatch = matchAndFilterChildrenByObject(child, objSearch, objectBasicMap);
            if (childMatch) {
                matchedChildren.add(child);
            }
        }
        parentNode.setChildren(matchedChildren);
        return CollectionUtils.isNotEmpty(matchedChildren);
    }

    /**
     * 给定父子关系，以目标对象为根节点 自顶向下构建血缘树
     *
     * @param childrenMap
     * @param objId
     * @return
     */
    private Node buildSubTree(Map<Long, Set<Long>> childrenMap, Long objId){
        Preconditions.checkArgument(null != childrenMap, "全量血缘图不能为空！");
        Preconditions.checkArgument(null != objId, "对象ID不能为空！");
        // 获取父节点指向子节点的映射 <objId, Set<ChildId>>
        if(loopCheck(childrenMap)){
            throw new ObjException("存在环路，无法完成血缘关系的构建！");
        }
        // 自顶向下 层次遍历构建树
        List<Node> rootNodes =  this.buildTreeCore(childrenMap, Sets.newHashSet(objId));
        return rootNodes.get(0);
    }

    /**
     * 给定子父关系集合以及选中的叶节点对象，自底向上构建血缘树
     *
     * @param parentsMap 全量血缘图
     * @param selectedObjId 叶节点对象集合
     * @param targetObjId 目标父节点
     * @return
     */
    private List<Node> buildSuperTree(Map<Long, Set<Long>> parentsMap, Set<Long> selectedObjId, Long targetObjId){
        Preconditions.checkArgument(MapUtils.isNotEmpty(parentsMap), "子父关系集合不能为空！");
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(selectedObjId), "对象ID集合不能为空！");
        // 1. 环路检测
        if(loopCheck(parentsMap)){
            throw new ObjException("存在环路，无法完成血缘关系构建！");
        }
        // 2. 层次遍历 自低向上 找出所有与目标对象直接/间接关联的父子关系childrenMap: <objId, [childId, ...]>
        Map<Long, Set<Long>> childrenMap = Maps.newHashMap();
        Queue<Long> queue = Lists.newLinkedList();
        selectedObjId.forEach(queue::offer);
        while(!queue.isEmpty()){
            // 当前节点出队
            Long currObjId = queue.poll();
            Set<Long> currParentIds = parentsMap.getOrDefault(currObjId, Sets.newHashSet());
            if(CollectionUtils.isEmpty(currParentIds) || (currParentIds.size() == 1 && currParentIds.contains(null))){
                // 根节点
                childrenMap.putIfAbsent(currObjId, Sets.newHashSet());
            }else {
                currParentIds.forEach(currParentId -> {
                    if(currParentId != null) {
                        Set<Long> childIds = childrenMap
                                .computeIfAbsent(currParentId, k -> Sets.newHashSet());
                        childIds.add(currObjId);
                    }else {
                        // 根节点
                        childrenMap.putIfAbsent(currObjId, Sets.newHashSet());
                    }
                });
            }
            // 父节点入队
            currParentIds.forEach(currParentId -> {
                if(currParentId != null) {
                    queue.offer(currParentId);
                }
            });
        }

        // 3. 寻找根节点
        Set<Long> fromSet = Sets.newHashSet();
        Set<Long> toSet = Sets.newHashSet();
        for (Long from : childrenMap.keySet()) {
            fromSet.add(from);
            toSet.addAll(childrenMap.get(from));
        }
        Set<Long> rootNodeIds = fromSet.stream()
                .filter(k -> !toSet.contains(k))
                .collect(Collectors.toSet());

        // 4. 构建所有的父血缘树
        if(targetObjId != null){
            return this.buildTreeCore(childrenMap, Collections.singleton(targetObjId));
        }else {
            return this.buildTreeCore(childrenMap, rootNodeIds);
        }
    }

    /**
     * 给定根节点 自顶向下 构建树
     *
     * @param childrenMap 对象指向子对象的映射 <objId, [childId, ...]>
     * @param rootNodeIds 根对象ID
     * @return
     */
    private List<Node> buildTreeCore(Map<Long, Set<Long>> childrenMap, Set<Long> rootNodeIds){
        // 参数校验
        Preconditions.checkArgument(null != childrenMap, "关系集合不能为空");
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(rootNodeIds), "根节点集合不能为空");

        // 层次遍历，从根节点开始自顶向下构建树
        List<Node> rootNodes = rootNodeIds.stream().map(k -> new Node(k)).collect(Collectors.toList());
        Queue<Node> nodeQueue = Lists.newLinkedList();
        rootNodes.forEach(nodeQueue::offer);
        while (!nodeQueue.isEmpty()){
            // 当前节点出队
            Node currNode = nodeQueue.poll();
            Long currNodeId = currNode.getObjId();
            // 获取当前节点的所有子节点
            Set<Long> childrenIds = childrenMap.getOrDefault(currNodeId, Sets.newHashSet());
            List<Node> childrenNodes = childrenIds.stream()
                    .map(k -> new Node(k))
                    .collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(childrenNodes)) {
                currNode.setChildren(childrenNodes);
            }
            // 所有子节点入队
            childrenNodes.forEach(nodeQueue::offer);
        }
        return rootNodes;
    }

    /**
     * 环路检查
     *
     * @param relationMap 父子、子父关系映射，<objId, Set<childIds>>或<objId, Set<parentIds>>
     * @return
     */
    public Boolean loopCheck(Map<Long, Set<Long>> relationMap){
        // 1. 当<objId, Set<childIds>>则寻找所有根节点，当<objId, Set<parentIds>>则寻找所有叶子节点
        Set<Long> fromSet = Sets.newHashSet();
        Set<Long> toSet = Sets.newHashSet();
        for (Long from : relationMap.keySet()) {
            fromSet.add(from);
            Set<Long> pset = relationMap.getOrDefault(from, Sets.newHashSet());
            pset = pset.stream().filter(Objects::nonNull).collect(Collectors.toSet());
            toSet.addAll(pset);
        }
        Set<Long> rootIds = fromSet.stream()
                .filter(k -> !toSet.contains(k))
                .collect(Collectors.toSet());

        // 2. 层次遍历，构建所有路径并检测是否存在环路
        Queue<List<Long>> pathQueue = Lists.newLinkedList();
        rootIds.forEach(k -> pathQueue.offer(Lists.newArrayList(k)));
        while(!pathQueue.isEmpty()){
            List<Long> currPath = pathQueue.poll();
            Long currPathLastNodeId = currPath.get(currPath.size()-1);
            Set<Long> nextNodeIdSet = relationMap.getOrDefault(currPathLastNodeId, Sets.newHashSet());
            nextNodeIdSet = nextNodeIdSet.stream().filter(Objects::nonNull).collect(Collectors.toSet());
            for (Long nextNodeId : nextNodeIdSet) {
                // 检测路径中是否存在环路
                if(currPath.contains(nextNodeId)){
                    log.warn("存在环路，objId={}已在路径{}上", nextNodeId, currPath);
                    return true;
                }
                List<Long> nextPath = Lists.newArrayList(currPath);
                nextPath.add(nextNodeId);
                // 新路径入队
                pathQueue.offer(nextPath);
            }
        }
        return false;
    }

    //============实时测试树===============
    /**
     * 根据一批spm构建血缘森林结构
     * @param spmAsObjIdLists
     * @return
     */
    public List<NodeOfTestTree> buildTreeForTest(List<List<Long>> spmAsObjIdLists,Map<Long,ObjectBasic> objMap){
        Map<Long,Set<Long>> parentsMap = new HashMap<>();
        Map<Long,Set<Long>> childrenMap = new HashMap<>();
        Set<Long> allObjIds = new HashSet<>();
        for (List<Long> spmAsObjIdList : spmAsObjIdLists) {
            allObjIds.addAll(spmAsObjIdList);
            if(spmAsObjIdList.size() == 1){
                Set<Long> parents = parentsMap.computeIfAbsent(spmAsObjIdList.get(0),k->new HashSet<>());
                parents.add(ObjectHelper.virtualRootNode);
            }
            for (int i=0;i<spmAsObjIdList.size()-1;i++){
                Set<Long> parents = parentsMap.computeIfAbsent(spmAsObjIdList.get(i),k->new HashSet<>());
                parents.add(spmAsObjIdList.get(i+1));
            }
        }
        for (Long objId : parentsMap.keySet()) {
            Set<Long> parentObjIds = parentsMap.get(objId);
            for (Long parentObjId : parentObjIds) {
                Set<Long> currentChildrenSet = childrenMap.computeIfAbsent(parentObjId,k->new HashSet<>());
                currentChildrenSet.add(objId);
            }
        }
        LinageGraph linageGraph = new LinageGraph();
        linageGraph.setAllObjIds(allObjIds);
        linageGraph.setParentsMap(parentsMap);
        linageGraph.setChildrenMap(childrenMap);
        return buildForestByGraphForTest(linageGraph,objMap);
    }


    public List<NodeOfTestTree> buildForestByGraphForTest(LinageGraph linageGraph, Map<Long, ObjectBasic> objMap){
        Set<Long> rootObjIds = new HashSet<>();
        Set<Long> allObjIds = linageGraph.getAllObjIds();
        Map<Long,Set<Long>> childrenMap = linageGraph.getChildrenMap();
        Map<Long, Set<Long>> parentsMap = linageGraph.getParentsMap();
        for (Long objId : allObjIds) {
            if(CollectionUtils.isEmpty(parentsMap.get(objId))){
                rootObjIds.add(objId);
            }else if(parentsMap.get(objId).contains(ObjectHelper.virtualRootNode)){
                rootObjIds.add(objId);;
            }
        }
        List<NodeOfTestTree> roots = new ArrayList<>();
        for (Long rootObjId : rootObjIds) {
            ObjectBasic obj = objMap.get(rootObjId);
            if(obj == null) continue;
            NodeOfTestTree rootTreeNode = new NodeOfTestTree();
            rootTreeNode.setObjId(rootObjId);
            rootTreeNode.setOid(obj.getOid());
            rootTreeNode.setObjType(obj.getType());
            rootTreeNode.setSpm(obj.getOid());
            rootTreeNode.setObjName(obj.getName());

            Set<Long> children = childrenMap.get(rootObjId);
            LinkedList<NodeOfTestTree> queue = new LinkedList<>();
            roots.add(rootTreeNode);
            if(CollectionUtils.isEmpty(children)){
                continue;
            }
            List<NodeOfTestTree> rootChildren = new ArrayList<>();
            for (Long childObjId : children) {
                ObjectBasic childObj = objMap.get(childObjId);
                NodeOfTestTree childNode = new NodeOfTestTree();
                String spm = childObj.getOid() + "|" + rootTreeNode.getSpm();
                childNode.setObjId(childObjId);
                childNode.setObjType(childObj.getType());
                childNode.setSpm(spm);
                childNode.setOid(childObj.getOid());
                childNode.setObjName(childObj.getName());
                rootChildren.add(childNode);
                queue.offer(childNode);
            }
            rootTreeNode.setChildren(rootChildren);
            while(!queue.isEmpty()){
                NodeOfTestTree currentTreeNode = queue.poll();
                Long objId = currentTreeNode.getObjId();
                Set<Long> currentChildren = childrenMap.get(objId);
                List<NodeOfTestTree> currenChildrenNode = new ArrayList<>();
                if(!CollectionUtils.isEmpty(currentChildren)){
                    for (Long currentChild : currentChildren) {
                        ObjectBasic childObj = objMap.get(currentChild);
                        NodeOfTestTree childNode = new NodeOfTestTree();
                        String spm = childObj.getOid() + "|" + currentTreeNode.getSpm();
                        childNode.setObjId(currentChild);
                        childNode.setObjType(childObj.getType());
                        childNode.setSpm(spm);
                        childNode.setOid(childObj.getOid());
                        childNode.setObjName(childObj.getName());
                        currenChildrenNode.add(childNode);
                        queue.offer(childNode);
                    }
                }
                if(!CollectionUtils.isEmpty(currenChildrenNode)){
                    currentTreeNode.setChildren(currenChildrenNode);
                }
            }
        }
        return roots;
    }

    /**
     * 获取对象血缘树中所有对象ID
     *
     * @param rootNodes 根节点集合
     * @return
     */
    public Set<Long> getObjIdsFromTree(List<Node> rootNodes){
        if(CollectionUtils.isEmpty(rootNodes)){
            return Sets.newHashSet();
        }

        Set<Long> objIds = Sets.newHashSet();
        // 层次遍历
        Queue<Node> nodeQueue = Lists.newLinkedList();
        rootNodes.forEach(nodeQueue::offer);
        while (!nodeQueue.isEmpty()){
            // 当前节点出队
            Node currNode = nodeQueue.poll();
            objIds.add(currNode.getObjId());
            // 子节点入队
            List<Node> currChildren = currNode.getChildren();
            if(CollectionUtils.isNotEmpty(currChildren)){
                currChildren.forEach(nodeQueue::offer);
            }
        }

        return objIds;
    }

}
