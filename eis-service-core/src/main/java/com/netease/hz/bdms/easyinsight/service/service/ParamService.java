package com.netease.hz.bdms.easyinsight.service.service;

import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.ParamSimpleDTO;

import java.util.Collection;
import java.util.List;

public interface ParamService {

  List<ParamSimpleDTO> getParamByCode(String code, Long appId);

  Integer getParamSizeByCode(String code, Long appId);

  ParamSimpleDTO getParamById(Long paramId);

  List<ParamSimpleDTO> getByAppId(Long appId);

  Long createParam(ParamSimpleDTO param);

  Integer updateParam(ParamSimpleDTO param);


  Integer deleteParam(Long paramId);

  Integer searchParamSize(String search, List<Integer> paramTypes, List<String> createEmails,
      List<Integer> valueTypes, String code, Long appId, Collection<Long> ids);

  List<Long> searchParamIdsByName(String search, List<Integer> paramTypes, List<String> createEmails,
      List<Integer> valueTypes, String code, Long appId, Collection<Long> ids);

  List<ParamSimpleDTO> searchParam(String search, List<Integer> paramTypes,
      List<String> createEmails, List<Integer> valueTypes, String code, Long appId,
      Collection<Long> ids, String orderBy,
      String orderRule, Integer offset, Integer pageSize);

  List<ParamSimpleDTO> listAllByAppIdAndCodes(List<String> codes, Long appId, Integer paramType);

  List<ParamSimpleDTO> getParamByIds(Collection<Long> paramIds);


  List<UserSimpleDTO> getCreators(Long appId, Integer paramType);

  Integer updateCode(Long appId, Integer paramType, String oldCode, String newCode);
}
