package com.netease.hz.bdms.easyinsight.service.service;

import com.netease.hz.bdms.easyinsight.common.dto.spm.SpmMapItemDTO;

import java.util.Collection;
import java.util.List;

/**
 * @author: xumengqiang
 * @date: 2021/11/10 19:10
 */
public interface SpmMapItemService {
    /**
     * 批量创建
     *
     * @param spmMapItemDTOCollection
     * @return
     */
    List<Long> create(Collection<SpmMapItemDTO> spmMapItemDTOCollection);

    /**
     * 依据 spmId 批量删除
     * @param spmIds
     * @return
     */
    Integer deleteBySpmId(Collection<Long> spmIds);

    /**
     * 根据 spmId 查询
     *
     * @param spmIds
     * @return
     */
    List<SpmMapItemDTO> getBySpmIds(Collection<Long> spmIds);
}
