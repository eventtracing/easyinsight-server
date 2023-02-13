package com.netease.hz.bdms.easyinsight.service.service.obj;

import com.netease.hz.bdms.easyinsight.dao.EisObjAllRelationReleaseMapper;
import com.netease.hz.bdms.easyinsight.dao.model.EisObjAllRelationRelease;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ObjRelationReleaseService {

    @Autowired
    EisObjAllRelationReleaseMapper objAllRelationReleaseMapper;

    public List<EisObjAllRelationRelease> search(EisObjAllRelationRelease query){
        return Optional.ofNullable(objAllRelationReleaseMapper.select(query)).orElse(new ArrayList<>());
    }

    public List<EisObjAllRelationRelease> getAllRelationsByReleaseId(Long releaseId){
        EisObjAllRelationRelease query = new EisObjAllRelationRelease();
        query.setTerminalReleaseId(releaseId);
        return Optional.ofNullable(objAllRelationReleaseMapper.select(query)).orElse(new ArrayList<>());
    }

    public void insertBatch(List<EisObjAllRelationRelease> list){
        objAllRelationReleaseMapper.insertBatch(list);
    }
}
