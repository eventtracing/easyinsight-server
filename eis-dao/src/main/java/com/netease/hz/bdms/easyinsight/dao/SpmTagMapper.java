package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.common.OpenSource;
import com.netease.hz.bdms.easyinsight.dao.model.SpmTag;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * @author: xumengqiang
 * @date: 2021/11/10 14:13
 */
@Repository
public interface SpmTagMapper {
    /**
     * 批量插入
     *
     * @param spmTagCollection spm绑定标签集合
     * @return
     */
    Integer insert(@Param("spmTagCollection") Collection<SpmTag> spmTagCollection);

    /**
     * 批量删除
     *
     * @param spmIdCollection spmId集合
     * @return
     */
    Integer deleteBySpmId(@Param("spmIdCollection") Collection<Long> spmIdCollection);

    /**
     * 依据 spmId 查询
     *
     * @param spmIdCollection pmId集合
     * @return
     */
    List<SpmTag> selectBySpmIds(@Param("spmIdCollection") Collection<Long> spmIdCollection);

    /**
     * 依据主键删除
     * @param ids 主键集合
     * @return
     */
    Integer deleteByIds(@Param("ids") Collection<Long> ids);
}
