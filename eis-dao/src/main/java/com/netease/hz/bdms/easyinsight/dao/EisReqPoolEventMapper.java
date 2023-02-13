package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.EisReqPoolEvent;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Set;

public interface EisReqPoolEventMapper extends Mapper<EisReqPoolEvent> {

    List<EisReqPoolEvent> selectBatchByIds(@Param("ids") Set<Long> ids);

    List<EisReqPoolEvent> selectBatchByEventIds(@Param("eventIds") Set<Long> ids);

}