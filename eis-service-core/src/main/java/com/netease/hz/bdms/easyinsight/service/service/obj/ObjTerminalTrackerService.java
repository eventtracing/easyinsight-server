package com.netease.hz.bdms.easyinsight.service.service.obj;

import com.google.common.collect.Lists;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.exception.ServerException;
import com.netease.hz.bdms.easyinsight.dao.EisObjTerminalTrackerMapper;
import com.netease.hz.bdms.easyinsight.dao.model.EisObjTerminalTracker;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class ObjTerminalTrackerService {

    @Autowired
    public EisObjTerminalTrackerMapper objTerminalTrackerMapper;

    public List<EisObjTerminalTracker> search(EisObjTerminalTracker query){
        return Optional.ofNullable(objTerminalTrackerMapper.select(query)).orElse(new ArrayList<>());
    }

    public EisObjTerminalTracker getById(Long id){
        return objTerminalTrackerMapper.selectByPrimaryKey(id);
    }

    public List<EisObjTerminalTracker> getByIds(Set<Long> ids){
        if(CollectionUtils.isNotEmpty(ids)) {
            return objTerminalTrackerMapper.selectBatchByIds(ids);
        }
        return Lists.newArrayList();
    }

    public List<EisObjTerminalTracker> getBatchByChangeHistoryIds(Set<Long> changeHistoryIds){
        if(CollectionUtils.isNotEmpty(changeHistoryIds)){
            return objTerminalTrackerMapper.selectBatchByChangeHistoryIds(changeHistoryIds);
        }
        return Lists.newArrayList();
    }

    public void insert(EisObjTerminalTracker entity){
        // 公共信息填充
        Long appId = EtContext.get(ContextConstant.APP_ID);
        UserDTO currUser = EtContext.get(ContextConstant.USER);
        if(null != currUser) {
            entity.setCreateName(currUser.getUserName());
            entity.setCreateEmail(currUser.getEmail());
            entity.setUpdateName(currUser.getUserName());
            entity.setUpdateEmail(currUser.getEmail());
        }
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        if(null != appId){
            entity.setAppId(appId);
        }
        objTerminalTrackerMapper.insert(entity);
    }

    public void insertBatch(List<EisObjTerminalTracker> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        // 公共信息填充
        Long appId = EtContext.get(ContextConstant.APP_ID);
        UserDTO currUser = EtContext.get(ContextConstant.USER);
        if (null != currUser && null != appId){
            list.forEach(entity -> {
                entity.setCreateName(currUser.getUserName());
                entity.setCreateEmail(currUser.getEmail());
                entity.setUpdateName(currUser.getUserName());
                entity.setUpdateEmail(currUser.getEmail());
                entity.setAppId(appId);
                entity.setCreateTime(new Date());
                entity.setUpdateTime(new Date());
            });
        }
        objTerminalTrackerMapper.insertBatch(list);
    }

    public void deleteById(Long id){
        objTerminalTrackerMapper.deleteByPrimaryKey(id);
    }

    public void update(EisObjTerminalTracker entity){
        if(entity.getId() == null){
            log.error("更新tracker未使用主键");
            throw new ServerException("更新tracker失败");
        }
        // 公共信息填充
        UserDTO currUser = EtContext.get(ContextConstant.USER);
        if(null != currUser) {
            entity.setUpdateName(currUser.getUserName());
            entity.setUpdateEmail(currUser.getEmail());
        }
        objTerminalTrackerMapper.updateByPrimaryKeySelective(entity);
    }

    public void updateBatch(List<EisObjTerminalTracker> list){
        if(CollectionUtils.isEmpty(list)) {
            return;
        }
        // 公共信息填充
        UserDTO currUser = EtContext.get(ContextConstant.USER);
        if (null != currUser){
            list.forEach(entity -> {
                entity.setUpdateName(currUser.getUserName());
                entity.setUpdateEmail(currUser.getEmail());
            });
        }
        objTerminalTrackerMapper.updateBatch(list);
    }

    public void deleteByIds(Collection<Long> ids){
        // 参数检查
        if(CollectionUtils.isEmpty(ids)){
            return;
        }
        // 批量删除
        objTerminalTrackerMapper.deleteByIds(ids);
    }
}
