package com.netease.hz.bdms.easyinsight.service.service.impl;

import com.google.common.collect.Lists;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.dao.EisReqObjChangeHistoryMapper;
import com.netease.hz.bdms.easyinsight.dao.model.EisReqObjChangeHistory;
import com.netease.hz.bdms.easyinsight.service.service.ReqObjChangeHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author: xumengqiang
 * @date: 2021/12/9 16:48
 */
@Slf4j
@Service
public class ReqObjChangeHistoryServiceImpl implements ReqObjChangeHistoryService {
    @Autowired
    private EisReqObjChangeHistoryMapper eisReqObjChangeHistoryMapper;

    @Override
    public Long insert(EisReqObjChangeHistory eisReqObjChangeHistory) {

        // 信息填充
        UserDTO currentUser = EtContext.get(ContextConstant.USER);
        if(null != currentUser) {
            eisReqObjChangeHistory.setCreateName(currentUser.getUserName());
            eisReqObjChangeHistory.setCreateEmail(currentUser.getEmail());
            eisReqObjChangeHistory.setUpdateName(currentUser.getUserName());
            eisReqObjChangeHistory.setUpdateEmail(currentUser.getEmail());
        }
        eisReqObjChangeHistory.setCreateTime(new Date());
        eisReqObjChangeHistory.setUpdateTime(new Date());
        // 插入记录
        eisReqObjChangeHistoryMapper.insert(eisReqObjChangeHistory);
        return eisReqObjChangeHistory.getId();
    }


    @Override
    public List<EisReqObjChangeHistory> getByReqIdAndObjId(Long reqPoolId, Long objId){
        List<EisReqObjChangeHistory> objChangeHistoryList= eisReqObjChangeHistoryMapper.selectByReqIdAndObjId(reqPoolId, objId);
        return Optional.ofNullable(objChangeHistoryList).orElse(Lists.newArrayList());
    }



    @Override
    public Integer deleteByIds(Collection<Long> ids) {
        if(CollectionUtils.isNotEmpty(ids)){
            return eisReqObjChangeHistoryMapper.deleteByIds(ids);
        }
        return 0;
    }

}
