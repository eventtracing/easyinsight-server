package com.netease.hz.bdms.easyinsight.service.service;

import com.netease.hz.bdms.easyinsight.common.dto.spm.SpmMapInfoDTO;

import java.util.Collection;
import java.util.List;

public interface SpmMapInfoService {
    List<Long> create(Collection<SpmMapInfoDTO> spmMapInfoDTOList);

    SpmMapInfoDTO getByPrimaryKey(Long id);

    /**
     * 按条件删除
     * @param spmMapInfoDTO
     * @return
     */
    Integer delete(SpmMapInfoDTO spmMapInfoDTO);

    void deleteAll();
}

