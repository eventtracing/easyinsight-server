package com.netease.hz.bdms.easyinsight.service.service.requirement;

import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.dao.EisReqPoolSpmMapper;
import com.netease.hz.bdms.easyinsight.dao.model.EisReqPoolSpm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * spm待指派项CRUD
 */
@Slf4j
@Service
public class ReqSpmPoolService {

    @Autowired
    EisReqPoolSpmMapper eisReqPoolSpmMapper;

    public EisReqPoolSpm getById(Long id){
        return eisReqPoolSpmMapper.selectByPrimaryKey(id);
    }

    public List<EisReqPoolSpm> getBatchByIds(Set<Long> ids){
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>(0);
        }
        return Optional.ofNullable(eisReqPoolSpmMapper.selectBatchByIds(ids)).orElse(new ArrayList<>());
    }

    public List<EisReqPoolSpm> getBatchByObjIds(Set<Long> ids){
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>(0);
        }
        return Optional.ofNullable(eisReqPoolSpmMapper.selectBatchByObjIds(ids)).orElse(new ArrayList<>());
    }

    public List<EisReqPoolSpm> search(EisReqPoolSpm query){
        return Optional.ofNullable(eisReqPoolSpmMapper.select(query)).orElse(new ArrayList<>());
    }

    public List<EisReqPoolSpm> searchLast(EisReqPoolSpm query){
        return Optional.ofNullable(eisReqPoolSpmMapper.queryLastSpm(query)).orElse(new ArrayList<>());
    }

    public void insert(EisReqPoolSpm entity){
        UserDTO currUser = EtContext.get(ContextConstant.USER);
        Long appId = EtContext.get(ContextConstant.APP_ID);
        if(null != currUser) {
            entity.setCreateName(currUser.getUserName());
            entity.setCreateEmail(currUser.getEmail());
            entity.setUpdateName(currUser.getUserName());
            entity.setUpdateEmail(currUser.getEmail());
        }
        if(null != appId){
            entity.setAppId(appId);
        }


        eisReqPoolSpmMapper.insert(entity);
    }

    public void insertBatch(List<EisReqPoolSpm> list){
        if(CollectionUtils.isNotEmpty(list)) {
            // 公共信息填充
            UserDTO currUser = EtContext.get(ContextConstant.USER);
            Long appId = EtContext.get(ContextConstant.APP_ID);
            list.forEach(reqPoolSpm -> {
                if (null != currUser) {
                    reqPoolSpm.setCreateName(currUser.getUserName());
                    reqPoolSpm.setCreateEmail(currUser.getEmail());
                    reqPoolSpm.setUpdateName(currUser.getUserName());
                    reqPoolSpm.setUpdateEmail(currUser.getEmail());
                }
                if (null != appId) {
                    reqPoolSpm.setAppId(appId);
                }
            });
            eisReqPoolSpmMapper.insertBatch(list);
        }
    }

    public void deleteById(Long id){
        eisReqPoolSpmMapper.deleteByPrimaryKey(id);
    }

    public void deleteByIds(Set<Long> ids){
        if(CollectionUtils.isEmpty(ids)){
            return;
        }
        eisReqPoolSpmMapper.deleteByIds(ids);
    }
}
