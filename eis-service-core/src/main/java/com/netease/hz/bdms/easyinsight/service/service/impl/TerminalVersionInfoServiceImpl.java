package com.netease.hz.bdms.easyinsight.service.service.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.dao.TerminalVersionInfoMapper;
import com.netease.hz.bdms.easyinsight.dao.model.EisTerminalVersionInfo;
import com.netease.hz.bdms.easyinsight.service.service.TerminalVersionInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author: xumengqiang
 * @date: 2021/12/23 10:04
 */
@Slf4j
@Service
public class TerminalVersionInfoServiceImpl implements TerminalVersionInfoService {
    @Autowired
    TerminalVersionInfoMapper terminalVersionInfoMapper;

    @Override
    public EisTerminalVersionInfo getById(Long id){
        return terminalVersionInfoMapper.selectById(id);
    }
    /**
     * 按主键搜索
     *
     * @param ids
     * @return
     */
    @Override
    public List<EisTerminalVersionInfo> getByIds(Collection<Long> ids) {
        if(CollectionUtils.isNotEmpty(ids)){
            return terminalVersionInfoMapper.selectByIds(ids);
        }
        return Lists.newArrayList();
    }

    /**
     * 条件查询
     * @param query
     * @return
     */
    @Override
    public List<EisTerminalVersionInfo> search(EisTerminalVersionInfo query) {
        Preconditions.checkArgument(null != query, "查询条件不能为空");
        return terminalVersionInfoMapper.search(query);
    }

    /**
     * 按名称查询
     *
     * @param name
     * @return
     */
    @Override
    public EisTerminalVersionInfo getByName(String name) {
        Preconditions.checkArgument(StringUtils.isNotBlank(name), "端版本名称不能为空");
        Long appId = EtContext.get(ContextConstant.APP_ID);
        EisTerminalVersionInfo terminalVersionInfo = terminalVersionInfoMapper.selectByName(appId, name);
        return terminalVersionInfo;
    }

    @Override
    public List<EisTerminalVersionInfo> getByNames(Set<String> names) {
        if(CollectionUtils.isNotEmpty(names)){
            Long appId = EtContext.get(ContextConstant.APP_ID);
            return terminalVersionInfoMapper.selectByNames(appId, names);
        }
        return Lists.newArrayList();
    }

    /**
     * 新增端版本
     *
     * @param terminalVersionInfo
     */
    @Override
    public Long create(EisTerminalVersionInfo terminalVersionInfo) {
        Preconditions.checkArgument(null != terminalVersionInfo, "插入记录不能为空");
        terminalVersionInfoMapper.insert(terminalVersionInfo);
        return terminalVersionInfo.getId();
    }

    /**
     * 更新端版本信息
     *
     * @param terminalVersionInfo
     */
    @Override
    public void update(EisTerminalVersionInfo terminalVersionInfo) {
        Preconditions.checkArgument(null != terminalVersionInfo && null != terminalVersionInfo.getId(),
                "更新信息不能为空且主键值不能为空");
        terminalVersionInfoMapper.update(terminalVersionInfo);
    }

    /**
     * 按主键删除
     *
     * @param ids
     * @return
     */
    @Override
    public Integer deleteByIds(Collection<Long> ids) {
        if(CollectionUtils.isNotEmpty(ids)){
            return terminalVersionInfoMapper.deleteByIds(ids);
        }
        return 0;
    }
}
