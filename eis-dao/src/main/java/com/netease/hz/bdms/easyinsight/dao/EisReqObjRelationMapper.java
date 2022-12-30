package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.EisReqObjRelation;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface EisReqObjRelationMapper extends Mapper<EisReqObjRelation> {

    List<EisReqObjRelation> selectBatchByObjIds(@Param("objIds")Set<Long> objIds);

    void insertBatch(@Param("list")List<EisReqObjRelation> list);

    void deleteByIds(@Param("ids") Collection<Long> ids);

}