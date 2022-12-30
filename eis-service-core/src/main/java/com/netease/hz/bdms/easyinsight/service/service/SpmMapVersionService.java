package com.netease.hz.bdms.easyinsight.service.service;

import com.netease.hz.bdms.easyinsight.dao.model.SpmMapVersion;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author: xumengqiang
 * @date: 2022/3/2 19:04
 */
public interface SpmMapVersionService {
    /**
     * 批量插入操作
     *
     * @param spmMapVersions SPM映射版本信息
     * @return
     */
    List<Long> create(Collection<SpmMapVersion> spmMapVersions);

    /**
     * 依据条件查询
     *
     * @param condition 条件
     * @return
     */
    List<SpmMapVersion> search(SpmMapVersion condition);

    /**
     * 依据spmId批量查询
     *
     * @param spmIds SpmId集合
     * @param terminalId 终端ID
     * @return
     */
    List<SpmMapVersion> getBySpmIds(Set<Long> spmIds, Long terminalId);

    /**
     * 依据主键删除
     *
     * @param ids 主键集合
     * @return
     */
    Integer deleteByIds(Set<Long> ids);
}
