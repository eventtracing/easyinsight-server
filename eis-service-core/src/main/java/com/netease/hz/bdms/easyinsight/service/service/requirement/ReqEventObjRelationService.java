package com.netease.hz.bdms.easyinsight.service.service.requirement;

import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.dao.EisEventObjRelationMapper;
import com.netease.hz.bdms.easyinsight.dao.EisReqPoolEventMapper;
import com.netease.hz.bdms.easyinsight.dao.model.EisEventObjRelation;
import com.netease.hz.bdms.easyinsight.dao.model.EisReqPoolEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class ReqEventObjRelationService {

    @Autowired
    EisEventObjRelationMapper eisEventObjRelationMapper;

//    public EisReqPoolEvent getById(Long id){
//        return reqPoolEventMapper.selectByPrimaryKey(id);
//    }
//
//    public List<EisReqPoolEvent> getBatchByIds(Set<Long> ids){
//        if (CollectionUtils.isEmpty(ids)) {
//            return new ArrayList<>(0);
//        }
//        return Optional.ofNullable(reqPoolEventMapper.selectBatchByIds(ids)).orElse(new ArrayList<>());
//    }

    public List<EisEventObjRelation> getByEventEntityIds(Set<Long> entityIds){
        return Optional.ofNullable(eisEventObjRelationMapper.selectByEventEntityIds(entityIds)).orElse(new ArrayList<>());
    }

    public List<EisEventObjRelation> getByEventEntityId(Long entityId){
        return Optional.ofNullable(eisEventObjRelationMapper.selectByEventEntityId(entityId)).orElse(new ArrayList<>());
    }

    public void insertBatch(List<EisEventObjRelation> entitys){
        // 公共信息
        UserDTO currUser = EtContext.get(ContextConstant.USER);
        entitys.forEach(entity -> {
            entity.setCreateName(currUser.getUserName());
            entity.setCreateEmail(currUser.getEmail());
            entity.setUpdateName(currUser.getUserName());
            entity.setUpdateEmail(currUser.getEmail());
            entity.setCreateTime(new Date());
            entity.setUpdateTime(new Date());
            entity.setCreateTime(new Date());
            entity.setUpdateTime(new Date());
        });
        eisEventObjRelationMapper.insertBatch(entitys);
    }

    public void deleteByEntityId(Long entityId){
        eisEventObjRelationMapper.deleteByEntityId(entityId);
    }

}
