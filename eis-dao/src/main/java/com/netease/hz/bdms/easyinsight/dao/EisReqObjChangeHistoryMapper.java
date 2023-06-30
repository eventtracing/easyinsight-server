package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.EisReqObjChangeHistory;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

public interface EisReqObjChangeHistoryMapper {
    /**
     * 依据主键 批量删除
     *
     * @param ids 主键集合
     * @return 删除记录条数
     */
    Integer deleteByIds(@Param("ids") Collection<Long> ids);

    /**
     * 插入
     * @param eisReqObjChangeHistory
     * @return
     */
    Integer insert(EisReqObjChangeHistory eisReqObjChangeHistory);

    /**
     * 查询对象变更记录
     *
     * @param reqPoolId
     * @param objId
     * @return
     */
    List<EisReqObjChangeHistory> selectByReqIdAndObjId(@Param("reqPoolId") Long reqPoolId, @Param("objId") Long objId);


}