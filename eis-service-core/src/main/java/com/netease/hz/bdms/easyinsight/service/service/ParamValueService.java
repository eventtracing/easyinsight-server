package com.netease.hz.bdms.easyinsight.service.service;

import com.netease.hz.bdms.easyinsight.common.dto.param.ParamValueSimpleDTO;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ParamValueService {
  List<ParamValueSimpleDTO> getById(Long id, String search);

  List<ParamValueSimpleDTO> getByIds(Set<Long> paramValueIds);

  Integer getSizeById(Long id);

  Integer deleteValue(Collection<Long> paramValueIds);

  void updateValue(List<ParamValueSimpleDTO> updateValues);

  void addValue(List<ParamValueSimpleDTO> addValues);

  List<ParamValueSimpleDTO> getByParamIds(Set<Long> paramIds);

  Integer deleteByParamId(Long paramId);
}
