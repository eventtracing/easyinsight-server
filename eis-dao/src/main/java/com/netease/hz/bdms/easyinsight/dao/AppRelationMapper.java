package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.AppRelation;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppRelationMapper {

    List<AppRelation> listAll();

    Integer insert(AppRelation appRelation);

    Integer delete(AppRelation appRelation);
}
