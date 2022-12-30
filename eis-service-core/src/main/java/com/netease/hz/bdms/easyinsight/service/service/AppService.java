package com.netease.hz.bdms.easyinsight.service.service;

import com.netease.hz.bdms.easyinsight.common.dto.app.AppSimpleDTO;

import java.util.List;
import java.util.Set;

public interface AppService {
  List<AppSimpleDTO> getAppsByDomainId(Long domainId);

  Integer getAppSizeByDomainId(Long domainId);

  Long createApp(AppSimpleDTO appSimpleDTO);

  Integer updateApp(AppSimpleDTO appSimpleDTO);

  Integer deleteApp(Long appId);

  Integer searchAppSize(String search, Long domainId);

  List<AppSimpleDTO> searchApp(String search, Long domainId, String orderBy, String orderRule,
                               Integer offset, Integer pageSize);

  AppSimpleDTO getAppById(Long appId);

  /**
   * 根据产品ID集合获取产品信息
   *
   * @param appIds 产品ID集合
   * @return
   */
  List<AppSimpleDTO> getByIds(Set<Long> appIds);
}
