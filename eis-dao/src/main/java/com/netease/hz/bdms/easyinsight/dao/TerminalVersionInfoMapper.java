package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.EisTerminalVersionInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author: xumengqiang
 * @date: 2021/12/22 20:48
 */
@Repository
public interface TerminalVersionInfoMapper {
    /**
     * 插入记录
     *
     * @param terminalVersionInfo
     * @return
     */
    Integer insert(EisTerminalVersionInfo terminalVersionInfo);

    /**
     * 依据端版本名称搜索
     *
     * @param name
     * @return
     */
    EisTerminalVersionInfo selectByName(@Param("appId")Long appId, @Param("name") String name);

    /**
     * 按名称 批量搜索
     *
     * @param names
     * @return
     */
    List<EisTerminalVersionInfo> selectByNames(@Param("appId") Long appId, @Param("names") Set<String> names);

    /**
     * 依据主键批量搜索
     *
     * @param ids
     * @return
     */
    List<EisTerminalVersionInfo> selectByIds(@Param("ids") Collection<Long> ids);

    /**
     * 依据主键搜索
     *
     * @param id
     * @return
     */
    EisTerminalVersionInfo selectById(Long id);

    /**
     * 条件查询
     *
     * @param query
     * @return
     */
    List<EisTerminalVersionInfo> search(EisTerminalVersionInfo query);

    /**
     * 依据主键 批量删除
     *
     * @param ids
     * @return
     */
    Integer deleteByIds(@Param("ids") Collection<Long> ids);

    /**
     * 更新
     *
     * @param terminalVersionInfo
     */
    void update(EisTerminalVersionInfo terminalVersionInfo);
}
