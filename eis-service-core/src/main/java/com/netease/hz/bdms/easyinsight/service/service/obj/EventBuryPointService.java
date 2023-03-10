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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
        eisEventBuryPointMapper.insertSelective(eventBuryPoint);
    }

    @Deprecated
    public void insertBatch(List<EisEventBuryPoint> list){
        // ????????????
        UserDTO currUser = EtContext.get(ContextConstant.USER);
        if(null != currUser) {
            list.forEach(eventBuryPoint -> {
                eventBuryPoint.setCreateName(currUser.getUserName());
                eventBuryPoint.setCreateEmail(currUser.getEmail());
                eventBuryPoint.setUpdateName(currUser.getUserName());
                eventBuryPoint.setUpdateEmail(currUser.getEmail());
            });
        }

        eisEventBuryPointMapper.insertBatch(list);
    }

    public Integer deleteById(Long id){
        return eisEventBuryPointMapper.deleteByPrimaryKey(id);
    }

    public void update(EisEventBuryPoint eventBuryPoint){
        Preconditions.checkArgument(null != eventBuryPoint, "???????????????????????????");
        // ????????????????????????
        if(null == eventBuryPoint.getId()){
            throw new ParamException("?????????????????????????????????");
        }
        // ????????????
        UserDTO currUser = EtContext.get(ContextConstant.USER);
        if(null != currUser) {
            eventBuryPoint.setUpdateName(currUser.getUserName());
            eventBuryPoint.setUpdateEmail(currUser.getEmail());
        }
        eisEventBuryPointMapper.updateByPrimaryKeySelective(eventBuryPoint);
    }

}
