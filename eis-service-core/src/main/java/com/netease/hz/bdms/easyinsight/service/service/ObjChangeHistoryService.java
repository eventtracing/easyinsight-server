package com.netease.hz.bdms.easyinsight.service.service;


import com.netease.hz.bdms.easyinsight.dao.model.EisObjChangeHistory;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author: xumengqiang
 * @date: 2021/12/9 16:29
 */
public interface ObjChangeHistoryService {
    /**
     * 插入对象变更记录
     *
     * @param objChangeHistory 对象变更信息
     * @return
     */
    Long insert(EisObjChangeHistory objChangeHistory);

    /**
     * 批量插入
     * @param list
     */
    void insertBatch(List<EisObjChangeHistory> list);

    /**
     * 根据需求id查询关联新建/变更记录
     * @param reqPoolId
     * @return
     */
    List<EisObjChangeHistory> getByReqPoolId(Long reqPoolId);

    /**
     * 查询指定冲突状态
     * @param reqPoolId
     * @param conflictStatus
     * @return
     */
    List<EisObjChangeHistory> getByConflictStatus(Long reqPoolId, String conflictStatus);


    /**
     * 查询指定冲突状态
     * @param conflictStatus
     * @return
     */
    List<EisObjChangeHistory> getByConflictStatus(Set<Long> reqPoolIds, String conflictStatus);

    /**
     * 查询指定冲突状态
     * @param conflictStatus
     * @return
     */
    List<EisObjChangeHistory> getAllByConflictStatus(String conflictStatus);

    /**
     * 查询所有双端不一致
     */
    List<EisObjChangeHistory> getAllNotConsistency();

    void updateConflictStatus(Long reqPoolId, Collection<Long> objIds, String conflictStatus);


    /**
     * 依据对象ID和需求ID 查询对象新建/变更记录
     *
     * @param objId 对象ID
     * @param reqId 需求ID
     * @return
     */
    List<EisObjChangeHistory> getByObjAndReqPoolId(Long objId, Long reqId);


    /**
     * 依据主键进行查询
     *
     * @param historyId 主键ID
     * @return
     */
    EisObjChangeHistory getById(Long historyId);


    /**
     * 依据主键批量查询
     *
     * @param historyIds 主键ID集合
     * @return
     */
    List<EisObjChangeHistory> getByIds(Collection<Long> historyIds);


    /**
     * 按主键批量删除
     *
     * @param ids 主键集合
     * @return 删除记录数量
     */
    Integer deleteByIds(Collection<Long> ids);

    /**
     * 依据主键更新记录信息
     *
     * @param objChangeHistory 更新后的信息
     */
    void update(EisObjChangeHistory objChangeHistory);

    /**
     * 查询指定conflictStatus类型的所有数据
     */
    Set<Long> getDistinctReqPoolIdByConflictStatus(String conflictStatus);
}
