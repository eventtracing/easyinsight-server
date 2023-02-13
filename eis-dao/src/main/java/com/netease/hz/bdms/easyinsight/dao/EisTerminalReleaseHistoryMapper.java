package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.EisTerminalReleaseHistory;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Set;

public interface EisTerminalReleaseHistoryMapper extends Mapper<EisTerminalReleaseHistory> {
    /**
     * 按主键批量查询
     *
     * @param ids 主键ID
     * @return
     */
    List<EisTerminalReleaseHistory> selectByIds(@Param("ids") Set<Long> ids);
}