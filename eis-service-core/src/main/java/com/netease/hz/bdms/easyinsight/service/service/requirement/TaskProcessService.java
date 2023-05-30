package com.netease.hz.bdms.easyinsight.service.service.requirement;

import com.google.common.collect.Lists;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.enums.ProcessStatusEnum;
import com.netease.hz.bdms.easyinsight.common.enums.ReqPoolTypeEnum;
import com.netease.hz.bdms.easyinsight.common.enums.ReqTaskStatusEnum;
import com.netease.hz.bdms.easyinsight.common.vo.requirement.CancleSpmAssignVO;
import com.netease.hz.bdms.easyinsight.dao.EisTaskProcessMapper;
import com.netease.hz.bdms.easyinsight.dao.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TaskProcessService {

    @Resource
    private EisTaskProcessMapper eisTaskProcessMapper;

    @Resource
    private ReqTaskService reqTaskService;

    public EisTaskProcess getById(Long id){
        return eisTaskProcessMapper.selectByPrimaryKey(id);
    }

    public List<EisTaskProcess> search(EisTaskProcess query){
        return eisTaskProcessMapper.select(query);
    }

    public List<EisTaskProcess> getBatchByIds(Set<Long> ids){
        if(CollectionUtils.isNotEmpty(ids)){
            return eisTaskProcessMapper.selectBatchByIds(ids);
        }
        return Lists.newArrayList();
    }

    public List<EisTaskProcess> getBatchByTaskIds(Set<Long> taskIds){
        if(CollectionUtils.isNotEmpty(taskIds)){
            return eisTaskProcessMapper.selectBatchByTaskIds(taskIds);
        }
        return Lists.newArrayList();
    }

    public List<EisTaskProcess> getBatchBySpmBjObjIds(Set<String> spmByObjIds, Integer status){
        if(CollectionUtils.isNotEmpty(spmByObjIds)){
            return eisTaskProcessMapper.selectBatchBySpmBjObjIds(spmByObjIds, status);
        }
        return Lists.newArrayList();
    }

    public List<EisTaskProcess> getByReqPoolEntityIds(ReqPoolTypeEnum reqPoolTypeEnum, Set<Long> reqPoolIds){
        if (CollectionUtils.isEmpty(reqPoolIds) || reqPoolTypeEnum == null) {
            return Lists.newArrayList();
        }
        return eisTaskProcessMapper.getByReqPoolEntityIds(reqPoolTypeEnum.getReqPoolType(), reqPoolIds);
    }

    public void insert(EisTaskProcess entity){
        // 公共信息插入
        UserDTO currentUser = EtContext.get(ContextConstant.USER);
        if(null != currentUser){
            entity.setCreateName(currentUser.getUserName());
            entity.setCreateEmail(currentUser.getEmail());
            entity.setUpdateName(currentUser.getUserName());
            entity.setUpdateEmail(currentUser.getEmail());
        }
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        eisTaskProcessMapper.insert(entity);
    }

    public void insertBatch(List<EisTaskProcess> list){
        if(CollectionUtils.isEmpty(list)) {
            return;
        }
        // 公共信息插入
        UserDTO currentUser = EtContext.get(ContextConstant.USER);
        list.forEach(taskProcess -> {
            if(null != currentUser) {
                taskProcess.setCreateName(currentUser.getUserName());
                taskProcess.setCreateEmail(currentUser.getEmail());
                taskProcess.setUpdateName(currentUser.getUserName());
                taskProcess.setUpdateEmail(currentUser.getEmail());
            }
            taskProcess.setCreateTime(new Date());
            taskProcess.setUpdateTime(new Date());
        });
        eisTaskProcessMapper.insertBatch(list);
    }

    public void updateById(EisTaskProcess entity){
        // 公共信息插入
        UserDTO currentUser = EtContext.get(ContextConstant.USER);
        if(null != currentUser){
            entity.setUpdateName(currentUser.getUserName());
            entity.setUpdateEmail(currentUser.getEmail());
        }
        eisTaskProcessMapper.updateByPrimaryKeySelective(entity);
    }

    public void updateBatch(List<EisTaskProcess> list){
        if(CollectionUtils.isEmpty(list)) {
            return;
        }
        // 公共信息插入
        UserDTO currentUser = EtContext.get(ContextConstant.USER);
        if(null != currentUser){
            list.forEach(taskProcess -> {
                taskProcess.setUpdateName(currentUser.getUserName());
                taskProcess.setUpdateEmail(currentUser.getEmail());
            });
        }
        eisTaskProcessMapper.updateBatch(list);
    }

    public void updateVerifier(List<Long> processIds, String verifierName, String verifierEmail) {
        if (CollectionUtils.isEmpty(processIds)) {
            return;
        }
        // 公共信息插入
        UserDTO currentUser = EtContext.get(ContextConstant.USER);
        String updateName = null;
        String updateEmail = null;
        if (null != currentUser) {
            updateName = currentUser.getUserName();
            updateEmail = currentUser.getEmail();
        }
        eisTaskProcessMapper.updateVerifier(processIds, verifierName, verifierEmail, updateName, updateEmail);
    }

    public void updateOwner(List<Long> processIds, String ownerName, String ownerEmail) {
        if (CollectionUtils.isEmpty(processIds)) {
            return;
        }
        // 公共信息插入
        UserDTO currentUser = EtContext.get(ContextConstant.USER);
        String updateName = null;
        String updateEmail = null;
        if (null != currentUser) {
            updateName = currentUser.getUserName();
            updateEmail = currentUser.getEmail();
        }
        eisTaskProcessMapper.updateOwner(processIds, ownerName, ownerEmail, updateName, updateEmail);
    }

    public void deleteById(Long id){
        eisTaskProcessMapper.deleteByPrimaryKey(id);
    }

    public void deleteByIds(Set<Long> ids){
        if(CollectionUtils.isNotEmpty(ids)) {
            eisTaskProcessMapper.deleteByIds(ids);
        }
    }

    public void deleteByInfos(CancleSpmAssignVO cancleAssignVO){
        eisTaskProcessMapper.deleteByInfos(cancleAssignVO.getReqPoolId(), cancleAssignVO.getTaskId(), cancleAssignVO.getSpmByObjId());
    }

    public void deleteUnReleasedProcessesByReqPoolEntityIds(ReqPoolTypeEnum reqPoolTypeEnum, Set<Long> reqPoolEntityIds){
        if (reqPoolTypeEnum == null) {
            return;
        }
        if (CollectionUtils.isEmpty(reqPoolEntityIds)) {
            return;
        }
        List<EisTaskProcess> processes = eisTaskProcessMapper.getByReqPoolEntityIds(reqPoolTypeEnum.getReqPoolType(), reqPoolEntityIds);
        processes = processes.stream()
                .filter(e -> !e.getStatus().equals(ProcessStatusEnum.ONLINE.getState()))
                .collect(Collectors.toList());
        Set<Long> ids = processes.stream().map(e -> e.getId()).collect(Collectors.toSet());
        if(CollectionUtils.isEmpty(ids)){
            return;
        }
        eisTaskProcessMapper.deleteByIds(ids);
    }

    /**
     * @return taskIds
     */
    public Set<Long> updateUnReleasedProcessesByReqPoolEntityIds(ReqPoolTypeEnum reqPoolTypeEnum, List<EisReqPoolSpm> insertList, Set<Long> reqPoolEntityIds){
        if (reqPoolTypeEnum == null) {
            return new HashSet<>();
        }
        if (CollectionUtils.isEmpty(insertList)) {
            return new HashSet<>();
        }
        if (CollectionUtils.isEmpty(reqPoolEntityIds)) {
            return new HashSet<>();
        }

        List<EisTaskProcess> processes = eisTaskProcessMapper.getByReqPoolEntityIds(reqPoolTypeEnum.getReqPoolType(), reqPoolEntityIds);
        List<EisTaskProcess> insertProcess = new ArrayList<>();

        for(EisTaskProcess eisTaskProcess : processes) {

            EisReqTask eisReqTask = reqTaskService.getById(eisTaskProcess.getTaskId());

            for(EisReqPoolSpm eisReqPoolSpm : insertList) {
                if (eisReqTask.getTerminalId().equals(eisReqPoolSpm.getTerminalId()) && eisTaskProcess.getSpmByObjId().equals(eisReqPoolSpm.getSpmByObjId())) {
                    eisTaskProcess.setId(null);
                    eisTaskProcess.setStatus(ProcessStatusEnum.START.getState());
                    eisTaskProcess.setReqPoolEntityId(eisReqPoolSpm.getId());
                    insertProcess.add(eisTaskProcess);
                }
            }
        }

        if (CollectionUtils.isEmpty(insertProcess)) {
            return new HashSet<>();
        }

        eisTaskProcessMapper.insertBatch(insertProcess);
        for (EisTaskProcess eisTaskProcess : insertProcess) {
            Integer taskStatus = getTaskNewStatusByProcesses(eisTaskProcess.getTaskId());
            EisReqTask taskUpdateQuery = new EisReqTask();
            taskUpdateQuery.setId(eisTaskProcess.getTaskId());
            taskUpdateQuery.setStatus(taskStatus);
            reqTaskService.updateById(taskUpdateQuery);
        }

        return insertProcess.stream().map(EisTaskProcess::getTaskId).collect(Collectors.toSet());
    }

    public Integer getTaskNewStatusByProcesses(Long taskId){
        EisTaskProcess query = new EisTaskProcess();
        query.setTaskId(taskId);
        List<EisTaskProcess> processes = search(query);
        if(CollectionUtils.isEmpty(processes)){
            return ReqTaskStatusEnum.START.getState();
        }
        Integer minStatus = ReqTaskStatusEnum.ONLINE.getState();
        for (EisTaskProcess process : processes) {
            if(process.getStatus() < minStatus){
                minStatus = process.getStatus();
            }
        }
        return minStatus;
    }


    public Set<Long> getObjIdsOfRelease(Long reqPoolId){
        EisTaskProcess query = new EisTaskProcess();
        query.setReqPoolId(reqPoolId);
        query.setStatus(6);
        List<EisTaskProcess> processes = search(query);
        if(CollectionUtils.isEmpty(processes)){
            return new HashSet<>();
        }

        Set<Long> objIds = new HashSet<>();
        for (EisTaskProcess process : processes) {
            objIds.add(process.getObjId());
        }
        return objIds;
    }

}

