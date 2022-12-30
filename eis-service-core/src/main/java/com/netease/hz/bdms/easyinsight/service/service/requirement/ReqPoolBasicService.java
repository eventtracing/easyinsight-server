package com.netease.hz.bdms.easyinsight.service.service.requirement;

import com.google.common.collect.Lists;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.query.ReqPoolPageQuery;
import com.netease.hz.bdms.easyinsight.dao.EisReqPoolMapper;
import com.netease.hz.bdms.easyinsight.dao.model.EisReqPool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class ReqPoolBasicService {

    @Autowired
    EisReqPoolMapper reqPoolMapper;

    public EisReqPool getById(Long id){
        return reqPoolMapper.selectByPrimaryKey(id);
    }

    public List<EisReqPool> getByIds(Collection<Long> ids){
        if(CollectionUtils.isNotEmpty(ids)) {
            return reqPoolMapper.selectByIds(ids);
        }
        return Lists.newArrayList();
    }

    public List<EisReqPool> search(EisReqPool query){
        return Optional.ofNullable(reqPoolMapper.select(query)).orElse(new ArrayList<>());
    }

    public void insert(EisReqPool entity){
        UserDTO currentLoginUser = EtContext.get(ContextConstant.USER);
        Long appId = EtContext.get(ContextConstant.APP_ID);
        if(currentLoginUser != null){
            entity.setCreateEmail(currentLoginUser.getEmail());
            entity.setCreateName(currentLoginUser.getUserName());
        }
        if(appId != null){
            entity.setAppId(appId);
        }
        reqPoolMapper.insertSelective(entity);
    }

    public List<EisReqPool> queryForPage(ReqPoolPageQuery query){
        return Optional.ofNullable(reqPoolMapper.queryForPage(query)).orElse(new ArrayList<>());
    }

    public void updateById(EisReqPool entity){
        reqPoolMapper.updateByPrimaryKeySelective(entity);
    }

    public void deleteById(Long id){
        reqPoolMapper.deleteByPrimaryKey(id);
    }

}
