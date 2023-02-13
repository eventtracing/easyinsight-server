package com.netease.hz.bdms.easyinsight.service.helper;

import com.google.common.collect.Maps;
import com.netease.hz.bdms.easyinsight.common.enums.ProcessStatusEnum;
import com.netease.hz.bdms.easyinsight.common.enums.ReqPoolTypeEnum;
import com.netease.hz.bdms.easyinsight.common.obj.ThreeTuple;
import com.netease.hz.bdms.easyinsight.common.util.CommonUtil;
import com.netease.hz.bdms.easyinsight.dao.model.EisReqPoolSpm;
import com.netease.hz.bdms.easyinsight.dao.model.EisTaskProcess;
import com.netease.hz.bdms.easyinsight.dao.model.ObjectBasic;
import com.netease.hz.bdms.easyinsight.service.service.ObjectBasicService;
import com.netease.hz.bdms.easyinsight.dao.model.ObjMappings;
import com.netease.hz.bdms.easyinsight.service.service.requirement.ReqSpmPoolService;
import com.netease.hz.bdms.easyinsight.service.service.requirement.TaskProcessService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: xumengqiang
 * @date: 2021/12/31 14:12
 */

@Component
@Slf4j
public class ReqPoolSpmHelper {

    @Resource
    private ReqSpmPoolService reqSpmPoolService;

    @Resource
    private TaskProcessService taskProcessService;

    @Resource
    private ObjectBasicService objectBasicService;

    public Map<String, EisReqPoolSpm> getLatestSpmMap(Long appId, Long terminalId){

        // 1. 查询指定产品及端下 所有spm信息
        EisReqPoolSpm query = new EisReqPoolSpm();
        query.setAppId(appId);
        query.setTerminalId(terminalId);
        List<EisReqPoolSpm> reqPoolSpmList = reqSpmPoolService.search(query);

        Set<Long> allObjIds = CollectionUtils.isEmpty(reqPoolSpmList) ? new HashSet<>() :
                reqPoolSpmList.stream().flatMap(o -> CommonUtil.transSpmToObjIdList(o.getSpmByObjId()).stream()).collect(Collectors.toSet());

        // 2. 查询对象Oid
        ObjMappings objMappings = objectBasicService.getMapping(appId, allObjIds);
        Map<Long, String> objIdToOidMap = objMappings.getObjIdToOidMap();

        // 3. 对于同一spm, 保留最新需求下的
        Map<String, EisReqPoolSpm> reqPoolSpmMap = Maps.newHashMap();
        for (EisReqPoolSpm reqPoolSpm : reqPoolSpmList) {
            String spmByObjId = reqPoolSpm.getSpmByObjId();
            String spm = Arrays.stream(spmByObjId.split("\\|"))
                    .map(Long::valueOf)
                    .map(k -> objIdToOidMap.getOrDefault(k, "error"))
                    .collect(Collectors.joining("|"));
            if(reqPoolSpmMap.containsKey(spm)
                    && reqPoolSpmMap.get(spm).getUpdateTime().compareTo(reqPoolSpm.getUpdateTime()) > 0){
                continue;
            }
            reqPoolSpmMap.put(spm, reqPoolSpm);
        }
        return reqPoolSpmMap;
    }

    /**
     *
     * @return
     */
    public Map<ThreeTuple<String, Long, Long>, EisReqPoolSpm> getLatestSpmMap(){
        // 1. 查询所有产品下的全部spm信息
        EisReqPoolSpm query = new EisReqPoolSpm();
        List<EisReqPoolSpm> reqPoolSpmList = reqSpmPoolService.search(query);

        // 2. 查询对象Oid
        Set<Long> objIds = reqPoolSpmList.stream()
                .map(EisReqPoolSpm::getSpmByObjId)
                .flatMap(k -> Arrays.stream(k.split("\\|")))
                .map(Long::valueOf)
                .collect(Collectors.toSet());
        List<ObjectBasic> objectBasicList = objectBasicService.getByIds(objIds);
        Map<Long, String> objIdToOidMap = objectBasicList.stream()
                .collect(Collectors.toMap(ObjectBasic::getId, ObjectBasic::getOid));

        // 3.同一产品下的同一SPM, 保留最新需求下的
        Map<ThreeTuple<String, Long, Long>, EisReqPoolSpm> reqPoolSpmMap = Maps.newHashMap();
        for (EisReqPoolSpm reqPoolSpm : reqPoolSpmList) {
            Long appId = reqPoolSpm.getAppId();
            Long terminalId = reqPoolSpm.getTerminalId();
            String spmByObjId = reqPoolSpm.getSpmByObjId();
            String spm = Arrays.stream(spmByObjId.split("\\|"))
                    .map(Long::valueOf)
                    .map(k -> objIdToOidMap.getOrDefault(k, "error"))
                    .collect(Collectors.joining("|"));
            ThreeTuple<String, Long, Long> key = new ThreeTuple<>(spm, appId, terminalId);

            if(reqPoolSpmMap.containsKey(key)
                    && reqPoolSpmMap.get(key).getUpdateTime().compareTo(reqPoolSpm.getUpdateTime()) > 0){
                continue;
            }
            reqPoolSpmMap.put(key, reqPoolSpm);
        }
        return reqPoolSpmMap;

    }

    /**
     *
     * @param reqPoolSpmList
     * @return
     */
    private Map<String, EisReqPoolSpm> buildLatestSpmMap(List<EisReqPoolSpm> reqPoolSpmList){
        if (CollectionUtils.isEmpty(reqPoolSpmList)) {
            return new HashMap<>();
        }
        // 1. 查询对象Oid
        Set<Long> objIds = reqPoolSpmList.stream()
                .map(EisReqPoolSpm::getObjId).collect(Collectors.toSet());
        List<ObjectBasic> objectBasicList = objectBasicService.getByIds(objIds);
        Map<Long, String> objIdToOidMap = objectBasicList.stream()
                .collect(Collectors.toMap(ObjectBasic::getId, ObjectBasic::getOid));

        // 2. 对于同一spm, 保留最新需求下的
        Map<String, EisReqPoolSpm> reqPoolSpmMap = Maps.newHashMap();
        for (EisReqPoolSpm reqPoolSpm : reqPoolSpmList) {
            String spmByObjId = reqPoolSpm.getSpmByObjId();
            String spm = Arrays.stream(spmByObjId.split("\\|"))
                    .map(Long::valueOf)
                    .map(k -> objIdToOidMap.getOrDefault(k, "error"))
                    .collect(Collectors.joining("|"));
            if(reqPoolSpmMap.containsKey(spm)
                    && reqPoolSpmMap.get(spm).getUpdateTime().compareTo(reqPoolSpm.getUpdateTime()) > 0){
                continue;
            }
            reqPoolSpmMap.put(spm, reqPoolSpm);
        }
        return reqPoolSpmMap;
    }

    public Map<String, Boolean> getSpmIsDeployed(Long appId, Long terminalId){
        // 1.获取全部信息
        EisReqPoolSpm query = new EisReqPoolSpm();
        query.setAppId(appId);
        query.setTerminalId(terminalId);
        List<EisReqPoolSpm> reqPoolSpmList = reqSpmPoolService.search(query);

        Set<Long> objIds = reqPoolSpmList.stream()
                .map(EisReqPoolSpm::getSpmByObjId)
                .flatMap(k -> Arrays.stream(k.split("\\|")))
                .map(Long::valueOf)
                .collect(Collectors.toSet());
        List<ObjectBasic> objectBasicList = objectBasicService.getByIds(objIds);
        Map<Long, String> objIdToOidMap = objectBasicList.stream()
                .collect(Collectors.toMap(ObjectBasic::getId, ObjectBasic::getOid));

        Map<Long, Integer> reqPoolEntityIdToStatusMap = this.getReqPoolSpmStatusMap();

        // 2. 构建(spm, isDeployed)映射
        Map<String, Boolean> spmToIsDeployedMap = Maps.newHashMap();
        for (EisReqPoolSpm reqPoolSpm : reqPoolSpmList) {
            String spmByObjId = reqPoolSpm.getSpmByObjId();
            String spm = Arrays.stream(spmByObjId.split("\\|"))
                    .map(Long::valueOf)
                    .map(k -> objIdToOidMap.getOrDefault(k, "error"))
                    .collect(Collectors.joining("|"));
            Long reqPoolEntityId = reqPoolSpm.getId();
            Integer status = reqPoolEntityIdToStatusMap.getOrDefault(reqPoolEntityId, 0);
            if(ProcessStatusEnum.ONLINE.getState().equals(status)){
                spmToIsDeployedMap.put(spm, true);
            }else {
                spmToIsDeployedMap.putIfAbsent(spm, false);
            }
        }
        return spmToIsDeployedMap;
    }

    /**
     *
     * @return
     */
    public Map<Long, Integer> getReqPoolSpmStatusMap() {
        // 1.获取全部信息
        EisReqPoolSpm query = new EisReqPoolSpm();
        List<EisReqPoolSpm> reqPoolSpmList = reqSpmPoolService.search(query);

        Set<Long> reqPoolEntityIds = reqPoolSpmList.stream()
                .map(EisReqPoolSpm::getId).collect(Collectors.toSet());
        List<EisTaskProcess> taskProcessesList = taskProcessService.getByReqPoolEntityIds(ReqPoolTypeEnum.SPM_DEV, reqPoolEntityIds);
        Map<Long, Integer> reqPoolEntityIdToStatusMap = taskProcessesList.stream()
                .collect(Collectors.toMap(EisTaskProcess::getReqPoolEntityId, EisTaskProcess::getStatus, (oldV, newV) -> oldV));

        return reqPoolEntityIdToStatusMap;
    }

}
