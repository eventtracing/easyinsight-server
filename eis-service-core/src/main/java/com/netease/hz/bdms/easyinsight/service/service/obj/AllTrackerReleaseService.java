package com.netease.hz.bdms.easyinsight.service.service.obj;

import com.google.common.collect.Lists;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.dao.EisAllTrackerReleaseMapper;
import com.netease.hz.bdms.easyinsight.dao.model.EisAllTrackerRelease;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AllTrackerReleaseService {

    @Autowired
    EisAllTrackerReleaseMapper allTrackerReleaseMapper;

    public List<EisAllTrackerRelease> search(EisAllTrackerRelease query){
        return Optional.ofNullable(allTrackerReleaseMapper.select(query)).orElse(new ArrayList<>());
    }

    public List<EisAllTrackerRelease> searchByReleaseIdAndObjIds(Long terminalReleaseId, List<Long> objIds) {
        if (terminalReleaseId == null || CollectionUtils.isEmpty(objIds)) {
            return new ArrayList<>(0);
        }
        return Optional.ofNullable(allTrackerReleaseMapper.selectByReleaseIdAndObjIds(terminalReleaseId, objIds)).orElse(new ArrayList<>());
    }

    public void insertBatch(List<EisAllTrackerRelease> list){
        UserDTO currUser = EtContext.get(ContextConstant.USER);
        if(null != currUser){
            list.forEach(eisAllTrackerRelease -> {
                eisAllTrackerRelease.setCreateName(currUser.getUserName());
                eisAllTrackerRelease.setCreateEmail(currUser.getEmail());
                eisAllTrackerRelease.setUpdateName(currUser.getUserName());
                eisAllTrackerRelease.setUpdateEmail(currUser.getEmail());
            });
        }
        allTrackerReleaseMapper.insertBatch(list);
    }

    public EisAllTrackerRelease getById(Long id) {
        return allTrackerReleaseMapper.selectByPrimaryKey(id);
    }

    public List<EisAllTrackerRelease> getByIds(Set<Long> ids) {
        if(CollectionUtils.isNotEmpty(ids)) {
            return allTrackerReleaseMapper.selectByIds(ids);
        }
        return Lists.newArrayList();
    }

    public List<EisAllTrackerRelease> getByReleaseId(Long releaseId) {
        EisAllTrackerRelease query = new EisAllTrackerRelease();
        query.setTerminalReleaseId(releaseId);
        return allTrackerReleaseMapper.select(query);
    }

    public List<EisAllTrackerRelease> getByReleaseIds(Set<Long> releaseIds) {
        if (CollectionUtils.isEmpty(releaseIds)) {
            return new ArrayList<>(0);
        }
        List<EisAllTrackerRelease> l = allTrackerReleaseMapper.selectByReleaseIds(releaseIds);
        if (l == null) {
            return new ArrayList<>(0);
        }
        return l;
    }

    public EisAllTrackerRelease getByReleaseIdAndObjId(Long releaseId, Long objId) {
        EisAllTrackerRelease query = new EisAllTrackerRelease();
        query.setTerminalReleaseId(releaseId);
        query.setObjId(objId);
        List<EisAllTrackerRelease> list = allTrackerReleaseMapper.select(query);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        Collections.sort(list, Comparator.comparingLong(EisAllTrackerRelease::getId).reversed());
        return list.get(0);
    }

    public void updateById(EisAllTrackerRelease entity){
        allTrackerReleaseMapper.updateByPrimaryKeySelective(entity);
    }
}
