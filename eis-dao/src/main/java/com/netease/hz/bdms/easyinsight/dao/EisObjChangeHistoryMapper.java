package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.EisObjChangeHistory;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface EisObjChangeHistoryMapper extends Mapper<EisObjChangeHistory> {
    /**
     * 依据主键 批量删除
     *
     * @param ids 主键集合
     * @return 删除记录条数
     */
    Integer deleteByIds(@Param("ids") Collection<Long> ids);

    /**
     * 依据主键 批量查询
     *
     * @param ids 主键集合
     * @return
     */
    List<EisObjChangeHistory> selectByIds(@Param("ids") Collection<Long> ids);

    /**
     * 依据主键 批量查询
     *
     * @param conflictStatus
     * @return
     */
    List<EisObjChangeHistory> selectByReqPoolIdsAndConflictStatus(@Param("reqPoolIds") Set<Long> reqPoolIds, @Param("conflictStatus") String conflictStatus);

    /**
     * 查询所有冲突状态
     *
     * @param conflictStatus
     * @return
     */
    List<EisObjChangeHistory> selectByConflictStatus(@Param("conflictStatus") String conflictStatus);

    /**
     * 查询所有双端不一致
     * @return
     */
    List<EisObjChangeHistory> selectAllNotConsistency();

    /**
     * 依据主键 批量查询
     *
     * @param conflictStatus
     * @return
     */
    Set<Long> selectDistinctReqPoolIdByConflictStatus(@Param("conflictStatus") String conflictStatus);

    /**
     * 批量插入
     * @param list
     */
    void insertBatch(@Param("list") List<EisObjChangeHistory> list);

    /**
     * 更新conflictStatus
     * @param reqPoolId 需求组ID
     * @param objIds 对象ID
     * @param conflictStatus 状态
     * @return
     */
    Integer updateConflictStatus(@Param("reqPoolId") Long reqPoolId, @Param("objIds") Set<Long> objIds, @Param("conflictStatus") String conflictStatus);

}