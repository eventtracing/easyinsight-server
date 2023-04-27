package com.netease.hz.bdms.easyinsight.service.helper;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.netease.eis.adapters.CacheAdapter;
import com.netease.hz.bdms.easyinsight.common.bo.lineage.LinageGraph;
import com.netease.hz.bdms.easyinsight.common.bo.lineage.Node;
import com.netease.hz.bdms.easyinsight.common.dto.spm.ArtificialSpmInfoDTO;
import com.netease.hz.bdms.easyinsight.common.dto.spm.SpmInfoDTO;
import com.netease.hz.bdms.easyinsight.common.dto.spm.SpmMapInfoDTO;
import com.netease.hz.bdms.easyinsight.common.dto.terminal.TerminalSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.enums.ObjTypeEnum;
import com.netease.hz.bdms.easyinsight.common.enums.ProcessStatusEnum;
import com.netease.hz.bdms.easyinsight.common.enums.SpmMapStatusEnum;
import com.netease.hz.bdms.easyinsight.common.enums.SpmSourceTypeEnum;
import com.netease.hz.bdms.easyinsight.common.obj.ThreeTuple;
import com.netease.hz.bdms.easyinsight.common.obj.TwoTuple;
import com.netease.hz.bdms.easyinsight.common.util.JsonUtils;
import com.netease.hz.bdms.easyinsight.dao.model.EisReqPoolSpm;
import com.netease.hz.bdms.easyinsight.dao.model.EisTerminalReleaseHistory;
import com.netease.hz.bdms.easyinsight.dao.model.ObjectBasic;
import com.netease.hz.bdms.easyinsight.service.service.*;
import com.netease.hz.bdms.easyinsight.service.service.terminalrelease.TerminalReleaseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author: xumengqiang
 * @date: 2021/11/2 17:13
 */
@Slf4j
@Component
public class SpmMapHelper {
    @Autowired
    SpmMapInfoService spmMapInfoService;

    @Autowired
    SpmInfoService spmInfoService;

    @Autowired
    ArtificialSpmInfoService artificialSpmInfoService;

    @Autowired
    ObjectBasicService objBasicService;

    @Autowired
    SpmTagService spmTagService;

    @Autowired
    SpmMapItemService spmMapItemService;

    @Autowired
    ReqPoolSpmHelper reqPoolSpmHelper;

    @Autowired
    ObjTagService objTagService;

    @Autowired
    TagService tagService;

    @Autowired
    LineageHelper lineageHelper;

    @Autowired
    TerminalReleaseService terminalReleaseService;

    @Autowired
    TerminalService terminalService;

    @Resource
    private CacheAdapter cacheAdapter;

    @Transactional(rollbackFor = Throwable.class)
    public void updateSpmMapRelationInfoTable(List<SpmMapInfoDTO> spmMapInfoDTOList) {
        // 参数检查
        if(CollectionUtils.isEmpty(spmMapInfoDTOList)) return;
        // 删除表`eis_spm_map_relation_info`中全部数据
        spmMapInfoService.deleteAll();
        // 批量写入
        spmMapInfoService.create(spmMapInfoDTOList);
    }

    /**
     * 依据表`eis_req_pool_spm`，同步更新`eis_spm_info`
     */
    @Transactional(rollbackFor = Throwable.class)
    public void syncSpmMapInfo(){
        // 1. 获取表`eis_req_pool_spm`中在不同产品所有端下的SPM信息
        Map<ThreeTuple<String, Long, Long>, EisReqPoolSpm> spmToReqPoolSpmMap =
                reqPoolSpmHelper.getLatestSpmMap();
        Set<ThreeTuple<String, Long, Long>> spmAppIdTupleSet = spmToReqPoolSpmMap.keySet();
        // 2. 获取表`eis_spm_info`中不同产品下 所有SPM信息
        List<SpmInfoDTO> spmInfoDTOS = spmInfoService.listAll();
        List<String> currentSpmAndTerminals = spmInfoDTOS.stream().map(dto -> dto.getSpm()+dto.getTerminalId()).collect(Collectors.toList());
        Map<ThreeTuple<String, Long, Long>, SpmInfoDTO> spmAppIdTupleToInfoMap =
                spmInfoDTOS.stream()
                        .collect(Collectors.toMap(k -> new ThreeTuple<>(k.getSpm(), k.getAppId(), k.getTerminalId()),
                                Function.identity()));
        // 3. 计算表`eis_spm_info`需新增的spm
        List<ThreeTuple<String, Long, Long>> spmAppIdTupleInsertList = spmAppIdTupleSet.stream()
                .filter(k -> !spmAppIdTupleToInfoMap.containsKey(k))
                .distinct()
                .collect(Collectors.toList());
        // 4. 计算表`eis_spm_map_info`需删除的spm
        List<ThreeTuple<String, Long, Long>> spmAppIdTupleRemoveList = spmAppIdTupleToInfoMap.keySet().stream()
                .filter(k -> {
                    SpmInfoDTO spmInfoDTO = spmAppIdTupleToInfoMap.get(k);
                    return spmInfoDTO.getSource().equals(SpmSourceTypeEnum.SCHEDULE.getStatus()) && !spmAppIdTupleSet.contains(k);
                })
                .collect(Collectors.toList());
        // 5. 查询表`eis_obj`构建 <oid, name> 映射
        ObjectBasic objQuery = new ObjectBasic();
        List<ObjectBasic> objectBasicList = objBasicService.search(objQuery);
        Map<TwoTuple<String, Long>, String> oidToNameMap = objectBasicList.stream()
                .collect(Collectors.toMap(k -> new TwoTuple<>(k.getOid(), k.getAppId()), k -> k.getName()));
        // 6. 分别依据spmRemoveList以及spmInsertList进行删除及插入操作
        List<SpmInfoDTO> spmInfoDTOList = Lists.newArrayList();
        for (ThreeTuple<String, Long, Long> spmAppIdTuple: spmAppIdTupleInsertList) {
            String spm = spmAppIdTuple.getFirst();
            Long appId = spmAppIdTuple.getSecond();
            Long terminalId = spmAppIdTuple.getThree();
            EisReqPoolSpm currReqPoolSpm = spmToReqPoolSpmMap.get(spmAppIdTuple);
            SpmInfoDTO spmInfoDTO = new SpmInfoDTO();
            // 构建SPM中文名称
            String name = Arrays.stream(spm.split("\\|"))
                    .map(oid -> oidToNameMap.getOrDefault(new TwoTuple<>(oid, appId), "error"))
                    .collect(Collectors.joining("|"));
            // 数据填充
            spmInfoDTO.setSpm(spm)
                    .setTerminalId(terminalId)
                    .setName(name)
                    .setStatus(SpmMapStatusEnum.UNCONFIRMED.getStatus())
                    .setAppId(appId)
                    .setCreateName(currReqPoolSpm.getCreateName())
                    .setCreateEmail(currReqPoolSpm.getCreateEmail())
                    .setUpdateName(currReqPoolSpm.getUpdateName())
                    .setSource(SpmSourceTypeEnum.SCHEDULE.getStatus())
                    .setUpdateEmail(currReqPoolSpm.getUpdateEmail());
            // 加入列表
            spmInfoDTOList.add(spmInfoDTO);

        }

        // 7.查询根节点信息
        List<TerminalSimpleDTO> terminalSimpleDTOList = terminalService.getAll();
        List<Long> terminals = terminalSimpleDTOList.stream().map(TerminalSimpleDTO::getId).collect(Collectors.toList());
//        Map<TwoTuple<Long,Long>, ObjectBasic> popObjectMap = new HashMap<>();
        Map<TwoTuple<Long,Long>, ObjectBasic> rootObjectMap = new HashMap<>();
        for(Long terminalId : terminals) {
            EisTerminalReleaseHistory latestRelease = terminalReleaseService.getLatestRelease(terminalId);
            if(latestRelease == null) continue;
            Long baseReleaseId = latestRelease.getId();
            // 1. 获取全量血缘图
            LinageGraph graph = lineageHelper.genReleasedLinageGraph(baseReleaseId);
            Map<Long, Set<Long>> parentsMap = graph.getParentsMap();
            Set<Long> objIds = Sets.newHashSet();
            for (Long objId : parentsMap.keySet()) {
                objIds.add(objId);
                objIds.addAll(parentsMap.getOrDefault(objId, Sets.newHashSet()));
            }
//            // 2. 获取当前端下的浮层结点
//            List<ObjectBasic> objectBasicList = objBasicService.getByIds(objIds);
//            objectBasicList = objectBasicList.stream().filter(k -> ObjTypeEnum.POPOVER.getType().equals(k.getType())).collect(Collectors.toList());
//            Map<TwoTuple<Long,Long>, ObjectBasic> thisPopObjectMap = objectBasicList.stream()
//                    .filter(objectBasic -> objectBasic.getType().equals(ObjTypeEnum.PAGE.getType()))
//                    .collect(Collectors.toMap(k -> new TwoTuple<>(k.getId(), terminalId), Function.identity()));
//            popObjectMap.putAll(thisPopObjectMap);
            // 3. 获取当前端下的根页面结点
            List<Node> rootNodes = lineageHelper.getObjTree(graph, objIds);
            List<Long> rootObjIds = rootNodes.stream().map(Node::getObjId).collect(Collectors.toList());
            List<ObjectBasic> rootObjList = objBasicService.getByIds(rootObjIds);
            Map<TwoTuple<Long,Long>, ObjectBasic> thisRootObjectMap = rootObjList.stream()
                    .filter(objectBasic -> objectBasic.getType().equals(ObjTypeEnum.PAGE.getType()))
                    .collect(Collectors.toMap(k -> new TwoTuple<>(k.getId(), terminalId), Function.identity()));
            rootObjectMap.putAll(thisRootObjectMap);
        }

        spmInfoDTOS.addAll(spmInfoDTOList);
        if(CollectionUtils.isNotEmpty(spmAppIdTupleRemoveList)) {
            List<SpmInfoDTO> spmInTaskDTORemoveList = spmAppIdTupleRemoveList.stream()
                    .map(spmAppIdTupleToInfoMap::get)
                    .collect(Collectors.toList());
            spmInfoDTOS.removeAll(spmInTaskDTORemoveList);
        }

        //浮层衍生的spm
        List<SpmInfoDTO> addSpmMapInfoDTOList = Lists.newArrayList();
        List<SpmInfoDTO> deleteSpm = Lists.newArrayList();
        for(SpmInfoDTO spmInfoDTO : spmInfoDTOList){
            String currSpm = spmInfoDTO.getSpm();
            if(StringUtils.isBlank(currSpm)) continue;
            String[] currOidArr = currSpm.split("\\|");
            if(CollectionUtils.isEmpty(Arrays.asList(currOidArr))) continue;

            String currOid = currOidArr[currOidArr.length -1];
            ObjectBasic objectBasic = objBasicService.getByOid(spmInfoDTO.getAppId(), currOid);
            if(objectBasic == null || !objectBasic.getType().equals(ObjTypeEnum.POPOVER.getType())) continue;

            //拼上所有根页面
            Map<String, String> newSpms = new HashMap<>();
            for(TwoTuple<Long,Long> key : rootObjectMap.keySet()){
                if(!key.getSecond().equals(spmInfoDTO.getTerminalId())) continue;
                ObjectBasic objectBasic1 = rootObjectMap.get(key);
                String newSpm = currSpm + "|" + objectBasic1.getOid();
                String newSpmName = spmInfoDTO.getName() + "|" + objectBasic1.getName();
                if(!currentSpmAndTerminals.contains(newSpm + spmInfoDTO.getTerminalId())) {
                    newSpms.put(newSpm, newSpmName);
                }
            }

            if(newSpms.size() > NumberUtils.LONG_ZERO){
                deleteSpm.add(spmInfoDTO);
                for(String newSpm : newSpms.keySet()){
                    String spmInfoJson = JsonUtils.toJson(spmInfoDTO);
                    SpmInfoDTO addSpmInfoDTO = JsonUtils.parseObject(spmInfoJson, SpmInfoDTO.class);
                    if(addSpmInfoDTO == null) continue;
                    addSpmInfoDTO.setId(null);
                    addSpmInfoDTO.setSpm(newSpm);
                    addSpmInfoDTO.setName(newSpms.get(newSpm));
                    addSpmInfoDTO.setSource(SpmSourceTypeEnum.POPVOLE.getStatus());
                    addSpmMapInfoDTOList.add(addSpmInfoDTO);
                }
            }
        }

        spmInfoDTOList.addAll(addSpmMapInfoDTOList);

        // 插入记录
        List<Long> spmInfoIds = spmInfoService.create(spmInfoDTOList);
        List<ArtificialSpmInfoDTO> artificialSpmInfoDTOS = new ArrayList<>();
        int x = 0;
        for(SpmInfoDTO spmInfoDTO : spmInfoDTOList){
            ArtificialSpmInfoDTO artificialSpmInfoDTO = new ArtificialSpmInfoDTO();
            if(spmInfoIds.size() > x){
                artificialSpmInfoDTO.setId(spmInfoIds.get(x));
            }
            x++;
            if(!spmInfoDTO.getSource().equals(SpmSourceTypeEnum.POPVOLE.getStatus())) continue;
//            artificialSpmInfoDTO.setId(spmInfoDTO.getId());
            artificialSpmInfoDTO.setName(spmInfoDTO.getName());
            artificialSpmInfoDTO.setSpm(spmInfoDTO.getSpm());
            artificialSpmInfoDTO.setStatus(spmInfoDTO.getStatus());
            artificialSpmInfoDTO.setSpmStatus(ProcessStatusEnum.START.getState());
            artificialSpmInfoDTO.setSpmTag("{}");
            artificialSpmInfoDTO.setSpmOldList("");
            artificialSpmInfoDTO.setNote("");
            artificialSpmInfoDTO.setVersion("");
            artificialSpmInfoDTO.setSource(SpmSourceTypeEnum.POPVOLE.getStatus());
            artificialSpmInfoDTO.setAppId(spmInfoDTO.getAppId());
            artificialSpmInfoDTO.setTerminalId(spmInfoDTO.getTerminalId());
            artificialSpmInfoDTO.setCreateTime(new Timestamp(System.currentTimeMillis()));
            artificialSpmInfoDTO.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            artificialSpmInfoDTOS.add(artificialSpmInfoDTO);
        }
        artificialSpmInfoService.create(artificialSpmInfoDTOS);

        // 删除记录
        if(CollectionUtils.isNotEmpty(spmAppIdTupleRemoveList)) {
            List<SpmInfoDTO> spmInTaskDTORemoveList = spmAppIdTupleRemoveList.stream()
                    .map(k -> spmAppIdTupleToInfoMap.get(k))
                    .collect(Collectors.toList());
            List<Long> spmIdRemovedList = spmInTaskDTORemoveList.stream()
                    .map(SpmInfoDTO::getId)
                    .collect(Collectors.toList());
            spmInfoService.deleteByIds(spmIdRemovedList);
            spmTagService.deleteBySpmId(spmIdRemovedList);
            spmMapItemService.deleteBySpmId(spmIdRemovedList);
        }

        cacheAdapter.setWithExpireTime("spmListHasRelease", "1", 600);

    }

    /**
     * 依据表`eis_spm_info`，同步更新浮层spm
     */
    @Transactional(rollbackFor = Throwable.class)
    public void transform(){

        Long[] terminals = new Long[]{10L,11L,12L,16L,17L,18L,22L,23L,24L};
        Map<TwoTuple<Long,Long>, ObjectBasic> popObjectMap = new HashMap<>();
        Map<TwoTuple<Long,Long>, ObjectBasic> rootObjectMap = new HashMap<>();

        for(Long terminalId : terminals) {
            EisTerminalReleaseHistory latestRelease = terminalReleaseService.getLatestRelease(terminalId);
            Long baseReleaseId = latestRelease.getId();
            // 1. 获取全量血缘图
            LinageGraph graph = lineageHelper.genReleasedLinageGraph(baseReleaseId);
            Map<Long, Set<Long>> parentsMap = graph.getParentsMap();
            Set<Long> objIds = Sets.newHashSet();
            for (Long objId : parentsMap.keySet()) {
                objIds.add(objId);
                objIds.addAll(parentsMap.getOrDefault(objId, Sets.newHashSet()));
            }

            // 2. 获取当前端下的浮层结点
            List<ObjectBasic> objectBasicList = objBasicService.getByIds(objIds);
            objectBasicList = objectBasicList.stream().filter(k -> ObjTypeEnum.POPOVER.getType().equals(k.getType())).collect(Collectors.toList());
            Map<TwoTuple<Long,Long>, ObjectBasic> thisPopObjectMap = objectBasicList.stream()
                    .filter(objectBasic -> objectBasic.getType().equals(ObjTypeEnum.PAGE.getType()))
                    .collect(Collectors.toMap(k -> new TwoTuple<>(k.getId(), terminalId), Function.identity()));
            popObjectMap.putAll(thisPopObjectMap);

            // 3. 获取当前端下的根页面结点
            List<Node> rootNodes = lineageHelper.getObjTree(graph, objIds);
            List<Long> rootObjIds = rootNodes.stream().map(Node::getObjId).collect(Collectors.toList());
            List<ObjectBasic> rootObjList = objBasicService.getByIds(rootObjIds);
            Map<TwoTuple<Long,Long>, ObjectBasic> thisRootObjectMap = rootObjList.stream()
                    .filter(objectBasic -> objectBasic.getType().equals(ObjTypeEnum.PAGE.getType()))
                    .collect(Collectors.toMap(k -> new TwoTuple<>(k.getId(), terminalId), Function.identity()));
            rootObjectMap.putAll(thisRootObjectMap);
        }

        // 4. 获取表`eis_spm_info`中不同产品下 所有SPM信息
        List<SpmInfoDTO> spmInfoDTOS = spmInfoService.listAll();

        //浮层衍生的spm
        List<SpmInfoDTO> addSpmMapInfoDTOList = Lists.newArrayList();
        List<String> deleteSpm = Lists.newArrayList();
        for(SpmInfoDTO spmInfoDTO : spmInfoDTOS){
            String currSpm = spmInfoDTO.getSpm();
            if(StringUtils.isBlank(currSpm)) continue;
            String[] currOidArr = currSpm.split(",");
            if(CollectionUtils.isEmpty(Arrays.asList(currOidArr))) continue;

            String currOid = currOidArr[currOidArr.length -1];
            ObjectBasic objectBasic = objBasicService.getByOid(spmInfoDTO.getAppId(), currOid);
            if(objectBasic == null || !objectBasic.getType().equals(ObjTypeEnum.POPOVER.getType())) continue;

            //拼上所有根页面
            Map<String, String> newSpms = new HashMap<>();
            for(TwoTuple<Long,Long> key : rootObjectMap.keySet()){
                if(!key.getSecond().equals(spmInfoDTO.getTerminalId())) continue;
                ObjectBasic objectBasic1 = rootObjectMap.get(key);
                String newSpm = currSpm + "|" + objectBasic1.getOid();
                String newSpmName = spmInfoDTO.getSpm() + "|" + objectBasic1.getName();
                newSpms.put(newSpm, newSpmName);
            }

            if(newSpms.size() > NumberUtils.LONG_ZERO){
                deleteSpm.add(spmInfoDTO.getSpm());
                for(String newSpm : newSpms.keySet()){
                    String spmInfoJson = JsonUtils.toJson(spmInfoDTO);
                    SpmInfoDTO addSpmInfoDTO = JsonUtils.parseObject(spmInfoJson, SpmInfoDTO.class);
                    if(addSpmInfoDTO == null) continue;
                    addSpmInfoDTO.setSpm(newSpm);
                    addSpmInfoDTO.setName(newSpms.get(newSpm));
                    addSpmMapInfoDTOList.add(addSpmInfoDTO);
                }
            }

        }

        spmInfoDTOS = spmInfoDTOS.stream().filter(info -> !deleteSpm.contains(info.getSpm())).collect(Collectors.toList());
        spmInfoDTOS.addAll(addSpmMapInfoDTOList);


    }

}
