package com.netease.hz.bdms.easyinsight.service.service.requirement;

import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.dao.EisReqPoolEventMapper;
import com.netease.hz.bdms.easyinsight.dao.model.EisReqPoolEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class ReqEventPoolService {

    @Autowired
    EisReqPoolEventMapper reqPoolEventMapper;

    public EisReqPoolEvent getById(Long id){
        return reqPoolEventMapper.selectByPrimaryKey(id);
    }

    public List<EisReqPoolEvent> getBatchByIds(Set<Long> ids){
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>(0);
        }
        return Optional.ofNullable(reqPoolEventMapper.selectBatchByIds(ids)).orElse(new ArrayList<>());
    }

    public List<EisReqPoolEvent> getBatchByEventIds(Set<Long> ids){
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>(0);
        }
        return Optional.ofNullable(reqPoolEventMapper.selectBatchByEventIds(ids)).orElse(new ArrayList<>());
    }

    public List<EisReqPoolEvent> search(EisReqPoolEvent query){
        return Optional.ofNullable(reqPoolEventMapper.select(query)).orElse(new ArrayList<>());
    }

    public void insert(EisReqPoolEvent entity){
        // 公共信息
        UserDTO currUser = EtContext.get(ContextConstant.USER);
        entity.setCreateName(currUser.getUserName());
        entity.setCreateEmail(currUser.getEmail());
        entity.setUpdateName(currUser.getUserName());
        entity.setUpdateEmail(currUser.getEmail());

        reqPoolEventMapper.insert(entity);
    }

    public void deleteById(Long id){
        reqPoolEventMapper.deleteByPrimaryKey(id);
    }

}
