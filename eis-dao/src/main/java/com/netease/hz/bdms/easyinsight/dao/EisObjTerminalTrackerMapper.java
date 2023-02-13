package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.EisObjTerminalTracker;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface EisObjTerminalTrackerMapper extends Mapper<EisObjTerminalTracker> {

    List<EisObjTerminalTracker> selectBatchByIds(@Param("ids") Set<Long> ids);

    List<EisObjTerminalTracker> selectBatchByChangeHistoryIds(@Param("changeHistoryIds") Set<Long> changeHistoryIds);

    void updateBatch(@Param("list") List<EisObjTerminalTracker> list);

    void insertBatch(@Param("list")List<EisObjTerminalTracker> list);

    void deleteByIds(@Param("ids") Collection<Long> ids);

    List<EisObjTerminalTracker> selectByObjIdAndTerminal(@Param("objId") Long objId,
                                              @Param("appId") Long appId,
                                              @Param("terminalId") Long terminalId);
}