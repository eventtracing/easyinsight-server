package com.netease.hz.bdms.easyinsight.service.service.obj;

import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.dao.EisReqObjRelationMapper;
import com.netease.hz.bdms.easyinsight.dao.model.EisReqObjRelation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

@Slf4j
@Service
public class ReqObjRelationService {

    @Autowired
    EisReqObjRelationMapper reqObjRelationMapper;

    public List<EisReqObjRelation> search(EisReqObjRelation query){
        return Optional.ofNullable(reqObjRelationMapper.select(query)).orElse(new ArrayList<>());
    }

    public List<EisReqObjRelation> getByReqIdAndTerminalId(Long reqId, Long terminalId){
        EisReqObjRelation query = new EisReqObjRelation();
        query.setReqPoolId(reqId);
        query.setTerminalId(terminalId);
        return search(query);
    }

    public EisReqObjRelation getById(Long id){
        return reqObjRelationMapper.selectByPrimaryKey(id);
    }

    public List<EisReqObjRelation> selectBatchByObjIds(Set<Long> objIds){
        return Optional.ofNullable(reqObjRelationMapper.selectBatchByObjIds(objIds)).orElse(new ArrayList<>());
    }

    public void insertBatch(List<EisReqObjRelation> list){
        if(!CollectionUtils.isEmpty(list)) {
            // 设置 创建人、最近更新人信息
            Long appId = EtContext.get(ContextConstant.APP_ID);
            UserDTO currUser = EtContext.get(ContextConstant.USER);
            String userName = currUser.getUserName();
            String userEmail = currUser.getEmail();
            list.forEach(reqObjRelation -> {
                reqObjRelation.setAppId(appId);
                reqObjRelation.setCreateName(userName);
                reqObjRelation.setCreateEmail(userEmail);
                reqObjRelation.setUpdateName(userName);
                reqObjRelation.setUpdateEmail(userEmail);
                reqObjRelation.setCreateTime(new Date());
                reqObjRelation.setUpdateTime(new Date());
            });

            reqObjRelationMapper.insertBatch(list);
        }
    }

    public void insert(EisReqObjRelation entity){
        // 基本信息设置
        Long appId = EtContext.get(ContextConstant.APP_ID);
        UserSimpleDTO currUser = EtContext.get(ContextConstant.USER);
        String userName = currUser.getUserName();
        String userEmail = currUser.getEmail();
        entity.setAppId(appId);
        entity.setCreateName(userName);
        entity.setCreateEmail(userEmail);
        entity.setUpdateName(userName);
        entity.setUpdateEmail(userEmail);
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        // 插入记录
        reqObjRelationMapper.insert(entity);
    }

    public void insert(Long objId,Long reqId,Long terminalId,Set<Long> parentObjIds){
        Long appId = EtContext.get(ContextConstant.APP_ID);
        UserSimpleDTO currUser = EtContext.get(ContextConstant.USER);
        String userName = currUser.getUserName();
        String userEmail = currUser.getEmail();
        if(CollectionUtils.isEmpty(parentObjIds)){
            EisReqObjRelation entity = new EisReqObjRelation();
            entity.setObjId(objId);
            entity.setReqPoolId(reqId);
            entity.setTerminalId(terminalId);
            entity.setAppId(appId);
            entity.setCreateName(userName);
            entity.setCreateEmail(userEmail);
            entity.setUpdateName(userName);
            entity.setUpdateEmail(userEmail);
            reqObjRelationMapper.insert(entity);
            return;
        }
        List<EisReqObjRelation> list = new ArrayList<>();
        for (Long parentObjId : parentObjIds) {
            EisReqObjRelation entity = new EisReqObjRelation();
            entity.setObjId(objId);
            entity.setReqPoolId(reqId);
            entity.setTerminalId(terminalId);
            entity.setParentObjId(parentObjId);
            entity.setAppId(appId);
            list.add(entity);
        }
        insertBatch(list);
    }

    public void deleteById(Long id){
        reqObjRelationMapper.deleteByPrimaryKey(id);
    }

    public void deleteByIds(Collection<Long> ids){
        if(CollectionUtils.isEmpty(ids)){
            return;
        }
        reqObjRelationMapper.deleteByIds(ids);
    }

    public void delete(Long objId,Long reqId,Long terminalId){
        Example example = new Example(EisReqObjRelation.class);
        example.createCriteria().andEqualTo("objId",objId)
                .andEqualTo("reqPoolId",reqId)
                .andEqualTo("terminalId",terminalId);
        reqObjRelationMapper.deleteByExample(example);
    }
}
