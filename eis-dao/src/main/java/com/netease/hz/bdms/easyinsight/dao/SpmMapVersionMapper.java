package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.SpmMapVersion;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * @author: xumengqiang
 * @date: 2022/3/2 18:39
 */
@Repository
public interface SpmMapVersionMapper {

    /**
     * 批量插入SPM映射生效版本信息
     *
     * @param spmMapVersionCollection 映射生效版本信息
     * @return
     */
    Integer insert(@Param("collection") Collection<SpmMapVersion> spmMapVersionCollection);

    /**
     * 条件查询
     *
     * @param queryCondition 查询条件
     * @return
     */
    List<SpmMapVersion> select(SpmMapVersion queryCondition);

    /**
     * 依据SpmId 批量查询
     *
     * @param spmIds spmId集合
     * @param terminalId 终端ID
     * @return
     */
    List<SpmMapVersion> selectBySpmIds(@Param("spmIds") Collection<Long> spmIds,
                                       @Param("terminalId") Long terminalId);

    /**
     * 依据主键删除
     * @param ids 主键集合
     * @return
     */
    Integer deleteByIds(@Param("ids") Collection<Long> ids);

}
