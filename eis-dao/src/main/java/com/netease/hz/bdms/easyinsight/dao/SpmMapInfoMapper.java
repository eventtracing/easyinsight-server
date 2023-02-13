package com.netease.hz.bdms.easyinsight.dao;


import com.netease.hz.bdms.easyinsight.dao.model.SpmMapInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface SpmMapInfoMapper {
    /**
     * 批量插入新老埋点映射数据信息
     *
     * @param spmMapInfoCollection
     * @return
     */
    Integer insertBatch(@Param("spmMapInfoCollection") Collection<SpmMapInfo> spmMapInfoCollection);

    /**
     * 根据主键搜索数据信息
     * @param id
     * @return
     */
    SpmMapInfo selectByPrimaryKey(Long id);

    /**
     * 按给出的条件删除
     *
     * @param spmMapInfo
     * @return
     */
    Integer delete(SpmMapInfo spmMapInfo);
}
