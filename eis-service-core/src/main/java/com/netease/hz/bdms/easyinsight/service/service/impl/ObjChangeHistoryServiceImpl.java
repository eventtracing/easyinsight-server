package com.netease.hz.bdms.easyinsight.service.service.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.exception.CommonException;
import com.netease.hz.bdms.easyinsight.dao.EisObjChangeHistoryMapper;
import com.netease.hz.bdms.easyinsight.dao.model.EisObjChangeHistory;
import com.netease.hz.bdms.easyinsight.service.service.ObjChangeHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author: xumengqiang
 * @date: 2021/12/9 16:48
 */
@Slf4j
@Service
public class ObjChangeHistoryServiceImpl implements ObjChangeHistoryService {
    @Autowired
    private EisObjChangeHistoryMapper objChangeHistoryMapper;

    @Override
    public Long insert(EisObjChangeHistory objChangeHistory) {

        // 信息填充
        UserDTO currentUser = EtContext.get(ContextConstant.USER);
        if(null != currentUser) {
            objChangeHistory.setCreateName(currentUser.getUserName());
            objChangeHistory.setCreateEmail(currentUser.getEmail());
            objChangeHistory.setUpdateName(currentUser.getUserName());
            objChangeHistory.setUpdateEmail(currentUser.getEmail());
        }
        // 插入记录
        objChangeHistoryMapper.insert(objChangeHistory);

        return objChangeHistory.getId();
    }

    @Override
    public void insertBatch(List<EisObjChangeHistory> list) {
        if(!CollectionUtils.isEmpty(list)){
            objChangeHistoryMapper.insertBatch(list);
        }
    }

    @Override
    public List<EisObjChangeHistory> getByReqPoolId(Long reqPoolId){
        EisObjChangeHistory query = new EisObjChangeHistory();
        query.setReqPoolId(reqPoolId);
        List<EisObjChangeHistory> objChangeHistoryList= objChangeHistoryMapper.select(query);
        return Optional.ofNullable(objChangeHistoryList).orElse(Lists.newArrayList());
    }

    @Override
    public List<EisObjChangeHistory> getByConflictStatus(Long reqPoolId, String conflictStatus) {
        if (StringUtils.isEmpty(conflictStatus)) {
            throw new CommonException("conflictStatus is empty");
        }
        EisObjChangeHistory query = new EisObjChangeHistory();
        query.setReqPoolId(reqPoolId);
        query.setConflictStatus(conflictStatus);
        List<EisObjChangeHistory> objChangeHistoryList = objChangeHistoryMapper.select(query);
        return Optional.ofNullable(objChangeHistoryList).orElse(Lists.newArrayList());
    }

    @Override
    public List<EisObjChangeHistory> getByConflictStatus(Set<Long> reqPoolIds, String conflictStatus) {
        if (StringUtils.isEmpty(conflictStatus)) {
            throw new CommonException("conflictStatus is empty");
        }
        if (CollectionUtils.isEmpty(reqPoolIds)) {
            return new ArrayList<>(0);
        }
        List<EisObjChangeHistory> objChangeHistoryList = objChangeHistoryMapper.selectByReqPoolIdsAndConflictStatus(reqPoolIds, conflictStatus);
        return Optional.ofNullable(objChangeHistoryList).orElse(Lists.newArrayList());
    }

    @Override
    public List<EisObjChangeHistory> getAllByConflictStatus(String conflictStatus) {
        if (StringUtils.isEmpty(conflictStatus)) {
            throw new CommonException("conflictStatus is empty");
        }
        List<EisObjChangeHistory> objChangeHistoryList = objChangeHistoryMapper.selectByConflictStatus(conflictStatus);
        return Optional.ofNullable(objChangeHistoryList).orElse(Lists.newArrayList());
    }

    @Override
    public List<EisObjChangeHistory> getAllNotConsistency() {
        List<EisObjChangeHistory> objChangeHistoryList = objChangeHistoryMapper.selectAllNotConsistency();
        return Optional.ofNullable(objChangeHistoryList).orElse(Lists.newArrayList());
    }

    @Override
    public void updateConflictStatus(Long reqPoolId, Collection<Long> objIds, String conflictStatus) {
        if (reqPoolId == null) {
            return;
        }
        if (CollectionUtils.isEmpty(objIds)) {
            return;
        }
        objChangeHistoryMapper.updateConflictStatus(reqPoolId, new HashSet<>(objIds), conflictStatus);
    }

    public List<EisObjChangeHistory> getByObjAndReqPoolId(Long objId, Long reqId) {
        // 1. 参数检查
        Preconditions.checkArgument(null != objId, "对象ID不能为空");
        Preconditions.checkArgument(null != reqId, "需求ID不能为空");

        // 2. 查询
        EisObjChangeHistory query = new EisObjChangeHistory();
        query.setObjId(objId);
        query.setReqPoolId(reqId);
        List<EisObjChangeHistory> objChangeHistoryList= objChangeHistoryMapper.select(query);

        return Optional.ofNullable(objChangeHistoryList).orElse(Lists.newArrayList());
    }

    @Override
    public EisObjChangeHistory getById(Long historyId) {
        Preconditions.checkArgument(null != historyId, "对象变更历史ID不能为空");
        return objChangeHistoryMapper.selectByPrimaryKey(historyId);
    }

    @Override
    public List<EisObjChangeHistory> getByIds(Collection<Long> historyIds) {
        if(CollectionUtils.isNotEmpty(historyIds)){
            return objChangeHistoryMapper.selectByIds(historyIds);
        }
        return Lists.newArrayList();
    }

    @Override
    public Integer deleteByIds(Collection<Long> ids) {
        if(CollectionUtils.isNotEmpty(ids)){
            return objChangeHistoryMapper.deleteByIds(ids);
        }
        return 0;
    }

    @Override
    public void update(EisObjChangeHistory objChangeHistory) {
        if (null == objChangeHistory || null == objChangeHistory.getId()) {
            throw new CommonException("对象变更信息或主键不能为空");

        }
        UserDTO currentUser = EtContext.get(ContextConstant.USER);
        if (null != currentUser) {
            objChangeHistory.setUpdateName(currentUser.getUserName());
            objChangeHistory.setUpdateEmail(currentUser.getEmail());
        }
        objChangeHistoryMapper.updateByPrimaryKey(objChangeHistory);
    }

    @Override
    public Set<Long> getDistinctReqPoolIdByConflictStatus(String conflictStatus) {
        if (StringUtils.isBlank(conflictStatus)) {
            return new HashSet<>();
        }
        Set<Long> res = objChangeHistoryMapper.selectDistinctReqPoolIdByConflictStatus(conflictStatus);
        if (res == null) {
            res = new HashSet<>();
        }
        return res;
    }
}
