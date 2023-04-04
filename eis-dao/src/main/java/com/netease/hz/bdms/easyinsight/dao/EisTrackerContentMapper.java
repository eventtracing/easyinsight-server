package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.TrackerContent;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface EisTrackerContentMapper {

    List<TrackerContent> listAll(Long trackerId);

    Integer batchInsert(@Param("trackerContents") List<TrackerContent> trackerContents);

    Integer deleteByIds(@Param("ids") Collection<Long> ids);
}
