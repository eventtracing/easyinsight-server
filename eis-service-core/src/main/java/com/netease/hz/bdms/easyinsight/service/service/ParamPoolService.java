package com.netease.hz.bdms.easyinsight.service.service;

import com.netease.hz.bdms.easyinsight.common.dto.param.parampool.ParamPoolItemDTO;

import java.util.List;

public interface ParamPoolService {
  List<ParamPoolItemDTO> getParamPoolItemByCode(String code, Long appId);

  Integer getParamPoolItemSizeByCode(String code, Long appId);

  ParamPoolItemDTO getParamPoolItemById(Long paramId);

  Long createParamPoolItem(ParamPoolItemDTO paramPoolItemDTO);

  Integer updateParamPoolItem(ParamPoolItemDTO paramPoolItemDTO);

  Integer deleteParamPoolItem(Long paramPoolItemId);

  Integer searchParamPoolItemSize(String search, Long appId);

  List<ParamPoolItemDTO> searchParamPoolItem(String search, Long appId, String orderBy,
      String orderRule, Integer offset, Integer pageSize);
}
