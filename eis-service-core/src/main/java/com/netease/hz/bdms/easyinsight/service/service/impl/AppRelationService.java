package com.netease.hz.bdms.easyinsight.service.service.impl;

import com.netease.hz.bdms.easyinsight.common.dto.obj.ObjectExtDTO;
import com.netease.hz.bdms.easyinsight.common.dto.terminal.TerminalSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.enums.ObjSpecialTypeEnum;
import com.netease.hz.bdms.easyinsight.common.enums.TerminalTypeEnum;
import com.netease.hz.bdms.easyinsight.common.exception.CommonException;
import com.netease.hz.bdms.easyinsight.common.util.JsonUtils;
import com.netease.hz.bdms.easyinsight.dao.AppRelationMapper;
import com.netease.hz.bdms.easyinsight.dao.ReleaseRelationMapper;
import com.netease.hz.bdms.easyinsight.dao.model.*;
import com.netease.hz.bdms.easyinsight.service.service.ObjectBasicService;
import com.netease.hz.bdms.easyinsight.service.service.TerminalService;
import com.netease.hz.bdms.easyinsight.service.service.obj.AllTrackerReleaseService;
import com.netease.hz.bdms.easyinsight.service.service.requirement.ReqPoolRelBaseService;
import com.netease.hz.bdms.easyinsight.service.service.terminalrelease.TerminalReleaseService;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 跨空间打通服务
 */
@Slf4j
@Service
public class AppRelationService {

    @Resource
    private AppRelationMapper appRelationMapper;

    @Resource
    private ReleaseRelationMapper releaseRelationMapper;

    @Resource
    private TerminalService terminalService;

    @Resource
    private ObjectBasicService objectBasicService;

    @Resource
    private TerminalReleaseService terminalReleaseService;

    @Resource
    private AllTrackerReleaseService trackerReleaseService;

    @Resource
    private ReqPoolRelBaseService reqPoolRelBaseService;

    public void addReleaseRelation(Long releaseId, Long parentReleaseId) {
        if (releaseId == null || parentReleaseId == null) {
            return;
        }
        releaseRelationMapper.insert(new ReleaseRelation().setReleaseId(releaseId).setParentReleaseId(parentReleaseId));
    }

    public void removeReleaseRelation(Long releaseId, Long parentReleaseId) {
        if (releaseId == null || parentReleaseId == null) {
            return;
        }
        releaseRelationMapper.delete(new ReleaseRelation().setReleaseId(releaseId).setParentReleaseId(parentReleaseId));
    }

    public List<Long> getParentReleaseIds(Long releaseId) {
        List<ReleaseRelation> relations = releaseRelationMapper.listByReleaseId(releaseId);
        return relations == null ? new ArrayList<>(0) : relations.stream().map(ReleaseRelation::getParentReleaseId).collect(Collectors.toList());
    }

    /**
     * 获取SPM中外部空间对象
     * @param spmsOfObjIdList
     * @param objMap 需要包括所有桥梁对象
     * @return
     */
    public static Set<Long> getOuterSpaceObjIds(List<List<Long>> spmsOfObjIdList, Map<Long,ObjectBasic> objMap) {
        // 所有SPM相关的桥梁ID
        Set<Long> allObjIds = spmsOfObjIdList.stream().flatMap(l -> l.stream()).collect(Collectors.toSet());
        Set<Long> allBridgeIds = new HashSet<>();
        allObjIds.forEach(objId -> {
            ObjectBasic relObj = objMap.get(objId);
            if (relObj != null && ObjSpecialTypeEnum.BRIDGE.getName().equals(relObj.getSpecialType())) {
                allBridgeIds.add(objId);
            }
        });

        Set<Long> allOtherAppObjIds = new HashSet<>();
        spmsOfObjIdList.forEach(objIdsOfSpm -> {
            int bridgeIdx = -1;
            for (int i = 0; i < objIdsOfSpm.size(); i++) {
                Long objId = objIdsOfSpm.get(i);
                if (allBridgeIds.contains(objId)) {
                    bridgeIdx = i;
                    break;
                }
            }
            // 如果SPM中桥梁位下挂载了其他对象（不在最后面），那么这个桥梁一定是挂载了其他端。此时把上面的标记为外部空间对象
            if (bridgeIdx > 0) {    // == 0 说明在叶子，不属于挂载情况，而是本端叶子是桥梁，不需要处理
                for (int i = bridgeIdx; i < objIdsOfSpm.size(); i++) {
                    Long objId = objIdsOfSpm.get(i);
                    allOtherAppObjIds.add(objId);
                }
            }
        });
        return allOtherAppObjIds;
    }

    /**
     * 获取桥梁下挂子空间ID
     * @param parentTerminalId 父空间端ID
     * @param bridgeObject 桥梁对象
     * @return
     */
    public Pair<Long, Long> getBridgeChildrenTerminalId(Long parentTerminalId, ObjectBasic bridgeObject) {
        if (bridgeObject == null) {
            return null;
        }
        if (bridgeObject.getExt() == null) {
            return null;
        }
        ObjectExtDTO objectExtDTO = JsonUtils.parseObject(bridgeObject.getExt(), ObjectExtDTO.class);
        if (objectExtDTO == null || objectExtDTO.getSubAppId() == null) {
            return null;
        }
        Long subAppId = objectExtDTO.getSubAppId();
        // 挂载同名端模式
        if (objectExtDTO.getSubTerminalId() == null || objectExtDTO.getSubTerminalId().equals(0L)) {
            try {
                return new Pair<>(subAppId, getSameNameTerminalId(parentTerminalId, objectExtDTO.getSubAppId()));
            } catch (Exception e) {
                log.warn("挂载同名端模式,子端不存在. parentTerminalId={}, objectExtDTO.getSubAppId()={}", parentTerminalId, objectExtDTO.getSubAppId(), e);
                return null;
            }
        }
        // 挂载指定端模式
        TerminalSimpleDTO parentTerminal = terminalService.getById(parentTerminalId);
        TerminalSimpleDTO subTerminal = terminalService.getById(objectExtDTO.getSubTerminalId());
        if (parentTerminal == null || subTerminal == null) {
            return null;
        }
        if (TerminalTypeEnum.of(parentTerminal.getName()) == TerminalTypeEnum.APP && TerminalTypeEnum.of(subTerminal.getName()) == TerminalTypeEnum.NON_APP) {
            // APP端可下挂非APP端
            return new Pair<>(subAppId, subTerminal.getId());
        }
        return null;
    }

    /**
     * 是否为跨空间同名端桥梁
     */
    public static boolean isCrossAppSameTerminalBridge(Long currentAppId, ObjectBasic bridgeObject) {
        if (bridgeObject.getExt() == null) {
            return false;
        }
        ObjectExtDTO objectExtDTO = JsonUtils.parseObject(bridgeObject.getExt(), ObjectExtDTO.class);
        if (objectExtDTO == null) {
            return false;
        }
        // 当前app必须是桥梁设置的子app
        if (!currentAppId.equals(objectExtDTO.getSubAppId())) {
            return false;
        }
        // 挂载同名端模式
        return objectExtDTO.getSubTerminalId() == null || objectExtDTO.getSubTerminalId().equals(0L);
    }

    /**
     * 是否为挂载指定端桥梁
     */
    public static boolean isSpecifiedTerminalBridge(Long currentAppId, Long currentTerminalId, ObjectBasic bridgeObject) {
        if (bridgeObject.getExt() == null) {
            return false;
        }
        ObjectExtDTO objectExtDTO = JsonUtils.parseObject(bridgeObject.getExt(), ObjectExtDTO.class);
        if (objectExtDTO == null) {
            return false;
        }
        // 当前app必须是桥梁设置的子app
        if (!currentAppId.equals(objectExtDTO.getSubAppId())) {
            return false;
        }
        // 挂载同名端模式
        if (objectExtDTO.getSubTerminalId() == null || objectExtDTO.getSubTerminalId().equals(0L)) {
            return false;
        }
        return objectExtDTO.getSubTerminalId().equals(currentTerminalId);
    }

    /**
     * 获取当前空间指定端下，每个端可使用的桥梁对象，父空间必须已上线，其中的桥梁才可以被使用
     * @param currentAppId
     * @return
     */
    public List<ObjectBasic> getParentBridgeCandidatesByReqPoolId(Long currentAppId, Long currentTerminalId, Long reqPoolId) {
        EisReqPoolRelBaseRelease currentUse = reqPoolRelBaseService.getCurrentUse(reqPoolId, currentTerminalId);
        if (currentUse == null) {
            return new ArrayList<>(0);
        }
        if (currentUse.getBaseReleaseId() == null || currentUse.getBaseReleaseId().equals(0L)) {
            return new ArrayList<>(0);
        }
        return getParentBridgeCandidatesByReleaseId(currentAppId, currentTerminalId, currentUse.getBaseReleaseId());
    }

    /**
     * 获取父端id -> 父端版本发布记录
     * @param terminalReleaseId
     * @return
     */
    public Map<Long, EisTerminalReleaseHistory> getParentTerminalReleaseMap(Long terminalReleaseId) {
        // 父空间必须已上线
        List<ReleaseRelation> releaseRelations = releaseRelationMapper.listByReleaseId(terminalReleaseId);
        if (CollectionUtils.isEmpty(releaseRelations)) {
            return new HashMap<>();
        }
        Set<Long> parentReleaseIds = releaseRelations.stream().map(ReleaseRelation::getParentReleaseId).collect(Collectors.toSet());
        List<EisTerminalReleaseHistory> parentReleases = terminalReleaseService.getByIds(parentReleaseIds);
        if (CollectionUtils.isEmpty(parentReleases)) {
            return new HashMap<>();
        }
        return parentReleases.stream().collect(Collectors.toMap(EisTerminalReleaseHistory::getTerminalId, o -> o,
                (v1, v2) -> v1.getCreateTime().getTime() > v2.getCreateTime().getTime() ? v1 : v2));
    }

    /**
     * 获取当前空间指定端下，每个端可使用的桥梁对象，父空间必须已上线，其中的桥梁才可以被使用
     * @param currentAppId
     * @return
     */
    public List<ObjectBasic> getParentBridgeCandidatesByReleaseId(Long currentAppId, Long currentTerminalId, Long terminalReleaseId) {
        if (currentAppId == null) {
            return new ArrayList<>(0);
        }

        Map<Long, EisTerminalReleaseHistory> parentTerminalReleaseMap = getParentTerminalReleaseMap(terminalReleaseId);
        if (MapUtils.isEmpty(parentTerminalReleaseMap)) {
            return new ArrayList<>(0);
        }

        List<ObjectBasic> allBridgeObjs = new ArrayList<>();
//        TerminalSimpleDTO currentTerminal = terminalService.getById(currentTerminalId);
        // 过滤出可挂载本端的bridge
        List<ObjectBasic> all = objectBasicService.getBySpecialType(ObjSpecialTypeEnum.BRIDGE.getName());
        all.forEach(o -> {
            // 挂载同名端模式
            if (isCrossAppSameTerminalBridge(currentAppId, o)) {
                allBridgeObjs.add(o);
                return;
            }
            // 挂载指定端模式
            if (isSpecifiedTerminalBridge(currentAppId, currentTerminalId, o)) {
                allBridgeObjs.add(o);
            }
        });

        //

        // 找出已上线的
        Map<Long, List<ObjectBasic>> allBridgeObjsGroupingByAppId = allBridgeObjs.stream().collect(Collectors.groupingBy(ObjectBasic::getAppId));
        Map<Long, ObjectBasic> bridgeMap = allBridgeObjs.stream().collect(Collectors.toMap(ObjectBasic::getId, o -> o, (oldV, newV) -> oldV));

        List<ObjectBasic> canUserBridges = new ArrayList<>();
        allBridgeObjsGroupingByAppId.forEach((bridgeAppId, bridges) -> {
            if (CollectionUtils.isEmpty(bridges)) {
                return;
            }
            List<TerminalSimpleDTO> bridgeTerminals = terminalService.getByAppId(bridgeAppId);
            if (CollectionUtils.isEmpty(bridgeTerminals)) {
                return;
            }
            bridgeTerminals.forEach(bridgeTerminal -> {
                EisTerminalReleaseHistory terminalReleaseHistory = parentTerminalReleaseMap.get(bridgeTerminal.getId());
                if (terminalReleaseHistory == null) {
                    return;
                }
                List<EisAllTrackerRelease> released = trackerReleaseService.searchByReleaseIdAndObjIds(terminalReleaseHistory.getId(), bridges.stream().map(ObjectBasic::getId).collect(Collectors.toList()));
                if (CollectionUtils.isEmpty(released)) {
                    return;
                }
                released.forEach(r -> {
                    ObjectBasic releasedBridge = bridgeMap.get(r.getObjId());
                    if (releasedBridge != null) {
                        canUserBridges.add(releasedBridge);
                    }
                });
            });
        });
        return canUserBridges;
    }

    /**
     * 获取对应空间name对应的terminal
     * @param terminalId
     * @param appId
     * @return
     */
    public Long getSameNameTerminalId(Long terminalId, Long appId) {
        TerminalSimpleDTO currentTerminal = terminalService.getById(terminalId);
        if (currentTerminal == null) {
            throw new CommonException("terminalId无效" + terminalId);
        }
        String currentName = currentTerminal.getName();
        List<TerminalSimpleDTO> parentTerminals = terminalService.getByAppId(appId);
        if (CollectionUtils.isEmpty(parentTerminals)) {
            throw new CommonException("appId无任何端" + appId);
        }
        List<TerminalSimpleDTO> filtered = parentTerminals.stream().filter(o -> StringUtils.equalsIgnoreCase(o.getName(), currentName)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(filtered)) {
            throw new CommonException("appId无任同名何端" + appId + " : " + currentName);
        }
        return filtered.get(0).getId();
    }

    /**
     * 获取父空间App类型对应的terminal
     * @param terminalId
     * @param parentAppId
     * @return
     */
    public List<Long> getParentAppTypeTerminalId(Long terminalId, Long parentAppId) {
        TerminalSimpleDTO currentTerminal = terminalService.getById(terminalId);
        if (currentTerminal == null) {
            throw new CommonException("terminalId无效" + terminalId);
        }
        List<TerminalSimpleDTO> parentTerminals = terminalService.getByAppId(parentAppId);
        if (CollectionUtils.isEmpty(parentTerminals)) {
            throw new CommonException("parentAppId无任何端" + parentAppId);
        }
        return parentTerminals.stream()
                .filter(o -> TerminalTypeEnum.of(o.getName()) == TerminalTypeEnum.APP)
                .map(TerminalSimpleDTO::getId).
                        collect(Collectors.toList());
    }

    /**
     * 获取每个桥梁向上桥接哪些端
     * @return
     */
    public Map<Long, List<Long>> getBridgeUpTerminalIdMapping(List<ObjectBasic> bridges, Long currentAppId, Long currentTerminalId) {
        if (CollectionUtils.isEmpty(bridges)) {
            return new HashMap<>();
        }
        Map<Long, List<Long>> result = new HashMap<>();
        bridges.forEach(b -> {
            // 跨app同端挂载
            boolean isCrossAppSameTerminalBridge = AppRelationService.isCrossAppSameTerminalBridge(currentAppId, b);
            if (isCrossAppSameTerminalBridge) {
                Long parentTerminalId = getSameNameTerminalId(currentTerminalId, b.getAppId());
                if (parentTerminalId == null) {
                    return;
                }
                result.put(b.getId(), Collections.singletonList(parentTerminalId));
                return;
            }
            // 指定端挂载
            boolean isSpecifiedTerminalBridge = AppRelationService.isSpecifiedTerminalBridge(currentAppId, currentTerminalId, b);
            if (isSpecifiedTerminalBridge) {
                List<Long> parentTerminalIds = getParentAppTypeTerminalId(currentTerminalId, b.getAppId());
                if (CollectionUtils.isNotEmpty(parentTerminalIds)) {
                    result.put(b.getId(), parentTerminalIds);
                }
                return;
            }
        });
        return result;
    }

    /**
     * 给出所有父子关系
     */
    public Set<Long> getParentAppIds(Long appId) {
        if (appId == null) {
            return new HashSet<>(0);
        }
        List<AppRelation> all = appRelationMapper.listAll();
        if (CollectionUtils.isEmpty(all)) {
            return new HashSet<>(0);
        }
        return all.stream().filter(r -> appId.equals(r.getAppId())).map(AppRelation::getParentAppId).collect(Collectors.toSet());
    }

    /**
     * 确保父子关系
     * 如果一个appId如果是父亲了，他就不能做任何儿子；
     * 如果一个appId是儿子了，他就不能做任何父亲
     */
    public void ensureRelationExist(Long parentAppId, Long appId) {
        List<AppRelation> all = appRelationMapper.listAll();
        checkValid(parentAppId, appId, all);
        List<AppRelation> exist = all.stream().filter(o -> parentAppId.equals(o.getParentAppId()) && appId.equals(o.getAppId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(exist)) {
            // 已存在 无需新增
            return;
        }
        appRelationMapper.insert(new AppRelation().setAppId(appId).setParentAppId(parentAppId));
    }

    private void checkValid(Long parentAppId, Long appId, List<AppRelation> all) {
        if (CollectionUtils.isEmpty(all)) {
            return;
        }
        if (parentAppId == null || appId == null) {
            throw new CommonException("父子空间参数为空");
        }
        if (parentAppId.equals(appId)) {
            throw new CommonException("无法把自己设置为自己的子空间");
        }
        Set<Long> parents = all.stream().map(AppRelation::getParentAppId).collect(Collectors.toSet());
        Set<Long> children = all.stream().map(AppRelation::getAppId).collect(Collectors.toSet());

        if (children.contains(parentAppId)) {
            throw new CommonException("空间" + parentAppId + "已作为子空间，无法设置为父空间");
        }

        if (parents.contains(appId)) {
            throw new CommonException("空间" + parentAppId + "已作为父空间，无法设置为子空间");
        }
    }
}
