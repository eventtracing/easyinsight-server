package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.common.OpenSource;
import com.netease.hz.bdms.easyinsight.dao.model.SpmInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * @author: xumengqiang
 * @date: 2021/11/9 11:31
 */
@Repository
public interface SpmInfoMapper {
    // 批量插入
    Integer insert(@Param("spmInfoCollection") Collection<SpmInfo> spmInfoCollection);

    // 按条件查询
    List<SpmInfo> select(SpmInfo queryCondition);

    // 按条件查询
    List<SpmInfo> selectLast(SpmInfo queryCondition);

    // 全部列出，建议后续改成分页，这样很坑
    List<SpmInfo> listAll();

    //
    List<SpmInfo> selectBySpm(@Param("spmCollection") Collection<String> spmCollection,
                              @Param("appId") Long addId);

    // 按条件删除
    Integer delete(SpmInfo deleteCondition);

    Integer deleteByIds(@Param("ids") Collection<Long> ids);

    Integer deleteBySource(@Param("source") Integer source);

    // 依据spm以及appId删除
    Integer deleteBySpm(Collection<String> spmCollection, Long appId);

    // 批量更新
    void update(List<SpmInfo> spmInfoList);

    // 批量更新
    void updateById(SpmInfo spmInfo);

    // 按条件查询,模糊查询
    List<SpmInfo> selectByNameOrCode(SpmInfo queryCondition);
}
