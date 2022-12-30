package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.ArtificialSpmInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ArtificialSpmInforMapper {
    // 批量插入
    Integer insert(@Param("spmInfoCollection") Collection<ArtificialSpmInfo> spmInfoCollection);

    // 按条件查询
    List<ArtificialSpmInfo> select(ArtificialSpmInfo queryCondition);

    // 全部列出，建议后续改成分页，这样很坑
    List<ArtificialSpmInfo> listAll();

    //
    List<ArtificialSpmInfo> selectBySpm(@Param("spmCollection") Collection<String> spmCollection,
                              @Param("appId") Long addId);

    // 按条件删除
    Integer delete(ArtificialSpmInfo deleteCondition);

    Integer deleteByIds(@Param("ids") Collection<Long> ids);

    Integer deleteBySource(@Param("source") Integer source);

    // 依据spm以及appId删除
    Integer deleteBySpm(Collection<String> spmCollection, Long appId);

    // 批量更新
    void update(List<ArtificialSpmInfo> spmInfoList);

    //
    void updateById(ArtificialSpmInfo spmInfo);
}
