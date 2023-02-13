package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.EisReqPoolSpm;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Set;

public interface EisReqPoolSpmMapper extends Mapper<EisReqPoolSpm> {

    List<EisReqPoolSpm> selectBatchByIds(@Param("ids") Set<Long> ids);

    List<EisReqPoolSpm> selectBatchByObjIds(@Param("objIds") Set<Long> ids);

    void insertBatch(@Param("list") List<EisReqPoolSpm> list);

    void deleteByIds(@Param("ids") Set<Long> ids);

}