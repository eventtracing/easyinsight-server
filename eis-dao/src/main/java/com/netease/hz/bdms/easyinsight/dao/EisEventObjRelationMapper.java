package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.EisEventObjRelation;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Set;

public interface EisEventObjRelationMapper extends Mapper<EisEventObjRelation> {

    List<EisEventObjRelation> selectByEventEntityId(@Param("entityId") Long entityId);

    List<EisEventObjRelation> selectByEventEntityIds(@Param("entityIds") Set<Long> entityIds);

    List<EisEventObjRelation> selectByObjId(@Param("objId") Long objId);

    void insertBatch(@Param("list") List<EisEventObjRelation> list);

    Integer deleteByEntityId(@Param("entityId") Long entityId);

}