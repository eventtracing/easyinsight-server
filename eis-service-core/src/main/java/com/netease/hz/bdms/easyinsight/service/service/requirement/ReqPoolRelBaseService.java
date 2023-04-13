package com.netease.hz.bdms.easyinsight.service.service.requirement;

import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.dao.EisReqPoolRelBaseReleaseMapper;
import com.netease.hz.bdms.easyinsight.dao.model.EisReqPoolRelBaseRelease;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReqPoolRelBaseService {

    @Autowired
    EisReqPoolRelBaseReleaseMapper reqPoolRelBaseReleaseMapper;

    public List<EisReqPoolRelBaseRelease> search(EisReqPoolRelBaseRelease query){
        return Optional.ofNullable(reqPoolRelBaseReleaseMapper.select(query)).orElse(new ArrayList<>());
    }

    public EisReqPoolRelBaseRelease getCurrentUse(Long reqPoolId,Long terminalId){
        EisReqPoolRelBaseRelease query = new EisReqPoolRelBaseRelease();
        query.setCurrentUse(true);
        query.setReqPoolId(reqPoolId);
        query.setTerminalId(terminalId);
        List<EisReqPoolRelBaseRelease> result = reqPoolRelBaseReleaseMapper.select(query);
        if(!CollectionUtils.isEmpty(result)){
            return result.get(0);
        }

        return null;
    }

    public List<EisReqPoolRelBaseRelease> batchGetCurrentUse(Set<Long> reqPoolIds) {
        if (CollectionUtils.isEmpty(reqPoolIds)) {
            return new ArrayList<>(0);
        }
        List<EisReqPoolRelBaseRelease> result = reqPoolRelBaseReleaseMapper.batchGetCurrentUse(reqPoolIds);
        return result == null ? new ArrayList<>(0) : result;
    }

    public EisReqPoolRelBaseRelease getFirstUse(Long reqPoolId,Long terminalId){
        EisReqPoolRelBaseRelease query = new EisReqPoolRelBaseRelease();
        query.setReqPoolId(reqPoolId);
        query.setTerminalId(terminalId);
        List<EisReqPoolRelBaseRelease> result = reqPoolRelBaseReleaseMapper.select(query);
        result = result.stream().sorted(Comparator.comparing(EisReqPoolRelBaseRelease::getCreateTime)).collect(Collectors.toList());
        return result.get(0);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void changeCurrentUse(Long reqPoolId, Long terminalId, Long newBaseReleaseId, boolean autoRebase) {
        if(newBaseReleaseId == null){
            newBaseReleaseId = 0L;
        }
        // 若当前已经是新基线，不必更新
        EisReqPoolRelBaseRelease currentUse = getCurrentUse(reqPoolId, terminalId);
        if(currentUse.getBaseReleaseId().equals(newBaseReleaseId)){
            return;
        }
        reqPoolRelBaseReleaseMapper.updateCurrentUse(reqPoolId, terminalId, true, false);

        EisReqPoolRelBaseRelease newBase = new EisReqPoolRelBaseRelease();
        newBase.setCurrentUse(true);
        newBase.setTerminalId(terminalId);
        newBase.setReqPoolId(reqPoolId);
        newBase.setBaseReleaseId(newBaseReleaseId);
        newBase.setAutoRebase(autoRebase);
        // 默认继承老的，用于自动变基非用户操作情况
        newBase.setCreateName(currentUse.getCreateName());
        newBase.setCreateEmail(currentUse.getCreateEmail());
        newBase.setUpdateName(currentUse.getUpdateName());
        newBase.setUpdateEmail(currentUse.getUpdateEmail());
        insert(newBase);
    }

    /**
     * 更新autoRebase字段
     */
    public void updateAutoRebase(Long reqPoolId, Long terminalId, boolean autoRebase) {
        EisReqPoolRelBaseRelease currentUse = getCurrentUse(reqPoolId, terminalId);
        if (currentUse == null) {
            return;
        }
        if (currentUse.getAutoRebase() != null && currentUse.getAutoRebase().equals(autoRebase)) {
            return;
        }
        currentUse.setAutoRebase(autoRebase);
        reqPoolRelBaseReleaseMapper.updateByPrimaryKeySelective(currentUse);
    }

    public void insert(EisReqPoolRelBaseRelease entity){
        // 公共信息填充
        UserDTO currUser = EtContext.get(ContextConstant.USER);
        if (currUser != null) {
            entity.setCreateName(currUser.getUserName());
            entity.setCreateEmail(currUser.getEmail());
            entity.setUpdateName(currUser.getUserName());
            entity.setUpdateEmail(currUser.getEmail());
        }
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        reqPoolRelBaseReleaseMapper.insert(entity);
    }

    public void insertBatch(List<EisReqPoolRelBaseRelease> list){
        if(CollectionUtils.isNotEmpty(list)){
            // 公共信息填充
            UserDTO currUser = EtContext.get(ContextConstant.USER);
            list.forEach(entity -> {
                if(currUser != null) {
                    entity.setCreateName(currUser.getUserName());
                    entity.setCreateEmail(currUser.getEmail());
                    entity.setUpdateName(currUser.getUserName());
                    entity.setUpdateEmail(currUser.getEmail());
                }
                entity.setCreateTime(new Date());
                entity.setUpdateTime(new Date());
            });

            reqPoolRelBaseReleaseMapper.insertBatch(list);
        }
    }

    public void deleteById(Long id){
        reqPoolRelBaseReleaseMapper.deleteByPrimaryKey(id);
    }

}
