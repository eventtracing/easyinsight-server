package com.netease.hz.bdms.easyinsight.service.service.terminalrelease;

import com.google.common.collect.Lists;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.dao.EisTerminalReleaseHistoryMapper;
import com.netease.hz.bdms.easyinsight.dao.model.EisTerminalReleaseHistory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
public class TerminalReleaseService {

    @Autowired
    EisTerminalReleaseHistoryMapper terminalReleaseHistoryMapper;

    public EisTerminalReleaseHistory getById(Long id){
        return terminalReleaseHistoryMapper.selectByPrimaryKey(id);
    }

    public List<EisTerminalReleaseHistory> search(EisTerminalReleaseHistory query){
        return Optional.ofNullable(terminalReleaseHistoryMapper.select(query)).orElse(new ArrayList<>());
    }

    public void updateById(EisTerminalReleaseHistory entity){
        // 公共信息插入
        UserDTO currUser = EtContext.get(ContextConstant.USER);
        if(null != currUser) {
            entity.setUpdateName(currUser.getUserName());
            entity.setUpdateEmail(currUser.getEmail());
        }
        terminalReleaseHistoryMapper.updateByPrimaryKeySelective(entity);
    }

    public void insert(EisTerminalReleaseHistory entity){
        // 公共信息插入
        UserDTO currUser = EtContext.get(ContextConstant.USER);
        if(null != currUser) {
            entity.setCreateName(currUser.getUserName());
            entity.setCreateEmail(currUser.getEmail());
            entity.setUpdateName(currUser.getUserName());
            entity.setUpdateEmail(currUser.getEmail());
        }
        terminalReleaseHistoryMapper.insertSelective(entity);
    }

    public List<EisTerminalReleaseHistory> getByIds(Set<Long> ids){
        if(CollectionUtils.isNotEmpty(ids)){
            return terminalReleaseHistoryMapper.selectByIds(ids);
        }
        return Lists.newArrayList();
    }

    public EisTerminalReleaseHistory getLatestRelease(Long terminalId){
        EisTerminalReleaseHistory query = new EisTerminalReleaseHistory();
        query.setLatest(true);
        query.setTerminalId(terminalId);
        List<EisTerminalReleaseHistory> releaseHistories = terminalReleaseHistoryMapper.select(query);
        if(CollectionUtils.isEmpty(releaseHistories)){
            return null;
        }
        //正常情况下一个端id对应一个最新的发布版本
        return releaseHistories.get(0);
    }

    /**
     * 发布上线
     * @param terminalId
     * @param terminalVersionId
     */
    @Transactional(rollbackFor = Throwable.class)
    public Long releaseAndUpdate(Long appId, Long terminalId,Long terminalVersionId){
        EisTerminalReleaseHistory latest = getLatestRelease(terminalId);
        Long preReleaseId = 0L;
        //若上一发布记录存在，则将上一发布记录的latest置为false并更新
        if(latest != null){
            preReleaseId = latest.getId();
            latest = new EisTerminalReleaseHistory();
            latest.setId(preReleaseId);
            latest.setLatest(false);
            // 更新
            this.updateById(latest);
        }
        EisTerminalReleaseHistory newRelease = new EisTerminalReleaseHistory();
        newRelease.setTerminalId(terminalId);
        newRelease.setTerminalVersionId(terminalVersionId);
        newRelease.setLatest(true);
        newRelease.setAppId(appId);
        newRelease.setPreReleaseId(preReleaseId);
        // 插入记录
        this.insert(newRelease);
        return newRelease.getId();
    }

}
