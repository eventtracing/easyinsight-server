package com.netease.hz.bdms.easyinsight.service.service.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.netease.hz.bdms.easyinsight.common.dto.app.AppSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.exception.CommonException;
import com.netease.hz.bdms.easyinsight.common.util.BeanConvertUtils;
import com.netease.hz.bdms.easyinsight.dao.AppMapper;
import com.netease.hz.bdms.easyinsight.dao.model.App;
import com.netease.hz.bdms.easyinsight.service.service.AppService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AppServiceImpl implements AppService {

    private static final Map<String, String> orderByMap = ImmutableMap
            .of("createTime", "create_time", "updateTime", "update_time");
    private static final Map<String, String> orderRuleMap = ImmutableMap
            .of("descend", "desc", "ascend", "asc");
    @Autowired
  private AppMapper appMapper;

  private AppSimpleDTO do2Dto(App app) {
    AppSimpleDTO appDTO = BeanConvertUtils.convert(app, AppSimpleDTO.class);
    if (appDTO != null) {
      UserSimpleDTO updater = new UserSimpleDTO(app.getUpdateEmail(), app.getUpdateName());
      UserSimpleDTO creator = new UserSimpleDTO(app.getCreateEmail(), app.getCreateName());

      appDTO.setCreator(creator)
          .setUpdater(updater);
    }
    return appDTO;
  }

  private App dto2Do(AppSimpleDTO appDTO) {
    App app = BeanConvertUtils.convert(appDTO, App.class);
    if (app != null) {
      UserSimpleDTO updater = appDTO.getUpdater();
      UserSimpleDTO creator = appDTO.getCreator();

      if (creator != null) {
        app.setCreateEmail(creator.getEmail())
            .setCreateName(creator.getUserName());
      }
      if (updater != null) {
        app.setUpdateEmail(updater.getEmail())
            .setUpdateName(updater.getUserName());
      }
    }
    return app;
  }

  @Override
  public List<AppSimpleDTO> getAppsByDomainId(Long domainId) {
    Preconditions.checkArgument(null != domainId, "域主键ID不能为空");
    List<App> apps = appMapper.selectByDomainId(domainId);

    return apps.stream().map(this::do2Dto).collect(Collectors.toList());
  }

  @Override
  public Integer getAppSizeByDomainId(Long domainId) {
    Preconditions.checkArgument(null != domainId, "域主键ID不能为空");
    return appMapper.selectSizeByDomainId(domainId);
  }

  @Override
  public Long createApp(AppSimpleDTO appSimpleDTO) {
    App app = dto2Do(appSimpleDTO);
    if (app == null) {
      throw new CommonException("产品对象不能为空");
    }
    if(app.getCreateTime() == null){
      app.setCreateTime(new Timestamp(System.currentTimeMillis()));
    }
    if(app.getUpdateTime() == null){
      app.setUpdateTime(new Timestamp(System.currentTimeMillis()));
    }
    appMapper.insert(app);
    return app.getId();
  }

  @Override
  public Integer updateApp(AppSimpleDTO appSimpleDTO) {
    App app = dto2Do(appSimpleDTO);
    Preconditions.checkArgument(null != app && null != app.getId(), "产品对象不能为空");
    return appMapper.update(app);
  }

  @Override
  public Integer deleteApp(Long appId) {
    Preconditions.checkArgument(null != appId, "产品ID不能为空");
    return appMapper.delete(appId);
  }

  @Override
  public Integer searchAppSize(String search, Long domainId) {
    return appMapper.searchAppSize(search, domainId);
  }

  @Override
  public List<AppSimpleDTO> searchApp(String search, Long domainId, String orderBy, String orderRule,
      Integer offset, Integer pageSize) {
    String dbOrderBy = orderByMap.get(orderBy);
    String dbOrderRule = orderRuleMap.get(orderRule);

    List<App> apps = appMapper.searchApp(search, domainId, dbOrderBy, dbOrderRule, offset, pageSize);
    return apps.stream().map(this::do2Dto).collect(Collectors.toList());
  }

  @Override
  public AppSimpleDTO getAppById(Long appId) {
    Preconditions.checkArgument(null != appId, "产品ID不能为空");

    App app = appMapper.selectByPrimaryKey(appId);
    return do2Dto(app);
  }

  @Override
  public List<AppSimpleDTO> getByIds(Set<Long> appIds) {
      List<App> appList = appMapper.selectByIds(appIds);
      List<AppSimpleDTO> appSimpleDTOList = appList.stream().map(this::do2Dto).collect(Collectors.toList());
    return appSimpleDTOList;
  }
}
