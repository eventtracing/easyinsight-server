package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.EisAllTrackerRelease;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.Collection;
import java.util.List;

public interface EisAllTrackerReleaseMapper extends Mapper<EisAllTrackerRelease> {

    void insertBatch(@Param("list") List<EisAllTrackerRelease> list);

    List<EisAllTrackerRelease> selectByIds(@Param("ids") Collection<Long> ids);

    List<EisAllTrackerRelease> selectByReleaseIdAndObjIds(
            @Param("terminalReleaseId") Long terminalReleaseId,
            @Param("objIds") Collection<Long> objIds);

    List<EisAllTrackerRelease> selectByReleaseIds(@Param("releaseIds") Collection<Long> releaseIds);

}