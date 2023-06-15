package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.common.query.TaskPageQuery;
import com.netease.hz.bdms.easyinsight.dao.model.EisReqTask;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Set;

public interface EisReqTaskMapper extends Mapper<EisReqTask> {

    List<EisReqTask> queryPagingList(TaskPageQuery taskPageQuery);

    List<EisReqTask> listAllByTerminalId(@Param("terminalId") long terminalId, @Param("startTime") String startTime, @Param("endTime") String endTime);

    List<EisReqTask> selectByTerminalVersionId(@Param("terminalVersionIds") Set<Long> terminalVersionIds);

    List<EisReqTask> selectByUserAndStatus(@Param("ownerEmail") String ownerEmail, @Param("status") Integer status);

    List<EisReqTask> selectBatchByIds(@Param("ids") Set<Long> ids);

    List<EisReqTask> selectBatchByReqIds(@Param("reqIds") Set<Long> ids);

    void insertBatch(@Param("list") List<EisReqTask> list);

    void updateBatch(List<EisReqTask> list);

    void deleteByIds(@Param("ids") Set<Long> ids);

}