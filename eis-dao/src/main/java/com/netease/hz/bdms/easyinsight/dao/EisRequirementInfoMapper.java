package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.EisRequirementInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface EisRequirementInfoMapper extends Mapper<EisRequirementInfo> {

    List<EisRequirementInfo> selectBatchByIds(@Param("ids") Set<Long> ids);

    void deleteByIds(@Param("ids") Set<Long> ids);

//    List<EisRequirementInfo> selectByIds(@Param("ids")Set<Long> ids);

}