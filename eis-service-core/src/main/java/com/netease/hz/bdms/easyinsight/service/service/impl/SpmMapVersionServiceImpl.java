package com.netease.hz.bdms.easyinsight.service.service.impl;

import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.dao.SpmMapVersionMapper;
import com.netease.hz.bdms.easyinsight.dao.model.SpmMapVersion;
import com.netease.hz.bdms.easyinsight.service.service.SpmMapVersionService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author: xumengqiang
 * @date: 2022/3/2 19:17
 */
@Service
public class SpmMapVersionServiceImpl implements SpmMapVersionService {
    @Autowired
    private SpmMapVersionMapper spmMapVersionMapper;

    @Override
    public List<Long> create(Collection<SpmMapVersion> spmMapVersionList) {
        if(CollectionUtils.isEmpty(spmMapVersionList)){
            return new ArrayList<>();
        }
        // 插入公共信息
        UserDTO currUser= EtContext.get(ContextConstant.USER);
        if(null != currUser){
            spmMapVersionList.forEach(spmMapVersion -> {
                spmMapVersion.setCreateEmail(currUser.getEmail())
                        .setCreateName(currUser.getUserName())
                        .setUpdateEmail(currUser.getEmail())
                        .setUpdateName(currUser.getUserName());
            });
        }
        spmMapVersionMapper.insert(spmMapVersionList);
        return spmMapVersionList.stream()
                .map(SpmMapVersion::getId).collect(Collectors.toList());
    }

    @Override
    public List<SpmMapVersion> search(SpmMapVersion condition) {
        if(null != condition){
            return spmMapVersionMapper.select(condition);
        }
        return new ArrayList<>();
    }

    @Override
    public List<SpmMapVersion> getBySpmIds(Set<Long> spmIds, Long terminalId) {
        if(CollectionUtils.isNotEmpty(spmIds) && null != terminalId){
            return spmMapVersionMapper.selectBySpmIds(spmIds, terminalId);
        }
        return new ArrayList<>();
    }

    @Override
    public Integer deleteByIds(Set<Long> ids) {
        if(CollectionUtils.isNotEmpty(ids)){
            return spmMapVersionMapper.deleteByIds(ids);
        }
        return 0;
    }
}
