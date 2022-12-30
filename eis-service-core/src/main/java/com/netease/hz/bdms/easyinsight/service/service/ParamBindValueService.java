package com.netease.hz.bdms.easyinsight.service.service;

import com.netease.hz.bdms.easyinsight.common.dto.param.parambind.ParamBindValueSimpleDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ParamBindValueService {
  void createParamBindValue(List<ParamBindValueSimpleDTO> param);

  Integer deleteByBindIds(List<Long> bindIds);

  Integer deleteByBindId(Long bindId);

  List<ParamBindValueSimpleDTO> getByBindIds(Set<Long> bindIds);

  Map<Long, Boolean> getParamValueUsed(Collection<Long> paramValueIds);
}
