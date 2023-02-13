package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.EisReqPoolRelBaseRelease;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Set;

public interface EisReqPoolRelBaseReleaseMapper extends Mapper<EisReqPoolRelBaseRelease> {

    void insertBatch(@Param("list") List<EisReqPoolRelBaseRelease> list);

    List<EisReqPoolRelBaseRelease> batchGetCurrentUse(@Param("reqPoolIds") Set<Long> reqPoolIds);

    void updateCurrentUse(Long reqPoolId, Long terminalId, boolean oldCurrentUse, boolean newCurrentUse);

    void deleteDuplicate(Long reqPoolId, Long terminalId, Long excludeId);
}