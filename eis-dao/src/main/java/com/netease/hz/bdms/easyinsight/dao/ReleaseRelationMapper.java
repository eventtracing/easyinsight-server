package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.ReleaseRelation;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReleaseRelationMapper {

    List<ReleaseRelation> listByReleaseId(Long releaseId);

    Integer insert(ReleaseRelation appRelation);

    Integer delete(ReleaseRelation appRelation);
}
