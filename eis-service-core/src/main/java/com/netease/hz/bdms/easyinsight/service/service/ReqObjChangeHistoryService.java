package com.netease.hz.bdms.easyinsight.service.service;


import com.netease.hz.bdms.easyinsight.dao.model.EisReqObjChangeHistory;

import java.util.Collection;
import java.util.List;

/**
 * @author: xumengqiang
 * @date: 2021/12/9 16:29
 */
public interface ReqObjChangeHistoryService {
    /**
     * 插入对象变更记录
     *
     * @param objChangeHistory 对象变更信息
     * @return
     */
    Long insert(EisReqObjChangeHistory objChangeHistory);

    /**
     * 批量插入
     * @param list
     */
    void insertBatch(List<EisReqObjChangeHistory> list);

    /**
     * 根据需求id查询关联新建/变更记录
     * @param reqPoolId
     * @return
     */
    List<EisReqObjChangeHistory> getByReqPoolId(Long reqPoolId);

    /**
     * 依据对象ID和需求ID 查询对象新建/变更记录
     *
     * @param objId 对象ID
     * @param reqId 需求ID
     * @return
     */
    List<EisReqObjChangeHistory> getByObjAndReqPoolId(Long objId, Long reqId);

    /**
     * 按主键批量删除
     *
     * @param ids 主键集合
     * @return 删除记录数量
     */
    Integer deleteByIds(Collection<Long> ids);

}
