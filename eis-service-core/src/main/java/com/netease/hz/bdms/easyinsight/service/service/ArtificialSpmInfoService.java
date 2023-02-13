package com.netease.hz.bdms.easyinsight.service.service;

import com.netease.hz.bdms.easyinsight.common.dto.spm.ArtificialSpmInfoDTO;

import java.util.Collection;
import java.util.List;

/**
 * SPM管理功能 - 手动维护SPM
 */
public interface ArtificialSpmInfoService {
    // 批量插入
    List<Long> create(Collection<ArtificialSpmInfoDTO> spmInfoDTOCollection);

    // 查询
    List<ArtificialSpmInfoDTO> getBySpm(Collection<String> spmCollection, Long appId);

//    // 搜索
//    List<SpmInfoDTO> search(Long appId, SpmInfo query);
//
    // 列出全部
    List<ArtificialSpmInfoDTO> listAll();
//
//    // 删除
//    Integer deleteBySpm(Collection<String> spmList, Long appId);
//
    //
    Integer deleteBySource(int source);
//
    // 更新
    void update(List<ArtificialSpmInfoDTO> spmInfoDTOS);

    // 更新单个记录
    void updateById(ArtificialSpmInfoDTO spmInfoDTOS);

}
