package com.netease.hz.bdms.easyinsight.service.service;

import com.netease.hz.bdms.easyinsight.dao.model.EisTerminalVersionInfo;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author: xumengqiang
 * @date: 2021/12/23 9:58
 */
public interface TerminalVersionInfoService {

    /**
     * 按主键搜索
     *
     * @param id
     * @return
     */
    EisTerminalVersionInfo getById(Long id);

    /**
     * 按主键搜索
     *
     * @param ids
     * @return
     */
    List<EisTerminalVersionInfo> getByIds(Collection<Long> ids);

    /**
     * 条件查询
     * @param query
     * @return
     */
    List<EisTerminalVersionInfo> search(EisTerminalVersionInfo query);

    /**
     * 按名称查询
     *
     * @param name
     * @return
     */
    EisTerminalVersionInfo getByName(String name);

    /**
     * 按名称 批量查询
     *
     * @param names
     * @return
     */
    List<EisTerminalVersionInfo> getByNames(Set<String> names);

    /**
     * 新增端版本
     *
     * @param terminalVersionInfo
     */
    Long create(EisTerminalVersionInfo terminalVersionInfo);

    /**
     * 更新端版本信息
     *
     * @param terminalVersionInfo
     */
    void update(EisTerminalVersionInfo terminalVersionInfo);

    /**
     * 按主键删除
     *
     * @param ids
     * @return
     */
    Integer deleteByIds(Collection<Long> ids);
}
