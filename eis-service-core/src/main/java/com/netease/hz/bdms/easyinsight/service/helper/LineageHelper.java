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
     * ??????????????????
     *
     * @param reqRelation
     * @param combineRelation
     * @return
     */
    public Map<Long,Set<Long>> combineReqRelation(Map<Long,Set<Long>> reqRelation, Map<Long,Set<Long>> combineRelation){
        //????????????????????????????????????????????????????????????????????????????????????????????????????????????
        Map<Long,Set<Long>> objToParentsCombine = new HashMap<>();
        for (Long objId : reqRelation.keySet()) {
            Set<Long> parentsOfCombine = objToParentsCombine.computeIfAbsent(objId,k->new HashSet<>());
            if(combineRelation.containsKey(objId)){
                //????????????????????????objId??????????????????????????????????????????????????????????????????
                Set<Long> parentsOfProcess = reqRelation.get(objId);
                Set<Long> parentsOfBase = combineRelation.get(objId);
                parentsOfCombine.addAll(parentsOfProcess);
                parentsOfCombine.addAll(parentsOfBase);
            }else {
                //?????????????????????objId,????????????????????????????????????????????????????????????????????????
                Set<Long> parentsOfProcess = reqRelation.get(objId);
                parentsOfCombine.addAll(parentsOfProcess);
            }
        }
        //???????????????????????????????????????????????????????????????????????????
        for (Long objId : combineRelation.keySet()) {
            Set<Long> parentsOfCombine = objToParentsCombine.computeIfAbsent(objId,k->new HashSet<>());
            if(!reqRelation.containsKey(objId)){
                parentsOfCombine.addAll(combineRelation.get(objId));
            }
        }
        return objToParentsCombine;
    }

    /**
     * ??????????????????????????????base + diff???????????????
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
     * ?????????????????????
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
        // ?????????????????????????????????????????????????????????
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
     * ??????????????????????????????base + diff???????????????
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
         * ??????id???????????????map???key????????????????????????base????????????????????????????????????????????????????????????????????????diff???????????????base?????????
         */
        //????????????????????????????????????map
        for (EisObjAllRelationRelease releasedRelation : releasedRelations) {
            allObjIds.add(releasedRelation.getObjId());
            if(releasedRelation.getParentObjId() != null){
                allObjIds.add(releasedRelation.getParentObjId());
            }
            Set<Long> parentsOfCurrent = releasedParentsRelationsMap
                    .computeIfAbsent(releasedRelation.getObjId(),k -> new HashSet<>());

            parentsOfCurrent.add(releasedRelation.getParentObjId());

        }
        //???????????????????????????map?????????objId???????????????????????????
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
     * ??????????????????????????????/????????????
     * @param spmsOfCurrentObjInReqLineage ??????????????????
     * @param bridgeUpTerminalIdMapping ????????????????????????
     * @param parentTerminalLinageGraphMap ?????????????????????
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
            Long rootObjId = objIdsOfSpm.get(objIdsOfSpm.size() - 1); // ????????????objId
            List<Long> bridgeUpTerminalIds = bridgeUpTerminalIdMapping.get(rootObjId);
            if (CollectionUtils.isEmpty(bridgeUpTerminalIds)) {
                result.add(objIdsOfSpm);
                return;
            }
            // rootObjId?????????????????????????????????spm
            boolean bridgeResolved = false;
            for (Long bridgeUpTerminalId : bridgeUpTerminalIds) {
                LinageGraph bridgeLinageGraph = parentTerminalLinageGraphMap.get(bridgeUpTerminalId);
                if (bridgeLinageGraph == null) {
                    continue;
                }
                // ?????????????????????spm??????
                List<List<Long>> bridgeSpmsOfParentSpace = getObjIdSpms(bridgeLinageGraph, rootObjId);
                if (CollectionUtils.isNotEmpty(bridgeSpmsOfParentSpace)) {
                    bridgeSpmsOfParentSpace.forEach(bridgeSpmOfParentSpace -> {
                        // ???????????????bridge spm
                        List<Long> fullSpm = new ArrayList<>(objIdsOfSpm);
                        if (bridgeSpmOfParentSpace.size() > 1) {
                            fullSpm.addAll(bridgeSpmOfParentSpace.subList(1, bridgeSpmOfParentSpace.size()));
                        }
                        result.add(fullSpm);
                    });
                    // ?????????????????????????????????????????????????????????
                    bridgeResolved = true;
                    break;
                }
            }
            // ??????????????????
            if (!bridgeResolved) {
                throw new CommonException("???????????????????????????????????????");
            }
        });
        return result;
    }

    /**
     * @param bridgeTerminalIds ????????????????????????id??????
     * @return key terminalId???value???parentAppId?????????terminalId?????????????????????
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
            // ??????????????????????????????????????????????????????????????????????????????????????????????????????
            LinageGraph linageGraph = genReleasedLinageGraphNoBridge(parentTerminalRelease.getId());
            if (linageGraph == null) {
                return;
            }
            result.put(bridgeTerminalId, linageGraph);
        });
        return result;
    }

    /**
     * ??????????????????????????????base + diff???????????????
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
         * ??????id???????????????map???key????????????????????????base????????????????????????????????????????????????????????????????????????diff???????????????base?????????
         */
        //????????????????????????????????????map
        for (EisObjAllRelationRelease releasedRelation : releasedRelations) {
            allObjIds.add(releasedRelation.getObjId());
            if(releasedRelation.getParentObjId() != null){
                allObjIds.add(releasedRelation.getParentObjId());
            }
            Set<Long> parentsOfCurrent = releasedParentsRelationsMap
                    .computeIfAbsent(releasedRelation.getObjId(),k -> new HashSet<>());
            parentsOfCurrent.add(releasedRelation.getParentObjId());

        }
        //???????????????????????????map?????????objId???????????????????????????
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
     * ??????????????????????????????base + diff???????????????
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
           throw new CommonException("baseReleaseId ??????" + baseReleaseId);
        }
        LinageGraph base = doGenReleasedLinageGraph(baseReleaseId);
        return mergeBridges(base, EtContext.get(ContextConstant.APP_ID), terminalReleaseHistory.getTerminalId(), baseReleaseId);
    }

    /**
     * ?????????????????????????????????
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
            throw new CommonException("baseReleaseId ??????" + baseReleaseId);
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
        // ??????????????????????????????????????????????????????????????????
        noParentsObjIds.forEach(noParentsObjId -> {
            LinageGraph graphOfBridge = graphOfEachBridge.get(noParentsObjId);
            if (graphOfBridge != null) {
                doMerge(noParentsObjId, base, graphOfBridge);
            }
        });
        // ??????childrenMap
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
     * ???????????????????????????
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
                // ?????????
                childrenMap.putIfAbsent(objId, Sets.newHashSet());
            } else {
                // ????????????
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
     * ??????????????????????????????????????????????????????
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
     * ????????????spm????????????????????????
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
     * ???????????????????????????????????????????????????????????????????????????objId??????????????????
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
//     * ????????????ObjId?????????????????????
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


    // ==============================================??????????????????============================================
    /**
     * ??????????????????????????????????????????????????????????????????????????????
     *
     * @param baseReleasedId ??????????????????
     * @param terminalId ???ID
     * @param reqPoolId ?????????ID
     * @return
     */
    public TotalLineageGraph getTotalLineageGraph(
            Long baseReleasedId, Long terminalId, Long reqPoolId){
        // 1. ????????????
        List<EisObjAllRelationRelease> releasedRelationList = objRelationReleaseService
                .getAllRelationsByReleaseId(baseReleasedId);
        List<EisReqObjRelation> reqPoolRelationList = reqObjRelationService
                .getByReqIdAndTerminalId(reqPoolId, terminalId);

        // 2. ?????????????????????
        // ????????????
        Map<Long, Set<Long>> baseParentsMap = Maps.newHashMap();
        for (EisObjAllRelationRelease baseRelation : releasedRelationList) {
            Long currObjId = baseRelation.getObjId();
            Long currParentObjId = baseRelation.getParentObjId();
            Set<Long> parentObjIdSet = baseParentsMap
                    .computeIfAbsent(currObjId, k -> Sets.newHashSet());

            parentObjIdSet.add(currParentObjId);

        }
        // ????????????
        Map<Long, Set<Long>> diffParentsMap = Maps.newHashMap();
        for (EisReqObjRelation diffRelation : reqPoolRelationList) {
            Long currObjId = diffRelation.getObjId();
            Long currParentObjId = diffRelation.getParentObjId();
            Set<Long> parentObjIdSet = diffParentsMap
                    .computeIfAbsent(currObjId, k -> Sets.newHashSet());

            parentObjIdSet.add(currParentObjId);

        }
        // ???????????????baseParentsMap, ???????????????????????????, ?????????????????????
        Map<Long, Set<Long>> addedRelationMap = Maps.newHashMap();  // ?????????????????????
        Map<Long, Set<Long>> deletedRelationMap = Maps.newHashMap();  // ?????????????????????
        for (Long currObjId : diffParentsMap.keySet()) {
            Set<Long> diffParentObjIdSet = diffParentsMap.get(currObjId);
            if(baseParentsMap.containsKey(currObjId)){
                // ??????????????????
                Set<Long> baseParentObjIdSet = baseParentsMap.get(currObjId);
                // ????????????
                Set<Long> addedParentObjIdSet = diffParentObjIdSet.stream()
                        .filter(k -> !baseParentObjIdSet.contains(k))
                        .collect(Collectors.toSet());
                baseParentObjIdSet.addAll(addedParentObjIdSet); // ????????????
                addedParentObjIdSet.forEach(parentId -> {
                    Set<Long> childObjIdSet = addedRelationMap
                            .computeIfAbsent(parentId, k -> Sets.newHashSet());
                    childObjIdSet.add(currObjId);
                });

                // ????????????
                Set<Long> deletedParentObjIdSet = baseParentObjIdSet.stream()
                        .filter(k -> !diffParentObjIdSet.contains(k))
                        .collect(Collectors.toSet());
                deletedParentObjIdSet.forEach(parentId -> {
                    Set<Long> childObjIdSet = deletedRelationMap
                            .computeIfAbsent(parentId, k -> Sets.newHashSet());
                    childObjIdSet.add(currObjId);
                });

            }else {
                // ??????????????????
                diffParentObjIdSet.forEach(parentId -> {
                    Set<Long> childrenObjIdSet = addedRelationMap
                            .computeIfAbsent(parentId, k -> Sets.newHashSet());
                    childrenObjIdSet.add(currObjId);
                });
                baseParentsMap.put(currObjId, diffParentObjIdSet);  // ????????????
            }
        }

        // ????????????????????????
        Map<Long, Set<Long>> childrenMap = Maps.newHashMap();
        Set<Long> allObjIds = Sets.newHashSet();
        for (Long currObjId : baseParentsMap.keySet()) {
            Set<Long> currParentIdSet = baseParentsMap.get(currObjId);
            allObjIds.add(currObjId);
            allObjIds.addAll(currParentIdSet);
            if(CollectionUtils.isEmpty(currParentIdSet)){
                // ?????????
                childrenMap.putIfAbsent(currObjId, Sets.newHashSet());
            }else {
                // ????????????
                currParentIdSet.forEach(parentId -> {
                    Set<Long> currChildrenIdSet = childrenMap
                            .computeIfAbsent(parentId, k -> Sets.newHashSet());
                    currChildrenIdSet.add(currObjId);
                });
            }
        }

        // 3. ????????????
        TotalLineageGraph totalLineageGraph = new TotalLineageGraph();
        totalLineageGraph.setAllObjIds(allObjIds);
        totalLineageGraph.setChildrenMap(childrenMap);
        totalLineageGraph.setParentsMap(baseParentsMap);
        totalLineageGraph.setAddedRelationMap(addedRelationMap);
        totalLineageGraph.setDeletedRelationMap(deletedRelationMap);

        // ?????????????????????????????????
        allObjIds.addAll(addedRelationMap.keySet());
        // ?????????????????????????????????base???
        return (TotalLineageGraph) mergeBridges(totalLineageGraph, EtContext.get(ContextConstant.APP_ID), terminalId, baseReleasedId);
    }

    /**
     * ????????????????????????????????????????????????
     *
     * @param graph ????????????
     * @param objId ??????ID
     * @return
     */
    public List<Node> getObjLineageTree(LinageGraph graph, Long objId){
        Preconditions.checkArgument(null != graph, "?????????????????????");
        Preconditions.checkArgument(null != objId, "??????ID????????????");
        // 1. ??????????????????????????????????????????
        Map<Long, Set<Long>> parentsMap = graph.getParentsMap();
        Map<Long, Set<Long>> childrenMap = graph.getChildrenMap();
        List<Node> superTrees = this.buildSuperTree(parentsMap, Sets.newHashSet(objId));
        Node subTree = this.buildSubTree(childrenMap, objId);

        // 2. ??????????????? (???subTree???????????????superTree????????????????????????)
        Queue<Node> nodeQueue = Lists.newLinkedList();
        superTrees.forEach(nodeQueue::offer);
        while (!nodeQueue.isEmpty()) {
            Node currNode = nodeQueue.poll();
            // ??????????????????????????????
            List<Node> currChildNodes = currNode.getChildren();
            if (CollectionUtils.isEmpty(currChildNodes)) {
                // ??????????????????????????????????????????
                currNode.setChildren(subTree.getChildren());
            } else {
                // ??????????????????????????????????????????????????????
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
     * ?????????????????????????????????????????????????????????????????????????????????
     *
     * @param graph ?????????
     * @param selectedObjIds
     * @return
     */
    public List<Node> getObjTree(LinageGraph graph, Set<Long> selectedObjIds){
        Preconditions.checkArgument(null != graph, "?????????????????????");
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(selectedObjIds), "???????????????????????????");

        Map<Long, Set<Long>> parentsMap = graph.getParentsMap();
        return this.buildSuperTree(parentsMap, selectedObjIds);
    }

    /**
     * ???SPM?????????
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
        // ????????????????????????????????????
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
            // ?????????????????????????????????
            if (!currentMatch) {
                return;
            }
            // ?????????????????????????????????????????????
            boolean match = matchAndFilterChildrenBySpm(rootNode, spmObjIds4Children);
            if (match) {
                result.add(rootNode);
            }
        });
        return result;
    }

    /**
     * ?????????????????????????????????????????????SPM?????????
     */
    private boolean matchAndFilterChildrenBySpm(Node parentNode, List<Long> spmObjIds) {
        // ??????????????????
        if (CollectionUtils.isEmpty(spmObjIds)) {
            return true;
        }

        List<Node> children = parentNode.getChildren();
        if (CollectionUtils.isEmpty(children)) {
            // spmObjIds??????????????????spm??????????????????????????????????????????????????????????????????SPM
            return false;
        }

        // ????????????children
        Pair<Long, List<Long>> p = extractLast(spmObjIds);
        Long currentObjIdToMatch = p.getKey();
        List<Long> spmObjIds4Children = p.getValue();

        List<Node> matchedChildren = new ArrayList<>();
        for (Node child : children) {
            // ??????????????????SPM
            boolean childMatch = child.getObjId().equals(currentObjIdToMatch);
            if (!childMatch) {
                continue;
            }
            // ???????????????????????????
            childMatch = matchAndFilterChildrenBySpm(child, spmObjIds4Children);
            if (childMatch) {
                matchedChildren.add(child);
            }
        }

        // ???????????????????????????
        parentNode.setChildren(matchedChildren);
        return CollectionUtils.isNotEmpty(matchedChildren);
    }

    /**
     * ???????????????????????????????????? + ???????????????
     */
    private Pair<Long, List<Long>> extractLast(List<Long> spmObjIds) {
        if (CollectionUtils.isEmpty(spmObjIds)) {
            throw new CommonException("");
        }
        return new Pair<>(spmObjIds.get(spmObjIds.size() - 1), new ArrayList<>(spmObjIds.subList(0, spmObjIds.size() - 1)));
    }

    /**
     * ???????????????oid?????????
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
            // ????????????
            if (objectBasic.getName().contains(objSearch) || objectBasic.getOid().contains(objSearch)) {
                rootNode.setChildren(new ArrayList<>(0));
                result.add(rootNode);
                continue;
            }

            // ????????????
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
            // ???????????????
            if (objectBasic.getName().contains(objSearch) || objectBasic.getOid().contains(objSearch)) {
                child.setChildren(new ArrayList<>(0));
                matchedChildren.add(child);
                continue;
            }
            // ??????????????????????????????????????????????????????????????????????????????????????????
            boolean childMatch = matchAndFilterChildrenByObject(child, objSearch, objectBasicMap);
            if (childMatch) {
                matchedChildren.add(child);
            }
        }
        parentNode.setChildren(matchedChildren);
        return CollectionUtils.isNotEmpty(matchedChildren);
    }

    /**
     * ???????????????????????????????????????????????? ???????????????????????????
     *
     * @param childrenMap
     * @param objId
     * @return
     */
    private Node buildSubTree(Map<Long, Set<Long>> childrenMap, Long objId){
        Preconditions.checkArgument(null != childrenMap, "??????????????????????????????");
        Preconditions.checkArgument(null != objId, "??????ID???????????????");
        // ??????????????????????????????????????? <objId, Set<ChildId>>
        if(loopCheck(childrenMap)){
            throw new ObjException("???????????????????????????????????????????????????");
        }
        // ???????????? ?????????????????????
        List<Node> rootNodes =  this.buildTreeCore(childrenMap, Sets.newHashSet(objId));
        return rootNodes.get(0);
    }

    /**
     * ????????????????????????????????????????????????????????????????????????????????????
     *
     * @param parentsMap ???????????????
     * @param selectedObjId ?????????????????????
     * @return
     */
    private List<Node> buildSuperTree(Map<Long, Set<Long>> parentsMap, Set<Long> selectedObjId){
        Preconditions.checkArgument(MapUtils.isNotEmpty(parentsMap), "?????????????????????????????????");
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(selectedObjId), "??????ID?????????????????????");
        // 1. ????????????
        if(loopCheck(parentsMap)){
            throw new ObjException("????????????????????????????????????????????????");
        }
        // 2. ???????????? ???????????? ?????????????????????????????????/???????????????????????????childrenMap: <objId, [childId, ...]>
        Map<Long, Set<Long>> childrenMap = Maps.newHashMap();
        Queue<Long> queue = Lists.newLinkedList();
        selectedObjId.forEach(queue::offer);
        while(!queue.isEmpty()){
            // ??????????????????
            Long currObjId = queue.poll();
            if(currObjId == null){
                log.info("xxxx");
            }
            Set<Long> currParentIds = parentsMap.getOrDefault(currObjId, Sets.newHashSet());
            if(CollectionUtils.isEmpty(currParentIds) || (currParentIds.size() == 1 && currParentIds.contains(null))){
                // ?????????
                childrenMap.putIfAbsent(currObjId, Sets.newHashSet());
            }else {
                currParentIds.forEach(currParentId -> {
                    if(currParentId != null) {
                        Set<Long> childIds = childrenMap
                                .computeIfAbsent(currParentId, k -> Sets.newHashSet());
                        childIds.add(currObjId);
                    }else {
                        // ?????????
                        childrenMap.putIfAbsent(currObjId, Sets.newHashSet());
                    }
                });
            }
            // ???????????????
            currParentIds.forEach(currParentId -> {
                if(currParentId != null) {
                    queue.offer(currParentId);
                }
            });
        }

        // 3. ???????????????
        Set<Long> fromSet = Sets.newHashSet();
        Set<Long> toSet = Sets.newHashSet();
        for (Long from : childrenMap.keySet()) {
            fromSet.add(from);
            toSet.addAll(childrenMap.get(from));
        }
        Set<Long> rootNodeIds = fromSet.stream()
                .filter(k -> !toSet.contains(k))
                .collect(Collectors.toSet());

        // 4. ???????????????????????????
        List<Node> rootNodes = this.buildTreeCore(childrenMap, rootNodeIds);

        return rootNodes;
    }

    /**
     * ??????????????? ???????????? ?????????
     *
     * @param childrenMap ?????????????????????????????? <objId, [childId, ...]>
     * @param rootNodeIds ?????????ID
     * @return
     */
    private List<Node> buildTreeCore(Map<Long, Set<Long>> childrenMap, Set<Long> rootNodeIds){
        // ????????????
        Preconditions.checkArgument(null != childrenMap, "????????????????????????");
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(rootNodeIds), "???????????????????????????");

        // ??????????????????????????????????????????????????????
        List<Node> rootNodes = rootNodeIds.stream().map(k -> new Node(k)).collect(Collectors.toList());
        Queue<Node> nodeQueue = Lists.newLinkedList();
        rootNodes.forEach(nodeQueue::offer);
        while (!nodeQueue.isEmpty()){
            // ??????????????????
            Node currNode = nodeQueue.poll();
            Long currNodeId = currNode.getObjId();
            // ????????????????????????????????????
            Set<Long> childrenIds = childrenMap.getOrDefault(currNodeId, Sets.newHashSet());
            List<Node> childrenNodes = childrenIds.stream()
                    .map(k -> new Node(k))
                    .collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(childrenNodes)) {
                currNode.setChildren(childrenNodes);
            }
            // ?????????????????????
            childrenNodes.forEach(nodeQueue::offer);
        }
        return rootNodes;
    }

    /**
     * ????????????
     *
     * @param relationMap ??????????????????????????????<objId, Set<childIds>>???<objId, Set<parentIds>>
     * @return
     */
    public Boolean loopCheck(Map<Long, Set<Long>> relationMap){
        // 1. ???<objId, Set<childIds>>??????????????????????????????<objId, Set<parentIds>>???????????????????????????
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

        // 2. ????????????????????????????????????????????????????????????
        Queue<List<Long>> pathQueue = Lists.newLinkedList();
        rootIds.forEach(k -> pathQueue.offer(Lists.newArrayList(k)));
        while(!pathQueue.isEmpty()){
            List<Long> currPath = pathQueue.poll();
            Long currPathLastNodeId = currPath.get(currPath.size()-1);
            Set<Long> nextNodeIdSet = relationMap.getOrDefault(currPathLastNodeId, Sets.newHashSet());
            nextNodeIdSet = nextNodeIdSet.stream().filter(Objects::nonNull).collect(Collectors.toSet());
            for (Long nextNodeId : nextNodeIdSet) {
                // ?????????????????????????????????
                if(currPath.contains(nextNodeId)){
                    log.warn("???????????????objId={}????????????{}???", nextNodeId, currPath);
                    return true;
                }
                List<Long> nextPath = Lists.newArrayList(currPath);
                nextPath.add(nextNodeId);
                // ???????????????
                pathQueue.offer(nextPath);
            }
        }
        return false;
    }

    //============???????????????===============
    /**
     * ????????????spm????????????????????????
     * @param spmAsObjIdLists
     * @return
     */
    public List<NodeOfTestTree> buildTreeForTest(List<List<Long>> spmAsObjIdLists,Map<Long,ObjectBasic> objMap){
        Map<Long,Set<Long>> parentsMap = new HashMap<>();
        Map<Long,Set<Long>> childrenMap = new HashMap<>();
        Set<Long> allObjIds = new HashSet<>();
        for (List<Long> spmAsObjIdList : spmAsObjIdLists) {
            allObjIds.addAll(spmAsObjIdList);
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
     * ????????????????????????????????????ID
     *
     * @param rootNodes ???????????????
     * @return
     */
    public Set<Long> getObjIdsFromTree(List<Node> rootNodes){
        if(CollectionUtils.isEmpty(rootNodes)){
            return Sets.newHashSet();
        }

        Set<Long> objIds = Sets.newHashSet();
        // ????????????
        Queue<Node> nodeQueue = Lists.newLinkedList();
        rootNodes.forEach(nodeQueue::offer);
        while (!nodeQueue.isEmpty()){
            // ??????????????????
            Node currNode = nodeQueue.poll();
            objIds.add(currNode.getObjId());
            // ???????????????
            List<Node> currChildren = currNode.getChildren();
            if(CollectionUtils.isNotEmpty(currChildren)){
                currChildren.forEach(nodeQueue::offer);
            }
        }

        return objIds;
    }

}
