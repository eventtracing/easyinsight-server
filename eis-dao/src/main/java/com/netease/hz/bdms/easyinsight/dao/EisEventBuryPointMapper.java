package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.EisEventBuryPoint;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Set;

public interface EisEventBuryPointMapper extends Mapper<EisEventBuryPoint> {

    List<EisEventBuryPoint> selectBatchByIds(@Param("ids") Set<Long> ids);

    void insertBatch(@Param("list") List<EisEventBuryPoint> list);

    void deleteByIds(@Param("ids") Set<Long> ids);

}