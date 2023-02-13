package com.netease.hz.bdms.easyinsight.service.service;

import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.terminal.TerminalSimpleDTO;

import java.util.Collection;
import java.util.List;

public interface TerminalService {

  List<TerminalSimpleDTO> getByName(String name, Long appId);

  TerminalSimpleDTO getById(Long terminalId);


  Long create(TerminalSimpleDTO terminalSimpleDTO);

  Integer update(TerminalSimpleDTO terminalSimpleDTO);


  Integer delete(Long terminalId);

  Integer searchTerminalSize(String search, List<Integer> terminalTypes, Boolean preset, Long appId);

  List<TerminalSimpleDTO> search(String search, List<Integer> terminalTypes, Boolean preset, Long appId,
                                 String orderBy, String orderRule, Integer offset, Integer pageSize);

  // 设置预置的终端信息：Android，iPhone
  List<TerminalSimpleDTO> getPresented(Long appId, UserSimpleDTO currentUser);

  List<TerminalSimpleDTO> getByIds(Collection<Long> terminalIds);

  List<TerminalSimpleDTO> getByAppId(Long appId);

  List<TerminalSimpleDTO> getAll();
}
