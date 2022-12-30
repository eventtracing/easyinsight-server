package com.netease.hz.bdms.easyinsight.service.service;

import com.netease.hz.bdms.easyinsight.common.dto.param.parambind.ParamBindSimpleDTO;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ParamBindService {
  List<ParamBindSimpleDTO> getByEntityIds(Collection<Long> entityIds,
                                          Collection<Integer> entityTypes,
                                          Long versionId, Long appId);

  List<ParamBindSimpleDTO> getByAppId(Long appId);

  Integer getParamBindSizeByEntityIds(Collection<Long> entityIds,
                                      Collection<Integer> entityTypes,
                                      Long versionId, Long appId);

  List<Long> getParamBindIdsByEntityIds(Collection<Long> entityIds,
                                        Integer entityType,
                                        Long versionId, Long appId);

  ParamBindSimpleDTO getById(Long paramBindId);

  List<ParamBindSimpleDTO> getParamBindByParamId(List<Long> paramIds, Integer entityType);

  Long createParamBind(ParamBindSimpleDTO param);


  Integer updateParamBind(ParamBindSimpleDTO param);


  Integer deleteParamBind(Long paramBindId);


  Integer deleteParamBind(Long entityId, Integer entityType, Long versionId, Long appId);

  Integer searchParamBindSize(Long entityId, Integer entityType, Long versionId, Long appId);

  List<ParamBindSimpleDTO> searchParamBind(Long entityId, Integer entityType, Long versionId,
                                           Long appId, String orderBy, String orderRule,
                                           Integer offset, Integer pageSize);

  Integer deleteByIds(Set<Long> ids);

}
