package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.SpmMapItem;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * @author: xumengqiang
 * @date: 2021/11/10 18:59
 */

@Repository
public interface SpmMapItemMapper {
    /**
     * 批量插入
     *
     * @param spmMapItemCollection 映射关系集合
     * @return
     */
    Integer insert(@Param("spmMapItemCollection") Collection<SpmMapItem> spmMapItemCollection);

    /**
     * 依据spmId 批量删除
     *
     * @param spmIds spmId集合
     * @return
     */
    Integer deleteBySpmId(@Param("spmIds") Collection<Long> spmIds);

    /**
     * 依据 spmId 查询
     *
     * @param spmIds
     * @return
     */
    List<SpmMapItem> selectBySpmIds(@Param("spmIds") Collection<Long> spmIds);
}
