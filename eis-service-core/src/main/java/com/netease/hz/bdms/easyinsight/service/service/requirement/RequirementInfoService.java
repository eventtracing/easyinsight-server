package com.netease.hz.bdms.easyinsight.service.service.requirement;

import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.dao.EisRequirementInfoMapper;
import com.netease.hz.bdms.easyinsight.dao.model.EisRequirementInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class RequirementInfoService {

    @Autowired
    EisRequirementInfoMapper requirementInfoMapper;

    public EisRequirementInfo getById(Long id){
        return requirementInfoMapper.selectByPrimaryKey(id);
    }

    public List<EisRequirementInfo> getByIds(Set<Long> ids){
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>(0);
        }
        return requirementInfoMapper.selectBatchByIds(ids);
    }

    public List<EisRequirementInfo> search(EisRequirementInfo query){
        return Optional.ofNullable(requirementInfoMapper.select(query)).orElse(new ArrayList<>());
    }

    public void insert(EisRequirementInfo entity){
        UserDTO currentLoginUser = EtContext.get(ContextConstant.USER);
        Long appId = EtContext.get(ContextConstant.APP_ID);
        if(currentLoginUser != null){
            entity.setCreateEmail(currentLoginUser.getEmail());
            entity.setCreateName(currentLoginUser.getUserName());
        }
        if(appId != null){
            entity.setAppId(appId);
        }
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        requirementInfoMapper.insertSelective(entity);
    }

    public void update(EisRequirementInfo entity){
        requirementInfoMapper.updateByPrimaryKeySelective(entity);
    }

    public void deleteById(Long id){
        requirementInfoMapper.deleteByPrimaryKey(id);
    }

    public void deleteByIds(Set<Long> ids){
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        requirementInfoMapper.deleteByIds(ids);
    }

}
