package com.netease.hz.bdms.easyinsight.service.service.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.terminal.TerminalSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.enums.TerminalBigTypeEnum;
import com.netease.hz.bdms.easyinsight.common.enums.TerminalTypeEnum;
import com.netease.hz.bdms.easyinsight.common.util.BeanConvertUtils;
import com.netease.hz.bdms.easyinsight.dao.TerminalMapper;
import com.netease.hz.bdms.easyinsight.dao.model.Terminal;
import com.netease.hz.bdms.easyinsight.service.service.TerminalService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TerminalServiceImpl implements TerminalService {

    private static final Map<String, String> orderByMap = ImmutableMap
            .of("createTime", "create_time", "updateTime", "update_time");
    private static final Map<String, String> orderRuleMap = ImmutableMap
            .of("descend", "desc", "ascend", "asc");
    @Autowired
    private TerminalMapper terminalMapper;

    private TerminalSimpleDTO do2Dto(Terminal terminal) {
        TerminalSimpleDTO terminalSimpleDTO = BeanConvertUtils
                .convert(terminal, TerminalSimpleDTO.class);
        if (null != terminalSimpleDTO) {
            UserSimpleDTO updater = new UserSimpleDTO(terminal.getUpdateEmail(),
                    terminal.getUpdateName());
            UserSimpleDTO creator = new UserSimpleDTO(terminal.getCreateEmail(),
                    terminal.getCreateName());

            terminalSimpleDTO.setCreator(creator)
                    .setUpdater(updater);
        }
        terminalSimpleDTO.setTerminalType(TerminalTypeEnum.of(terminalSimpleDTO.getName()).getType());
        return terminalSimpleDTO;
    }

    private Terminal dto2Do(TerminalSimpleDTO terminalSimpleDTO) {
        Terminal terminal = BeanConvertUtils.convert(terminalSimpleDTO, Terminal.class);
        if (terminal != null) {
            UserSimpleDTO updater = terminalSimpleDTO.getUpdater();
            UserSimpleDTO creator = terminalSimpleDTO.getCreator();

            if (creator != null) {
                terminal.setCreateEmail(creator.getEmail())
                        .setCreateName(creator.getUserName());
            }
            if (updater != null) {
                terminal.setUpdateEmail(updater.getEmail())
                        .setUpdateName(updater.getUserName());
            }
        }
        return terminal;
    }

    @Override
    public List<TerminalSimpleDTO> getByName(String name, Long appId) {
        Preconditions.checkArgument(null != appId, "产品ID不能为空");
        Preconditions.checkArgument(StringUtils.isNotBlank(name), "终端名称不能为空");

        List<Terminal> terminals = terminalMapper.selectByName(name, appId);
        return terminals.stream().map(this::do2Dto).collect(Collectors.toList());
    }

    @Override
    public TerminalSimpleDTO getById(Long terminalId) {
        Preconditions.checkArgument(null != terminalId, "终端ID不能为空");

        Terminal terminal = terminalMapper.selectByPrimaryKey(terminalId);
        return do2Dto(terminal);
    }

    @Override
    public Long create(TerminalSimpleDTO terminalSimpleDTO) {
        Terminal terminal = dto2Do(terminalSimpleDTO);
        Preconditions.checkArgument(null != terminal, "终端对象不能为空");

        terminalMapper.insert(terminal);
        return terminal.getId();
    }

    @Override
    public Integer update(TerminalSimpleDTO terminalSimpleDTO) {
        Terminal terminal = dto2Do(terminalSimpleDTO);
        Preconditions.checkArgument(null != terminal, "终端对象不能为空");
        Preconditions.checkArgument(null != terminal.getId(), "终端主键ID不能为空");

        return terminalMapper.update(terminal);
    }

    @Override
    public Integer delete(Long terminalId) {
        Preconditions.checkArgument(null != terminalId, "终端对象ID不能为空");
        return terminalMapper.delete(terminalId);
    }

    @Override
    public Integer searchTerminalSize(String search, List<Integer> terminalTypes, Boolean preset, Long appId) {
        return terminalMapper.searchTerminalSize(search, terminalTypes, preset, appId);
    }

    @Override
    public List<TerminalSimpleDTO> search(String search, List<Integer> terminalTypes,
                                          Boolean preset,
                                          Long appId, String orderBy, String orderRule, Integer offset, Integer pageSize) {
        String dbOrderBy = orderByMap.get(orderBy);
        String dbOrderRule = orderRuleMap.get(orderRule);

        List<Terminal> terminals = terminalMapper
                .searchTerminal(search, terminalTypes, preset, appId, dbOrderBy, dbOrderRule, offset, pageSize);
        return terminals.stream().map(this::do2Dto).collect(Collectors.toList());
    }

    @Override
    public List<TerminalSimpleDTO> getPresented(Long appId, UserSimpleDTO currentUser) {
        Preconditions.checkArgument(null != appId, "产品ID不能为空");
        Preconditions.checkArgument(null != currentUser, "创建人不能为空");

        List<TerminalSimpleDTO> terminals = Lists.newArrayList();
        TerminalSimpleDTO android = new TerminalSimpleDTO();
        android.setAppId(appId)
                .setName("Android")
                .setDescription("预置终端Android")
                .setPreset(true)
                .setType(TerminalBigTypeEnum.CLIENT.getType())
                .setCreator(currentUser)
                .setUpdater(currentUser);
        terminals.add(android);

        TerminalSimpleDTO iphone = new TerminalSimpleDTO();
        iphone.setAppId(appId)
                .setName("iPhone")
                .setDescription("预置终端iPhone")
                .setPreset(true)
                .setType(TerminalBigTypeEnum.CLIENT.getType())
                .setCreator(currentUser)
                .setUpdater(currentUser);
        terminals.add(iphone);

        TerminalSimpleDTO web = new TerminalSimpleDTO();
        web.setAppId(appId)
                .setName("Web")
                .setDescription("预置终端Web")
                .setPreset(true)
                .setType(TerminalBigTypeEnum.CLIENT.getType())
                .setCreator(currentUser)
                .setUpdater(currentUser);
        terminals.add(web);

        return terminals;
    }

    @Override
    public List<TerminalSimpleDTO> getByIds(Collection<Long> terminalIds) {

        if (CollectionUtils.isNotEmpty(terminalIds)) {
            List<Terminal> terminals = terminalMapper.selectByIds(terminalIds);
            return terminals.stream().map(this::do2Dto).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    @Override
    public List<TerminalSimpleDTO> getByAppId(Long appId) {

        if (appId != null) {
            List<Terminal> terminals = terminalMapper.selectByAppId(appId);
            return terminals.stream().map(this::do2Dto).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    @Override
    public List<TerminalSimpleDTO> getAll() {

        List<Terminal> terminals = terminalMapper.selectAll();
        if (terminals == null) return new ArrayList<>();
        return terminals.stream().map(this::do2Dto).collect(Collectors.toList());

    }
}
