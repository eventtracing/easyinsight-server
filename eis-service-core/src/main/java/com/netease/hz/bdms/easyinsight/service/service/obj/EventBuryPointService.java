package com.netease.hz.bdms.easyinsight.service.service.obj;

import com.google.common.base.Preconditions;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.exception.ParamException;
import com.netease.hz.bdms.easyinsight.dao.EisEventBuryPointMapper;
import com.netease.hz.bdms.easyinsight.dao.model.EisEventBuryPoint;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class EventBuryPointService {

    @Autowired
    EisEventBuryPointMapper eisEventBuryPointMapper;

    public List<EisEventBuryPoint> search(EisEventBuryPoint query){
        return Optional.ofNullable(eisEventBuryPointMapper.select(query)).orElse(new ArrayList<>());
    }

    public EisEventBuryPoint getById(Long id){
        return eisEventBuryPointMapper.selectByPrimaryKey(id);
    }

    public List<EisEventBuryPoint> getByIds(Set<Long> ids){
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>(0);
        }
        return Optional.ofNullable(eisEventBuryPointMapper.selectBatchByIds(ids)).orElse(new ArrayList<>());
    }

    public void insert(EisEventBuryPoint eventBuryPoint){
        UserDTO currUser = EtContext.get(ContextConstant.USER);
        if(null != currUser) {
            eventBuryPoint.setCreateName(currUser.getUserName());
            eventBuryPoint.setCreateEmail(currUser.getEmail());
            eventBuryPoint.setUpdateName(currUser.getUserName());
            eventBuryPoint.setUpdateEmail(currUser.getEmail());
        }
        eventBuryPoint.setCreateTime(new Date());
        eventBuryPoint.setUpdateTime(new Date());
        eisEventBuryPointMapper.insertSelective(eventBuryPoint);
    }

    @Deprecated
    public void insertBatch(List<EisEventBuryPoint> list){
        // 公共信息
        UserDTO currUser = EtContext.get(ContextConstant.USER);
        if(null != currUser) {
            list.forEach(eventBuryPoint -> {
                eventBuryPoint.setCreateName(currUser.getUserName());
                eventBuryPoint.setCreateEmail(currUser.getEmail());
                eventBuryPoint.setUpdateName(currUser.getUserName());
                eventBuryPoint.setUpdateEmail(currUser.getEmail());
                eventBuryPoint.setCreateTime(new Date());
                eventBuryPoint.setUpdateTime(new Date());
            });
        }

        eisEventBuryPointMapper.insertBatch(list);
    }

    public Integer deleteById(Long id){
        return eisEventBuryPointMapper.deleteByPrimaryKey(id);
    }

    public void update(EisEventBuryPoint eventBuryPoint){
        Preconditions.checkArgument(null != eventBuryPoint, "更新信息不能为空！");
        // 只能通过主键更新
        if(null == eventBuryPoint.getId()){
            throw new ParamException("未指定主键，更新失败！");
        }
        // 公共信息
        UserDTO currUser = EtContext.get(ContextConstant.USER);
        if(null != currUser) {
            eventBuryPoint.setUpdateName(currUser.getUserName());
            eventBuryPoint.setUpdateEmail(currUser.getEmail());
        }
        eisEventBuryPointMapper.updateByPrimaryKeySelective(eventBuryPoint);
    }

}
