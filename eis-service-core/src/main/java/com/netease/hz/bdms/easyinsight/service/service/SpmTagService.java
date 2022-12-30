package com.netease.hz.bdms.easyinsight.service.service;

import com.netease.hz.bdms.easyinsight.common.dto.tag.SpmTagSimpleDTO;

import java.util.Collection;
import java.util.List;

/**
 * @author: xumengqiang
 * @date: 2021/11/10 15:23
 */
public interface SpmTagService {

    List<Long> create(Collection<SpmTagSimpleDTO> spmTagSimpleDTOS);

    @Deprecated
    Integer deleteBySpmId(Collection<Long> spmIds);

    List<SpmTagSimpleDTO> getBySpmIds(Collection<Long> spmIds);

    Integer deleteByIds(Collection<Long> ids);
}
